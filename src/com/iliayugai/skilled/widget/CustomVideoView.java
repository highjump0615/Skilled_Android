package com.iliayugai.skilled.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

import com.iliayugai.skilled.utils.Config;

public class CustomVideoView extends VideoView {

    private static final String TAG = CustomVideoView.class.getSimpleName();

    private int mForceHeight = 0;
    private int mForceWidth = 0;

    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDimensions(int w, int h) {
        this.mForceHeight = h;
        this.mForceWidth = w;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (Config.DEBUG)
            Log.i(TAG, "onMeasure (" + widthMeasureSpec + ", " + heightMeasureSpec + ")");

        setMeasuredDimension(mForceWidth, mForceHeight);
    }
}