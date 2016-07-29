package com.iliayugai.skilled;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.iliayugai.skilled.adapter.ProfileAdapter;
import com.iliayugai.skilled.data.BlogCategory;
import com.iliayugai.skilled.data.BlogData;
import com.iliayugai.skilled.data.CommentData;
import com.iliayugai.skilled.utils.AmazonWebServiceUtils;
import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.PostUtils;
import com.iliayugai.skilled.utils.SkilledManager;
import com.iliayugai.skilled.widget.TabBar.TabHostProvider;
import com.iliayugai.skilled.widget.TabBar.TabView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends MyCustomActivity implements View.OnClickListener {

    private static final String TAG = ProfileActivity.class.getSimpleName();

    private static final int SETTING_REQUEST_CODE = 1000;
    private static final int EDIT_PROFILE_REQUEST_CODE = 2000;
    private static final int POST_TEXT_REQUEST_CODE = 3000;

    // widget
    private ParseImageView mImageProfileBackground;

    private ProfileAdapter mAdapter;
    private View mLayoutMore;

    //
    public static ArrayList<BlogData> mBlogList = new ArrayList<BlogData>();
    public static ArrayList<ParseObject> mImageBlogList = new ArrayList<ParseObject>();

    public int m_nFollowerCnt = 0;
    public int m_nFollowingCnt = 0;

    private int m_nMoreViewHeight;
    private boolean m_bMoreOn = false;
    private int m_nCurBlogNum = 0;

    private int m_nCurrentCount;
    private int m_nCountOnce;
    private int m_nMediaCountOnce;
    private int m_nMediaCurrentCount;

    private boolean m_bList;

    private PullToRefreshListView mPullRefreshListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate - called");

        TabHostProvider tabProvider = new MyTabHost(this);
        TabView tabView = tabProvider.getTabHost(MyTabHost.PROFILE_TAB_NAME);
        tabView.setCurrentView(R.layout.activity_profile);
        setContentView(tabView.render(MyTabHost.PROFILE_TAB_INDEX));

        m_nCountOnce = 3;
        m_nMediaCountOnce = 9;
        m_nCurrentCount = 0;

        m_bList = true;

        initViews();
        initMoreLayout();
        initPullToRefresh();

        loadDashboard();
        loadBlogData();
        loadImageBlogData();
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume - called");
        super.onResume();

        m_nCurrentCount = mBlogList.size();
        m_nMediaCurrentCount = mImageBlogList.size();


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

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.image_follow:
                break;

            case R.id.image_edit_profile:
                startActivityForResult(new Intent(this, EditProfileActivity.class), EDIT_PROFILE_REQUEST_CODE);
                overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                break;

            case R.id.layout_followers:
                FollowerActivity.m_bFollowing = false;
                FollowerActivity.mUser = ParseUser.getCurrentUser();
                startActivity(new Intent(this, FollowerActivity.class));
                overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                break;

            case R.id.layout_following:
                FollowerActivity.m_bFollowing = true;
                FollowerActivity.mUser = ParseUser.getCurrentUser();
                startActivity(new Intent(this, FollowerActivity.class));
                overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                break;

            case R.id.image_blog:
                m_bList = true;
                mAdapter.setListMode(true);
                mAdapter.notifyDataSetChanged();
                break;

            case R.id.image_picture:
                m_bList = false;
                mAdapter.setListMode(false);
//                loadImageBlogData();
                mAdapter.notifyDataSetChanged();
                break;

            case R.id.image_post_text:
                startActivityForResult(new Intent(this, PostActivity.class), POST_TEXT_REQUEST_CODE);
                overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                break;

            case R.id.image_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), SETTING_REQUEST_CODE);
                overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
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
                new AlertDialog.Builder(this)
                        .setTitle("Delete")
                        .setMessage("Do you really want to delete this post?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onMoreDelete(m_nCurBlogNum);
                                hideMoreView();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create()
                        .show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SETTING_REQUEST_CODE) {
            // In setting screen, user has already log out
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        } else if (requestCode == EDIT_PROFILE_REQUEST_CODE
                || requestCode == POST_TEXT_REQUEST_CODE
                || requestCode == PostActivity.POST_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (m_bList)
                    loadBlogData();
                else
                    loadImageBlogData();
            }
        }
    }

    private void initViews() {
        // hide title bar in my profile screen
        findViewById(R.id.layout_title_bar).setVisibility(View.GONE);

        mImageProfileBackground = (ParseImageView) findViewById(R.id.image_user_back);

        mLayoutMore = findViewById(R.id.layout_more_buttons);
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
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (m_bList)
                    loadBlogData();
                else {
                    loadImageBlogData();
                }
            }
        });

        // Get actual ListView
        final ListView feedListView = mPullRefreshListView.getRefreshableView();
        feedListView.setVerticalFadingEdgeEnabled(false);

        mAdapter = new ProfileAdapter(this, mBlogList, mImageBlogList, false);

        // You can also just use setListAdapter(mAdapter) or
        // mPullRefreshListView.setAdapter(mFeedAdapter);
        feedListView.setAdapter(mAdapter);

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

    private void loadDashboard() {
        ParseUser currentUser = ParseUser.getCurrentUser();

        SkilledManager.setSquareImage(mImageProfileBackground, currentUser, "background", null);

        // get posts & following info
        // Get follower count
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Following");
        query.whereEqualTo("followinguser", ParseUser.getCurrentUser());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    m_nFollowerCnt = parseObjects.size();
//                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        // Get following count
        query = ParseQuery.getQuery("Following");
        query.whereEqualTo("user", ParseUser.getCurrentUser());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    m_nFollowingCnt = parseObjects.size();
//                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        // Get blog count
        query = ParseQuery.getQuery("Blogs");
        query.whereEqualTo("user", currentUser);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    mAdapter.setPostBlogCount(parseObjects.size());
//                    mAdapter.notifyDataSetChanged();
                }
            }
        });
        mAdapter.notifyDataSetChanged();
    }

    private void loadBlogData() {
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (m_nCurrentCount != mBlogList.size()) {
            m_nCurrentCount = mBlogList.size();
        }

        // Get Blog
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Blogs");
        query.orderByDescending("createdAt");
        query.whereEqualTo("user", currentUser);

        query.setLimit(m_nCountOnce);
        query.setSkip(m_nCurrentCount);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    if (m_nCurrentCount == 0) {
                        mBlogList.clear();
                    }

                    if (parseObjects.size() > 0) {
                        for (ParseObject object : parseObjects) {
                            BlogData blog = new BlogData();

                            blog.strId = object.getObjectId();
                            blog.type = object.getInt("type");
                            blog.strTitle = object.getString("title");
                            blog.strContent = object.getString("text");
                            blog.strVideoName = object.getString("video");
                            blog.photoImage = object.getParseFile("image");
                            blog.date = object.getCreatedAt();
                            blog.user = ParseUser.getCurrentUser();
                            blog.object = object;
                            blog.bLiked = -1;
                            blog.nLikeCount = object.getInt("likes");

                            // set category
                            for (BlogCategory category : SkilledManager.mCategoryList) {
                                if (category.strId.equals(object.getString("category"))) {
                                    blog.category = category;
                                    break;
                                }
                            }
                            mBlogList.add(blog);
                        }
                    }

                    for (final BlogData blog : mBlogList) {
                        // get like info
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Likes");
                        query.whereEqualTo("blog", blog.object);
                        query.whereEqualTo("user", ParseUser.getCurrentUser());

                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> parseObjects, ParseException e) {
                                if (e == null) {
                                    if (parseObjects.size() > 0) {
                                        blog.bLiked = 1;
                                    } else {
                                        blog.bLiked = 0;
                                    }

                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    Log.d(TAG, "Error: " + e.getMessage());
                                }
                            }
                        });

                        // get comment info
                        query = ParseQuery.getQuery("Comments");
                        query.whereEqualTo("blog", blog.object);
                        query.orderByDescending("updatedAt");

                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> commentObjects, ParseException e) {
                                if (e == null) {
                                    blog.mCommentList = new ArrayList<CommentData>();

                                    for (ParseObject object : commentObjects) {
                                        CommentData comment = new CommentData();
                                        comment.user = object.getParseUser("user");
                                        comment.strContent = object.getString("content");
                                        comment.strUsername = object.getString("username");
                                        comment.date = object.getUpdatedAt();
                                        comment.object = object;

                                        blog.mCommentList.add(comment);
                                    }

                                    mAdapter.notifyDataSetChanged();
                                } else {
                                    Log.d(TAG, "Error: " + e.getMessage());
                                }
                            }
                        });
                        m_nCurrentCount += parseObjects.size();
                    }

                    final int old_pos = mPullRefreshListView.getRefreshableView().getFirstVisiblePosition() + 1;
                    mAdapter.notifyDataSetChanged();
                    mPullRefreshListView.onRefreshComplete();
                    mPullRefreshListView.getRefreshableView().post(new Runnable() {
                        @Override
                        public void run() {
                            mPullRefreshListView.getRefreshableView().setSelection(old_pos);
                        }
                    });


                } else {
                    CommonUtils.createErrorAlertDialog(ProfileActivity.this, "Alert", e.getMessage()).show();
                }
            }
        });
    }

    private void loadImageBlogData() {

        ParseUser currentUser = ParseUser.getCurrentUser();

        // Get Blog
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Blogs");
        query.orderByDescending("createdAt");
        query.whereEqualTo("user", currentUser);
        query.whereGreaterThan("type", BlogData.BlogText);

        query.setLimit(m_nMediaCountOnce);
        query.setSkip(m_nMediaCurrentCount);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    if (m_nMediaCurrentCount == 0) {
                        mImageBlogList.clear();
                    }

                    if (parseObjects.size() > 0) {
                        for (ParseObject object : parseObjects) {

                            boolean bExist = false;
                            for (ParseObject existObject : mImageBlogList) {
                                if (existObject.equals(object))
                                    bExist = true;
                            }

                            if (bExist)
                                break;


                            BlogData blog = new BlogData();

                            blog.strId = object.getObjectId();
                            blog.type = object.getInt("type");
                            blog.strTitle = object.getString("title");
                            blog.strContent = object.getString("text");
                            blog.strVideoName = object.getString("video");
                            blog.photoImage = object.getParseFile("image");
                            blog.date = object.getCreatedAt();
                            blog.user = ParseUser.getCurrentUser();
                            blog.object = object;
                            blog.bLiked = -1;
                            blog.nLikeCount = object.getInt("likes");

                            // set category
                            for (BlogCategory category : SkilledManager.mCategoryList) {
                                if (category.strId.equals(object.getString("category"))) {
                                    blog.category = category;
                                    break;
                                }
                            }
                            mImageBlogList.add(object);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                    mPullRefreshListView.onRefreshComplete();
                    m_nMediaCurrentCount += parseObjects.size();

                } else {
                    CommonUtils.createErrorAlertDialog(ProfileActivity.this, "Alert", e.getMessage()).show();
                }
            }
        });

    }

    /*************************************************************************/
    /*                               More                                    */
    /*************************************************************************/

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

    // Delete post
    private void onMoreDelete(int index) {
        final BlogData blogData = mBlogList.get(index);

        if (blogData.mCommentList != null) {
            for (CommentData commentData : blogData.mCommentList) {
                commentData.object.deleteInBackground();
            }
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Likes");
        query.whereEqualTo("blog", blogData.object);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    if (parseObjects != null) {
                        for (ParseObject likeObject : parseObjects) {
                            likeObject.deleteInBackground();
                        }

                        if (blogData.type == BlogData.BlogVideo) {
                            AmazonWebServiceUtils.deleteVideo(ProfileActivity.this, blogData.strVideoName);
                        }
                    }

                    blogData.object.deleteInBackground();
                } else {
                    if (Config.DEBUG) Log.e(TAG, e.getMessage());
                }
            }
        });

        mBlogList.remove(m_nCurBlogNum);
        for (ParseObject object : mImageBlogList) {
            if (object == blogData.object) {
                mImageBlogList.remove(object);
                break;
            }
        }

        mAdapter.notifyDataSetChanged();
    }

}
