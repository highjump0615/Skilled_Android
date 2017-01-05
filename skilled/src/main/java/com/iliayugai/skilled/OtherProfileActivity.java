package com.iliayugai.skilled;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.iliayugai.skilled.adapter.ProfileAdapter;
import com.iliayugai.skilled.data.BlogCategory;
import com.iliayugai.skilled.data.BlogData;
import com.iliayugai.skilled.data.CommentData;
import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.PostUtils;
import com.iliayugai.skilled.utils.SkilledManager;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OtherProfileActivity extends Activity implements View.OnClickListener {

    private static final String TAG = OtherProfileActivity.class.getSimpleName();

    private static final boolean DBG = false;
    // widget
    private ParseImageView mImageProfileBackground;

    private ProfileAdapter mAdapter;
    private View mLayoutMore;

    //
    private ArrayList<BlogData> mBlogList = new ArrayList<BlogData>();
    private ArrayList<ParseObject> mImageBlogList = new ArrayList<ParseObject>();

    public int m_nFollowerCnt = 0;
    public int m_nFollowingCnt = 0;

    private int m_nMoreViewHeight;
    private boolean m_bMoreOn = false;
    private int m_nCurBlogNum = 0;

    public boolean m_bFollowed;

    private int m_nCurrentCount;
    private int m_nMediaCurrentCount;
    private int m_nCountOnce;
    private int m_nMediaCountOnce;

    private boolean m_bList;

    public static ParseUser mUser;
    public static String mUserFullName;

    private PullToRefreshListView mPullRefreshListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate - called");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        m_nCountOnce = 3;
        m_nMediaCountOnce = 9;
        m_nCurrentCount = 0;

        m_bList = true;

        initTitleBar();
        initViews();
        initMoreLayout();

        initPullToRefresh();



        if (SkilledManager.sParseUserList.isEmpty() || SkilledManager.sParseUserNameList.isEmpty()) {
            SkilledManager.sParseUserList.add(mUser);
            SkilledManager.sParseUserNameList.add(mUserFullName);
        }
        loadBlogData();
        loadImageBlogData();
        loadDashboard();
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume - called");
        super.onResume();

        m_nCurrentCount = mBlogList.size();
        m_nMediaCurrentCount = mImageBlogList.size();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);


        if (!SkilledManager.sParseUserList.isEmpty()) {

            if (SkilledManager.sParseUserList.size() == 1) {
                SkilledManager.sParseUserList.clear();
            } else {
                int idx1 = SkilledManager.sParseUserList.size() - 1;
                if (idx1 > 0) {
                    ParseUser user = SkilledManager.sParseUserList.get(idx1 - 1);
                    SkilledManager.sParseUserList.remove(idx1);
                    if (!SkilledManager.sParseUserList.isEmpty())
                        mUser = user;
                }
            }


            if (DBG) {
                Log.e(TAG, "size = " + SkilledManager.sParseUserNameList.size());
                for (int i = 0; i < SkilledManager.sParseUserNameList.size(); i++) {
                    Log.e(TAG, "name[" + i + "] = " + SkilledManager.sParseUserNameList.get(i));
                }
            }

            if (SkilledManager.sParseUserNameList.size() == 1) {
                SkilledManager.sParseUserNameList.clear();
            } else {
                int idx2 = SkilledManager.sParseUserNameList.size() - 1;
                if (idx2 != 0) {
                    String userName = SkilledManager.sParseUserNameList.get(idx2 - 1);
                    SkilledManager.sParseUserNameList.remove(idx2);
                    if (!SkilledManager.sParseUserNameList.isEmpty())
                        mUserFullName = userName;
                }
            }
        }


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.image_trend:
                onBackPressed();
                break;

            case R.id.image_follow:
                onFollow();
                break;

            case R.id.layout_followers:
                FollowerActivity.mBlogObject = null;
                FollowerActivity.m_bFollowing = false;
                FollowerActivity.mUser = mUser;
                startActivity(new Intent(this, FollowerActivity.class));
                overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                break;

            case R.id.layout_following:
                FollowerActivity.mBlogObject = null;
                FollowerActivity.m_bFollowing = true;
                FollowerActivity.mUser = mUser;
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
        imageView.setImageResource(R.drawable.btn_back_bg);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        params.width = params.height = size;
        imageView.setLayoutParams(params);
        imageView.setPadding(padding, padding, padding, padding);

        // Search ImageView
        imageView = (ImageView) findViewById(R.id.image_search);
        imageView.setVisibility(View.INVISIBLE);

        // Title TextView
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-DemiBold.otf");
        TextView textTitle = (TextView) findViewById(R.id.text_nav_title);
        textTitle.setText(String.format("%s's Profile", mUserFullName));
        textTitle.setTypeface(typeFace);
        textTitle.setTextSize(textSize);

        View viewEdge = findViewById(R.id.view_h_line);
        params = (RelativeLayout.LayoutParams) viewEdge.getLayoutParams();
        params.height = (int) (res.getDimension(R.dimen.navigation_view_h_line_height) * Config.mScaleFactor);
        viewEdge.setLayoutParams(params);
    }

    private void initViews() {
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

        mAdapter = new ProfileAdapter(this, mBlogList, mImageBlogList, true);

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
        // Fetch user
        String objectId = mUser.getObjectId();

        if (!SkilledManager.mParseUserMap.containsKey(objectId)) {
            mUser.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        ParseUser user = (ParseUser) parseObject;

                        SkilledManager.mParseUserMap.put(user.getObjectId(), user);
                        SkilledManager.setSquareImage(mImageProfileBackground, user, "background", null);
                    }
                }
            });
        } else {
            ParseUser user = SkilledManager.mParseUserMap.get(objectId);
            mUser = user;

            SkilledManager.setSquareImage(mImageProfileBackground, user, "background", null);
        }

        // get posts & following info
        // Get follower count
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Following");
        query.whereEqualTo("followinguser", mUser);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    m_nFollowerCnt = parseObjects.size();

                    m_bFollowed = false;

                    for (ParseObject obj : parseObjects) {
                        if (obj.getParseUser("user").getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                            m_bFollowed = true;
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        // Get following count
        query = ParseQuery.getQuery("Following");
        query.whereEqualTo("user", mUser);


        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    m_nFollowingCnt = parseObjects.size();
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        // Get blog count
        query = ParseQuery.getQuery("Blogs");
        query.whereEqualTo("user", mUser);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    mAdapter.setPostBlogCount(parseObjects.size());
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    private void loadBlogData() {
        if (m_nCurrentCount != mBlogList.size())
            m_nCurrentCount = mBlogList.size();

        // Get Blog
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Blogs");
        query.orderByDescending("createdAt");
        query.whereEqualTo("user", mUser);

        query.setLimit(m_nCountOnce);
        query.setSkip(m_nCurrentCount);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    if (parseObjects.size() > 0) {
                        if (m_nCurrentCount == 0) {
                            mBlogList.clear();
                        }

                        for (ParseObject object : parseObjects) {
                            BlogData blog = new BlogData();

                            blog.strId = object.getObjectId();
                            blog.type = object.getInt("type");
                            blog.strTitle = object.getString("title");
                            blog.strContent = object.getString("text");
                            blog.strVideoName = object.getString("video");
                            blog.photoImage = object.getParseFile("image");
                            blog.date = object.getCreatedAt();
                            blog.user = mUser;
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


                    Log.e(TAG, "Like - notifyDataSetChanged");
                    for (final BlogData blog : mBlogList) {
                        // get like info
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Likes");
                        query.whereEqualTo("blog", blog.object);
                        query.whereEqualTo("user", mUser);

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

                        final int old_pos = mPullRefreshListView.getRefreshableView().getFirstVisiblePosition() + 1;
                        mAdapter.notifyDataSetChanged();
                        mPullRefreshListView.onRefreshComplete();
                        mPullRefreshListView.getRefreshableView().post(new Runnable() {
                            @Override
                            public void run() {
                                mPullRefreshListView.getRefreshableView().setSelection(old_pos);
                            }
                        });

                    }

                } else {
                    CommonUtils.createErrorAlertDialog(OtherProfileActivity.this, "Alert", e.getMessage()).show();
                }
            }
        });


    }

    private void loadImageBlogData() {
        // Get Blog
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Blogs");
        query.orderByDescending("createdAt");
        query.whereEqualTo("user", mUser);
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
                            BlogData blog = new BlogData();

                            blog.strId = object.getObjectId();
                            blog.type = object.getInt("type");
                            blog.strTitle = object.getString("title");
                            blog.strContent = object.getString("text");
                            blog.strVideoName = object.getString("video");
                            blog.photoImage = object.getParseFile("image");
                            blog.date = object.getCreatedAt();
                            blog.user = mUser;
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
                    CommonUtils.createErrorAlertDialog(OtherProfileActivity.this, "Alert", e.getMessage()).show();
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

    private void onFollow() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ImageView imageFollow = (ImageView) findViewById(R.id.image_follow);

        if (!m_bFollowed) {

            ParseObject following = ParseObject.create("Following");

            following.put("user", currentUser);
            following.put("username", SkilledManager.getUserNameToShow(currentUser));
            following.put("followinguser", mUser);
            following.put("followingusername", SkilledManager.getUserNameToShow(mUser));
            following.saveInBackground();

            m_nFollowerCnt++;
            m_bFollowed = true;

            imageFollow.setImageResource(R.drawable.btn_unfollow_bg);

            // send notification to followed user
            ParseQuery parseQuery = ParseInstallation.getQuery();
            parseQuery.whereEqualTo("user", mUser);

            // Send the notification.
            ParsePush push = new ParsePush();
            push.setQuery(parseQuery);

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("alert", String.format("%s follows you", SkilledManager.getUserNameToShow(ParseUser.getCurrentUser())));
            params.put("badge", "Increment");
            params.put("sound", "cheering.caf");

            JSONObject data = new JSONObject(params);

            push.setData(data);
            push.sendInBackground();

        } else {

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Following");
            query.whereEqualTo("user", currentUser);
            query.whereEqualTo("followinguser", mUser);

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null) {
                        for (ParseObject followobject : parseObjects) {
                            followobject.deleteInBackground();
                        }
                    } else {
                        // Log details of the failure
                        Log.e(TAG, e.getMessage());
                    }
                }
            });

            m_nFollowerCnt--;
            m_bFollowed = false;

            imageFollow.setImageResource(R.drawable.btn_follow_bg);
        }

        mAdapter.notifyDataSetChanged();
    }

}
