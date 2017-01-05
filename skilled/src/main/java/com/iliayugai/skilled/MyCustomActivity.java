package com.iliayugai.skilled;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by JM on 9/1/14.
 */
public class MyCustomActivity extends Activity {
    private static final String TAG = MyCustomActivity.class.getSimpleName();
    protected static final String EXIT_APPLICATION_ACTION = "com.iliayugai.skilled.ACTION_EXIT_APPLICATION";

    private BroadcastReceiver mExitAppReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "exit from " + context.toString());
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerExitApplicationReceiver();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        onExitApplication();
        overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
    }

    @Override
    protected void onDestroy() {
        unRegisterExitApplicationReceiver();
        super.onDestroy();
    }

    private void registerExitApplicationReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(EXIT_APPLICATION_ACTION);
        registerReceiver(mExitAppReceiver, intentFilter);
    }

    private void unRegisterExitApplicationReceiver() {
        try {
            unregisterReceiver(mExitAppReceiver);
        } catch (Exception e) {
        }
    }

    private void onExitApplication() {
        // 关闭其他开放活动
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(EXIT_APPLICATION_ACTION);
        sendBroadcast(broadcastIntent);

        finish();
    }

}
