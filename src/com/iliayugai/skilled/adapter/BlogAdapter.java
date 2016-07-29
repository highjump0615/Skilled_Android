package com.iliayugai.skilled.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.iliayugai.skilled.R;
import com.iliayugai.skilled.data.BlogData;
import com.iliayugai.skilled.utils.Config;
import com.iliayugai.skilled.utils.SkilledManager;
import com.iliayugai.skilled.view.BlogListViewHolder;
import com.parse.ParseException;

import java.util.ArrayList;

public class BlogAdapter extends ArrayAdapter<BlogData> {

    private static final String TAG = BlogAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<BlogData> mValues;

    public BlogAdapter(Context context, ArrayList<BlogData> values) {
        super(context, R.layout.layout_blog_item, values);

        mContext = context;
        mValues = values;

        if (Config.DEBUG ) Log.d(TAG, "parent context is " + context.toString());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        BlogListViewHolder viewHolder;

        if (rowView == null) {
            rowView = LayoutInflater.from(mContext).inflate(R.layout.layout_blog_item, null);
            viewHolder = new BlogListViewHolder(rowView, (View.OnClickListener) mContext);

            rowView.setTag(viewHolder);
        } else {
            viewHolder = (BlogListViewHolder) rowView.getTag();
        }

        new FillContentTask(position, viewHolder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);

        return rowView;
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
            BlogData blogData = mValues.get(mPosition);
            String objectId = blogData.user.getObjectId();

            if (!SkilledManager.mParseUserMap.containsKey(objectId)) {
                /*blogData.user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {

                        if (e == null) {
                            ParseUser user = (ParseUser) parseObject;
                            String objectId = user.getObjectId();
                            SkilledManager.mParseUserMap.put(objectId, user);
                        }
                    }
                });*/

                try {
                    blogData.user.fetchIfNeeded();
                    SkilledManager.mParseUserMap.put(objectId, blogData.user);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHolder.fillContent(mValues.get(mPosition), mPosition);
                }
            });
        }
    }

}
