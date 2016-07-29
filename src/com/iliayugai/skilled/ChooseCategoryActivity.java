package com.iliayugai.skilled;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.skilled.adapter.CategoryAdapter;
import com.iliayugai.skilled.data.BlogCategory;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.SkilledManager;
import com.iliayugai.skilled.widget.SearchPanel;
import com.iliayugai.skilled.widget.TabBar.TabHostProvider;
import com.iliayugai.skilled.widget.TabBar.TabView;

import java.util.ArrayList;

public class ChooseCategoryActivity extends Activity implements SearchPanel.SearchListener {

    private static final String TAG = ChooseCategoryActivity.class.getSimpleName();

    SearchPanel mSearchPanel;

    private ArrayAdapter<BlogCategory> mCategoryAdapter;

    private ArrayList<BlogCategory> mSuggestionCategories = new ArrayList<BlogCategory>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TabHostProvider tabProvider = new MyTabHost(this);
        TabView tabView = tabProvider.getTabHost("Favourite");
        tabView.setCurrentView(R.layout.activity_favourite2);
        setContentView(tabView.render(MyTabHost.FAVOURITE_TAB_INDEX));

        initTitleBar();
        initViews();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
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
        //imageView.setOnClickListener(this);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        params.width = params.height = size;
        imageView.setLayoutParams(params);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setVisibility(View.INVISIBLE);

        // Search ImageView
        imageView = (ImageView) findViewById(R.id.image_search);
        imageView.setVisibility(View.INVISIBLE);

        // Title TextView
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-DemiBold.otf");
        TextView textTitle = (TextView) findViewById(R.id.text_nav_title);
        textTitle.setText(R.string.choose_category);
        textTitle.setTypeface(typeFace);
        textTitle.setTextSize(textSize);

        View viewEdge = findViewById(R.id.view_h_line);
        params = (RelativeLayout.LayoutParams) viewEdge.getLayoutParams();
        params.height = (int) (res.getDimension(R.dimen.navigation_view_h_line_height) * Config.mScaleFactor);
        viewEdge.setLayoutParams(params);
    }

    private void initViews() {
        for (BlogCategory blogCategory : SkilledManager.mCategoryList)
            mSuggestionCategories.add(blogCategory);

        mSearchPanel = (SearchPanel) findViewById(R.id.search_panel);

        try {
            mSearchPanel.setSearchTag("tag_category_search");
            mSearchPanel.setSearchListener(this);
            mCategoryAdapter = new CategoryAdapter(this, mSuggestionCategories, false);
            mSearchPanel.setSearchAdapter(mCategoryAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Init search widget failed", e);
            finish();
        }
    }

    @Override
    public void onAutoSuggestion(String query) {
        mSuggestionCategories.clear();

        query = query.toLowerCase();

        for (BlogCategory blogCategory : SkilledManager.mCategoryList) {
            if (blogCategory.strName.toLowerCase().contains(query))
                mSuggestionCategories.add(blogCategory);
        }

        mCategoryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClickSearchResult(String query) {

    }

    @Override
    public void onItemClick(int position) {
        BlogCategory selectedCategory = mSuggestionCategories.get(position);

        Intent intent = new Intent();
        intent.setData(Uri.parse(selectedCategory.strId));
        setResult(RESULT_OK, intent);

        finish();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
    }

    @Override
    public void onClear() {
        mSuggestionCategories.clear();

        for (BlogCategory blogCategory : SkilledManager.mCategoryList)
            mSuggestionCategories.add(blogCategory);

        mCategoryAdapter.notifyDataSetChanged();
    }

}
