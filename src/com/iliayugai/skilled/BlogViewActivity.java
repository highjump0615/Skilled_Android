package com.iliayugai.skilled;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
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

import com.iliayugai.skilled.adapter.BlogAdapter;
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
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class BlogViewActivity extends Activity implements View.OnClickListener {

    private static final String TAG = BlogViewActivity.class.getSimpleName();

    private View mLayoutMore;
    private boolean m_bMoreOn = false;
    private int m_nMoreViewHeight;
    private boolean m_bReady = false;

    // Data to post
    public static BlogData mBlogData = new BlogData();

    private BlogAdapter mAdapter;

    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blog_view);

        initTitleBar();
        initMoreLayout();
        initViews();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.image_trend:
                onBackPressed();
                break;

            case R.id.btn_more_facebook:
                PostUtils.onMoreFacebook(this, mBlogData);
                hideMoreView();
                break;

            case R.id.btn_more_twitter:
                PostUtils.onMoreTwitter(this, mBlogData);
                hideMoreView();
                break;

            case R.id.btn_more_email:
                PostUtils.onMoreEmail(this, mBlogData);
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
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout.getLayoutParams();
        params.height = size;
        layout.setLayoutParams(params);

        // Trend ImageView
        ImageView imageView = (ImageView) findViewById(R.id.image_trend);
        imageView.setOnClickListener(this);
        params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
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
        textTitle.setText("");
        textTitle.setTypeface(typeFace);
        textTitle.setTextSize(textSize);

        View viewEdge = findViewById(R.id.view_h_line);
        params = (RelativeLayout.LayoutParams) viewEdge.getLayoutParams();
        params.height = (int) (res.getDimension(R.dimen.navigation_view_h_line_height) * Config.mScaleFactor);
        viewEdge.setLayoutParams(params);
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

    private void initViews() {
        ArrayList<BlogData> blogList = new ArrayList<BlogData>();
        blogList.add(mBlogData);

        ListView listView = (ListView) findViewById(R.id.list_one_blog);

        mAdapter = new BlogAdapter(this, blogList);
        listView.setAdapter(mAdapter);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                hideMoreView();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        if (mBlogData.user == null) {
            m_bReady = false;

            mProgressDialog = CommonUtils.createFullScreenProgress(this);
            mProgressDialog.show();

            mBlogData.object.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    mBlogData.type = parseObject.getInt("type");
                    mBlogData.strTitle = parseObject.getString("title");
                    mBlogData.strContent = parseObject.getString("text");
                    mBlogData.strVideoName = parseObject.getString("video");
                    mBlogData.photoImage = parseObject.getParseFile("image");
                    mBlogData.date = parseObject.getCreatedAt();
                    mBlogData.user = parseObject.getParseUser("user");

                    String objectId = mBlogData.user.getObjectId();
                    if (!SkilledManager.mParseUserMap.containsKey(objectId)) {
                        mBlogData.user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject parseObject, ParseException e) {
                                mProgressDialog.dismiss();

                                if (e == null) {
                                    ParseUser user = (ParseUser) parseObject;
                                    String objectId = user.getObjectId();
                                    SkilledManager.mParseUserMap.put(objectId, user);
                                }

                                m_bReady = true;
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                    // set category
                    for (BlogCategory category : SkilledManager.mCategoryList) {
                        if (category.strId.equals(parseObject.getString("category"))) {
                            mBlogData.category = category;
                            break;
                        }
                    }

                    mBlogData.nLikeCount = parseObject.getInt("likes");
                }
            });
        } else {
            m_bReady = true;
        }

        // get like info
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Likes");
        query.whereEqualTo("blog", mBlogData.object);
        query.whereEqualTo("user", ParseUser.getCurrentUser());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> likeObjects, ParseException e) {
                if (e == null) {
                    if (likeObjects.size() > 0)
                        mBlogData.bLiked = 1;
                    else
                        mBlogData.bLiked = 0;

                    mAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });

        mBlogData.mCommentList = new ArrayList<CommentData>();
        query = ParseQuery.getQuery("Comments");
        query.whereEqualTo("blog", mBlogData.object);
        query.orderByDescending("updatedAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> commentObjects, ParseException e) {
                if (e == null) {
                    if (commentObjects == null) return;

                    for (ParseObject object : commentObjects) {
                        CommentData comment = new CommentData();

                        comment.user = object.getParseUser("user");
                        comment.strContent = object.getString("content");
                        comment.strUsername = object.getString("username");
                        comment.date = object.getUpdatedAt();
                        comment.object = object;

                        mBlogData.mCommentList.add(comment);
                    }

                    mAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, e.getMessage());
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
            //m_nCurBlogNum = index;

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
