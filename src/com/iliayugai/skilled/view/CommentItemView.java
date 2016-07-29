/**
 * @author Ry
 * @Date 2013.12.22
 * @FileName CommentItemView.java
 *
 */

package com.iliayugai.skilled.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iliayugai.skilled.OtherProfileActivity;
import com.iliayugai.skilled.R;
import com.iliayugai.skilled.data.CommentData;
import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.SkilledManager;
import com.iliayugai.skilled.widget.Emojicon.EmojiconButton;
import com.iliayugai.skilled.widget.Emojicon.EmojiconTextView;
import com.iliayugai.skilled.widget.RoundedAvatarDrawable;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseUser;


public class CommentItemView extends LinearLayout {

    public CommentItemView(final Context context, CommentData commentInfo, int backColor) {
        super(context);

        final Resources res = context.getResources();
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/AvenirNext-Regular.otf");

        final RoundedAvatarDrawable defaultAvatarDrawable = new RoundedAvatarDrawable(res, R.drawable.profile_photo_default);

        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.comment_list_item, null);

        float scaledDensity = res.getDisplayMetrics().scaledDensity;

        layout.setBackgroundColor(backColor);

        // Photo ImageView
        final int photoViewSize = (int) (res.getDimension(R.dimen.comment_item_image_size) * Config.mScaleFactor);
        final ParseImageView imagePhoto = (ParseImageView) layout.findViewById(R.id.image_photo);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imagePhoto.getLayoutParams();
        params.width = params.height = photoViewSize;
        imagePhoto.setLayoutParams(params);
        imagePhoto.setImageBitmap(CommonUtils.getCroppedRoundBitmap(res, R.drawable.profile_photo_default, photoViewSize));

        // Author name
        final EmojiconButton buttonName = (EmojiconButton) layout.findViewById(R.id.button_name);

        params = (RelativeLayout.LayoutParams) buttonName.getLayoutParams();
        params.width = (int) (res.getDimension(R.dimen.comment_item_text_name_width) * Config.mScaleFactor);
        params.height = (int) (res.getDimension(R.dimen.comment_item_text_name_height) * Config.mScaleFactor);
        params.leftMargin = (int) (res.getDimension(R.dimen.comment_item_text_name_margin_left) * Config.mScaleFactor);
        params.topMargin = (int) (res.getDimension(R.dimen.comment_item_text_name_margin_top) * Config.mScaleFactor);
        float textSize = res.getDimension(R.dimen.comment_item_text_name_text_size) * Config.mFontScaleFactor;
        buttonName.setLayoutParams(params);
        buttonName.setTextSize(textSize);
        buttonName.setTypeface(typeface);
        buttonName.setEmojiconSize((int) (textSize * scaledDensity));
        buttonName.setText(commentInfo.strUsername, TextView.BufferType.SPANNABLE);
        buttonName.setTag(commentInfo.user);
        buttonName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser user = (ParseUser) v.getTag();

                if (user != null) {
                    OtherProfileActivity.mUser = user;
                    OtherProfileActivity.mUserFullName = ((Button) v).getText().toString();
                    context.startActivity(new Intent(context, OtherProfileActivity.class));
                    ((Activity) context).overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                }
            }
        });

        imagePhoto.setPlaceholder(defaultAvatarDrawable);

        // user photo
        // Fetch user
        String objectId = commentInfo.user.getObjectId();

        if (!SkilledManager.mParseUserMap.containsKey(objectId)) {
            commentInfo.user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        ParseUser user = (ParseUser) parseObject;

                        SkilledManager.mParseUserMap.put(user.getObjectId(), user);
                        SkilledManager.setAvatarImage(imagePhoto, user, "photo", defaultAvatarDrawable);
                    }
                }
            });
        } else {
            ParseUser user = SkilledManager.mParseUserMap.get(objectId);

            SkilledManager.setAvatarImage(imagePhoto, user, "photo", defaultAvatarDrawable);
        }

        // Comment
        EmojiconTextView textComment = (EmojiconTextView) layout.findViewById(R.id.text_comment);
        textComment.setText(commentInfo.strContent, TextView.BufferType.SPANNABLE);

        params = (RelativeLayout.LayoutParams) textComment.getLayoutParams();
        params.width = (int) (res.getDimension(R.dimen.comment_item_text_comment_width) * Config.mScaleFactor);
        int originalHeight = (int) (res.getDimension(R.dimen.comment_item_text_comment_height) * Config.mScaleFactor);
        params.topMargin = (int) (res.getDimension(R.dimen.comment_item_text_comment_margin_top) * Config.mScaleFactor);
        textSize = res.getDimension(R.dimen.comment_item_text_comment_text_size) * Config.mFontScaleFactor;

        int height = CommonUtils.getTextViewHeight(textComment);
        int expansion = (originalHeight < height) ? (int) (height * 1.1f) : 0;
        textComment.setLayoutParams(params);

        textComment.setTextSize(textSize);
        textComment.setTypeface(typeface);

        textComment.setEmojiconSize((int) (textSize * scaledDensity));

        // Item height
        height = (int) (res.getDimension(R.dimen.comment_item_height) * Config.mScaleFactor);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height + expansion);
        layout.setLayoutParams(layoutParams);
        int padding = (int) (res.getDimension(R.dimen.comment_item_padding) * Config.mScaleFactor);
        layout.setPadding(padding, 0, padding, 0);

        // Date
        TextView textDate = (TextView) layout.findViewById(R.id.text_comment_time);
        textDate.setText(CommonUtils.getTimeString(commentInfo.date));

        params = (RelativeLayout.LayoutParams) textDate.getLayoutParams();
        params.width = (int) (res.getDimension(R.dimen.comment_item_text_comment_time_width) * Config.mScaleFactor);
        params.height = (int) (res.getDimension(R.dimen.comment_item_text_comment_time_height) * Config.mScaleFactor);
        textSize = res.getDimension(R.dimen.comment_item_text_comment_time_text_size) * Config.mFontScaleFactor;
        textDate.setLayoutParams(params);
        textDate.setTextSize(textSize);
        textDate.setTypeface(typeface);

        addView(layout);
    }

}
