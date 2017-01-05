package com.iliayugai.skilled.adapter;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.iliayugai.skilled.OtherProfileActivity;
import com.iliayugai.skilled.R;
import com.iliayugai.skilled.data.BlogData;
import com.iliayugai.skilled.utils.SkilledManager;
import com.iliayugai.skilled.view.BlogListViewHolder;
import com.iliayugai.skilled.view.IViewHolder;
import com.iliayugai.skilled.view.ImagePostViewHolder;
import com.iliayugai.skilled.view.OtherProfileInfoViewHolder;
import com.iliayugai.skilled.view.ProfileInfoViewHolder;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;

public class ProfileAdapter extends BaseAdapter {

    private static final int TYPE_PROFILE = 0;
    private static final int TYPE_ALL_POST = 1;
    private static final int TYPE_IMAGE_POST = 2;
    private static final int TYPE_MAX_COUNT = TYPE_IMAGE_POST + 1;

    private Activity mContext;
    private boolean mIsOtherProfile;
    private boolean mIsList;
    private int mPostBlogCount = 0;

    private ArrayList mData = new ArrayList();

    private ArrayList<BlogData> mBlogList;
    private ArrayList<ParseObject> mImageBlogList;

    private LayoutInflater mInflater;

    public ProfileAdapter(Activity context, ArrayList<BlogData> blogList,
                          ArrayList<ParseObject> imageBlogList, boolean isOtherProfile) {
        mContext = context;

        mBlogList = blogList;
        mImageBlogList = imageBlogList;

        mIsOtherProfile = isOtherProfile;

        mIsList = true;
        mData = mBlogList;

        mInflater = LayoutInflater.from(mContext);
    }

    public void addItem(final BlogData item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    public void setListMode(boolean isList) {
        mIsList = isList;
        if (isList)
            mData = mBlogList;
        else
            mData = mImageBlogList;
    }

    public void setPostBlogCount(int count) {
        mPostBlogCount = count;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_PROFILE;

        return mIsList ? TYPE_ALL_POST : TYPE_IMAGE_POST;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    @Override
    public int getCount() {
        if (!mIsList)
            return 1 + ((mData.size() > 0) ? 1 : 0);
        else
            return mData.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        IViewHolder holder = null;
        int type = getItemViewType(position);

        if (convertView == null) {
            switch (type) {
                case TYPE_PROFILE:
                    convertView = mInflater.inflate(R.layout.layout_profile_info, null);
                    if (mIsOtherProfile)
                        holder = new OtherProfileInfoViewHolder(convertView, (View.OnClickListener) mContext);
                    else
                        holder = new ProfileInfoViewHolder(convertView, (View.OnClickListener) mContext);
                    break;

                case TYPE_ALL_POST:
                    convertView = mInflater.inflate(R.layout.layout_blog_item, null);
                    holder = new BlogListViewHolder(convertView, (View.OnClickListener) mContext);
                    break;

                case TYPE_IMAGE_POST:
                    convertView = mInflater.inflate(R.layout.layout_image_post_table, null);
                    holder = new ImagePostViewHolder(convertView, mContext);
                    break;

                default:
                    return null;
            }

            convertView.setTag(holder);
        } else {
            holder = (IViewHolder) convertView.getTag();
        }

        switch (type) {
            case TYPE_PROFILE:
                if (mIsOtherProfile)
                    ((OtherProfileInfoViewHolder) holder).fillView(OtherProfileActivity.mUser, mIsList, mPostBlogCount);
                else
                    ((ProfileInfoViewHolder) holder).fillView(ParseUser.getCurrentUser(), mIsList, mPostBlogCount);
                break;

            case TYPE_ALL_POST:
                if (position - 1 < mData.size()) {
                    new FillContentTask(position, (BlogListViewHolder) holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
                    //((BlogListViewHolder) holder).fillContent((BlogData) mData.get(position - 1), mS3Utils, position - 1);
                }
                break;

            case TYPE_IMAGE_POST:
                ((ImagePostViewHolder) holder).fillContent(mData);
                break;
        }

        return convertView;
    }

    private class FillContentTask extends AsyncTask<Void, Void, Void> {
        private int mPosition;
        private BlogListViewHolder mHolder;

        public FillContentTask(int position, BlogListViewHolder holder) {
            mPosition = position;
            mHolder = holder;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            BlogData blogData = (BlogData) mData.get(mPosition - 1);
            String objectId = blogData.user.getObjectId();

            if (!SkilledManager.mParseUserMap.containsKey(objectId)) {
                blogData.user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {

                        if (e == null) {
                            ParseUser user = (ParseUser) parseObject;
                            String objectId = user.getObjectId();
                            SkilledManager.mParseUserMap.put(objectId, user);
                        }
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHolder.fillContent((BlogData) mData.get(mPosition - 1), mPosition - 1);
                }
            });
        }
    }

}
