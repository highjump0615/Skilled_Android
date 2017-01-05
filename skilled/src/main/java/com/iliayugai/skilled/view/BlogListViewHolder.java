package com.iliayugai.skilled.view;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.skilled.BlogViewActivity;
import com.iliayugai.skilled.CategoryActivity;
import com.iliayugai.skilled.CommentActivity;
import com.iliayugai.skilled.FollowerActivity;
import com.iliayugai.skilled.HomeActivity;
import com.iliayugai.skilled.OtherProfileActivity;
import com.iliayugai.skilled.ProfileActivity;
import com.iliayugai.skilled.R;
import com.iliayugai.skilled.data.BlogCategory;
import com.iliayugai.skilled.data.BlogData;
import com.iliayugai.skilled.data.CommentData;
import com.iliayugai.skilled.utils.AmazonWebServiceUtils;
import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.SkilledManager;
import com.iliayugai.skilled.widget.CustomVideoView;
import com.iliayugai.skilled.widget.Emojicon.EmojiconButton;
import com.iliayugai.skilled.widget.Emojicon.EmojiconTextView;
import com.iliayugai.skilled.widget.RoundedAvatarDrawable;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.HashMap;

public class BlogListViewHolder extends IViewHolder {

    private static final String TAG = BlogListViewHolder.class.getSimpleName();

    public ParseImageView imagePhoto;
    public EmojiconButton btnUsername;
    public ImageView imageLocation;
    public TextView textLocation;
    public ImageView imageClock;
    public TextView textTime;
    public ViewGroup layoutImage;
    public ParseImageView imageImage;
    public CustomVideoView videoView;
    public View layoutTextPost;
    public EmojiconTextView textTitle;
    public EmojiconTextView textContent;
    public ImageView btnPlay;
    public View layoutLike;
    public View layoutLikeList;
    public ImageView imageLike;
    public TextView textLike;
    public Button btnCategory;
    public View layoutComment;
    public ImageView imageComment;
    public EmojiconTextView textComment;
    public ImageView btnLike;
    public ImageView btnComment;
    public ImageView btnMore;

    private Activity mActivity;

    private static Resources mResources;
    private RoundedAvatarDrawable mDefaultAvatarDrawable;
    private Drawable mDefaultBackground;

    // Prevent reload
    private String mPostImageUrl = "";
    private String mPostVideoUrl = "";


    public BlogListViewHolder(View listItemLayout, View.OnClickListener onClickListener) {

        mActivity = (Activity) onClickListener;
        mResources = mActivity.getResources();

        Typeface smallTextTypeface = Typeface.createFromAsset(mActivity.getAssets(), "fonts/AvenirNext-Regular.otf");
        Typeface largeTextTypeface = Typeface.createFromAsset(mActivity.getAssets(), "fonts/AvenirNext-Bold.otf");

        imagePhoto = (ParseImageView) listItemLayout.findViewById(R.id.image_photo);
        btnUsername = (EmojiconButton) listItemLayout.findViewById(R.id.button_name);
        imageLocation = (ImageView) listItemLayout.findViewById(R.id.image_location);
        textLocation = (TextView) listItemLayout.findViewById(R.id.text_location);
        imageClock = (ImageView) listItemLayout.findViewById(R.id.image_clock);
        textTime = (TextView) listItemLayout.findViewById(R.id.text_time);
        layoutTextPost = listItemLayout.findViewById(R.id.layout_post);
        textTitle = (EmojiconTextView) listItemLayout.findViewById(R.id.text_title);
        textContent = (EmojiconTextView) listItemLayout.findViewById(R.id.text_content);
        layoutImage = (ViewGroup) listItemLayout.findViewById(R.id.layout_image_movie);
        imageImage = (ParseImageView) listItemLayout.findViewById(R.id.image_post);
        videoView = (CustomVideoView) listItemLayout.findViewById(R.id.video_post);
        btnPlay = (ImageView) listItemLayout.findViewById(R.id.image_play);
        layoutLikeList = listItemLayout.findViewById(R.id.layout_like_list);
        layoutLike = listItemLayout.findViewById(R.id.layout_like);
        imageLike = (ImageView) listItemLayout.findViewById(R.id.image_like_icon);
        textLike = (TextView) listItemLayout.findViewById(R.id.text_like);
        btnCategory = (Button) listItemLayout.findViewById(R.id.button_category);
        layoutComment = listItemLayout.findViewById(R.id.layout_comment);
        imageComment = (ImageView) listItemLayout.findViewById(R.id.image_comment_icon);
        textComment = (EmojiconTextView) listItemLayout.findViewById(R.id.text_comment);
        btnLike = (ImageView) listItemLayout.findViewById(R.id.image_like);
        btnComment = (ImageView) listItemLayout.findViewById(R.id.image_comment);
        btnMore = (ImageView) listItemLayout.findViewById(R.id.image_more);

        btnUsername.setTypeface(smallTextTypeface);
        btnUsername.setText("");

        textLocation.setTypeface(smallTextTypeface);
        textLocation.setText("");

        textTime.setTypeface(smallTextTypeface);
        textTime.setText("");

        btnCategory.setTypeface(smallTextTypeface);

        textLike.setTypeface(smallTextTypeface);
        textContent.setTypeface(smallTextTypeface);
        textComment.setTypeface(smallTextTypeface);
        textTitle.setTypeface(largeTextTypeface);
        textTitle.setText("");

        layoutTextPost.setVisibility(View.VISIBLE);
        textTitle.setVisibility(View.GONE);

        // for inner scrolling
        //textContent.setMovementMethod(new ScrollingMovementMethod());
        textComment.setMovementMethod(new ScrollingMovementMethod());

        mDefaultAvatarDrawable = new RoundedAvatarDrawable(mResources, R.drawable.profile_photo_default);
        imagePhoto.setImageDrawable(mDefaultAvatarDrawable);
        mDefaultBackground = mActivity.getResources().getDrawable(R.drawable.home_default_image);
        layoutImage.setBackgroundColor(Color.TRANSPARENT);

        // scaling
        Config.scaleLayout(mActivity, "blog", listItemLayout);
        Config.processEmojiconViewHeight(mActivity, "blog", btnUsername);
        Config.processEmojiconViewHeight(mActivity, "blog", textTitle);
        Config.processEmojiconViewHeight(mActivity, "blog", textContent);
        Config.processEmojiconViewHeight(mActivity, "blog", textComment);
    }

    public void fillContent(final BlogData blogData, int position) {

        imageLocation.setVisibility(View.INVISIBLE);
        imageImage.setVisibility(View.INVISIBLE);
        layoutImage.setBackgroundColor(Color.TRANSPARENT);
        btnPlay.setVisibility(View.INVISIBLE);
        textTitle.setVisibility(View.GONE);
        videoView.stopPlayback();
        btnPlay.setEnabled(false);

        // user photo loading
        ParseUser user = SkilledManager.mParseUserMap.get(blogData.user.getObjectId());
        int loadResult = SkilledManager.isImageLoaded(imagePhoto, user, "photo");

        switch (loadResult) {
            case SkilledManager.IMAGE_NULL:
                imagePhoto.setImageDrawable(mDefaultAvatarDrawable);
                mPostVideoUrl = "";
                break;

            case SkilledManager.IMAGE_LOADED:
                if (imagePhoto.getDrawable() != mDefaultAvatarDrawable) break;

            case SkilledManager.IMAGE_UNLOADED:
                SkilledManager.setAvatarImage(imagePhoto, user, "photo", mDefaultAvatarDrawable);
                //SkilledManager.setLoadedImageUrl(imagePhoto, user, "photo");
                break;
        }

        // location text
        String location = user.getString("location");
        textLocation.setText(location);

        // location icon
        imageLocation.setVisibility(TextUtils.isEmpty(location) ? View.INVISIBLE : View.VISIBLE);

        // username button
        btnUsername.setText(SkilledManager.getUserNameToShow(user), TextView.BufferType.SPANNABLE);

        btnUsername.setTag(blogData.user);
        btnUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser user = (ParseUser) v.getTag();

                if (user != null && (mActivity instanceof HomeActivity || mActivity instanceof CategoryActivity)) {
                    OtherProfileActivity.mUser = user;
                    OtherProfileActivity.mUserFullName = ((Button) v).getText().toString();

                    mActivity.startActivity(new Intent(mActivity, OtherProfileActivity.class));
                    mActivity.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                }
            }
        });

        // Time text
        textTime.setText(CommonUtils.getTimeString(blogData.date));

        // Content text
        textContent.setText(blogData.strContent, TextView.BufferType.SPANNABLE);
        //textContent.setLineSpacing(0.0f, 1.2f);
        int height = textContent.getLineHeight() * textContent.getLineCount();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textContent.getLayoutParams();
        params.height = (int) (height/* * 0.85f*/);//(int) (CommonUtils.getTextViewHeight(textContent) * 1.2f);
        //params.height = (int) (mResources.getDimension(R.dimen.blog_text_content_large_height) * Config.mScaleFactor);
        textContent.setLayoutParams(params);

        //if (Config.DEBUG) Log.d(TAG, "++++++++ content = " + blogData.strContent + " , height = " + height);

        // check if text post or not
        if (blogData.type == BlogData.BlogText) {
            layoutImage.setVisibility(View.GONE);

            textTitle.setVisibility(View.VISIBLE);
            textTitle.setText(blogData.strTitle, TextView.BufferType.SPANNABLE);
        } else {
            layoutImage.setVisibility(View.VISIBLE);
            layoutImage.setBackgroundColor(Color.TRANSPARENT);
            imageImage.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.INVISIBLE);

            String url = blogData.photoImage.getUrl();
            if (!mPostImageUrl.equals(url)) {
                mPostImageUrl = url;

                imageImage.setPlaceholder(mDefaultBackground);
                imageImage.setParseFile(blogData.photoImage);
                imageImage.loadInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] bytes, ParseException e) {
                        if (e == null)
                            layoutImage.setBackgroundColor(Color.BLACK);
                    }
                });
            } else {
                layoutImage.setBackgroundColor(Color.BLACK);
            }

            if (blogData.type == BlogData.BlogVideo) {
                videoView.setVisibility(View.VISIBLE);
                btnPlay.setVisibility(View.VISIBLE);

                Log.w(TAG, "++++++++++++ " + blogData.strContent + ", " + blogData.strVideoName + " ++++++++++++");
                final String videoUrl = blogData.strVideoName.replace(".mov", ".mp4");

                if (!mPostVideoUrl.equals(videoUrl)) {
                    videoView.stopPlayback();
                    btnPlay.setImageResource(R.drawable.btn_play_bg);
                    btnPlay.setEnabled(false);

                    final String preSignedUrl = AmazonWebServiceUtils.getFileURL(videoUrl).toString();

                    // Setup VideoView
                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            int width = mediaPlayer.getVideoWidth();
                            int height = mediaPlayer.getVideoHeight();

                            Log.w(TAG, "video size = " + width + "x" + height);
                            Log.e(TAG, "mediaPlayer.getDuration() = " + mediaPlayer.getDuration());

                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                            );

                            int matchSize = Config.mRealScreenWidth;

                            if (width >= height) {
                                layoutParams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.image_post);
                                layoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.image_post);
                                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

                                float ratio = height / (width * 1.0f);
                                width = matchSize;
                                height = (int) (width * ratio);
                            } else {
                                layoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.image_post);
                                layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.image_post);
                                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

                                float ratio = width / (height * 1.0f);
                                height = matchSize;
                                width = (int) (height * ratio);
                            }

                            videoView.setLayoutParams(layoutParams);

                            videoView.setDimensions(width, height);
                            videoView.getHolder().setFixedSize(width, height);

                            layoutImage.setBackgroundColor(Color.BLACK);

                            videoView.seekTo(1);
                            imageImage.setVisibility(View.INVISIBLE);

                            btnPlay.setEnabled(true);

                            mPostVideoUrl = preSignedUrl;
                        }
                    });
                    videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            videoView.seekTo(1);
                            btnPlay.setImageResource(R.drawable.btn_play_bg);
                        }
                    });
                    videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            Log.e(TAG, "In VideoView, playing error occurred!");
                            btnPlay.setImageResource(R.drawable.btn_play_bg);
                            btnPlay.setEnabled(false);
                            imageImage.setVisibility(View.VISIBLE);
                            return true;
                        }
                    });
                    videoView.setVideoURI(Uri.parse(preSignedUrl));

                    btnPlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (videoView.isPlaying()) {
                                videoView.pause();
                                btnPlay.setImageResource(R.drawable.btn_play_bg);
                                videoView.clearFocus();
                            } else {
                                videoView.requestFocus();
                                videoView.start();
                                btnPlay.setImageResource(R.drawable.btn_pause_bg);
                            }
                        }
                    });
                }
            }

            /*params.height = (int) (mResources.getDimension(R.dimen.blog_text_content_height) * Config.mScaleFactor);
            params.height = (params.height < height) ? ViewGroup.LayoutParams.WRAP_CONTENT : height;
            textContent.setLayoutParams(params);*/
        }

        // Like text
        textLike.setText(blogData.nLikeCount + " likes");

        if (blogData.bLiked > 0) {
            imageLike.setImageResource(R.drawable.home_liked_icon);
            btnLike.setEnabled(false);
        } else if (blogData.bLiked == 0) {
            imageLike.setImageResource(R.drawable.home_like_icon);
            btnLike.setEnabled(true);
        } else { // undetermined
            imageLike.setImageResource(R.drawable.home_like_icon);
            btnLike.setEnabled(false);
        }

        // to liked user list
        layoutLikeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FollowerActivity.mBlogObject = blogData.object;
                FollowerActivity.m_bFollowing = false;
                FollowerActivity.mUser = blogData.user;
                mActivity.startActivity(new Intent(mActivity, FollowerActivity.class));
                mActivity.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
            }
        });

        // Category button
        final String strCategory;
        if (blogData.category != null && !TextUtils.isEmpty(blogData.category.strName)) {
            strCategory = blogData.category.strName;
        } else {
            strCategory = "Other All";
        }
        btnCategory.setText(String.format("[ %s ]", strCategory));

        btnCategory.setTag(blogData.category);
        btnCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BlogCategory category = (BlogCategory) v.getTag();

                if (category == null) {
                    SkilledManager.setCategory(null);
                    CommonUtils.moveNextActivityWithoutFinish(mActivity, CategoryActivity.class);
                } else if (!strCategory.equals(category.strName) || mActivity instanceof HomeActivity
                        || mActivity instanceof ProfileActivity || mActivity instanceof BlogViewActivity
                        || mActivity instanceof OtherProfileActivity) {
                    SkilledManager.setCategory(category);
                    CommonUtils.moveNextActivityWithoutFinish(mActivity, CategoryActivity.class);
                }
            }
        });

        // Comment text
        if (blogData.mCommentList != null && blogData.mCommentList.size() > 0) {
            CharSequence totalComments = new SpannableString("");

            for (CommentData commentData : blogData.mCommentList) {
                SpannableString spannableString = CommonUtils.getBoldNormalString(commentData.strUsername, commentData.strContent);
                totalComments = TextUtils.concat(totalComments, spannableString, "\n");
            }

            textComment.setText(totalComments, TextView.BufferType.SPANNABLE);
        } else {
            textComment.setTextColor(Color.GRAY);
            textComment.setText(R.string.no_comments_yet);
        }

        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blogData.bLiked == 0) {
                    ParseObject likeObject = ParseObject.create("Likes");
                    likeObject.put("blog", blogData.object);
                    likeObject.put("user", ParseUser.getCurrentUser());
                    likeObject.put("username", SkilledManager.getUserNameToShow(ParseUser.getCurrentUser()));
                    likeObject.put("tagertuser", blogData.user);
                    likeObject.put("type", blogData.type);
                    if (blogData.photoImage != null)
                        likeObject.put("thumbnail", blogData.photoImage);

                    likeObject.saveInBackground();

                    blogData.nLikeCount++;
                    imageLike.setImageResource(R.drawable.home_liked_icon);
                    textLike.setText(blogData.nLikeCount + " likes");

                    ParseObject blogObject = blogData.object;
                    blogObject.put("likes", blogData.nLikeCount);
                    blogObject.saveInBackground();

                    blogData.bLiked = 1;
                    btnLike.setEnabled(false);

                    // send notification to liked user
                    ParseQuery parseQuery = ParseInstallation.getQuery();
                    parseQuery.whereEqualTo("user", blogData.user);

                    // Send the notification.
                    ParsePush push = new ParsePush();
                    push.setQuery(parseQuery);

                    String strMessage;
                    if (blogData.type == BlogData.BlogText) {
                        strMessage = String.format("%s liked your text", SkilledManager.getUserNameToShow(ParseUser.getCurrentUser()));
                    } else if (blogData.type == BlogData.BlogImage) {
                        strMessage = String.format("%s liked your photo", SkilledManager.getUserNameToShow(ParseUser.getCurrentUser()));
                    } else {
                        strMessage = String.format("%s liked your video", SkilledManager.getUserNameToShow(ParseUser.getCurrentUser()));
                    }

                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("alert", strMessage);
                    params.put("badge", "Increment");
                    params.put("sound", "cheering.caf");
                    params.put("notifyType", "like");
                    params.put("notifyBlog", blogData.object.getObjectId());

                    JSONObject data = new JSONObject(params);

                    push.setData(data);
                    push.sendInBackground();
                }
            }
        });

        btnComment.setTag(blogData);
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentActivity.mBlogData = (BlogData) v.getTag();
                mActivity.startActivity(new Intent(mActivity, CommentActivity.class));
                mActivity.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
            }
        });

        btnMore.setTag(position);
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int index = (Integer) v.getTag();
                    mActivity.getClass().getMethod("showMoreView", int.class).invoke(mActivity, index);
                } catch (Throwable exc) {
                    exc.printStackTrace();
                }
            }
        });
    }

}
