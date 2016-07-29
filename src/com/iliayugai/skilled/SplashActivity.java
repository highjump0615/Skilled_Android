/**
 *
 */
package com.iliayugai.skilled;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.iliayugai.skilled.utils.Config;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends Activity {

    static final String TAG = SplashActivity.class.getSimpleName();
    public Timer mTransitionTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Config.calculateScaleFactor(this);

        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
        setContentView(R.layout.activity_splash);

        initView();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(SplashActivity.this, LandingActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                    }
                });
            }
        };
        mTransitionTimer = new Timer();
        mTransitionTimer.schedule(task, 4000);

        Log.d(TAG, "Splash screen would be disappeared after 4 seconds.");
    }

    @Override
    protected void onDestroy() {
        if (mTransitionTimer != null) {
            mTransitionTimer.cancel();
            mTransitionTimer = null;
        }

        super.onDestroy();
    }

    public void initView() {
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        Config.scaleLayout(this, "splash", rootView);

        TextView txtView = (TextView) findViewById(R.id.text_express);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/AvenirNext-Bold.otf");
        txtView.setTypeface(typeFace);
    }

}
