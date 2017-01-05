package com.iliayugai.skilled.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.FacebookError;
import com.facebook.widget.WebDialog;
import com.iliayugai.skilled.data.BlogData;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

public class PostUtils {

    private static final String TAG = PostUtils.class.getSimpleName();

    public static final String SHARE_SUBJECT = "Skilled Share";
    public static final String MESSAGE_TITLE = "Please enjoy the photoImage.";

    private static final String IMAGE_FILE_NAME = "skilledPostImage.png";

    ////////////////////////////////////////////////////////////////////////////////
    // Facebook Post
    ////////////////////////////////////////////////////////////////////////////////

    public static boolean hasPublishPermission() {
        Session session = ParseFacebookUtils.getSession();
        return session != null && session.getPermissions().contains("publish_actions") && session.getPermissions().contains("publish_actions");
    }

    /**
     * Message and photoImage post
     */
    public static void publishFeedDialog(final Context context, BlogData blogData) {
        Bundle params = new Bundle();
        params.putString("name", blogData.user.getUsername());
        params.putString("caption", blogData.strTitle);
        params.putString("description", blogData.strContent);
        //params.putString("link", "https://developers.facebook.com/android");
        if (blogData.photoImage != null)
            params.putString("picture", blogData.photoImage.getUrl());

        WebDialog feedDialog = (
                new WebDialog.FeedDialogBuilder(context,
                        Session.getActiveSession(),
                        params))
                .setOnCompleteListener(new WebDialog.OnCompleteListener() {

                    @Override
                    public void onComplete(Bundle values, FacebookException error) {
                        if (error == null) {
                            // When the story is posted, echo the success
                            // and the post Id.
                            final String postId = values.getString("post_id");
                            if (postId != null) {
                                CommonUtils.createErrorAlertDialog(context, "Success",
                                        "Successfully posted to Facebook").show();
                            } else {
                                // User clicked the Cancel button
                                CommonUtils.createErrorAlertDialog(context, "Alert",
                                        "Publish cancelled").show();
                            }
                        } else if (error instanceof FacebookOperationCanceledException) {
                            // User clicked the "x" button
                            CommonUtils.createErrorAlertDialog(context, "Alert",
                                    "Publish cancelled").show();
                        } else {
                            // Generic, ex: network error
                            CommonUtils.createErrorAlertDialog(context, "Alert",
                                    "Publishing error").show();
                        }
                    }
                }).build();
        feedDialog.show();
    }

    public static class FacebookVideoPostTask extends AsyncTask<BlogData, String, Void> {

        private Context mContext;
        private Dialog mProgressDialog;
        private AsyncFacebookRunner mAsyncRunner;

        public FacebookVideoPostTask(Context context) {
            mContext = context;
            mAsyncRunner = new AsyncFacebookRunner(ParseFacebookUtils.getFacebook());
            mProgressDialog = CommonUtils.createFullScreenProgress(context);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(BlogData... params) {
            BlogData blogData = params[0];

            try {
                byte[] data = CommonUtils.downloadRemoteFile(blogData.videoUrl);

                Bundle param = new Bundle();
                param.putString("title", blogData.user.getUsername());
                param.putString("name", "Skilled Video");
                param.putString("message", blogData.strContent);
                param.putByteArray("video.mov", data);
                param.putString("contentType", "movie/mov");

                mAsyncRunner.request("me/videos", param, "POST", new FBRequestListener(), null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressDialog.dismiss();
            super.onPostExecute(aVoid);
        }

        private byte[] readBytes(InputStream inputStream) throws IOException {
            // This dynamically extends to take the bytes you read.
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            // This is storage overwritten on each iteration with bytes.
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            // We need to know how may bytes were read to write them to the byteBuffer.
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }

            // And then we can return your byte array.
            return byteBuffer.toByteArray();
        }

        public class FBRequestListener implements AsyncFacebookRunner.RequestListener {

            @Override
            public void onComplete(String response, Object state) {
                Log.e("response", response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String Id = (String) jsonObject.get("id");
                    Log.d(TAG, "Video Id = " + Id);

                    Bundle param = new Bundle();
                    param.putString("type", "uploaded");

                    mAsyncRunner.request("me/videos/" + Id, param, "GET", new VideoDataRequestListener(), null);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onIOException(IOException e, Object state) {
                e.printStackTrace();
            }

            @Override
            public void onFileNotFoundException(FileNotFoundException e, Object state) {
                Log.e(TAG, "onFileNotFoundException");
                e.printStackTrace();
            }

            @Override
            public void onMalformedURLException(MalformedURLException e, Object state) {
                Log.e(TAG, "onMalformedURLException");
                e.printStackTrace();
            }

            @Override
            public void onFacebookError(FacebookError e, Object state) {
                Log.e(TAG, "onFacebookError");
                e.printStackTrace();
            }
        }

        public class VideoDataRequestListener implements AsyncFacebookRunner.RequestListener {

            @Override
            public void onComplete(String response, Object state) {
                Log.e(TAG, "VideoDataRequestListener() response = " + response);
            }

            @Override
            public void onIOException(IOException e, Object state) {
            }

            @Override
            public void onFileNotFoundException(FileNotFoundException e, Object state) {
            }

            @Override
            public void onMalformedURLException(MalformedURLException e, Object state) {
            }

            @Override
            public void onFacebookError(FacebookError e, Object state) {
            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////////
    //  Twitter Post
    ////////////////////////////////////////////////////////////////////////////////

    public static void postToTwitter(Context context, BlogData blogData) {
        String twitterLink;

        if (blogData.photoImage != null) {
            twitterLink = String.format("http://www.twitter.com/intent/tweet?url=%s&text=%s",
                    blogData.photoImage.getUrl(), Uri.encode(blogData.strContent + "\n"));
        } else {
            twitterLink = String.format("http://www.twitter.com/intent/tweet?text=%s", Uri.encode(blogData.strContent));
        }

        if (Config.DEBUG) Log.d(TAG, "twitter post link = " + twitterLink);

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(twitterLink));
            context.startActivity(intent);
        } catch (Exception e) {
            CommonUtils.createErrorAlertDialog(context, "Alert", "You could not post with Twitter on your device").show();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // SMS/MMS
    ////////////////////////////////////////////////////////////////////////////////

    public static void sendMessage(Context context,
                                   String subject, String text, String imgPath) {
        final Intent mmsIntent = new Intent(android.content.Intent.ACTION_SEND);

        String[] address = {""};
        mmsIntent.putExtra(Intent.EXTRA_EMAIL, address);
        mmsIntent.setType("vnd.android-dir/mms-sms");
        mmsIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        mmsIntent.putExtra(Intent.EXTRA_TEXT, text);
        mmsIntent.setType("photoImage/png");
        if (!TextUtils.isEmpty(imgPath))
            mmsIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imgPath));
        context.startActivity(Intent.createChooser(mmsIntent, "MMS:"));
    }

    ////////////////////////////////////////////////////////////////////////////////
    // email
    ////////////////////////////////////////////////////////////////////////////////

    public static void sendMail(Context context, String imgPath) {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        String[] address = {""};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, address);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, SHARE_SUBJECT);
        emailIntent.putExtra(Intent.EXTRA_TEXT, MESSAGE_TITLE);
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + imgPath));
        emailIntent.setType("message/rfc822");

        context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    public static void sendMail(Context context, String dstAddr,
                                String subject, String text) {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        String[] address = {dstAddr};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, address);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    /*************************************************************************/
    /*                                  Post                                 */

    /**
     * *********************************************************************
     */

    public static void onMoreFacebook(final Activity activity, final BlogData blogData) {
        final ParseUser user = ParseUser.getCurrentUser();

        // Check if user already linked to facebook
        if (!ParseFacebookUtils.isLinked(user)) {
            CommonUtils.createErrorAlertDialog(activity, "Warning",
                    "Facebook video sharing is only available for the users logged in with Facebook").show();
            /*
            ParseFacebookUtils.link(user, activity, new SaveCallback() {
                @Override
                public void done(ParseException ex) {
                    if (ParseFacebookUtils.isLinked(user)) {
                        Log.d(TAG, "Woohoo, user logged in with Facebook!");

                        // Check if user has publish permissions
                        if (!PostUtils.hasPublishPermission()) {
                            Session.NewPermissionsRequest newPermissionsRequest =
                                    new Session.NewPermissionsRequest(activity,
                                            Arrays.asList("publish_actions", "publish_stream"));

                            newPermissionsRequest.setCallback(new Session.StatusCallback() {
                                @Override
                                public void call(Session session, SessionState state, Exception exception) {
                                    if (exception == null) {
                                        ParseFacebookUtils.saveLatestSessionData(user);

                                        // Fetch Facebook user info if the session is active
                                        if (session != null && session.isOpened()) {
                                            postToFacebook(activity, blogData);
                                        }
                                    } else {
                                        CommonUtils.createErrorAlertDialog(activity,
                                                "Alert", "Could not request publish permissions").show();
                                    }
                                }
                            });
                            ParseFacebookUtils.getSession().requestNewPublishPermissions(newPermissionsRequest);

                        } else {
                            postToFacebook(activity, blogData);
                        }
                    }
                }
            });
            */
        } else {
            postToFacebook(activity, blogData);
        }
    }

    private static void postToFacebook(Activity activity, BlogData blogData) {
        PostUtils.publishFeedDialog(activity, blogData);
        //if (blogData.type == BlogData.BlogVideo)
        //    new PostUtils.FacebookVideoPostTask(HomeActivity.this).execute(blogData);
    }

    public static void onMoreTwitter(Activity activity, BlogData blogData) {
        PostUtils.postToTwitter(activity, blogData);
    }

    public static void onMoreEmail(final Activity activity, final BlogData blogData) {
        if (blogData.photoImage != null) {
            final Dialog progressDialog = CommonUtils.createFullScreenProgress(activity);
            progressDialog.show();

            blogData.photoImage.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {
                    if (e == null) {
                        File sdCardDirectory = Environment.getExternalStorageDirectory();
                        File imageFile = new File(sdCardDirectory, IMAGE_FILE_NAME);

                        if (CommonUtils.saveImageFile(bytes, imageFile.getAbsolutePath())) {
                            PostUtils.sendMessage(activity,
                                    "Skilled Share", blogData.strContent, imageFile.getAbsolutePath());
                        }
                    }
                    progressDialog.dismiss();
                }
            });
        } else {
            PostUtils.sendMessage(activity, "Skilled Share", blogData.strContent, "");
        }
    }

    public static void onMoreReport(Activity activity) {
        PostUtils.sendMail(activity, "info@skilledapp.co", "Report", "");
    }


}
