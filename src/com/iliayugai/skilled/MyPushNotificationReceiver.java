package com.iliayugai.skilled;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.iliayugai.skilled.utils.SkilledManager;
import com.parse.ParseObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class MyPushNotificationReceiver extends BroadcastReceiver {

    private static final String TAG = MyPushNotificationReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            String channel = intent.getExtras().getString("com.parse.Channel");
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

            Log.d(TAG, "got action " + action + " on channel " + channel + " with:");

            Iterator iterator = json.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                Log.d(TAG, "..." + key + " => " + json.getString(key));

                if (key.equals("notifyType")) {
                    SkilledManager.mStrNotifyType = json.getString(key);
                } else if (key.equals("notifyBlog")) {
                    String strNotifyBlogId = json.getString(key);
                    SkilledManager.mNotifyBlogObject = ParseObject.createWithoutData("Blogs", strNotifyBlogId);
                }
            }

            checkNotification(context);

        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }

    private void checkNotification(Context context) {
        if (!TextUtils.isEmpty(SkilledManager.mStrNotifyType)) {

            if (SkilledManager.mStrNotifyType.equals("follow")) {

                Intent intent = new Intent(context, NotifyActivity.class);
                intent.putExtra(NotifyActivity.CHOOSE_NOTIFICATION_TYPE, NotifyActivity.NOTIFICATION_FOLLOWING);
                context.startActivity(intent);

            } else if (SkilledManager.mStrNotifyType.equals("like")) {

                Intent intent = new Intent(context, NotifyActivity.class);
                intent.putExtra(NotifyActivity.CHOOSE_NOTIFICATION_TYPE, NotifyActivity.NOTIFICATION_LIKES);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            } else if (SkilledManager.mStrNotifyType.equals("comment")) {

                Intent intent = new Intent(context, NotifyActivity.class);
                intent.putExtra(NotifyActivity.CHOOSE_NOTIFICATION_TYPE, NotifyActivity.NOTIFICATION_COMMENTS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            } else if (SkilledManager.mStrNotifyType.equals("mention")) {

                Intent intent = new Intent(context, NotifyActivity.class);
                intent.putExtra(NotifyActivity.CHOOSE_NOTIFICATION_TYPE, NotifyActivity.NOTIFICATION_MENTIONS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }

            SkilledManager.mStrNotifyType = "";
        }
    }

}
