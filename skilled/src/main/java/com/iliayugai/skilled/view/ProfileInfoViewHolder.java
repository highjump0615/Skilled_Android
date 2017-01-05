package com.iliayugai.skilled.view;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.iliayugai.skilled.ProfileActivity;
import com.iliayugai.skilled.R;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.SkilledManager;
import com.iliayugai.skilled.widget.Emojicon.EmojiconTextView;
import com.parse.ParseImageView;
import com.parse.ParseUser;

public class ProfileInfoViewHolder extends IViewHolder {

    private static final String TAG = ProfileInfoViewHolder.class.getSimpleName();

    public ImageView imageFollow;
    public ImageView imageEditProfile;
    public ParseImageView imagePhoto;
    public EmojiconTextView textName;
    public TextView textLocation;
    public EmojiconTextView textAboutMe;
    public TextView textPosts;
    public TextView textFollowers;
    public TextView textFollowing;
    public ImageView imageBlog;
    public ImageView imagePicture;

    private Activity mActivity;


    public ProfileInfoViewHolder(View listItemLayout, View.OnClickListener onClickListener) {
        mActivity = (Activity) onClickListener;

        Typeface regularTypeface = Typeface.createFromAsset(mActivity.getAssets(), "fonts/AvenirNext-Regular.otf");
        Typeface demiTypeface = Typeface.createFromAsset(mActivity.getAssets(), "fonts/AvenirNext-DemiBold.otf");

        // Follow image
        imageFollow = (ImageView) listItemLayout.findViewById(R.id.image_follow);
        imageFollow.setOnClickListener(onClickListener);
        imageFollow.setVisibility(View.GONE);

        // edit profile image
        imageEditProfile = (ImageView) listItemLayout.findViewById(R.id.image_edit_profile);
        imageEditProfile.setOnClickListener(onClickListener);

        /// User Photo layout
        imagePhoto = (ParseImageView) listItemLayout.findViewById(R.id.image_user_photo);

        // Username TextView
        textName = (EmojiconTextView) listItemLayout.findViewById(R.id.text_username);
        textName.setTypeface(demiTypeface);

        // Location TextView
        textLocation = (TextView) listItemLayout.findViewById(R.id.text_location);
        textLocation.setTypeface(regularTypeface);

        // About TextView
        textAboutMe = (EmojiconTextView) listItemLayout.findViewById(R.id.text_about);
        textAboutMe.setTypeface(regularTypeface);

        /// Dashboard layout
        // Post count TextView
        textPosts = (TextView) listItemLayout.findViewById(R.id.text_posts_value);
        textPosts.setTypeface(demiTypeface);

        // Post label
        TextView textView = (TextView) listItemLayout.findViewById(R.id.text_posts_label);
        textView.setTypeface(regularTypeface);

        /// Followers
        listItemLayout.findViewById(R.id.layout_followers).setOnClickListener(onClickListener);

        // Followers count TextView
        textFollowers = (TextView) listItemLayout.findViewById(R.id.text_followers_value);
        textFollowers.setTypeface(demiTypeface);

        // Followers label
        textView = (TextView) listItemLayout.findViewById(R.id.text_followers_label);
        textView.setTypeface(regularTypeface);

        /// Following
        listItemLayout.findViewById(R.id.layout_following).setOnClickListener(onClickListener);

        // Following count TextView
        textFollowing = (TextView) listItemLayout.findViewById(R.id.text_following_value);
        textFollowing.setTypeface(demiTypeface);

        // Following label
        textView = (TextView) listItemLayout.findViewById(R.id.text_following_label);
        textView.setTypeface(regularTypeface);

        /// Function buttons
        // Blog ImageView
        imageBlog = (ImageView) listItemLayout.findViewById(R.id.image_blog);
        imageBlog.setOnClickListener(onClickListener);

        // Picture ImageView
        imagePicture = (ImageView) listItemLayout.findViewById(R.id.image_picture);
        imagePicture.setOnClickListener(onClickListener);

        // Text Post ImageView
        listItemLayout.findViewById(R.id.image_post_text).setOnClickListener(onClickListener);

        // Settings ImageView
        listItemLayout.findViewById(R.id.image_settings).setOnClickListener(onClickListener);

        Config.scaleLayout(mActivity, "profile", listItemLayout);
        Config.processEmojiconViewHeight(mActivity, "profile", textName);
        Config.processEmojiconViewHeight(mActivity, "profile", textAboutMe);
    }

    public void fillView(ParseUser user, boolean isListMode, int blogCount) {
        if (user == null) return;

        textName.setText(user.getString("fullname"));
        textLocation.setText(user.getString("location"));
        textAboutMe.setText(user.getString("about"));

        final Drawable placeholder = mActivity.getResources().getDrawable(R.drawable.profile_photo_default);

        int loadResult = SkilledManager.isImageLoaded(imagePhoto, user, "photo");

        switch (loadResult) {
            case SkilledManager.IMAGE_NULL:
                imagePhoto.setImageDrawable(placeholder);
                break;

            case SkilledManager.IMAGE_UNLOADED:
                SkilledManager.setSquareImage(imagePhoto, user, "photo", placeholder);
                //SkilledManager.setLoadedImageUrl(ProfileInfoViewHolder.this, user, "photo");
                break;
        }

        textPosts.setText(String.valueOf(blogCount));
        textFollowers.setText(String.valueOf(((ProfileActivity) mActivity).m_nFollowerCnt));
        textFollowing.setText(String.valueOf(((ProfileActivity) mActivity).m_nFollowingCnt));

        imageBlog.setSelected(isListMode);
        imagePicture.setSelected(!isListMode);
    }

}