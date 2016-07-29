package com.iliayugai.skilled;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.widget.Emojicon.EmojiconEditText;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class ChangePasswordActivity extends Activity implements View.OnClickListener {

    private static final String TAG = ChangePasswordActivity.class.getSimpleName();

    private EditText mEditOldPassword;
    private EditText mEditNewPassword;
    private EditText mEditRePassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_password);
        initTitleBar();
        initViews();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.image_trend:
                onBackPressed();
                break;

            case R.id.btn_update_it:
                onUpdate();
                break;
        }
    }

    /**
     * Initialize TitleBar
     */
    private void initTitleBar() {
        final Resources res = getResources();

        int size = (int) (res.getDimension(R.dimen.navigation_layout_title_bar_height) * Config.mScaleFactor);
        //int margin = (int) (res.getDimension(R.dimen.title_bar_margin) * Config.mScaleFactor);
        int padding = (int) (res.getDimension(R.dimen.title_bar_button_padding) * Config.mScaleFactor);
        float textSize = res.getDimension(R.dimen.navigation_text_nav_title_font_size) * Config.mFontScaleFactor;

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_title_bar);
        ViewGroup.LayoutParams layoutParams = layout.getLayoutParams();
        layoutParams.height = size;
        layout.setLayoutParams(layoutParams);

        // Trend ImageView
        ImageView imageView = (ImageView) findViewById(R.id.image_trend);
        imageView.setOnClickListener(this);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        params.width = params.height = size;
        imageView.setLayoutParams(params);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setImageResource(R.drawable.btn_back_bg);

        // Search ImageView
        imageView = (ImageView) findViewById(R.id.image_search);
        imageView.setVisibility(View.INVISIBLE);

        // Title TextView
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-DemiBold.otf");
        TextView textTitle = (TextView) findViewById(R.id.text_nav_title);
        textTitle.setText(R.string.change_password);
        textTitle.setTypeface(typeFace);
        textTitle.setTextSize(textSize);

        View viewEdge = findViewById(R.id.view_h_line);
        params = (RelativeLayout.LayoutParams) viewEdge.getLayoutParams();
        params.height = (int) (res.getDimension(R.dimen.navigation_view_h_line_height) * Config.mScaleFactor);
        viewEdge.setLayoutParams(params);
    }

    private void initViews() {
        Resources resources = getResources();

        int cellHeight = (int) (resources.getDimension(R.dimen.post_title_edit_height) * Config.mScaleFactor);
        int margin = (int) (resources.getDimension(R.dimen.edit_profile_layout_padding) * Config.mScaleFactor);
        int paddingTop = (int) (resources.getDimension(R.dimen.post_layout_image_padding_top) * Config.mScaleFactor);
        int paddingLeft = (int) (resources.getDimension(R.dimen.post_category_text_padding_left) * Config.mScaleFactor);
        float fontSize = resources.getDimension(R.dimen.post_category_button_text_size) * Config.mFontScaleFactor;
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Light.otf");

        // Old Password EditText
        mEditOldPassword = (EmojiconEditText) findViewById(R.id.edit_old_password);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mEditOldPassword.getLayoutParams();
        params.height = cellHeight;
        params.topMargin = margin;
        mEditOldPassword.setLayoutParams(params);
        mEditOldPassword.setPadding(paddingLeft, 0, paddingLeft, 0);
        mEditOldPassword.setTypeface(typeface);
        mEditOldPassword.setTextSize(fontSize);

        // New Password EditText
        mEditNewPassword = (EmojiconEditText) findViewById(R.id.edit_new_password);
        params = (LinearLayout.LayoutParams) mEditNewPassword.getLayoutParams();
        params.height = cellHeight;
        params.topMargin = paddingTop;
        mEditNewPassword.setLayoutParams(params);
        mEditNewPassword.setPadding(paddingLeft, 0, paddingLeft, 0);
        mEditNewPassword.setTypeface(typeface);
        mEditNewPassword.setTextSize(fontSize);

        // Retype Password EditText
        mEditRePassword = (EditText) findViewById(R.id.edit_retype_password);
        params = (LinearLayout.LayoutParams) mEditRePassword.getLayoutParams();
        params.height = cellHeight;
        params.topMargin = paddingTop;
        mEditRePassword.setLayoutParams(params);
        mEditRePassword.setPadding(paddingLeft, 0, paddingLeft, 0);
        mEditRePassword.setTypeface(typeface);
        mEditRePassword.setTextSize(fontSize);

        // Update button
        Button button = (Button) findViewById(R.id.btn_update_it);
        params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.height = (int) (resources.getDimension(R.dimen.large_button_height) * Config.mScaleFactor);
        button.setLayoutParams(params);
        button.setOnClickListener(this);
    }

    private void onUpdate() {
        String oldPassword = mEditOldPassword.getText().toString();
        final String newPassword = mEditNewPassword.getText().toString();
        String retypePassword = mEditRePassword.getText().toString();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(retypePassword)) {
            CommonUtils.createErrorAlertDialog(this, "Alert", "Type new password").show();
            return;
        }

        if (!TextUtils.isEmpty(newPassword) && newPassword.equals(retypePassword)) {
            final Dialog dialog = CommonUtils.createProgressDialog(this, "");
            dialog.show();

            ParseUser.logInInBackground(ParseUser.getCurrentUser().getUsername(), oldPassword, new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    dialog.dismiss();

                    if (e == null && parseUser != null) {
                        ParseUser user = ParseUser.getCurrentUser();
                        user.setPassword(newPassword);
                        user.saveInBackground();
                        onBackPressed();
                    } else {
                        CommonUtils.createErrorAlertDialog(ChangePasswordActivity.this, "Alert", "Old password is wrong").show();
                    }
                }
            });
        } else {
            CommonUtils.createErrorAlertDialog(this, "Alert", "Retype password").show();
        }
    }

}
