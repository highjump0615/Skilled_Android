package com.iliayugai.skilled;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.iliayugai.skilled.adapter.BlogAdapter;
import com.iliayugai.skilled.data.BlogCategory;
import com.iliayugai.skilled.data.BlogData;
import com.iliayugai.skilled.data.CommentData;
import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.PostUtils;
import com.iliayugai.skilled.utils.SkilledManager;
import com.iliayugai.skilled.utils.SystemHelper;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends Activity implements View.OnClickListener {

    private static final String TAG = CategoryActivity.class.getSimpleName();

    private int m_nCurBlogNum;

    private int m_nMoreViewHeight;

    private int m_nCurrentCount;

    private int m_nCountOnce;

    private boolean m_bTrendMode;
    private boolean m_bSearchOn;
    private boolean m_bMoreOn;
    private boolean mSearchLaunched;

    private String mStrSearchKey;

    private BlogAdapter mFeedAdapter;

    private TextView mTextTitle;
    private EditText mEditSearch;
    private ImageView mBtnTrend;
    private ImageView mBtnSearch;

    private View mLayoutMore;

    private PullToRefreshListView mPullRefreshListView;

    private Dialog mProgressDialog;

    private int mLoadedDataCount;

    private ArrayList<BlogData> mBlogList = new ArrayList<BlogData>();
    private BlogCategory mCategory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        initTitleBar();
        initMoreLayout();
        initPullToRefresh();

        m_nCountOnce = 5;
        m_bTrendMode = false;
        m_bSearchOn = false;
        m_bMoreOn = false;
        mSearchLaunched = false;

        mStrSearchKey = "";
        m_nCurrentCount = 0;

        int nCategorySelected = SkilledManager.getSelectedCategory();

        if (nCategorySelected >= 0) {
            mCategory = SkilledManager.mCategoryList.get(nCategorySelected);
        }

        // get category name
        if (mCategory != null) {
            mTextTitle.setText(mCategory.strName);
        } else {
            mTextTitle.setText("All");
        }

        getBlog(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PostActivity.POST_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                m_nCurrentCount = 0;
                getBlog(false);
            }
        } else
            ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        getBlogComment();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.image_trend:
                onBackPressed();
                break;

            case R.id.image_search:
                m_bTrendMode = !m_bTrendMode;
                if (m_bTrendMode) {
                    mBtnSearch.setImageResource(R.drawable.btn_trend_bg);
                } else {
                    mBtnSearch.setImageResource(R.drawable.btn_trendhot_bg);
                }

                m_nCurrentCount = 0;
                getBlog(true);
                break;

            case R.id.btn_more_facebook:
                PostUtils.onMoreFacebook(this, mBlogList.get(m_nCurBlogNum));
                hideMoreView();
                break;

            case R.id.btn_more_twitter:
                PostUtils.onMoreTwitter(this, mBlogList.get(m_nCurBlogNum));
                hideMoreView();
                break;

            case R.id.btn_more_email:
                PostUtils.onMoreEmail(this, mBlogList.get(m_nCurBlogNum));
                hideMoreView();
                break;

            case R.id.btn_more_report:
                PostUtils.onMoreReport(this);
                hideMoreView();
                break;

            case R.id.btn_more_close:
                hideMoreView();
                break;
        }
    }

    /*************************************************************************/
    /*                          Initialize Layouts                           */
    /*************************************************************************/

    /**
     * Initialize TitleBar
     */
    private void initTitleBar() {
        Config.scaleLayout(this, "navigation", findViewById(R.id.layout_title_bar));

        // Trend ImageView
        mBtnTrend = (ImageView) findViewById(R.id.image_trend);
        mBtnTrend.setImageResource(R.drawable.btn_back_bg);
        mBtnTrend.setOnClickListener(this);

        // Search ImageView
        mBtnSearch = (ImageView) findViewById(R.id.image_search);
        mBtnSearch.setImageResource(R.drawable.btn_trendhot_bg);
        mBtnSearch.setOnClickListener(this);

        // Title TextView
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-DemiBold.otf");
        mTextTitle = (TextView) findViewById(R.id.text_nav_title);
        mTextTitle.setTypeface(typeFace);

        // Search EditText
        mEditSearch = (EditText) findViewById(R.id.edit_search);
        typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-Regular.otf");
        mEditSearch.setTypeface(typeFace);

        mEditSearch.setOnKeyListener(new View.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on Enter key press
                    mStrSearchKey = mEditSearch.getText().toString();
                    if (!TextUtils.isEmpty(mStrSearchKey)) {
                        mSearchLaunched = true;
                        m_nCurrentCount = 0;
                        getBlog(true);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void initMoreLayout() {
        m_nMoreViewHeight = (int) (getResources().getDimension(R.dimen.more_layout_height) * Config.mScaleFactor);

        mLayoutMore = findViewById(R.id.layout_more_buttons);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mLayoutMore.getLayoutParams();
        params.height = m_nMoreViewHeight;
        mLayoutMore.setLayoutParams(params);

        int padding = (int) (getResources().getDimension(R.dimen.more_button_padding) * Config.mScaleFactor);

        // facebook
        ImageView imageView = (ImageView) findViewById(R.id.btn_more_facebook);
        imageView.setPadding(0, padding, 0, padding);
        imageView.setOnClickListener(this);

        // twitter
        imageView = (ImageView) findViewById(R.id.btn_more_twitter);
        imageView.setPadding(0, padding, 0, padding);
        imageView.setOnClickListener(this);

        // email
        imageView = (ImageView) findViewById(R.id.btn_more_email);
        imageView.setPadding(0, padding, 0, padding);
        imageView.setOnClickListener(this);

        // report
        imageView = (ImageView) findViewById(R.id.btn_more_report);
        imageView.setPadding(0, padding, 0, padding);
        imageView.setOnClickListener(this);

        // close
        imageView = (ImageView) findViewById(R.id.btn_more_close);
        imageView.setPadding(0, padding, 0, padding);
        imageView.setOnClickListener(this);
    }

    private void initPullToRefresh() {
        mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);

        mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                m_nCurrentCount = 0;
                //new GetDataTask().execute();
                getBlog(false);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                getBlog(false);
            }
        });

        // Get actual ListView
        final ListView feedListView = mPullRefreshListView.getRefreshableView();
        feedListView.setVerticalFadingEdgeEnabled(false);

        mFeedAdapter = new BlogAdapter(this, mBlogList);

        // You can also just use setListAdapter(mAdapter) or
        // mPullRefreshListView.setAdapter(mFeedAdapter);
        feedListView.setAdapter(mFeedAdapter);

        feedListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                hideMoreView();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }

    /*************************************************************************/
    /*                          Get Blog from Parse                          */
    /*************************************************************************/

    /**
     * Get Blog from parse.com
     */
    private void getBlog(final boolean isShowLoading) {

        mLoadedDataCount = 0;

        if (isShowLoading) {
            mProgressDialog = CommonUtils.createFullScreenProgress(CategoryActivity.this);
            mProgressDialog.show();
        }

        ParseQuery<ParseObject> blogQuery = ParseQuery.getQuery("Blogs");

        // filter category
        ArrayList<String> categoryList = new ArrayList<String>();
        if (mCategory == null) {
            for (BlogCategory category : SkilledManager.mCategoryList) {
                categoryList.add(category.strId);
            }
        } else {
            categoryList.add(mCategory.strId);
        }
        blogQuery.whereContainedIn("category", categoryList);

        if (m_bTrendMode) {
            blogQuery.orderByDescending("likes");
        } else {
            blogQuery.orderByDescending("createdAt");
        }

        blogQuery.setLimit(m_nCountOnce);
        blogQuery.setSkip(m_nCurrentCount);

        blogQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> blogObjectList, ParseException e) {
                if (e == null) {
                    if (m_nCurrentCount == 0) {
                        mBlogList.clear();
                        //if (blogObjectList.size() > 0) mFeedListView.setVisibility(View.VISIBLE);
                    }

                    // set parent objects
                    for (final ParseObject blogObject : blogObjectList) {

                        boolean bDuplicated = false;

                        // check whether duplicates
                        for (BlogData blogData : mBlogList) {
                            if (blogData.strId.equals(blogObject.getObjectId())) {
                                bDuplicated = true;
                                break;
                            }
                        }

                        if (bDuplicated) {
                            continue;
                        }

                        final BlogData blog = new BlogData();

                        blog.strId = blogObject.getObjectId();
                        blog.type = blogObject.getInt("type");
                        blog.strTitle = blogObject.getString("title");
                        blog.strContent = blogObject.getString("text");
                        blog.strVideoName = blogObject.getString("video");
                        blog.photoImage = blogObject.getParseFile("image");
                        blog.date = blogObject.getCreatedAt();
                        blog.user = blogObject.getParseUser("user");
                        blog.object = blogObject;
                        blog.bLiked = -1;
                        blog.nLikeCount = blogObject.getInt("likes");

                        // set category
                        for (BlogCategory category : SkilledManager.mCategoryList) {
                            if (category.strId.equals(blogObject.getString("category"))) {
                                blog.category = category;
                                break;
                            }
                        }

                        mBlogList.add(blog);

                        // Fetch user
                        String objectId = blog.user.getObjectId();

                        if (!SkilledManager.mParseUserMap.containsKey(objectId)) {
                            blog.user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if (e == null) {
                                        ParseUser user = (ParseUser) parseObject;

                                        SkilledManager.mParseUserMap.put(user.getObjectId(), user);

                                        getBlogLikes(blogObject, blog, blogObjectList, isShowLoading);
                                    } else {
                                        if (isShowLoading) mProgressDialog.dismiss();
                                        mPullRefreshListView.onRefreshComplete();

                                        CommonUtils.createErrorAlertDialog(CategoryActivity.this, "Alert", e.getMessage()).show();
                                    }
                                }
                            });
                        } else {
                            getBlogLikes(blogObject, blog, blogObjectList, isShowLoading);
                        }
                    }

                    m_nCurrentCount += blogObjectList.size();


                    // in the case of result size is equal to 0
                    if (blogObjectList.size() == 0) {
                        mFeedAdapter.notifyDataSetChanged();
                        if (isShowLoading) mProgressDialog.dismiss();
                        mPullRefreshListView.onRefreshComplete();
                    }

                    final int old_pos = mPullRefreshListView.getRefreshableView().getFirstVisiblePosition() + 1;
                    mFeedAdapter.notifyDataSetChanged();
                    mPullRefreshListView.onRefreshComplete();
                    mPullRefreshListView.getRefreshableView().post(new Runnable() {
                        @Override
                        public void run() {
                            mPullRefreshListView.getRefreshableView().setSelection(old_pos);
                        }
                    });


                } else {
                    if (isShowLoading) mProgressDialog.dismiss();
                    mPullRefreshListView.onRefreshComplete();

                    CommonUtils.createErrorAlertDialog(CategoryActivity.this, "Alert", e.getMessage()).show();
                }
                getBlogComment();
            }
        });
    }

    private void getBlogLikes(final ParseObject blogObject,
                              final BlogData blogData,
                              final List<ParseObject> blogObjectList,
                              final boolean isShowLoading) {
        // get like info
        ParseQuery<ParseObject> likeQuery = ParseQuery.getQuery("Likes");
        likeQuery.whereEqualTo("blog", blogObject);
        likeQuery.whereEqualTo("user", ParseUser.getCurrentUser());

        likeQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> likeObjects, ParseException e) {
                mLoadedDataCount++;

                if (mLoadedDataCount == blogObjectList.size()) {
                    mLoadedDataCount = 0;
                    if (Config.DEBUG) Log.e(TAG, "Final +++++++++ notifyDataSetChanged");
                    if (isShowLoading) mProgressDialog.dismiss();
                    mPullRefreshListView.onRefreshComplete();
                }

                if (e == null) {
                    if (likeObjects.size() > 0) {
                        blogData.bLiked = 1;
                    } else {
                        blogData.bLiked = 0;
                    }

                    mFeedAdapter.notifyDataSetChanged();
                    Log.e(TAG, "Like - notifyDataSetChanged");
                } else {
                    // Log details of the failure
                    Log.e(TAG, "Error: " + e.getCode() + ", " + e.getMessage());
                }
            }
        });
    }

    private void getBlogComment() {

        for (final BlogData blogData : mBlogList) {
            // get comment info
            ParseQuery<ParseObject> commentQuery = ParseQuery.getQuery("Comments");
            commentQuery.whereEqualTo("blog", blogData.object);
            commentQuery.orderByDescending("updatedAt");

            commentQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> commentObjects, ParseException e) {
                    if (e == null) {
                        blogData.mCommentList = new ArrayList<CommentData>();

                        for (ParseObject object : commentObjects) {
                            CommentData comment = new CommentData();
                            comment.user = object.getParseUser("user");
                            comment.strContent = object.getString("content");
                            comment.strUsername = object.getString("username");
                            comment.date = object.getUpdatedAt();
                            comment.object = object;

                            blogData.mCommentList.add(comment);
                        }

                        mFeedAdapter.notifyDataSetChanged();
                    } else {
                        // Log details of the failure
                        Log.e(TAG, "Error: " + e.getCode() + ", " + e.getMessage());
                    }
                }
            });
        }
    }

    /*************************************************************************/
    /*                          Search & More                                */
    /*************************************************************************/

    private void toggleSearchView() {
        TranslateAnimation anim;
        int width = Config.getDisplayWidth(this);

        m_bSearchOn = !m_bSearchOn;

        if (m_bSearchOn) {
            mEditSearch.setVisibility(View.VISIBLE);
            anim = new TranslateAnimation(width, 0.0f, 0.0f, 0.0f);
            mEditSearch.requestFocus();
            SystemHelper.showKeyboard(CategoryActivity.this, mEditSearch);
        } else {
            anim = new TranslateAnimation(0.0f, width, 0.0f, 0.0f);
            anim.setAnimationListener(searchViewCollapseListener);
        }

        anim.setDuration(200);
        anim.setInterpolator(new AccelerateInterpolator(1.0f));
        mEditSearch.startAnimation(anim);
    }

    Animation.AnimationListener searchViewCollapseListener = new Animation.AnimationListener() {
        public void onAnimationEnd(Animation animation) {
            mEditSearch.setVisibility(View.GONE);
            SystemHelper.hideKeyboard(CategoryActivity.this, mEditSearch);

            mEditSearch.setText("");
            mStrSearchKey = "";
            m_nCurrentCount = 0;

            if (mSearchLaunched) {
                mSearchLaunched = false;
                getBlog(true);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    };

    // This function was called from Adapter item
    public void showMoreView(int index) {
        if (!m_bMoreOn) {
            m_bMoreOn = true;
            m_nCurBlogNum = index;

            mLayoutMore.setVisibility(View.VISIBLE);
            TranslateAnimation anim = new TranslateAnimation(0.0f, 0.0f, m_nMoreViewHeight, 0.0f);
            anim.setDuration(300);
            anim.setInterpolator(new AccelerateInterpolator(1.0f));
            mLayoutMore.startAnimation(anim);
        }
    }

    public void hideMoreView() {
        if (m_bMoreOn) {
            m_bMoreOn = false;

            TranslateAnimation anim = new TranslateAnimation(0.0f, 0.0f, 0.0f, m_nMoreViewHeight);
            anim.setAnimationListener(moreViewCollapseListener);
            anim.setDuration(300);
            anim.setInterpolator(new AccelerateInterpolator(1.0f));
            mLayoutMore.startAnimation(anim);
        }
    }

    Animation.AnimationListener moreViewCollapseListener = new Animation.AnimationListener() {
        public void onAnimationEnd(Animation animation) {
            mLayoutMore.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    };

}






