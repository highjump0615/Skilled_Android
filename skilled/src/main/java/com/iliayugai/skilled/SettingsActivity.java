package com.iliayugai.skilled;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.iliayugai.skilled.rate.AppRater;
import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.PostUtils;
import com.iliayugai.skilled.utils.SkilledManager;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class SettingsActivity extends Activity implements View.OnClickListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        initTitleBar();
        initViews();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        Intent intent = new Intent(this, TextActivity.class);

        switch (id) {
            case R.id.image_trend:
                onBackPressed();
                break;

            case R.id.btn_send_feedback:
                PostUtils.sendMail(this, "info@skilledapp.co", "Send feedback", "");
                break;

            case R.id.btn_rate_us:
                AppRater.showRateDialog(this, null);
                break;

            case R.id.btn_clear_cache:
                onClearCache();
                break;

            case R.id.btn_privacy_policy:
                intent.putExtra(TextActivity.HELP_TEXT_INDEX, 0);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                break;

            case R.id.btn_term_of_use:
                intent.putExtra(TextActivity.HELP_TEXT_INDEX, 1);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                break;

            case R.id.btn_faq:
                intent.putExtra(TextActivity.HELP_TEXT_INDEX, 2);
                //startActivity(intent);
                //overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                break;

            case R.id.btn_logout:
                Log.e(TAG, "blogSize = " + ProfileActivity.mBlogList.size());
                Log.e(TAG, "imageBlogSize = " + ProfileActivity.mImageBlogList.size());
                ProfileActivity.mBlogList.clear();
                ProfileActivity.mImageBlogList.clear();

                if (ParseUser.getCurrentUser() != null)
                    ParseUser.logOut();
                if (ParseFacebookUtils.getSession() != null && ParseFacebookUtils.getSession().isOpened())
                    ParseFacebookUtils.getSession().closeAndClearTokenInformation();

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(MyCustomActivity.EXIT_APPLICATION_ACTION);
                sendBroadcast(broadcastIntent);

                intent = new Intent(this, LandingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
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
        textTitle.setText(R.string.settings);
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
        int paddingLeft = (int) (resources.getDimension(R.dimen.post_category_text_padding_left) * Config.mScaleFactor);
        float fontSize = resources.getDimension(R.dimen.post_category_button_text_size) * Config.mFontScaleFactor;
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Light.otf");

        /// Push Notification layout
        View layout = findViewById(R.id.layout_push_notification);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.height = cellHeight;
        params.topMargin = margin;
        layout.setLayoutParams(params);

        // Label TextView for Push Notification ToggleView
        TextView textView = (TextView) findViewById(R.id.text_push_notification);
        textView.setPadding(paddingLeft, 0, 0, 0);
        textView.setTypeface(typeface);
        textView.setTextSize(fontSize);

        // ToggleView for Push Notification
        ToggleButton togglePush = (ToggleButton) findViewById(R.id.toggle_push_notifications);
        params = (LinearLayout.LayoutParams) togglePush.getLayoutParams();
        params.width = (int) (resources.getDimension(R.dimen.toggle_button_width) * Config.mScaleFactor);
        params.height = (int) (resources.getDimension(R.dimen.toggle_button_height) * Config.mScaleFactor);
        params.rightMargin = margin;
        togglePush.setLayoutParams(params);
        togglePush.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "Push Notification was " + (isChecked ? "enabled" : "disabled"));
            }
        });

        /// Feedback Layout
        layout = findViewById(R.id.layout_feedback);
        params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.height = cellHeight * 2;
        params.topMargin = margin;
        layout.setLayoutParams(params);

        // Send feedback
        Button button = (Button) findViewById(R.id.btn_send_feedback);
        params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.height = cellHeight;
        button.setLayoutParams(params);
        button.setPadding(paddingLeft, 0, 0, 0);
        button.setTextSize(fontSize);
        button.setTypeface(typeface);
        button.setOnClickListener(this);

        // Rate us
        button = (Button) findViewById(R.id.btn_rate_us);
        params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.height = cellHeight;
        button.setLayoutParams(params);
        button.setPadding(paddingLeft, 0, 0, 0);
        button.setTextSize(fontSize);
        button.setTypeface(typeface);
        button.setOnClickListener(this);

        /// Clear cache
        layout = findViewById(R.id.layout_clear_cache);
        params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.height = cellHeight;
        params.topMargin = margin;
        layout.setLayoutParams(params);

        button = (Button) findViewById(R.id.btn_clear_cache);
        params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.height = cellHeight;
        button.setLayoutParams(params);
        button.setPadding(paddingLeft, 0, 0, 0);
        button.setTextSize(fontSize);
        button.setTypeface(typeface);
        button.setOnClickListener(this);

        /// Help layout
        layout = findViewById(R.id.layout_help);
        params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.height = cellHeight * 3;
        params.topMargin = margin;
        layout.setLayoutParams(params);

        // Privacy Policy
        button = (Button) findViewById(R.id.btn_privacy_policy);
        params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.height = cellHeight;
        button.setLayoutParams(params);
        button.setPadding(paddingLeft, 0, 0, 0);
        button.setTextSize(fontSize);
        button.setTypeface(typeface);
        button.setOnClickListener(this);

        // Terms of Use
        button = (Button) findViewById(R.id.btn_term_of_use);
        params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.height = cellHeight;
        button.setLayoutParams(params);
        button.setPadding(paddingLeft, 0, 0, 0);
        button.setTextSize(fontSize);
        button.setTypeface(typeface);
        button.setOnClickListener(this);

        // FAQs
        button = (Button) findViewById(R.id.btn_faq);
        params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.height = cellHeight;
        button.setLayoutParams(params);
        button.setPadding(paddingLeft, 0, 0, 0);
        button.setTextSize(fontSize);
        button.setTypeface(typeface);
        button.setOnClickListener(this);

        // Logout button
        button = (Button) findViewById(R.id.btn_logout);
        params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.height = (int) (resources.getDimension(R.dimen.large_button_height) * Config.mScaleFactor);
        button.setLayoutParams(params);
        button.setOnClickListener(this);
    }

    private void onClearCache() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.confirmation)
                .setMessage(getString(R.string.confirmation_content))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CommonUtils.CacheManager.cleanDir(getCacheDir(), Integer.MAX_VALUE);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setCancelable(false)
                .create();

        dialog.show();
    }

}
