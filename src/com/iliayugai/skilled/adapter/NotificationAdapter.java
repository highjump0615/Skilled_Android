package com.iliayugai.skilled.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.iliayugai.skilled.NotifyActivity;
import com.iliayugai.skilled.R;
import com.iliayugai.skilled.data.BlogData;
import com.iliayugai.skilled.data.NotificationData;
import com.iliayugai.skilled.utils.CommonUtils;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.SkilledManager;
import com.iliayugai.skilled.view.IViewHolder;
import com.iliayugai.skilled.widget.Emojicon.EmojiconTextView;
import com.iliayugai.skilled.widget.RoundedAvatarDrawable;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionException;

public class NotificationAdapter extends ArrayAdapter<NotificationData> {

    private static final String TAG = NotificationAdapter.class.getSimpleName();

    private Context mContext;

    private ArrayList<NotificationData> mFollowingValues;
    private ArrayList<NotificationData> mLikeValues;
    private ArrayList<NotificationData> mCommentValues;
    private ArrayList<NotificationData> mMentionValues;

    private int mNotificationType;

    private LayoutInflater mInflater;

    private RoundedAvatarDrawable mDefaultAvatarDrawable;
    private Drawable mDefaultBackgroundDrawable;

    private class NotificationViewHolder extends IViewHolder {
        public ParseImageView imagePhoto;
        public EmojiconTextView textName;
        public TextView textTime;
        public ParseImageView imageThumbnail;

        public String postPhotoUrl = "";

        public int notificationType = NotifyActivity.NOTIFICATION_FOLLOWING;
    }

    public NotificationAdapter(Context context, ArrayList<NotificationData> followingValues,
                               ArrayList<NotificationData> likeValues,
                               ArrayList<NotificationData> commentValues,
                               ArrayList<NotificationData> mentionValues) {
        super(context, R.layout.notify_list_item, followingValues);

        mContext = context;
        mFollowingValues = followingValues;
        mLikeValues = likeValues;
        mCommentValues = commentValues;
        mMentionValues = mentionValues;

        mInflater = LayoutInflater.from(mContext);
        Resources resources = mContext.getResources();

        mDefaultAvatarDrawable = new RoundedAvatarDrawable(resources, R.drawable.profile_photo_default);
        mDefaultBackgroundDrawable = mContext.getResources().getDrawable(R.drawable.profile_img_default);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        final NotificationViewHolder viewHolder;

        if (rowView == null) {
            rowView = mInflater.inflate(R.layout.notify_list_item, null);
            viewHolder = new NotificationViewHolder();

            viewHolder.imagePhoto = (ParseImageView) rowView.findViewById(R.id.image_photo);
            viewHolder.textName = (EmojiconTextView) rowView.findViewById(R.id.text_notify);
            viewHolder.textTime = (TextView) rowView.findViewById(R.id.text_time);
            viewHolder.imageThumbnail = (ParseImageView) rowView.findViewById(R.id.image_thumbnail);

            Config.scaleLayout(mContext, "notify", rowView);
            Config.processEmojiconViewHeight(mContext, "notify", viewHolder.textName);

            rowView.setTag(viewHolder);
        } else {
            viewHolder = (NotificationViewHolder) rowView.getTag();
        }

        NotificationData data = null;// = mValues.get(position);

        switch (mNotificationType) {
            case NotifyActivity.NOTIFICATION_FOLLOWING:
                data = mFollowingValues.get(position);
                break;

            case NotifyActivity.NOTIFICATION_LIKES:
                data = mLikeValues.get(position);
                break;

            case NotifyActivity.NOTIFICATION_COMMENTS:
                data = mCommentValues.get(position);
                break;

            case NotifyActivity.NOTIFICATION_MENTIONS:
                data = mMentionValues.get(position);
                break;

            default:
                break;
        }

        viewHolder.imagePhoto.setPlaceholder(mDefaultAvatarDrawable);
        viewHolder.imageThumbnail.setPlaceholder(mDefaultBackgroundDrawable);

        if (data != null) {
            if (viewHolder.notificationType != data.type) {
                viewHolder.notificationType = data.type;
                viewHolder.imagePhoto.setImageDrawable(mDefaultAvatarDrawable);
                viewHolder.photoUrl = "";
            }

            if (data.type == NotifyActivity.NOTIFICATION_FOLLOWING) {
                rowView.setBackgroundResource(R.drawable.post_category);
            } else {
                rowView.setBackgroundColor(Color.TRANSPARENT);
            }

            String objectId = data.user.getObjectId();

            if (SkilledManager.mParseUserMap.containsKey(objectId)) {
                ParseUser user = SkilledManager.mParseUserMap.get(objectId);
                int loadResult = SkilledManager.isImageLoaded(viewHolder.imagePhoto, user, "photo");

                switch (loadResult) {
                    case SkilledManager.IMAGE_NULL:
                        viewHolder.imagePhoto.setImageDrawable(mDefaultAvatarDrawable);
                        break;

                    case SkilledManager.IMAGE_LOADED:
                        if (viewHolder.imagePhoto.getDrawable() != mDefaultAvatarDrawable) break;

                    case SkilledManager.IMAGE_UNLOADED:
                        SkilledManager.setAvatarImage(viewHolder.imagePhoto, user, "photo", mDefaultAvatarDrawable);
                        break;
                }
            } else {
                viewHolder.imagePhoto.setImageDrawable(mDefaultAvatarDrawable);
            }

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

            viewHolder.textName.setText(text);
            viewHolder.textTime.setText(CommonUtils.getTimeString(data.date));

            if ((data.type == NotifyActivity.NOTIFICATION_LIKES
                    || data.type == NotifyActivity.NOTIFICATION_COMMENTS
                    || data.type == NotifyActivity.NOTIFICATION_MENTIONS) && data.image != null) {
                String url = data.image.getUrl();

                if (!url.equals(viewHolder.postPhotoUrl)) {
                    viewHolder.postPhotoUrl = url;

                    viewHolder.imageThumbnail.setVisibility(View.VISIBLE);

                    viewHolder.imageThumbnail.setParseFile(data.image);

                    try {
                        viewHolder.imageThumbnail.loadInBackground();
                    } catch (java.util.concurrent.RejectedExecutionException ex) {
                    }
                }
            } else {
                viewHolder.imageThumbnail.setVisibility(View.INVISIBLE);
                viewHolder.postPhotoUrl = "";
            }
        }

        return rowView;
    }




    @Override
    public int getCount() {
        switch (mNotificationType) {
            case NotifyActivity.NOTIFICATION_FOLLOWING:
                return mFollowingValues.size();

            case NotifyActivity.NOTIFICATION_LIKES:
                return mLikeValues.size();

            case NotifyActivity.NOTIFICATION_COMMENTS:
                return mCommentValues.size();

            case NotifyActivity.NOTIFICATION_MENTIONS:
                return mMentionValues.size();

            default:
                return 0;
        }
    }

    public void setNotificationType(int type) {
        mNotificationType = type;
    }

}
