/**
 *
 */
package com.iliayugai.skilled.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SystemHelper {

    /**
     * Get version for current application
     *
     * @param context is current Activity
     * @param cls     is class
     * @return string version
     */
    public static String getVersionName(Context context, Class<?> cls) {
        try {
            ComponentName componentName = new ComponentName(context, cls);
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(componentName.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * Hide always Soft Keyboard
     *
     * @param context is current Activity
     */
    public static void hideKeyboard(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (editText != null) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            editText.clearFocus();
            //editText.setInputType(0);
        }
    }

    /**
     * Show always Soft Keyboard
     *
     * @param context is current Activity
     */
    public static void showKeyboard(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (editText != null) {
            imm.showSoftInput(editText, 0);
        }
    }

    /**
     * Gets Date with UTC time zone
     *
     * @param date is concrete date
     * @return new instance calendar
     */
    public static Calendar getCalendarUTC(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        return calendar;
    }
}
