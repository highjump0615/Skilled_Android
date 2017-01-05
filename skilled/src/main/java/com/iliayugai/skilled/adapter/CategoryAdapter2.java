/**
 *
 */
package com.iliayugai.skilled.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.iliayugai.skilled.R;
import com.iliayugai.skilled.data.BlogCategory;
import com.iliayugai.skilled.utils.Config;

import java.util.HashMap;
import java.util.List;

public class CategoryAdapter2 extends ArrayAdapter<BlogCategory> {

    private static final HashMap<String, Integer> mImageResIdMap;

    private Context mContext;
    private Resources mResources;

    private List<BlogCategory> mSuggestions;

    static {
        mImageResIdMap = new HashMap<String, Integer>();
        mImageResIdMap.put("Models", R.drawable.item_models_bg);
        mImageResIdMap.put("Photography", R.drawable.item_photography_bg);
        mImageResIdMap.put("Comedy", R.drawable.item_comedy_bg);
        mImageResIdMap.put("Music", R.drawable.item_music_bg);
        mImageResIdMap.put("Dance", R.drawable.item_dance_bg);
        mImageResIdMap.put("Art", R.drawable.item_art_bg);
        mImageResIdMap.put("Sports", R.drawable.item_sports_bg);
        mImageResIdMap.put("Beautiful People", R.drawable.item_beatiful_people_bg);
        mImageResIdMap.put("Writing", R.drawable.item_writing_bg);
        mImageResIdMap.put("Geek", R.drawable.item_geek_bg);
        mImageResIdMap.put("Film", R.drawable.item_film_bg);
        mImageResIdMap.put("Animals", R.drawable.item_animals_bg);
        mImageResIdMap.put("Culinary Art", R.drawable.item_culinary_art_bg);
        mImageResIdMap.put("Technology", R.drawable.item_technology_bg);
        mImageResIdMap.put("Magic", R.drawable.item_magic_bg);
        mImageResIdMap.put("Fashion", R.drawable.item_fashion_bg);
        mImageResIdMap.put("Other", R.drawable.item_other_bg);
        mImageResIdMap.put("Fitness and Health", R.drawable.item_fitness_and_health_bg);
        mImageResIdMap.put("Educational", R.drawable.item_educational_bg);
        mImageResIdMap.put("Motivational", R.drawable.item_motivational_bg);
        mImageResIdMap.put("Cars and Motors", R.drawable.item_cars_and_motors_bg);
    }

    /**
     * @param context Activity context
     * @param objects Category item ArrayList
     */
    public CategoryAdapter2(Context context, List<BlogCategory> objects) {
        super(context, R.layout.category_list_item, objects);

        this.mContext = context;
        this.mResources = mContext.getResources();

        this.mSuggestions = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = (ImageView) convertView;

        if (imageView == null) {
            imageView = new ImageView(mContext);
            int width = (int) (mResources.getDimension(R.dimen.favourite_item_width) * Config.mScaleFactor);
            int height = (int) (mResources.getDimension(R.dimen.favourite_item_height) * Config.mScaleFactor);

            GridView.LayoutParams params = new AbsListView.LayoutParams(width, height);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        BlogCategory suggestion = mSuggestions.get(position);

        if (suggestion != null) {
            imageView.setImageResource(mImageResIdMap.get(suggestion.strName));
        }

        return imageView;
    }

}
