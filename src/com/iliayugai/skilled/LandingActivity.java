/**
 *
 */
package com.iliayugai.skilled;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.PostUtils;
import com.parse.CountCallback;
import com.parse.LogInCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Administrator
 */
public class LandingActivity extends Activity implements View.OnClickListener {

    private static final String TAG = LandingActivity.class.getSimpleName();

    private boolean mButtonPressed = false;

    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_landing);
        initViews();

        if (Config.DEBUG) getKeyHashForFacebook();

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            CommonUtils.gotoMain(this, HomeActivity.class);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mButtonPressed = false;
    }

    @Override
    public void onClick(View view) {
        if (mButtonPressed) return;

        mButtonPressed = true;
        int id = view.getId();

        switch (id) {
            case R.id.button_facebook:
                onFacebookLogin();
                break;

            case R.id.button_twitter:
                onTwitterLogin();
                break;

            case R.id.button_sign_up:
                CommonUtils.moveNextActivity(this, SignUpActivity.class);
                break;

            case R.id.button_sign_in:
                CommonUtils.moveNextActivity(this, SignInActivity.class);
                break;
        }
    }

    /**
     * Initialize views and scale views
     */
    private void initViews() {
        Config.scaleLayout(this, "landing", findViewById(R.id.layout_root));

        // welcome text
        TextView txtView = (TextView) findViewById(R.id.text_welcome);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/ProximaNovaBold.otf");
        txtView.setTypeface(typeFace);

        // social text
        txtView = (TextView) findViewById(R.id.text_social);
        typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-Regular.otf");
        txtView.setTypeface(typeFace);

        // facebook button
        Button button = (Button) findViewById(R.id.button_facebook);
        button.setOnClickListener(this);

        // twitter button
        button = (Button) findViewById(R.id.button_twitter);
        button.setOnClickListener(this);

        // never text
        txtView = (TextView) findViewById(R.id.text_never);
        txtView.setTypeface(typeFace);

        // or text
        txtView = (TextView) findViewById(R.id.text_or);
        typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-Bold.otf");
        txtView.setTypeface(typeFace);

        // sign up button
        button = (Button) findViewById(R.id.button_sign_up);
        button.setOnClickListener(this);

        // sign in button
        button = (Button) findViewById(R.id.button_sign_in);
        button.setOnClickListener(this);
    }

    /*
     * Facebook in Parse
     */
    private void getKeyHashForFacebook() {
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    this.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /*
     * Facebook in Parse
     */
    private void onFacebookLogin() {
        mProgressDialog = CommonUtils.createFullScreenProgress(LandingActivity.this);
        mProgressDialog.show();

        //List<String> permissions = Arrays.asList("user_about_me", "email", "user_location");
        List<String> permissions = Arrays.asList(
                ParseFacebookUtils.Permissions.User.ABOUT_ME,
                ParseFacebookUtils.Permissions.User.EMAIL,
                ParseFacebookUtils.Permissions.User.LOCATION);

        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
            @Override
            public void done(final ParseUser user, ParseException err) {
                if (user == null) {
                    onLoadingStopped();

                    if (Config.DEBUG) Log.d(TAG, "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    if (Config.DEBUG) Log.d(TAG, "User signed up and logged in through Facebook!");

                    if (!PostUtils.hasPublishPermission()) {
                        // Request publish permission
                        Session.NewPermissionsRequest newPermissionsRequest =
                                new Session.NewPermissionsRequest(LandingActivity.this,
                                        Arrays.asList("publish_actions", "publish_stream"));
                        newPermissionsRequest.setCallback(new Session.StatusCallback() {
                            @Override
                            public void call(Session session, SessionState state, Exception exception) {
                                if (exception == null) {
                                    ParseFacebookUtils.saveLatestSessionData(user);

                                    // Fetch Facebook user info if the session is active
                                    if (session != null && session.isOpened()) {
                                        makeUserWithFacebookInfo();
                                    } else {
                                        onLoadingStopped();
                                    }
                                } else {
                                    CommonUtils.createErrorAlertDialog(LandingActivity.this,
                                            "Alert", "Could not request publish permissions").show();
                                    onLoadingStopped();
                                }
                            }
                        });
                        ParseFacebookUtils.getSession().requestNewPublishPermissions(newPermissionsRequest);
                    } else {
                        ParseFacebookUtils.saveLatestSessionData(user);

                        // Fetch Facebook user info if the session is active
                        if (ParseFacebookUtils.getSession() != null
                                && ParseFacebookUtils.getSession().isOpened()) {
                            makeUserWithFacebookInfo();
                        } else {
                            onLoadingStopped();
                        }
                    }
                } else {
                    onLoadingStopped();

                    if (Config.DEBUG) Log.d(TAG, "User logged in through Facebook!");
                    CommonUtils.gotoMain(LandingActivity.this, HomeActivity.class);
                }
            }
        });
    }

    private void makeUserWithFacebookInfo() {
        Request request = Request.newMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(final GraphUser user, Response response) {
                        if (user != null) {
                            final ParseUser currentUser = ParseUser.getCurrentUser();

                            ParseQuery<ParseUser> query = ParseUser.getQuery();
                            //query.orderByAscending("email");
                            query.whereEqualTo("email", user.getProperty("email"));
                            query.countInBackground(new CountCallback() {
                                @Override
                                public void done(int i, ParseException e) {
                                    if (e == null && i > 0) {
                                        CommonUtils.createErrorAlertDialog(LandingActivity.this,
                                                "Alert", String.format("%s is already existing", user.getProperty("email"))).show();

                                        currentUser.deleteInBackground();
                                        Session.getActiveSession().closeAndClearTokenInformation();
                                        onLoadingStopped();

                                    } else {
                                        if (user.getUsername() != null)
                                            currentUser.setUsername(user.getUsername());

                                        if (user.getName() != null)
                                            currentUser.put("fullname", user.getName());

                                        if (user.getLocation().getProperty("name") != null) {
                                            currentUser.put("location", user.getLocation().getProperty("name"));
                                        }

                                        currentUser.setEmail((String) user.getProperty("email"));

                                        // Populate the JSON object
                                        String facebookId = user.getId();

                                        String profilePictureURL = String.format("https://graph.facebook.com/%s/picture?type=large&return_ssl_resources=1", facebookId);

                                        CommonUtils.GetImageDataTask getImageDataTask = new CommonUtils.GetImageDataTask();
                                        getImageDataTask.execute(profilePictureURL);
                                        try {
                                            byte[] imageData = getImageDataTask.get();

                                            if (imageData != null) {
                                                ParseFile photoFile = new ParseFile(imageData);
                                                currentUser.put("photo", photoFile);
                                            }
                                        } catch (InterruptedException e1) {
                                            e1.printStackTrace();
                                        } catch (ExecutionException e1) {
                                            e1.printStackTrace();
                                        }

                                        // By specifying no write privileges for the ACL, we can ensure the role cannot be altered.
                                        ParseACL roleACL = new ParseACL();
                                        roleACL.setPublicReadAccess(true);
                                        roleACL.setReadAccess(currentUser, true);
                                        roleACL.setWriteAccess(currentUser, true);
                                        currentUser.setACL(roleACL);

                                        currentUser.saveInBackground();

                                        onLoadingStopped();

                                        // Show the user info
                                        CommonUtils.gotoMain(LandingActivity.this, HomeActivity.class);
                                    }
                                }
                            });

                        } else if (response.getError() != null) {
                            if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
                                    || (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
                                Log.d(TAG, "The facebook session was invalidated.");
                                ParseUser.logOut();
                            } else {
                                Log.d(TAG, "Some other error: " + response.getError().getErrorMessage());
                            }

                            onLoadingStopped();
                        }
                    }
                }
        );
        request.executeAsync();
    }

    /*
     * Twitter in Parse
     */
    private void onTwitterLogin() {
        mProgressDialog = CommonUtils.createFullScreenProgress(LandingActivity.this);
        mProgressDialog.show();

        ParseTwitterUtils.logIn(this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user == null) {
                    Log.d(TAG, "Uh oh. The user cancelled the Twitter login.");
                    onLoadingStopped();
                } else if (user.isNew()) {
                    Log.d(TAG, "User signed up and logged in through Twitter!");

                    makeUserWithTwitterInfo();
                } else {
                    Log.d(TAG, "User logged in through Twitter!");

                    onLoadingStopped();
                    CommonUtils.gotoMain(LandingActivity.this, HomeActivity.class);
                }
            }
        });
    }

    private void makeUserWithTwitterInfo() {
        final ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.setUsername(ParseTwitterUtils.getTwitter().getScreenName());

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", currentUser.getUsername());
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int i, ParseException e) {
                if (e == null && i > 0) {
                    CommonUtils.createErrorAlertDialog(LandingActivity.this,
                            "Alert", String.format("%s is already existing", currentUser.getUsername())).show();

                    currentUser.deleteInBackground();
                    onLoadingStopped();

                } else {
                    String urlShow = String.format("https://api.twitter.com/1.1/users/show.json?screen_name=%s", currentUser.getUsername());
                    GetHttpResponseTask asyncTask = new GetHttpResponseTask();

                    try {
                        asyncTask.execute(urlShow);
                        String result = asyncTask.get();

                        if (result != null) {
                            JSONObject resultObject = new JSONObject(result);

                            currentUser.put("fullname", resultObject.getString("name"));

                            String profilePictureURL = resultObject.getString("profile_image_url_https");

                            CommonUtils.GetImageDataTask getImageDataTask = new CommonUtils.GetImageDataTask();
                            getImageDataTask.execute(profilePictureURL);
                            try {
                                byte[] imageData = getImageDataTask.get();

                                if (imageData != null) {
                                    ParseFile photoFile = new ParseFile(imageData);
                                    currentUser.put("photo", photoFile);
                                }
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            } catch (ExecutionException e1) {
                                e1.printStackTrace();
                            }

                            // By specifying no write privileges for the ACL, we can ensure the role cannot be altered.
                            ParseACL roleACL = new ParseACL();
                            roleACL.setPublicReadAccess(true);
                            roleACL.setReadAccess(currentUser, true);
                            roleACL.setWriteAccess(currentUser, true);
                            currentUser.setACL(roleACL);

                            currentUser.saveInBackground();

                            // Show the user info
                            CommonUtils.gotoMain(LandingActivity.this, HomeActivity.class);
                        }
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    } catch (ExecutionException e1) {
                        e1.printStackTrace();
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                    onLoadingStopped();
                }
            }
        });

    }

    private class GetHttpResponseTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpClient client = new DefaultHttpClient();
            HttpGet verifyGet = new HttpGet(params[0]);
            ParseTwitterUtils.getTwitter().signRequest(verifyGet);

            try {
                HttpResponse response = client.execute(verifyGet);
                if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return EntityUtils.toString(response.getEntity());
                }
            } catch (IOException e) {
                if (Config.DEBUG) e.printStackTrace();
            }

            return null;
        }

    }

    private void onLoadingStopped() {
        if (mProgressDialog != null) mProgressDialog.dismiss();
        mButtonPressed = false;
    }

}
