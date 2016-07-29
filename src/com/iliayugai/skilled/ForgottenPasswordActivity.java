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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.Config;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

public class ForgottenPasswordActivity extends Activity implements View.OnClickListener {

    private static final String TAG = ForgottenPasswordActivity.class.getSimpleName();

    private EditText mEditEmail;

    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forgot_password);
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

            case R.id.btn_request:
                onRequest();
                break;

            case R.id.btn_to_login:
                startActivity(new Intent(this, SignInActivity.class));
                overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
                finish();
                break;
        }
    }

    private void initViews() {
        final Resources res = getResources();
        RelativeLayout.LayoutParams layoutParams;
        LinearLayout.LayoutParams params;

        // header layout
        int nWidth, nHeight;

        nHeight = (int) (res.getDimension(R.dimen.signup_activity_back_size) * Config.mScaleFactor);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.layout_header);
        layoutParams = (RelativeLayout.LayoutParams) relativeLayout.getLayoutParams();
        layoutParams.height = nHeight;
        relativeLayout.setLayoutParams(layoutParams);

        // back button
        ImageView imageView = (ImageView) findViewById(R.id.btn_back);
        imageView.setOnClickListener(this);

        int nPadding = (int) (res.getDimension(R.dimen.signup_activity_back_padding) * Config.mScaleFactor);
        imageView.setPadding(nPadding, nPadding, nPadding, nPadding);

        layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        layoutParams.width = nHeight;
        imageView.setLayoutParams(layoutParams);

        // title text
        float fFontSize = res.getDimension(R.dimen.signup_activity_header_font_size) * Config.mFontScaleFactor;

        TextView txtView = (TextView) findViewById(R.id.text_header_title);

        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-DemiBold.otf");
        txtView.setTypeface(typeFace);
        txtView.setTextSize(fFontSize);

        // logo photoImage
        imageView = (ImageView) findViewById(R.id.image_logo);

        params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        int nLogoSize = (int) (res.getDimension(R.dimen.splash_image_logo_width) * Config.mScaleFactor);
        params.height = params.width = nLogoSize;
        imageView.setLayoutParams(params);

        // edit layout
        int nMarginTop, nPaddingLeft;

        nWidth = (int) (res.getDimension(R.dimen.edit_text_width) * Config.mScaleFactor);
        nHeight = (int) (res.getDimension(R.dimen.edit_text_height) * Config.mScaleFactor);
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
        typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-Regular.otf");
        fFontSize = res.getDimension(R.dimen.signup_activity_header_font_size) * Config.mFontScaleFactor;

        mEditEmail = (EditText) findViewById(R.id.edt_email);
        mEditEmail.setTypeface(typeFace);
        mEditEmail.setTextSize(fFontSize);

        // request button
        nWidth = (int) (res.getDimension(R.dimen.button_width) * Config.mScaleFactor);
        nHeight = (int) (res.getDimension(R.dimen.button_height) * Config.mScaleFactor);
        nMarginTop = (int) (res.getDimension(R.dimen.signup_activity_margin) * Config.mScaleFactor);

        imageView = (ImageView) findViewById(R.id.btn_request);
        imageView.setOnClickListener(this);
        params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        params.width = nWidth;
        params.height = nHeight;
        params.topMargin = nMarginTop;
        imageView.setLayoutParams(params);

        // "Nevermind" button
        nWidth = (int) (res.getDimension(R.dimen.forgotten_activity_never_button_width) * Config.mScaleFactor);
        nHeight = (int) (res.getDimension(R.dimen.forgotten_activity_never_button_height) * Config.mScaleFactor);

        imageView = (ImageView) findViewById(R.id.btn_to_login);
        imageView.setOnClickListener(this);
        params = (LinearLayout.LayoutParams) imageView.getLayoutParams();
        params.width = nWidth;
        params.height = nHeight;
        params.topMargin = nMarginTop;
        imageView.setLayoutParams(params);
    }

    private void onRequest() {
        if (TextUtils.isEmpty(mEditEmail.getText().toString())) {
            CommonUtils.createErrorAlertDialog(this, "Alert", "Fill user name and password").show();
            return;
        }

        ParseUser.requestPasswordResetInBackground(mEditEmail.getText().toString(), new RequestPasswordResetCallback() {
            @Override
            public void done(ParseException e) {
                mProgressDialog.dismiss();

                if (e == null) {
                    Log.d(TAG, "Request has been submitted successfully");
                    CommonUtils.createErrorAlertDialog(ForgottenPasswordActivity.this,
                            "Alert", "Request has been submitted").show();
                } else {
                    CommonUtils.createErrorAlertDialog(ForgottenPasswordActivity.this,
                            "Alert", e.getMessage()).show();
                }
            }
        });

        mProgressDialog = CommonUtils.createFullScreenProgress(this);
        mProgressDialog.show();
    }

}
