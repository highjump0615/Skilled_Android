package com.iliayugai.skilled;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.iliayugai.skilled.adapter.CategoryAdapter2;
import com.iliayugai.skilled.data.BlogCategory;
import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.SkilledManager;
import com.iliayugai.skilled.widget.SearchPanel2;
import com.iliayugai.skilled.widget.TabBar.TabHostProvider;
import com.iliayugai.skilled.widget.TabBar.TabView;

import java.util.ArrayList;

public class ChooseCategoryActivity2 extends Activity implements SearchPanel2.SearchListener {

    private static final String TAG = ChooseCategoryActivity2.class.getSimpleName();

    SearchPanel2 mSearchPanel;

    private ArrayAdapter<BlogCategory> mCategoryAdapter;

    private ArrayList<BlogCategory> mSuggestionCategories = new ArrayList<BlogCategory>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        TabHostProvider tabProvider = new MyTabHost(this);
//        TabView tabView = tabProvider.getTabHost(MyTabHost.FAVOURITE_TAB_NAME);
//        tabView.setCurrentView(R.layout.activity_favourite2);
        setContentView(R.layout.activity_favourite2);

        initViews();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
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

        findViewById(R.id.button_all).setVisibility(View.GONE);
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
