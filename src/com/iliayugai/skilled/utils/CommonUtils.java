package com.iliayugai.skilled.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.iliayugai.skilled.R;
import com.parse.FindCallback;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {

    private static final String TAG = CommonUtils.class.getSimpleName();

    /**
     * Move to destination activity class with animate transition.
     */
    public static void moveNextActivity(Activity source, Class<?> destinationClass) {
        source.startActivity(new Intent(source, destinationClass));
        source.finish();
        source.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
    }

    /**
     * Move to destination activity class with animate transition without finishing itself.
     */
    public static void moveNextActivityWithoutFinish(Activity source, Class<?> destinationClass) {
        source.startActivity(new Intent(source, destinationClass));
        source.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
    }

    /**
     * Go to destination activity class.
     */
    public static void gotoMain(final Activity source, final Class<?> destinationClass) {
        final Dialog progressDialog = CommonUtils.createFullScreenProgress(source);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Category");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                if (e == null) {
                    // save user info for push notification
                    ParseInstallation.getCurrentInstallation().put("user", ParseUser.getCurrentUser());
                    ParseInstallation.getCurrentInstallation().saveEventually();

                    SkilledManager.setCategories(source, parseObjects);

                    moveNextActivity(source, destinationClass);
                    progressDialog.dismiss();
                } else {
                    progressDialog.dismiss();
                    CommonUtils.createErrorAlertDialog(source, "Alert", e.getMessage());
                }
            }
        });

        progressDialog.show();
    }

    /**
     * Copy folder to external storage.
     */
    public static void copyFolderToExternalStorage(Context context, String name) {
        // "Name" is the name of your folder!
        AssetManager assetManager = context.getAssets();
        String[] files = null;

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            // Checking file on assets sub-folder
            try {
                files = assetManager.list(name);
            } catch (IOException e) {
                Log.e(TAG, "Failed to get asset file list.", e);
            }

            if (files == null) return;

            // Analyzing all file on assets sub-folder
            for (String filename : files) {
                InputStream in = null;
                OutputStream out = null;
                // First: checking if there is already a target folder
                File folder = new File(Environment.getExternalStorageDirectory() + "/yourTargetFolder/" + name);
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdir();
                }
                if (success) {
                    // Moving all the files on external SD
                    try {
                        in = assetManager.open(name + "/" + filename);
                        out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/yourTargetFolder/" + name + "/" + filename);
                        Log.i(TAG, Environment.getExternalStorageDirectory() + "/yourTargetFolder/" + name + "/" + filename);
                        copyFile(in, out);
                        in.close();
                        in = null;
                        out.flush();
                        out.close();
                        out = null;
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to copy asset file: " + filename, e);
                    }
                } else {
                    // Do something else on failure
                }
            }
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            // is to know is we can neither read nor write
        }
    }

    /**
     * Copy asset folder or file from assets to given path.
     */
    public static boolean copyAssetFolder(AssetManager assetManager,
                                          String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))
                    res &= copyAsset(assetManager, fromAssetPath + "/" + file, toPath + "/" + file);
                else
                    res &= copyAssetFolder(assetManager, fromAssetPath + "/" + file, toPath + "/" + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Copy asset file from assets to given path.
     */
    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);

            in.close();
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method used by copyAssets() on purpose to copy a file.
     */
    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * Show toast message alert.
     */
    public static void showToastAlert(final Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Create error AlertDialog.
     */
    public static Dialog createErrorAlertDialog(final Context context, int msgResId) {
        return new AlertDialog.Builder(context)
                .setTitle(R.string.app_name)
                .setMessage(msgResId)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();
    }

    /**
     * Create error AlertDialog.
     */
    public static Dialog createErrorAlertDialog(final Context context, String message) {
        return new AlertDialog.Builder(context)
                .setTitle(R.string.app_name)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();
    }

    /**
     * Create error AlertDialog.
     */
    public static Dialog createErrorAlertDialog(final Context context, String title, String message) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();
    }

    /**
     * Create bubble Dialog.
     */
    public static Dialog createBubbleDialog(final Context context, CharSequence message, int color) {
        Dialog dialog = new Dialog(context);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_bubble);

        TextView textView = (TextView) dialog.findViewById(R.id.text_dialog);
        textView.setText(message);
        textView.setTextColor(color);
        textView.setPadding(10, 10, 10, 10);

        return dialog;
    }

    /**
     * Create progress dialog.
     */
    public static Dialog createProgressDialog(final Context context, CharSequence title) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle(title);
        return dialog;
    }

    /**
     * Create full screen progress.
     */
    public static Dialog createFullScreenProgress(final Context context) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_loading);
        dialog.setCancelable(false);
        return dialog;
    }

    /**
     * Create horizontal progress dialog
     */
    public static ProgressDialog createHorizontalProgressDialog(final Context context, String message) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        return progressDialog;
    }


    ////////////////////////////////////////////////////////////////////////////////
    // email
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Check if given mail has correct format.
     */
    public static boolean isEmailValid(String email) {
        String regExpression = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";

        Pattern pattern = Pattern.compile(regExpression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);

        if (matcher.matches())
            return true;
        else
            return false;
    }

    /**
     * Send mail.
     */
    public static void sendMail(Context context, String text) {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        String[] address = {""};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, address);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        //emailIntent.setType("message/rfc822");
        //emailIntent.setType("photoImage/gif");
        emailIntent.setType("text/html");

        context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    /**
     * Send mail.
     */
    public static void sendMail(Context context, String dstAddr,
                                String subject, String text, boolean isRequestResult) {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND);

        String[] address = {dstAddr};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, address);
        emailIntent.setType("text/html");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        if (isRequestResult)
            ((Activity) context).startActivityForResult(Intent.createChooser(emailIntent, "Send mail..."), 1);
        else
            context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    ////////////////////////////////////////////////////////////////////////////////
    // ETC
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Get user device id.
     */
    public static String getUDID(Context context) {
        return Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @SuppressWarnings("deprecation")
    /**
     * Get size of current device screen.
     */
    public static Point getDisplaySize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        size.x = display.getWidth();
        size.y = display.getHeight();

        return size;
    }

    /**
     * Convert dp to pixel unit
     */
    public static int dpToPx(int dp, Context ctx) {
        Resources r = ctx.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    //originally: http://stackoverflow.com/questions/5418510/disable-the-touch-events-for-all-the-views
    //modified for the needs here
    /**
     * Enable views including own sub views.
     */
    public static void enableViewGroup(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            if (view.isFocusable())
                view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                enableViewGroup((ViewGroup) view, enabled);
            } else if (view instanceof ListView) {
                if (view.isFocusable())
                    view.setEnabled(enabled);
                ListView listView = (ListView) view;
                int listChildCount = listView.getChildCount();
                for (int j = 0; j < listChildCount; j++) {
                    if (view.isFocusable())
                        listView.getChildAt(j).setEnabled(false);
                }
            }
        }
    }

    /**
     * Check whether file is exist or not
     */
    public static boolean checkFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * Extract real file name from url string
     */
    public static String extractFileName(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    /**
     * Get passed days from given date
     *
     * @param dateText
     * @return
     */
    public static int getPassedDays(String dateText) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        Date date = new Date();
        try {
            date = formatter.parse(dateText);
        } catch (ParseException e) {
            if (Config.DEBUG) e.printStackTrace();
        }

        Calendar thatDay = Calendar.getInstance();
        thatDay.setTime(date);

        Calendar today = Calendar.getInstance();

        long diff = today.getTimeInMillis() - thatDay.getTimeInMillis(); //result in millis
        return (int) (diff / (24 * 60 * 60 * 1000));
    }

    /**
     * Get passed time from given date
     *
     * @param strDateTime
     * @return
     */
    public static String makeCommentTimeString(String strDateTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        Date date = new Date();
        try {
            date = formatter.parse(strDateTime);
        } catch (ParseException e) {
            if (Config.DEBUG) e.printStackTrace();
        }

        Calendar thatDay = Calendar.getInstance();
        thatDay.setTime(date);

        Calendar today = Calendar.getInstance();

        long diff = today.getTimeInMillis() - thatDay.getTimeInMillis(); //result in millis

        long second = diff / 1000;
        int min = (int) second / 60;
        int hour = min / 60;
        int day = hour / 24;

        if (min < 0) {
            return "";
        } else if (min <= 1) {
            return String.format("%d minute ago", min);
        } else if (min < 60) {
            return String.format("%d minutes ago", min);
        } else if (min >= 60 && min < 60 * 24) {
            if (hour == 1) {
                return String.format("%d hour ago", hour);
            } else if (hour < 24) {
                return String.format("%d hours ago", hour);
            }
        } else {
            if (day == 1)
                return String.format("%d day ago", day);
            else
                return String.format("%d days ago", day);
        }

        return "";
    }

    /**
     * Get passed time from given date
     *
     * @param date
     * @return
     */
    public static String getTimeString(Date date) {
        Calendar thatDay = Calendar.getInstance();
        thatDay.setTime(date);

        Calendar today = Calendar.getInstance();

        long diff = today.getTimeInMillis() - thatDay.getTimeInMillis(); //result in millis

        long second = diff / 1000;
        int min = (int) second / 60;
        int hour = min / 60;
        int day = hour / 24;
        int month = day / 30;
        int year = month / 12;

        if (min < 0) {
            return "";
        } else if (min <= 1) {
            return String.format("%d minute ago", min);
        } else if (min < 60) {
            return String.format("%d minutes ago", min);
        } else if (min >= 60 && min < 60 * 24) {
            if (hour == 1) {
                return String.format("%d hour ago", hour);
            } else if (hour < 24) {
                return String.format("%d hours ago", hour);
            }
        } else if (day < 31) {
            if (day == 1)
                return String.format("%d day ago", day);
            else
                return String.format("%d days ago", day);
        } else if (month < 12) {
            if (month == 1)
                return String.format("%d month ago", month);
            else
                return String.format("%d months ago", month);
        } else {
            if (year == 1)
                return String.format("%d year ago", year);
            else
                return String.format("%d years ago", year);
        }

        return "";
    }

    /*public static String getTipString(String str) {
        if (TextUtils.isEmpty(str)) return "";

        //String highlightedString = str.replaceAll("<tip>(\\S+)<tip>", "<font color='#FFFF00'>" + "$1" + "</font>");

        Pattern pattern = Pattern.compile("<tip>(\\S+)<tip>");
        Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    public static Spannable getHighlightedString(String str, String tip) {
        if (TextUtils.isEmpty(str)) return new SpannableString("");
        if (TextUtils.isEmpty(tip)) return new SpannableString(str);

        String strippedString = str.replace("<tip>", "");
        Spannable spannableString = new SpannableString(strippedString);
        int start = strippedString.indexOf(tip);

        //use a loop to change text color
        spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), start, start + tip.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), start, start + tip.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public static Spannable getHighlightedString(String str, ClickableSpan clickableSpan) {
        if (TextUtils.isEmpty(str)) return new SpannableString("");

        //String highlightedString = str.replaceAll("<tip>(\\S+)<tip>", "<font color='#FFFF00'>" + "$1" + "</font>");

        Pattern pattern;
        if (getWordCountInString(str, "<tip>") > 2) {
            pattern = Pattern.compile("<tip>(\\S+)<tip>");
        } else {
            pattern = Pattern.compile("<tip>(\\D+)<tip>");
        }

        Matcher matcher = pattern.matcher(str);
        String strippedString = str.replace("<tip>", "");
        Spannable spannableString = new SpannableString(strippedString);

        while (matcher.find()) {
            String tip = matcher.group(1);
            int start = strippedString.indexOf(tip);

            //use a loop to change text color
            spannableString.setSpan(new BackgroundColorSpan(Color.YELLOW), start, start + tip.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), start, start + tip.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(clickableSpan, start, start + tip.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableString;
    }

    private static int getWordCountInString(String str, String findStr) {
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {
            lastIndex = str.indexOf(findStr, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += findStr.length();
            }
        }

        return count;
    }*/

    /**
     * Apply custom font typeface to all views on screen.
     */
    public static void applyFont(final Context context, final View root, final String fontName) {
        try {
            if (root instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) root;
                for (int i = 0; i < viewGroup.getChildCount(); i++)
                    applyFont(context, viewGroup.getChildAt(i), fontName);
            } else if (root instanceof TextView)
                ((TextView) root).setTypeface(Typeface.createFromAsset(context.getAssets(), fontName));
        } catch (Exception e) {
            Log.e(TAG, String.format("Error occurred when trying to apply %s font for %s view", fontName, root));
            e.printStackTrace();
        }
    }

    /**
     * Apply custom font size to all sub views on screen.
     */
    public static void applyFontSize(final View root, final float fontSize) {
        if (root instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) root;
            for (int i = 0; i < viewGroup.getChildCount(); i++)
                applyFontSize(viewGroup.getChildAt(i), fontSize);
        } else if (root instanceof TextView) {
            ((TextView) root).setTextSize(fontSize);
        }
    }

    /**
     * Read photoImage data from web url
     */
    public static byte[] downloadRemoteFile(String fileUrl) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            Log.d(TAG, "download remote file URL = " + fileUrl);

            InputStream inputStream = null;

            try {
                inputStream = getHttpConnection(fileUrl);

                byte[] bytes = new byte[1024];

                while (inputStream.read(bytes) != -1)
                    byteArrayOutputStream.write(bytes);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            if (inputStream != null) inputStream.close();
        } catch (Exception e) {
            if (Config.DEBUG) e.printStackTrace();
        }

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Makes HttpURLConnection and returns InputStream
     */
    private static InputStream getHttpConnection(String urlString) throws IOException {
        InputStream stream = null;
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        try {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return stream;
    }

    /**
     * AsyncTask to get image data from remote file.
     */
    public static class GetImageDataTask extends AsyncTask<String, Void, byte[]> {

        @Override
        protected byte[] doInBackground(String... params) {
            return downloadRemoteFile(params[0]);
        }

    }

    /**
     * Get byte data from bitmap variable.
     */
    public static byte[] getByteFromBitmap(Bitmap bitmap) {
        if (bitmap == null) return new byte[0];

        /*
        //calculate how many bytes our photoImage consists of.
        int bytes = bitmap.getByteCount();
        //or we can calculate bytes this way. Use a different value than 4 if you don't use 32bit images.
        //int bytes = b.getWidth()*b.getHeight()*4;

        ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
        bitmap.copyPixelsToBuffer(buffer); //Move the byte data to the buffer

        return buffer.array();
        */
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        byte[] byteArray = stream.toByteArray();
        try {
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteArray;
    }

    /**
     * Get cropped bitmap with given bitmap.
     */
    public static Bitmap getCroppedBitmap(String imageFilePath, Rect rect) throws IOException {
        Bitmap croppedBitmap = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(imageFilePath, true);
            croppedBitmap = decoder.decodeRegion(rect, null);
            decoder.recycle();
        } else {
            Bitmap bitmapOriginal = BitmapFactory.decodeFile(imageFilePath, null);
            croppedBitmap = Bitmap.createBitmap(bitmapOriginal, rect.left, rect.top, rect.width(), rect.height());
        }

        return croppedBitmap;
    }

    /**
     * Get cropped round bitmap with bitmap data and radius.
     */
    public static Bitmap getCroppedRoundBitmap(byte[] bitmapData, int radius) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bmp = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length, options);

        Bitmap circleBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);

        // User Shader
        BitmapShader shader = new BitmapShader(bmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(shader);

        // Draw the canvas
        Canvas canvas = new Canvas(circleBitmap);
        canvas.drawCircle(bmp.getWidth() / 2, bmp.getHeight() / 2, bmp.getWidth() / 2, paint);

        return circleBitmap;

        /*Bitmap sbmp;
        if (bmp.getWidth() != radius || bmp.getHeight() != radius)
            sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
        else
            sbmp = bmp;
        Bitmap output = Bitmap.createBitmap(sbmp.getWidth(),
                sbmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(sbmp.getWidth() / 2 + 0.7f, sbmp.getHeight() / 2 + 0.7f,
                sbmp.getWidth() / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        return output;*/
    }

    /**
     * Get cropped round bitmap with bitmap data and radius.
     */
    public static Bitmap getCroppedRoundBitmap(Resources resources, int resourceId, int radius) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bmp = BitmapFactory.decodeResource(resources, resourceId);

        Bitmap sbmp;
        if (bmp.getWidth() != radius || bmp.getHeight() != radius)
            sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
        else
            sbmp = bmp;
        Bitmap output = Bitmap.createBitmap(sbmp.getWidth(),
                sbmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(sbmp.getWidth() / 2 + 0.7f, sbmp.getHeight() / 2 + 0.7f,
                sbmp.getWidth() / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        return output;
    }

    /**
     * Save bitmap data to external file.
     */
    public static boolean saveImageFile(byte[] data, String fileName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, options);

        boolean success = false;

        // Encode the file as a PNG photoImage.
        FileOutputStream outStream;
        try {
            File imageFile = new File(fileName);
            outStream = new FileOutputStream(imageFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            /* 100 to keep full quality of the photoImage */

            outStream.flush();
            outStream.close();
            success = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return success;
    }

    /**
     * Adjust orientation of JPEG image file and return bitmap.
     */
    public static Bitmap adjustBitmap(Uri jpegUri) {
        try {
            ExifInterface exif = new ExifInterface(jpegUri.getPath());

            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);

            Matrix matrix = new Matrix();
            if (rotation != 0f) {
                matrix.preRotate(rotationInDegrees);
            }

            Bitmap sourceBitmap = getBitmapFromUri(jpegUri);
            Bitmap adjustedBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0,
                    sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);

            sourceBitmap.recycle();

            return adjustedBitmap;
        } catch (IOException e) {
            if (Config.DEBUG) e.printStackTrace();
        }

        return null;
    }

    /**
     * Get degree from ExifInterface orientation.
     */
    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    public static final int ContentModeScaleAspectFill = 0;
    public static final int ContentModeScaleAspectFit = 1;

    /**
     * Resize bitmap.
     */
    public static Bitmap resizeImageWithContentMode(Bitmap bitmap, int contentMode, Point bounds, boolean filter) {
        float horizontalRatio = bounds.x / bitmap.getWidth();
        float verticalRatio = bounds.y / bitmap.getHeight();
        float ratio = 0;

        switch (contentMode) {
            case ContentModeScaleAspectFill:
                ratio = Math.max(horizontalRatio, verticalRatio);
                break;

            case ContentModeScaleAspectFit:
                ratio = Math.min(horizontalRatio, verticalRatio);
                break;

            default:
                new Throwable("Unsupported content mode");
        }

        int width = (int) (bitmap.getWidth() * ratio);
        int height = (int) (bitmap.getHeight() * ratio);

        return Bitmap.createScaledBitmap(bitmap, width, height, filter);
    }

    /**
     * Get the string that are represents "<b>boldStr</b> normalStr"
     */
    public static SpannableString getBoldNormalString(String boldStr, String normalStr) {
        SpannableString ss;

        if (TextUtils.isEmpty(boldStr)) {
            ss = new SpannableString(normalStr);
        } else {
            int pos = boldStr.length();

            ss = new SpannableString(boldStr + "  " + normalStr);
            ss.setSpan(new ForegroundColorSpan(Color.WHITE), 0, pos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new StyleSpan(Typeface.BOLD), 0, pos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(Color.GRAY), pos + 2, boldStr.length() + normalStr.length() + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return ss;
    }

    /**
     * Get width and height of given view on device screen.
     */
    public static Point getViewSize(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();

        return new Point(width, height);
    }

    /**
     * Get real height of TextView in pixel unit.
     */
    public static int getTextViewHeight(TextView textView) {
        //textView.setText(text);
        //textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        DisplayMetrics metrics = textView.getContext().getResources().getDisplayMetrics();

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(widthMeasureSpec, heightMeasureSpec);
        return textView.getMeasuredHeight();
    }

    /**
     * Checking device has camera hardware or not
     */
    public static boolean isDeviceSupportCamera(Context context) {
        // this device has a camera
        return context.getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }

    /**
     * Get bitmap from internal image file.
     */
    public static Bitmap getBitmapFromUri(Uri fileUri) {
        // bitmap factory
        BitmapFactory.Options options = new BitmapFactory.Options();

        // downsizing photoImage as it throws OutOfMemory Exception for larger
        // images
        options.inSampleSize = 8;
        options.inMutable = true;

        return BitmapFactory.decodeFile(fileUri.getPath(), options);
    }

    /**
     * Convert image uri to file
     */
    public static String/*File*/ convertImageUriToFile(Context context, Uri imageUri) {
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID/*, MediaStore.Images.ImageColumns.ORIENTATION*/};
            cursor = context.getContentResolver().query(
                    imageUri,
                    projection, // Which columns to return
                    null,       // WHERE clause; which rows to return (all rows)
                    null,       // WHERE clause selection arguments (none)
                    null);      // Order-by clause (ascending by name)

            int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            //int orientation_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);

            if (cursor.moveToFirst()) {
                //String orientation = cursor.getString(orientation_ColumnIndex);
                return cursor.getString(file_ColumnIndex)/*new File(cursor.getString(file_ColumnIndex))*/;
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * directory name to store captured images and videos
     */
    private static final String IMAGE_DIRECTORY_NAME = "skilled_captured_image";

    /*
     * returning photoImage / video
     */
    public static File getOutputMediaFile(boolean isImageType/*int type*/) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (isImageType) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        }

        return mediaFile;
    }

    /**
     * Clear data of app storage
     */
    public static class CacheManager {

        private static final long MAX_SIZE = 5242880L; // 5MB

        public static void cacheData(Context context, byte[] data, String name) throws IOException {

            File cacheDir = context.getCacheDir();
            long size = getDirSize(cacheDir);
            long newSize = data.length + size;

            if (newSize > MAX_SIZE) {
                cleanDir(cacheDir, newSize - MAX_SIZE);
            }

            File file = new File(cacheDir, name);
            FileOutputStream os = new FileOutputStream(file);
            try {
                os.write(data);
            }
            finally {
                os.flush();
                os.close();
            }
        }

        public static byte[] retrieveData(Context context, String name) throws IOException {

            File cacheDir = context.getCacheDir();
            File file = new File(cacheDir, name);

            if (!file.exists()) {
                // Data doesn't exist
                return null;
            }

            byte[] data = new byte[(int) file.length()];
            FileInputStream is = new FileInputStream(file);
            try {
                is.read(data);
            }
            finally {
                is.close();
            }

            return data;
        }

        public static void cleanDir(File dir, long bytes) {

            long bytesDeleted = 0;
            File[] files = dir.listFiles();

            for (File file : files) {
                bytesDeleted += file.length();

                if (file.isDirectory())
                    cleanDir(file, bytes);
                else
                    file.delete();

                if (bytesDeleted >= bytes) {
                    break;
                }
            }
        }

        private static long getDirSize(File dir) {

            long size = 0;
            File[] files = dir.listFiles();

            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                }
            }

            return size;
        }
    }

}
