package com.iliayugai.skilled.rate;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iliayugai.skilled.R;

public class AppRater {

    public static final String PREF_FILE_NAME = "app_rater";
    private static final String PREF_DO_NOT_SHOW_AGAIN = "do_not_show_again";
    private static final String PREF_LAUNCH_COUNT = "launch_count";
    private static final String PREF_DATE_FIRST_LAUNCH = "date_first_launch";

    private static final int DAYS_UNTIL_PROMPT = 3;
    private static final int LAUNCHES_UNTIL_PROMPT = 7;

    private static final boolean IS_PRO_VERSION = true;

    private static String APP_NAME;

    public static void appLaunched(Context context) {
        APP_NAME = context.getString(R.string.app_name);

        SharedPreferences prefs = context.getSharedPreferences(PREF_FILE_NAME, 0);
        if (prefs.getBoolean(PREF_DO_NOT_SHOW_AGAIN, false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong(PREF_LAUNCH_COUNT, 0) + 1;
        editor.putLong(PREF_LAUNCH_COUNT, launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong(PREF_DATE_FIRST_LAUNCH, 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(PREF_DATE_FIRST_LAUNCH, date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(context, editor);
            }
        }

        editor.commit();
    }

    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle("Rate " + APP_NAME);

        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(mContext);
        textView.setText("If you enjoy using \"" + APP_NAME + "\", please take a moment to rate it. Thanks for your support!");
        textView.setWidth(240);
        textView.setPadding(4, 0, 4, 10);
        linearLayout.addView(textView);

        Button buttonRate = new Button(mContext);
        buttonRate.setText("Rate " + APP_NAME);
        buttonRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRate(mContext);
                dialog.dismiss();
            }
        });
        linearLayout.addView(buttonRate);

        Button buttonRemind = new Button(mContext);
        buttonRemind.setText("Remind me later");
        buttonRemind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        linearLayout.addView(buttonRemind);

        Button buttonNoThank = new Button(mContext);
        buttonNoThank.setText("No, thanks");
        buttonNoThank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean(PREF_DO_NOT_SHOW_AGAIN, true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        linearLayout.addView(buttonNoThank);

        dialog.setContentView(linearLayout);
        dialog.show();
    }

    public static void doRate(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + context.getPackageName())));
        } catch (Exception e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }

    public static void gotoProVersion(Context context) {
        if (IS_PRO_VERSION) {
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://developer?id=APPS+4+BUILDERS")));
            } catch (Exception e) {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/developer?id=APPS+4+BUILDERS")));
            }
        } else {
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.app4builders.MaterialWeightCalculatorPRO")));
            } catch (Exception e) {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=com.app4builders.MaterialWeightCalculatorPRO")));
            }
        }
    }

}
