package com.iliayugai.skilled;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.skilled.adapter.NotificationAdapter;
import com.iliayugai.skilled.data.BlogCategory;
import com.iliayugai.skilled.data.BlogData;
import com.iliayugai.skilled.data.NotificationData;
import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.SkilledManager;
import com.iliayugai.skilled.view.IViewHolder;
import com.iliayugai.skilled.widget.Emojicon.EmojiconTextView;
import com.iliayugai.skilled.widget.RoundedAvatarDrawable;
import com.iliayugai.skilled.widget.TabBar.TabHostProvider;
import com.iliayugai.skilled.widget.TabBar.TabView;
import com.makeramen.segmented.SegmentedGroup;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotifyActivity extends MyCustomActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = NotifyActivity.class.getSimpleName();

    public static final String CHOOSE_NOTIFICATION_TYPE = "choose_notification_type";

    public static final int NOTIFICATION_FOLLOWING = 0;
    public static final int NOTIFICATION_LIKES = 1;
    public static final int NOTIFICATION_COMMENTS = 2;
    public static final int NOTIFICATION_MENTIONS = 3;

    SegmentedGroup mSegment;

//    private NotificationAdapter mAdapter;
    private ArrayList<NotificationData> mFollowingList = new ArrayList<NotificationData>();
    private ArrayList<NotificationData> mLikeList= new ArrayList<NotificationData>();
    private ArrayList<NotificationData> mCommentList = new ArrayList<NotificationData>();
    private ArrayList<NotificationData> mMentionsList = new ArrayList<NotificationData>();

    private int mNotifyType = NOTIFICATION_FOLLOWING;

    private int m_nCountOnce = 10;

    private int m_nFollowCurrentCount = 0;
    private int m_nLikeCurrentCount = 0;
    private int m_nCommentCurrentCount = 0;
    private int m_nMentionCurrentCount = 0;

    private ParseQuery<ParseObject> mQuery = ParseQuery.getQuery("Following");

    private RoundedAvatarDrawable mDefaultAvatarDrawable;
    private Drawable mDefaultBackgroundDrawable;

    private LinearLayout mFollowListLayout;
    private LinearLayout mLikesListLayout;
    private LinearLayout mCommentsListLayout;
    private LinearLayout mMentionsListLayout;

    private class NotificationViewHolder extends IViewHolder {
        public ParseImageView imagePhoto;
        public EmojiconTextView textName;
        public TextView textTime;
        public ParseImageView imageThumbnail;

        public String postPhotoUrl = "";

        public int notificationType = NotifyActivity.NOTIFICATION_FOLLOWING;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate - called");
        super.onCreate(savedInstanceState);

        Config.calculateScaleFactor(this);
        Intent intent = getIntent();

        // Track app opens.
        ParseAnalytics.trackAppOpened(intent);

        TabHostProvider tabProvider = new MyTabHost(this);
        TabView tabView = tabProvider.getTabHost(MyTabHost.NOTIFICATION_TAB_NAME);
        tabView.setCurrentView(R.layout.activity_notify);
        setContentView(tabView.render(MyTabHost.NOTIFICATION_TAB_INDEX));

        initTitleBar();
        initViews();


        mDefaultAvatarDrawable = new RoundedAvatarDrawable(getResources(), R.drawable.profile_photo_default);
        mDefaultBackgroundDrawable = getResources().getDrawable(R.drawable.profile_img_default);

        mFollowListLayout = (LinearLayout)findViewById(R.id.list_follow);
        mLikesListLayout = (LinearLayout)findViewById(R.id.list_likes);
        mCommentsListLayout = (LinearLayout)findViewById(R.id.list_comments);
        mMentionsListLayout = (LinearLayout)findViewById(R.id.list_mentions);
        if (intent != null && intent.getExtras() != null) {
            parseIntent(intent);
        } else {
//            loadNotifications();
            loadFollowingNotifications();
        }
    }


    @Override
    protected void onResume() {
        Log.e(TAG, "onResume - called");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy - called");
    }

    private void forcePhotoImage(NotificationData data, final NotificationViewHolder viewHolder) {
        ParseUser user = null;
        try {
            user = data.user.fetchIfNeeded();
        } catch (ParseException ex) {

        }
        SkilledManager.setAvatarImage(viewHolder.imagePhoto, user, "photo", mDefaultAvatarDrawable);

//        String objectId = data.user.getObjectId();
//
//        if (!SkilledManager.mParseUserMap.containsKey(objectId)) {
//            data.user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
//                @Override
//                public void done(ParseObject parseObject, ParseException e) {
//                    if (e == null) {
//                        ParseUser user = (ParseUser) parseObject;
//                        SkilledManager.mParseUserMap.put(user.getObjectId(), user);
//                        SkilledManager.setAvatarImage(viewHolder.imagePhoto, user, "photo", mDefaultAvatarDrawable);
//                    }
//                }
//            });
//        } else {
//            ParseUser user = SkilledManager.mParseUserMap.get(objectId);
//            SkilledManager.setAvatarImage(viewHolder.imagePhoto, user, "photo", mDefaultAvatarDrawable);
//        }

    }

    private void attachView(ArrayList<NotificationData> arrayList, LinearLayout layout) {
        if (arrayList.size() > 0) {
            layout.removeAllViews();

            long startTime = System.currentTimeMillis();

            for (final NotificationData data : arrayList) {
                View rowView = LayoutInflater.from(this).inflate(R.layout.notify_list_item, null);
                final NotificationViewHolder viewHolder = new NotificationViewHolder();

                viewHolder.imagePhoto = (ParseImageView) rowView.findViewById(R.id.image_photo);
                viewHolder.textName = (EmojiconTextView) rowView.findViewById(R.id.text_notify);
                viewHolder.textTime = (TextView) rowView.findViewById(R.id.text_time);
                viewHolder.imageThumbnail = (ParseImageView) rowView.findViewById(R.id.image_thumbnail);

                Config.scaleLayout(this, "notify", rowView);
                Config.processEmojiconViewHeight(this, "notify", viewHolder.textName);


                viewHolder.imagePhoto.setPlaceholder(mDefaultAvatarDrawable);
                viewHolder.imageThumbnail.setPlaceholder(mDefaultBackgroundDrawable);

                if (data != null) {
                    if (viewHolder.notificationType != data.type) {
                        viewHolder.notificationType = data.type;
                        viewHolder.imagePhoto.setImageDrawable(mDefaultAvatarDrawable);
                        viewHolder.photoUrl = "";
                    }

                    if (data.type == NOTIFICATION_FOLLOWING) {
                        rowView.setBackgroundResource(R.drawable.post_category);
                    } else {
                        rowView.setBackgroundColor(Color.TRANSPARENT);
                    }


                    forcePhotoImage(data, viewHolder);


                    String text = "";
                    switch (data.type) {
                        case NotifyActivity.NOTIFICATION_FOLLOWING:
                            text = String.format("%s followed you", data.strUsername);
                            break;

                        case NotifyActivity.NOTIFICATION_LIKES:
                            if (data.postType == BlogData.BlogImage)
                                text = String.format("%s liked your photo", data.strUsername);
                            else if (data.postType == BlogData.BlogVideo)
                                text = String.format("%s liked your video", data.strUsername);
                            else
                                text = String.format("%s liked your text", data.strUsername);
                            break;

                        case NotifyActivity.NOTIFICATION_COMMENTS:
                            text = String.format("%s commented your post", data.strUsername);
                            break;

                        case NotifyActivity.NOTIFICATION_MENTIONS:
                            text = String.format("%s mentioned you", data.strUsername);
                            break;
                    }
                    Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-Regular.otf");
                    viewHolder.textName.setTypeface(typeFace);
                    viewHolder.textName.setText(text);
                    viewHolder.textTime.setTypeface(typeFace);
                    viewHolder.textTime.setText(CommonUtils.getTimeString(data.date));


                    if ((data.type == NotifyActivity.NOTIFICATION_LIKES
                            || data.type == NotifyActivity.NOTIFICATION_COMMENTS
                            || data.type == NotifyActivity.NOTIFICATION_MENTIONS) && data.image != null) {
                        String url = data.image.getUrl();

                        if (!url.equals(viewHolder.postPhotoUrl)) {
                            viewHolder.postPhotoUrl = url;

                            viewHolder.imageThumbnail.setVisibility(View.VISIBLE);

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        viewHolder.imageThumbnail.setParseFile(data.image);
                                        viewHolder.imageThumbnail.loadInBackground();
                                    } catch (java.util.concurrent.RejectedExecutionException ex) {
                                    }
                                }
                            }).start();

                        }
                    } else {
                        viewHolder.imageThumbnail.setVisibility(View.INVISIBLE);
                        viewHolder.postPhotoUrl = "";
                    }


                    viewHolder.imageThumbnail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ParseObject parseObject = null;
                            try {
                                parseObject = data.blog.fetchIfNeeded();
                            } catch (ParseException ex) {

                            }

                            BlogData blogData = new BlogData();

                            blogData.strId = parseObject.getObjectId();
                            blogData.type = parseObject.getInt("type");
                            blogData.strTitle = parseObject.getString("title");
                            blogData.strContent = parseObject.getString("text");
                            blogData.strVideoName = parseObject.getString("video");
                            blogData.photoImage = parseObject.getParseFile("image");
                            blogData.date = parseObject.getCreatedAt();
                            blogData.user = parseObject.getParseUser("user");

                            for (BlogCategory category : SkilledManager.mCategoryList) {
                                if (category.strId.equals(parseObject.getString("category"))) {
                                    blogData.category = category;
                                    break;
                                }
                            }
                            blogData.object = parseObject;

                            blogData.bLiked = -1;
                            blogData.nLikeCount = parseObject.getInt("likes");

                            BlogViewActivity.mBlogData = blogData;

                            CommonUtils.moveNextActivityWithoutFinish(NotifyActivity.this, BlogViewActivity.class);
                        }
                    });

                    rowView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (data != null) {
                                OtherProfileActivity.mUser = data.user;
                                OtherProfileActivity.mUserFullName = data.strUsername;

                                Log.d(TAG, "Move to other profile view for " + data.strUsername);

                                startActivity(new Intent(NotifyActivity.this, OtherProfileActivity.class));
                                overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                            }

                        }
                    });
                    layout.addView(rowView);
                }
            }
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;

            Log.e(TAG, "AttachView ElpasedTime = " + elapsedTime / 1000 + "." + elapsedTime % 1000 + "ms");
        }
    }

//    private void addViewHolder(int notificationType) {
//        switch (notificationType) {
//            case NOTIFICATION_FOLLOWING:
//                attachView(mFollowingList, mFollowListLayout);
//                break;
//
//            case NOTIFICATION_LIKES:
//                attachView(mLikeList, mLikesListLayout);
//                break;
//
//            case NOTIFICATION_COMMENTS:
//                attachView(mCommentList, mCommentsListLayout);
//                break;
//
//            case NOTIFICATION_MENTIONS:
//                attachView(mMentionsList, mMentionsListLayout);
//                break;
//        }
//    }


    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.image_trend:
                onBackPressed();
                break;
        }
    }

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        NotificationData data = null;
//
//        switch (mNotifyType) {
//            case NOTIFICATION_FOLLOWING:
//                data = mFollowingList.get(position);
//                break;
//
//            case NOTIFICATION_LIKES:
//                data = mLikeList.get(position);
//                break;
//
//            case NOTIFICATION_COMMENTS:
//                data = mCommentList.get(position);
//                break;
//
//            case NOTIFICATION_MENTIONS:
//                data = mMentionsList.get(position);
//                break;
//        }
//
//        if (data != null) {
//            OtherProfileActivity.mUser = data.user;
//            OtherProfileActivity.mUserFullName = data.strUsername;
//
//            Log.d(TAG, "Move to other profile view for " + data.strUsername);
//
//            startActivity(new Intent(this, OtherProfileActivity.class));
//            overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
//        }
//    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mQuery.cancel();
            }
        }).start();

        switch (checkedId) {
            case R.id.radio_following:
                m_nFollowCurrentCount = 0;
                mFollowListLayout.setVisibility(View.VISIBLE);
                mLikesListLayout.setVisibility(View.GONE);
                mCommentsListLayout.setVisibility(View.GONE);
                mMentionsListLayout.setVisibility(View.GONE);
                if (mFollowingList.size() == 0)
                    loadFollowingNotifications();
                break;

            case R.id.radio_likes:
                m_nLikeCurrentCount = 0;
                mFollowListLayout.setVisibility(View.GONE);
                mLikesListLayout.setVisibility(View.VISIBLE);
                mCommentsListLayout.setVisibility(View.GONE);
                mMentionsListLayout.setVisibility(View.GONE);
                if (mLikeList.size() == 0)
                    loadLikesNotifications();
                break;

            case R.id.radio_comments:
                m_nCommentCurrentCount = 0;
                mFollowListLayout.setVisibility(View.GONE);
                mLikesListLayout.setVisibility(View.GONE);
                mCommentsListLayout.setVisibility(View.VISIBLE);
                mMentionsListLayout.setVisibility(View.GONE);
                if (mCommentList.size() == 0)
                    loadCommentsNotifications();
                break;

            case R.id.radio_mentions:
                m_nMentionCurrentCount = 0;
                mFollowListLayout.setVisibility(View.GONE);
                mLikesListLayout.setVisibility(View.GONE);
                mCommentsListLayout.setVisibility(View.GONE);
                mMentionsListLayout.setVisibility(View.VISIBLE);
                if (mMentionsList.size() == 0)
                    loadMentionsNotifications();
                break;
        }


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                mQuery.cancel();
//            }
//        }).start();

//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                loadNotifications();
//            }
//        }, 300);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.e(TAG, "onNewIntent() - called");
        super.onNewIntent(intent);

        // Track app opens.
        ParseAnalytics.trackAppOpened(intent);

        if (intent != null && intent.getExtras() != null) {
            parseIntent(intent);
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
        imageView.setVisibility(View.INVISIBLE);

        // Search ImageView
        imageView = (ImageView) findViewById(R.id.image_search);
        imageView.setVisibility(View.INVISIBLE);

        // Title TextView
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-DemiBold.otf");
        TextView textTitle = (TextView) findViewById(R.id.text_nav_title);
        textTitle.setText(R.string.notifications);
        textTitle.setTypeface(typeFace);
        textTitle.setTextSize(textSize);

        View viewEdge = findViewById(R.id.view_h_line);
        params = (RelativeLayout.LayoutParams) viewEdge.getLayoutParams();
        params.height = (int) (res.getDimension(R.dimen.navigation_view_h_line_height) * Config.mScaleFactor);
        viewEdge.setLayoutParams(params);
    }

    private void initViews() {
        mSegment = (SegmentedGroup) findViewById(R.id.segment_type);
        mSegment.setTintColor(getResources().getColor(R.color.tint_color), getResources().getColor(R.color.navigation_bar_back_color));
        mSegment.setOnCheckedChangeListener(this);
        Config.scaleLayout(this, "notify", mSegment);

//        mAdapter = new NotificationAdapter(this, mFollowingList, mLikeList, mCommentList, mMentionsList);

//        ListView listView = (ListView) findViewById(R.id.list_follower);
//        listView.setAdapter(mAdapter);
//        listView.setOnItemClickListener(this);
    }

    private void onCheckSegment(int type) {
        int segmentId;

        switch (type) {
            case NOTIFICATION_FOLLOWING:
                segmentId = R.id.radio_following;
                break;

            case NOTIFICATION_LIKES:
                segmentId = R.id.radio_likes;
                break;

            case NOTIFICATION_COMMENTS:
                segmentId = R.id.radio_comments;
                break;

            case NOTIFICATION_MENTIONS:
                segmentId = R.id.radio_mentions;
                break;

            default:
                return;
        }

        mSegment.check(segmentId);

        if (segmentId == mSegment.getCheckedRadioButtonId())
            loadNotifications();

        if (type > NOTIFICATION_FOLLOWING) {
            final Dialog progressDialog = CommonUtils.createFullScreenProgress(this);
            progressDialog.show();

            SkilledManager.mNotifyBlogObject.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject blogObject, ParseException e) {
                    progressDialog.dismiss();

                    if (e == null) {
                        BlogData blogData = new BlogData();

                        blogData.strId = blogObject.getObjectId();
                        blogData.type = blogObject.getInt("type");
                        blogData.strTitle = blogObject.getString("title");
                        blogData.strContent = blogObject.getString("text");
                        blogData.strVideoName = blogObject.getString("video");
                        blogData.photoImage = blogObject.getParseFile("image");
                        blogData.date = blogObject.getCreatedAt();
                        blogData.user = blogObject.getParseUser("user");

                        for (BlogCategory category : SkilledManager.mCategoryList) {
                            if (category.strId.equals(blogObject.getString("category"))) {
                                blogData.category = category;
                                break;
                            }
                        }

                        blogData.object = blogObject;

                        blogData.bLiked = -1;
                        blogData.nLikeCount = blogObject.getInt("likes");

                        BlogViewActivity.mBlogData = blogData;

                        CommonUtils.moveNextActivityWithoutFinish(NotifyActivity.this, BlogViewActivity.class);
                    }
                }
            });
        }
    }

    private void loadNotifications() {
        loadFollowingNotifications();
        loadLikesNotifications();
        loadCommentsNotifications();
        loadMentionsNotifications();
    }

    private void fetchUser(NotificationData notificationData) {
        // Fetch user
        String objectId = notificationData.user.getObjectId();

        if (!SkilledManager.mParseUserMap.containsKey(objectId)) {
            notificationData.user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        ParseUser user = (ParseUser) parseObject;

                        SkilledManager.mParseUserMap.put(user.getObjectId(), user);
                    }
                }
            });
        }

    }

    private void loadFollowingNotifications() {
        Log.e(TAG, "Following =====================");
        // get following info
        mQuery = ParseQuery.getQuery("Following");
        mQuery.whereEqualTo("followinguser", ParseUser.getCurrentUser());
        mQuery.orderByDescending("updatedAt");

        mQuery.setLimit(m_nCountOnce);
        mQuery.setSkip(m_nFollowCurrentCount);

        final long startTime = System.currentTimeMillis();

        mQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    if (m_nFollowCurrentCount == 0)
                        mFollowingList.clear();

                    for (ParseObject obj : parseObjects) {
                        final NotificationData notifyData = new NotificationData();

                        notifyData.user = obj.getParseUser("user");
                        notifyData.strUsername = obj.getString("username");
                        notifyData.type = NOTIFICATION_FOLLOWING;
                        notifyData.date = obj.getUpdatedAt();

                        mFollowingList.add(notifyData);

                        // Fetch user
                        fetchUser(notifyData);
                    }
                    m_nFollowCurrentCount += parseObjects.size();

                    calculateTime(startTime);

                    attachView(mFollowingList, mFollowListLayout);

                } else {
                    CommonUtils.createErrorAlertDialog(NotifyActivity.this, "Alert", e.getMessage()).show();
                }
            }
        });

    }


    private void loadLikesNotifications() {
        Log.e(TAG, "Likes =====================");
        // get like info
        mQuery = ParseQuery.getQuery("Likes");
        mQuery.whereEqualTo("targetuser", ParseUser.getCurrentUser());
        mQuery.orderByDescending("updatedAt");

        mQuery.setLimit(m_nCountOnce);
        mQuery.setSkip(m_nLikeCurrentCount);

        final long startTime = System.currentTimeMillis();

        mQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    if (m_nLikeCurrentCount == 0) {
                        mLikeList.clear();
                    }

                    for (ParseObject obj : parseObjects) {
                        int nType = obj.getInt("type");

                        final NotificationData notifyData = new NotificationData();

                        notifyData.user = obj.getParseUser("user");
                        notifyData.strUsername = obj.getString("username");

                        notifyData.type = NOTIFICATION_LIKES;
                        notifyData.postType = nType;

                        notifyData.image = obj.getParseFile("thumbnail");
                        notifyData.date = obj.getUpdatedAt();
                        notifyData.blog = obj.getParseObject("blog");

                        mLikeList.add(notifyData);


                        // Fetch user
                        fetchUser(notifyData);
                    }

                    m_nLikeCurrentCount += parseObjects.size();
                    calculateTime(startTime);
                    attachView(mLikeList, mLikesListLayout);
                } else {
                    CommonUtils.createErrorAlertDialog(NotifyActivity.this, "Alert", e.getMessage()).show();
                }
            }
        });
    }

    private void loadCommentsNotifications() {
        // get like info
        Log.e(TAG, "Comment =====================");
        mQuery = ParseQuery.getQuery("Comments");
        mQuery.whereEqualTo("targetuser", ParseUser.getCurrentUser());
        mQuery.orderByDescending("updatedAt");
        mQuery.setLimit(m_nCountOnce);
        mQuery.setSkip(m_nCommentCurrentCount);

        final long startTime = System.currentTimeMillis();

        mQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {

                if (e == null) {

                    if (m_nCommentCurrentCount == 0)
                        mCommentList.clear();

                    for (ParseObject obj : parseObjects) {

                        final NotificationData notifyData = new NotificationData();

                        notifyData.user = obj.getParseUser("user");
                        notifyData.strUsername = obj.getString("username");
                        notifyData.strComment = obj.getString("content");
                        notifyData.image = obj.getParseFile("thumbnail");
                        notifyData.date = obj.getUpdatedAt();
                        notifyData.type = NOTIFICATION_COMMENTS;
                        notifyData.blog = obj.getParseObject("blog");

                        mCommentList.add(notifyData);

                        // Fetch user
                        fetchUser(notifyData);
                    }
                    m_nCommentCurrentCount += parseObjects.size();

                    calculateTime(startTime);
                    attachView(mCommentList, mCommentsListLayout);
                } else {
                    CommonUtils.createErrorAlertDialog(NotifyActivity.this, "Alert", e.getMessage()).show();
                }
            }
        });
    }


    private void calculateTime(long startTime) {
        long endTime = System.currentTimeMillis();
        long queryElapsedTime = endTime - startTime;
        Log.e(TAG, "queryTime = " + queryElapsedTime / 1000 + "." + queryElapsedTime % 1000 + "ms");
    }
    private void loadMentionsNotifications() {
        Log.e(TAG, "Mentions =====================");
        // get mention info
        mQuery = ParseQuery.getQuery("Mentions");
        mQuery.whereEqualTo("targetuser", ParseUser.getCurrentUser());
        mQuery.orderByDescending("updatedAt");
        mQuery.setLimit(m_nCountOnce);
        mQuery.setSkip(m_nMentionCurrentCount);

        final long startTime = System.currentTimeMillis();

        mQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {

                    if (m_nMentionCurrentCount == 0)
                        mMentionsList.clear();

                    for (ParseObject obj : parseObjects) {
                        final NotificationData notifyData = new NotificationData();

                        notifyData.user = obj.getParseUser("user");
                        notifyData.strUsername = obj.getString("username");
                        notifyData.image = obj.getParseFile("thumbnail");
                        notifyData.date = obj.getUpdatedAt();
                        notifyData.type = NOTIFICATION_MENTIONS;
                        notifyData.blog = obj.getParseObject("blog");

                        mMentionsList.add(notifyData);

                        // Fetch user
                        fetchUser(notifyData);
                    }
                    m_nMentionCurrentCount += parseObjects.size();
                    calculateTime(startTime);
                    attachView(mMentionsList, mMentionsListLayout);
                } else {
                    CommonUtils.createErrorAlertDialog(NotifyActivity.this, "Alert", e.getMessage()).show();
                }
            }
        });
    }


    /*************************************************************************/
    /*                          Push Notification Parse                      */
    /*************************************************************************/

    private void parseIntent(Intent intent) {
        try {
            String action = intent.getAction();
            String channel = intent.getExtras().getString("com.parse.Channel");
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

            Log.d(TAG, "got action " + action + " on channel " + channel + " with:");

            Iterator iterator = json.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                Log.d(TAG, "..." + key + " => " + json.getString(key));

                if (key.equals("notifyType")) {
                    SkilledManager.mStrNotifyType = json.getString(key);
                } else if (key.equals("notifyBlog")) {
                    String strNotifyBlogId = json.getString(key);
                    SkilledManager.mNotifyBlogObject = ParseObject.createWithoutData("Blogs", strNotifyBlogId);
                }
            }

            if (!TextUtils.isEmpty(SkilledManager.mStrNotifyType))
                SkilledManager.mStrNotifyType = "follow";

            checkNotification();

        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }

    private void checkNotification() {
        if (!TextUtils.isEmpty(SkilledManager.mStrNotifyType)) {

            int segmentId;

            if (SkilledManager.mStrNotifyType.equals("follow")) {
                segmentId = NotifyActivity.NOTIFICATION_FOLLOWING;
            } else if (SkilledManager.mStrNotifyType.equals("like")) {
                segmentId = NotifyActivity.NOTIFICATION_LIKES;
            } else if (SkilledManager.mStrNotifyType.equals("comment")) {
                segmentId = NotifyActivity.NOTIFICATION_COMMENTS;
            } else if (SkilledManager.mStrNotifyType.equals("mention")) {
                segmentId = NotifyActivity.NOTIFICATION_MENTIONS;
            } else {
                return;
            }

            SkilledManager.mStrNotifyType = "";
            onCheckSegment(segmentId);
        }
    }
}
