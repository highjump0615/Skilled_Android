package com.handmark.pulltorefresh.library.internal;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import com.handmark.pulltorefresh.library.R;

public class Utils {

	static final String LOG_TAG = "PullToRefresh";

    public static float mScaleFactor = 0.0f;
    public static float mFontScaleFactor = 0.0f;

    public static void calculateScaleFactor(Context context) {
        final Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();

        int design_width = (int) resources.getDimension(R.dimen.design_screen_width);

        float width;
        if (metrics.widthPixels < metrics.heightPixels) {
            width = metrics.widthPixels;
        } else {
            width = metrics.heightPixels;
        }

        mScaleFactor = width / (design_width/* * metrics.scaledDensity*/);
        mFontScaleFactor = mScaleFactor / metrics.density;
    }

	public static void warnDeprecation(String deprecated, String replacement) {
		Log.w(LOG_TAG, "You're using the deprecated " + deprecated + " attr, please switch over to " + replacement);
	}

}
