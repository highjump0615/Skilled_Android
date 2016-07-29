/**
 *
 */

package com.iliayugai.skilled.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.skilled.R;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.SystemHelper;

import java.lang.reflect.Field;

public class SearchPanel2 extends LinearLayout implements
        EditText.OnTouchListener, TextWatcher,
        EditText.OnEditorActionListener,
        EditText.OnFocusChangeListener, OnScrollListener {

    private Context context;

    private EditText searchEditText;
    private Button buttonAll;
    private ImageView imageMagnify;
    private ImageView imageCancel;
    private GridView gridCategory;

    private SearchListener searchListener;
    private boolean enabledSearchPanel = false;
    private boolean applicationSetText = false;

    public interface SearchListener {
        void onAutoSuggestion(String query);

        void onClickSearchResult(String query);

        void onItemClick(int position);

        void onClear();

        void onSearchAll();
    }

    /**
     * Constructor with style
     *
     * @param context  is current context of activity
     * @param attrs    is set of attributes
     * @param defStyle is concrete style
     */
    public SearchPanel2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * Constructor with attributes
     *
     * @param context is current context of activity
     * @param attrs   is set of attributes
     */
    public SearchPanel2(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode())
            init(context);
    }

    /**
     * Simple constructor
     *
     * @param context is current context of activity
     */
    public SearchPanel2(Context context) {
        super(context);
        init(context);
    }

    /**
     * Inflate search_panel2.xml and
     * configuration all view
     *
     * @param context is current context of activity
     */
    private void init(Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.search_panel2, this, true);

        buttonAll = (Button) findViewById(R.id.button_all);
        buttonAll.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (searchListener != null)
                    searchListener.onSearchAll();
            }
        });

        imageMagnify = (ImageView) findViewById(R.id.image_search_icon);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageMagnify.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        imageMagnify.setLayoutParams(params);

        imageCancel = (ImageView) findViewById(R.id.image_cancel_icon);
        imageCancel.setVisibility(View.GONE);
        imageCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                enableSearchPanel(false);
            }
        });

        searchEditText = (EditText) findViewById(R.id.edit_search);

        searchEditText.setOnTouchListener(this);
        searchEditText.addTextChangedListener(this);
        searchEditText.setOnEditorActionListener(this);
        searchEditText.setOnFocusChangeListener(this);

        searchEditText.setFocusable(false);

        scaleLayouts();
    }

    /**
     * Activate/disable search panel
     */
    public boolean onTouch(final View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            searchEditText.setFocusable(true);
            searchEditText.requestFocusFromTouch();
            if (!enabledSearchPanel) {
                enableSearchPanel(true);
            }
        }
        return false;
    }

    public EditText getSearchEditText() {
        return searchEditText;
    }

    public void setSearchEditText(EditText searchEditText) {
        this.searchEditText = searchEditText;
    }

    public Button getButtonAll() {
        return buttonAll;
    }

    public void setButtonAll(Button buttonAll) {
        this.buttonAll = buttonAll;
    }

    public GridView getGridView() {
        return gridCategory;
    }

    /**
     *
     */
    public void afterTextChanged(Editable s) {
        if (!applicationSetText) {
            String query = s.toString();
            int amount = query.length();
            if (amount == 0) {
                imageCancel.setVisibility(View.GONE);
            } else {
                imageCancel.setVisibility(View.VISIBLE);
            }

            if (searchListener != null) {
                searchListener.onAutoSuggestion(query);
            }

            if (!enabledSearchPanel) {
                enableSearchPanel(true);
            }
        } else {
            applicationSetText = false;
        }
    }

    /**
     * Not use
     */
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    /**
     * Not use
     */
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    /**
     * Enter event occurs on the method call onClickSearchResult
     */
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {

            String query = v.getText().toString();
            searchEditText.setText(query);
            if (searchListener != null) {
                searchListener.onClickSearchResult(query);
            }
            hideKeyboard();
            return true;
        }
        return false;
    }

    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus && v instanceof EditText) {
            hideKeyboard();
        }
    }

    /**
     * Set visibility in true for search panel (cancel button and etc)
     *
     * @param enable if true then the search panel is appear otherwise disappear
     */
    public void enableSearchPanel(boolean enable) {
        if (enable) {
            // Show Soft Keyboard
            showKeyboard();

            searchEditText.setEnabled(true);

            //buttonAll.setVisibility(View.VISIBLE);

            enabledSearchPanel = true;
            searchEditText.requestFocus();
            //searchEditText.setGravity(Gravity.CENTER_VERTICAL);
        } else {
            clear();
            searchEditText.setFocusable(false);
            //searchEditText.setGravity(Gravity.CENTER);

            if (searchListener != null) {
                searchListener.onClear();
            }
        }
    }

    /**
     * Sets tag for finding frame1 (normal page)
     * and frame2 (auto suggestion page)
     *
     * @param tag is unique string per page
     * @throws Exception if tag not found
     */
    public void setSearchTag(String tag) throws Exception {
        try {
            Class<R.string> res = R.string.class;
            Field firstField = res.getField(tag);
            int pageFirstId = firstField.getInt(null);

            View parentView = (View) getParent();
            ViewGroup frame1 = (ViewGroup) parentView.findViewWithTag(getResources().getString(pageFirstId));

            if (frame1 == null) {
                throw new Exception("Couldn't find frame for output result");
            }

            gridCategory = (GridView) frame1;
            gridCategory.setOnScrollListener(this);
            gridCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (searchListener != null) {
                        searchListener.onItemClick(position);
                    }
                }
            });
            searchEditText.setOnFocusChangeListener(this);
            searchEditText.clearFocus();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    /**
     * Sets text query empty and
     * clear search result
     */
    public void clear() {
        // Hide Soft Keyboard
        hideKeyboard();
        searchEditText.setText("");

        imageCancel.setVisibility(View.GONE);

        enabledSearchPanel = false;
    }

    /**
     * Sets text query empty and
     * clear search result
     */
    public void clearWithoutQueryText() {
        // Hide Soft Keyboard
        hideKeyboard();

        searchEditText.setText("");
        enabledSearchPanel = false;
    }

    public SearchListener getSearchListener() {
        return searchListener;
    }

    public void setSearchListener(SearchListener searchListener) {
        this.searchListener = searchListener;
    }

    public void setTextQuery(String text) {
        applicationSetText = true;
        searchEditText.setText(text);
    }

    public void setSearchAdapter(ArrayAdapter searchAdapter) {
        gridCategory.setAdapter(searchAdapter);
    }

    public void hideKeyboard() {
        SystemHelper.hideKeyboard(context, searchEditText);

        enabledSearchPanel = false;

        if (searchEditText.length() == 0) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageMagnify.getLayoutParams();
            params.addRule(RelativeLayout.LEFT_OF, 0);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            imageMagnify.setLayoutParams(params);
        }
    }

    public void showKeyboard() {
        SystemHelper.showKeyboard(context, searchEditText);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageMagnify.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
        params.addRule(RelativeLayout.LEFT_OF, RelativeLayout.TRUE);
        imageMagnify.setLayoutParams(params);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        hideKeyboard();
    }

    private void scaleLayouts() {
        searchEditText.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/AvenirNext-Regular.otf"));
        Config.scaleLayout(context, "search", this);
    }

}
