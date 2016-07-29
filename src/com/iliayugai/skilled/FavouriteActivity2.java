package com.iliayugai.skilled;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.iliayugai.skilled.adapter.CategoryAdapter2;
import com.iliayugai.skilled.data.BlogCategory;
import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.SkilledManager;
import com.iliayugai.skilled.widget.SearchPanel2;
import com.iliayugai.skilled.widget.TabBar.TabHostProvider;
import com.iliayugai.skilled.widget.TabBar.TabView;

import java.util.ArrayList;

public class FavouriteActivity2 extends MyCustomActivity implements SearchPanel2.SearchListener {

    private static final String TAG = FavouriteActivity2.class.getSimpleName();

    SearchPanel2 mSearchPanel;

    private ArrayAdapter<BlogCategory> mCategoryAdapter;

    private ArrayList<BlogCategory> mSuggestionCategories = new ArrayList<BlogCategory>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate - called");
        super.onCreate(savedInstanceState);

        TabHostProvider tabProvider = new MyTabHost(this);
        TabView tabView = tabProvider.getTabHost(MyTabHost.FAVOURITE_TAB_NAME);
        tabView.setCurrentView(R.layout.activity_favourite2);
        setContentView(tabView.render(MyTabHost.FAVOURITE_TAB_INDEX));

        initViews();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.e(TAG, "onNewIntent - called");
        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        Log.e(TAG, "onDestroy - called");
    }

    private void initViews() {
        for (BlogCategory blogCategory : SkilledManager.mCategoryList)
            mSuggestionCategories.add(blogCategory);

        mSearchPanel = (SearchPanel2) findViewById(R.id.search_panel);

        try {
            mSearchPanel.setSearchTag("tag_category_search");
            mSearchPanel.setSearchListener(this);
            mCategoryAdapter = new CategoryAdapter2(this, mSuggestionCategories);
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
        SkilledManager.setCategory(selectedCategory);

        CommonUtils.moveNextActivityWithoutFinish(this, CategoryActivity.class);
    }

    @Override
    public void onSearchAll() {
        SkilledManager.setCategory(null);
        CommonUtils.moveNextActivityWithoutFinish(this, CategoryActivity.class);
    }

    @Override
    public void onClear() {
        mSuggestionCategories.clear();

        for (BlogCategory blogCategory : SkilledManager.mCategoryList)
            mSuggestionCategories.add(blogCategory);

        mCategoryAdapter.notifyDataSetChanged();
    }

}
