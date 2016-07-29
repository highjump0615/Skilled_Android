/**
 *
 */
package com.iliayugai.skilled.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iliayugai.skilled.R;
import com.iliayugai.skilled.data.BlogCategory;
import com.iliayugai.skilled.utils.Config;

import java.util.List;

public class CategoryAdapter extends ArrayAdapter<BlogCategory> {

    private Context mContext;
    private Resources mResources;
    private boolean mCheckable;

    private List<BlogCategory> mSuggestions;
    private Typeface mTypeface;

    public class ViewHolder {
        public TextView textView;
        public ImageView imageView;
        public View selectLayout;
    }

    /**
     * @param context
     * @param objects
     */
    public CategoryAdapter(Context context, List<BlogCategory> objects, boolean checkable) {
        super(context, R.layout.category_list_item, objects);

        this.mContext = context;
        this.mResources = mContext.getResources();
        this.mCheckable = checkable;

        this.mSuggestions = objects;
        this.mTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/AvenirNext-Regular.otf");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;

        if (view == null) {
            view = View.inflate(mContext, R.layout.category_list_item, null);

            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) view.findViewById(R.id.search_list_item_text);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.image_check);
            viewHolder.selectLayout = view.findViewById(R.id.layout_selected_status);
            view.setTag(viewHolder);

            // TextView
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.textView.getLayoutParams();
            params.leftMargin = (int) (mResources.getDimension(R.dimen.search_category_name_margin_left) * Config.mScaleFactor);
            viewHolder.textView.setLayoutParams(params);

            float textSize = mResources.getDimension(R.dimen.search_category_name_text_size) * Config.mFontScaleFactor;
            viewHolder.textView.setTextSize(textSize);
            viewHolder.textView.setTypeface(mTypeface);

            // ImageView
            params = (LinearLayout.LayoutParams) viewHolder.imageView.getLayoutParams();
            params.width = params.height = (int) (mResources.getDimension(R.dimen.search_category_check_image_size) * Config.mScaleFactor);
            params.leftMargin = params.rightMargin = (int) (mResources.getDimension(R.dimen.search_category_check_margin_left) * Config.mScaleFactor);
            params.bottomMargin = params.topMargin = (int) (mResources.getDimension(R.dimen.search_category_check_margin_top) * Config.mScaleFactor);
            viewHolder.imageView.setLayoutParams(params);
            viewHolder.imageView.setVisibility(mCheckable ? View.VISIBLE : View.INVISIBLE);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BlogCategory suggestion = mSuggestions.get(position);

        if (suggestion != null) {
            viewHolder.textView.setText(suggestion.strName);
            viewHolder.imageView.setSelected(suggestion.nSelected > 0);

            if (suggestion.nSelected > 0)
                viewHolder.selectLayout.setBackgroundResource(R.drawable.rounded_list_item_selected);
            else
                viewHolder.selectLayout.setBackgroundResource(R.drawable.rounded_list_item_unselected);
        }

        return view;
    }

}
