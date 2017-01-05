/**
 *
 */
package com.iliayugai.skilled;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.Config;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * @author Administrator
 */
public class SignUpActivity extends Activity implements View.OnClickListener {

    private static final String TAG = SignUpActivity.class.getSimpleName();

    private EditText mEditEmail;
    private EditText mEditUserName;
    private EditText mEditPassword;

    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);
        initViews();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, LandingActivity.class));
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        switch (id) {
            case R.id.btn_back:
                onBackPressed();
                break;

            case R.id.button_sign_up:
                onSignUp();
                break;

            case R.id.btn_to_login:
                CommonUtils.moveNextActivity(this, SignInActivity.class);
        }
    }

    private void initViews() {
        final Resources res = getResources();
        RelativeLayout.LayoutParams layoutParams;
        LinearLayout.LayoutParams params;

        // header layout
        int nWidth;
        int nHeight = (int) (res.getDimension(R.dimen.signup_activity_back_size) * Config.mScaleFactor);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.layout_header);
        layoutParams = (RelativeLayout.LayoutParams) relativeLayout.getLayoutParams();
        layoutParams.height = nHeight;
        relativeLayout.setLayoutParams(layoutParams);

        // back button
        ImageView imageView = (ImageView) findViewById(R.id.btn_back);

        int nPadding = (int) (res.getDimension(R.dimen.signup_activity_back_padding) * Config.mScaleFactor);
        imageView.setPadding(nPadding, nPadding, nPadding, nPadding);

        layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();

        layoutParams.width = nHeight;
        imageView.setLayoutParams(layoutParams);
        imageView.setOnClickListener(this);

        // title text
        float fFontSize = res.getDimension(R.dimen.signup_activity_header_font_size) * Config.mFontScaleFactor;

        TextView txtView = (TextView) findViewById(R.id.text_header_title);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-DemiBold.otf");
        txtView.setTypeface(typeFace);
        txtView.setTextSize(fFontSize);

        typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-Regular.otf");

        // logo photoImage
        imageView = (ImageView) findViewById(R.id.image_logo);

        params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        int nLogoSize = (int) (res.getDimension(R.dimen.splash_image_logo_width) * Config.mScaleFactor);
        params.height = params.width = nLogoSize;
        imageView.setLayoutParams(params);

        // edit layout
        int nMarginTop, nPaddingLeft;

        nWidth = (int) (res.getDimension(R.dimen.edit_text_width) * Config.mScaleFactor);
        nHeight = (int) (res.getDimension(R.dimen.signup_activity_edit_height) * Config.mScaleFactor);
        nMarginTop = (int) (res.getDimension(R.dimen.signup_activity_logo_gap) * Config.mScaleFactor);
        nPaddingLeft = (int) (res.getDimension(R.dimen.signup_activity_edit_padding) * Config.mScaleFactor);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_edit);
        linearLayout.setPadding(nPaddingLeft, 0, 0, 0);

        params = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
        params.width = nWidth;
        params.height = nHeight;
        params.topMargin = nMarginTop;
        linearLayout.setLayoutParams(params);

        // edit text
        fFontSize = res.getDimension(R.dimen.signup_activity_header_font_size) * Config.mFontScaleFactor;

        mEditEmail = (EditText) findViewById(R.id.edt_email);
        mEditEmail.setTypeface(typeFace);
        mEditEmail.setTextSize(fFontSize);
        mEditUserName = (EditText) findViewById(R.id.edt_username);
        mEditUserName.setTypeface(typeFace);
        mEditUserName.setTextSize(fFontSize);
        mEditPassword = (EditText) findViewById(R.id.edt_password);
        mEditPassword.setTypeface(typeFace);
        mEditPassword.setTextSize(fFontSize);

        // sign up button
        nWidth = (int) (res.getDimension(R.dimen.button_width) * Config.mScaleFactor);
        nHeight = (int) (res.getDimension(R.dimen.button_height) * Config.mScaleFactor);
        nMarginTop = (int) (res.getDimension(R.dimen.signup_activity_margin) * Config.mScaleFactor);

        Button button = (Button) findViewById(R.id.button_sign_up);
        button.setOnClickListener(this);
        params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.width = nWidth;
        params.height = nHeight;
        params.topMargin = nMarginTop;
        button.setLayoutParams(params);

        // go to login button
        nWidth = (int) (res.getDimension(R.dimen.signup_activity_login_but_width) * Config.mScaleFactor);
        nHeight = (int) (res.getDimension(R.dimen.signup_activity_login_but_height) * Config.mScaleFactor);

        imageView = (ImageView) findViewById(R.id.btn_to_login);
        params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        params.width = nWidth;
        params.height = nHeight;
        params.topMargin = nMarginTop;
        imageView.setLayoutParams(params);
        imageView.setOnClickListener(this);
    }

    private void onSignUp() {
        if (TextUtils.isEmpty(mEditEmail.getText().toString())
                || TextUtils.isEmpty(mEditUserName.getText().toString())
                || TextUtils.isEmpty(mEditPassword.getText().toString())) {
            CommonUtils.createErrorAlertDialog(this, "Alert", "Fill user name, password and mail address").show();
            return;
        }

        final ParseUser user = new ParseUser();
        user.setEmail(mEditEmail.getText().toString());
        user.setUsername(mEditUserName.getText().toString());
        user.setPassword(mEditPassword.getText().toString());

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                mProgressDialog.dismiss();

                if (e == null) {
                    Log.d(TAG, "Sign up success with Parse user");

                    ParseACL roleACL = new ParseACL();
                    roleACL.setPublicReadAccess(true);
                    roleACL.setReadAccess(user, true);
                    roleACL.setWriteAccess(user, true);
                    user.setACL(roleACL);

                    user.saveInBackground();

                    CommonUtils.gotoMain(SignUpActivity.this, HomeActivity.class);

                } else {
                    CommonUtils.createErrorAlertDialog(SignUpActivity.this, "Alert", e.getMessage()).show();
                }
            }
        });



        mProgressDialog = CommonUtils.createFullScreenProgress(this);
        mProgressDialog.show();
    }

}
