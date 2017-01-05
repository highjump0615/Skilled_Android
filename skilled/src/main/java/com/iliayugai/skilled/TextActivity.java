package com.iliayugai.skilled;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.skilled.utils.Config;

public class TextActivity extends Activity implements View.OnClickListener {

    public static final String HELP_TEXT_INDEX = "help_text_index";

    private static String[] textFiles = {
            "Privacy.html",
            "Terms.html",
            "FAQs.html",
    };

    private static int[] TITLE_RES_IDS = {
            R.string.privacy_policy,
            R.string.terms_of_use,
            R.string.faq
    };

    private int mDocType = 0;

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        mDocType = getIntent().getIntExtra(HELP_TEXT_INDEX, 0);

        setContentView(R.layout.activity_web_view);

        initTitleBar();

        float fontSize = getResources().getDimension(R.dimen.title_font_size) * Config.mFontScaleFactor;
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.setBackgroundColor(Color.BLACK);

        // For Immediate text display
        final WebSettings webSettings = webView.getSettings();
        webSettings.setDefaultFontSize((int) fontSize);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setAppCacheEnabled(false);
        webSettings.setBlockNetworkImage(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setGeolocationEnabled(false);
        webSettings.setNeedInitialFocus(false);
        webSettings.setSaveFormData(false);

        webView.loadUrl("file:///android_asset/" + textFiles[mDocType]);
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
        textTitle.setText(TITLE_RES_IDS[mDocType]);
        textTitle.setTypeface(typeFace);
        textTitle.setTextSize(textSize);

        View viewEdge = findViewById(R.id.view_h_line);
        params = (RelativeLayout.LayoutParams) viewEdge.getLayoutParams();
        params.height = (int) (res.getDimension(R.dimen.navigation_view_h_line_height) * Config.mScaleFactor);
        viewEdge.setLayoutParams(params);
    }

}
