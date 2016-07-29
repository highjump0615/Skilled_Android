package com.iliayugai.skilled;

import android.app.Application;
import android.util.Log;

import com.iliayugai.skilled.utils.Config;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseTwitterUtils;
import com.parse.PushService;

public class SkilledApplication extends Application {

    private static final String TAG = SkilledApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        // Parse init
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));
        ParseFacebookUtils.initialize(getString(R.string.facebook_app_id));
        ParseTwitterUtils.initialize(getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret));

        // Set default activity for Push Notification
        PushService.setDefaultPushCallback(this, NotifyActivity.class);

        // Save the current Installation to Parse.
        ParseInstallation.getCurrentInstallation().saveInBackground();

        // Associate the device with a user
        /*ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("user", ParseUser.getCurrentUser());
        installation.saveInBackground();*/

        if (Config.DEBUG) Log.d(TAG, "Parse initialized!");
    }

}
