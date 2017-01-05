package com.iliayugai.skilled.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.iliayugai.skilled.R;
import com.iliayugai.skilled.data.FollowingLikeData;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.SkilledManager;
import com.iliayugai.skilled.view.IViewHolder;
import com.iliayugai.skilled.widget.Emojicon.EmojiconTextView;
import com.iliayugai.skilled.widget.RoundedAvatarDrawable;
import com.parse.ParseImageView;
import com.parse.ParseUser;

import java.util.ArrayList;

public class PersonAdapter extends ArrayAdapter<FollowingLikeData> {

    private static final String TAG = PersonAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<FollowingLikeData> mValues;
    private LayoutInflater mInflater;

    private RoundedAvatarDrawable mDefaultAvatarDrawable;

    private class FollowersViewHolder extends IViewHolder {
        public ParseImageView imagePhoto;
        public EmojiconTextView textName;
    }

    public PersonAdapter(Context context, ArrayList<FollowingLikeData> values) {
        super(context, R.layout.person_list_item, values);

        mContext = context;
        mValues = values;

        mInflater = LayoutInflater.from(mContext);
        Resources resources = mContext.getResources();

        mDefaultAvatarDrawable = new RoundedAvatarDrawable(resources, R.drawable.profile_photo_default);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        final FollowersViewHolder viewHolder;

        // Get item list view
        if (rowView == null) {
            rowView = mInflater.inflate(R.layout.person_list_item, null);
            viewHolder = new FollowersViewHolder();

            viewHolder.imagePhoto = (ParseImageView) rowView.findViewById(R.id.image_photo);
            viewHolder.textName = (EmojiconTextView) rowView.findViewById(R.id.text_name);

            Config.scaleLayout(mContext, "person", rowView);
            Config.processEmojiconViewHeight(mContext, "person", viewHolder.textName);

            rowView.setTag(viewHolder);
        } else {
            viewHolder = (FollowersViewHolder) rowView.getTag();
        }

        // Fill content of list item view
        FollowingLikeData data = mValues.get(position);

        if (data != null) {
            String objectId = data.userObject.getObjectId();

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

            viewHolder.textName.setText(data.username);
        }

        return rowView;
    }

//    private void loadPhotoImage(FollowersViewHolder viewHolder, ParseUser user) {
//        int loadResult = SkilledManager.isImageLoaded(viewHolder.imagePhoto, user, "photo");
//
//        switch (loadResult) {
//            case SkilledManager.IMAGE_NULL:
//                viewHolder.imagePhoto.setImageDrawable(mDefaultAvatarDrawable);
//                break;
//
//            case SkilledManager.IMAGE_LOADED:
//                if (viewHolder.imagePhoto.getDrawable() != mDefaultAvatarDrawable) break;
//
//            case SkilledManager.IMAGE_UNLOADED:
//                SkilledManager.setAvatarImage(viewHolder.imagePhoto, user, "photo", mDefaultAvatarDrawable);
//                break;
//        }
//    }

}
