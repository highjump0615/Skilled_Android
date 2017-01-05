package com.netcompss.ffmpeg4android_client;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class Prefs {
	public static final boolean isProd = false;
	public static final String TAG = "ffmpeg4android";
	private static final String EXPIRATION_DATE_KEY = "expiration_date223";
	private static final int TRIAL_MINUTES = 60 * 24 * 15;
	
	
	//public static final String DEFAULT_WORK_FOLDER = "/sdcard/videokit/";
	//public static final String FFMPEG4ANDROID_LOG_FILE_PATH_DEFAULT = DEFAULT_WORK_FOLDER + "ffmpeg4android.log";
	//public static final String VIDEOKIT_LOG_FILE_PATH_DEFAULT = DEFAULT_WORK_FOLDER + "videokit.log";
	//public static final String VK_LOG_DEFAULT = DEFAULT_WORK_FOLDER + "vk.log";
	
	public static final String DEFAULT_OUT_FOLDER = "/sdcard/videokit/";
	public static final String DEFAULT_WORK_FOLDER = "/sdcard/videokit/";
	
	private static String _workFolder =  DEFAULT_WORK_FOLDER;
		
	public static boolean forceStopFlag = false;
	public static boolean transcodingIsRunning = false;
	public static String durationOfCurrent = null;
	
	private String outFolder = null;
	
	public static long inputFileSize = -1;
	public static long outputFileSize = -1;
	
	public static final int FILE_TYPE_VIDEO = 0;
	public static final int FILE_TYPE_AUDIO = 1;
	public static final int FILE_TYPE_PIC   = 2;
	
	public static final int NOTIFICATION_ID = 5326;
	
	
	
	public static boolean noFfmpeg4androidLog = false;
	private static String _versionName = "9.0.05";

	public static boolean isComplex(Context ctx) {
		boolean isComplex = getSharedPreferences(ctx).getBoolean("ffmpeg4android_isComplex", true);
		Log.i(Prefs.TAG, "ffmpeg4android_isComplex: " + isComplex);
		return isComplex;
	}

	public static void setComplex(Context ctx, boolean isComplex) {
		Log.i(Prefs.TAG, "ffmpeg4android_setComplex: " + isComplex);
		SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
		editor.putBoolean("ffmpeg4android_isComplex", isComplex);
		editor.commit();
		
	}


	private Context mContext;
	

	static SharedPreferences getSharedPreferences(Context ctx) {
        return ctx.getSharedPreferences(EXPIRATION_DATE_KEY, Context.MODE_MULTI_PROCESS);
	}
	
	public void setContext(Context ctx) {
		this.mContext = ctx;
	}
	
	public String getOutFolder() {
		if (outFolder == null) {
			return DEFAULT_OUT_FOLDER;
		}
		else {
			return outFolder;
		}
	}
	
	public static boolean isAndroidVersionKitkatOrAbove() {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
		     return true;
		}
		else {
			return false;
		}
	}
	
	/*
	public Long getExpirationDate() {
		SharedPreferences preferences = mContext.getSharedPreferences(EXPIRATION_DATE_KEY, Context.MODE_PRIVATE);
		Long dateSaved = preferences.getLong(EXPIRATION_DATE_KEY, 0);
		if (dateSaved.longValue() == 0) {
			Calendar cal2DaysToTheFuture = Calendar.getInstance(); 
			cal2DaysToTheFuture.add(Calendar.MINUTE, TRIAL_MINUTES);
			Editor editor = preferences.edit();
			editor.putLong(EXPIRATION_DATE_KEY, new Long(cal2DaysToTheFuture.getTimeInMillis()));
			editor.commit();
			return cal2DaysToTheFuture.getTimeInMillis();
		} else {
		 return dateSaved;
		}
	}
	*/
	
	
	/*
	public boolean isTrialExpired() {
		
		if (Calendar.getInstance().getTimeInMillis() > getExpirationDate()) {
			return true;
		} else {
			Log.d(Prefs.TAG, "Trial license will expire at: " + new Date(getExpirationDate()));
			return false;
		}
	}
	*/
	
	public static void setRemoteNotificationIconId(Context ctx, int remoteNotificationIconId) {
		 Log.i(Prefs.TAG, "set remoteNotificationIconId: " + remoteNotificationIconId);
		 SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
		 editor.putInt("RemoteNotificationIconId", remoteNotificationIconId);
		 editor.commit();
	}
	
	public static int getRemoteNotificationIconId(Context ctx) {
		int remoteNotificationIconId = getSharedPreferences(ctx).getInt("RemoteNotificationIconId", -1);
		Log.i(Prefs.TAG, "get remoteNotificationIconId: " + remoteNotificationIconId);
		return remoteNotificationIconId;
	}
	
	
	
	public static void setLastCommand(Context ctx, String command) {
		 Log.i(Prefs.TAG, "set lastCommand: " + command);
		 SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
		 editor.putString("LastCommand", command);
		 editor.commit();
	}
	
	public static String getLastCommand(Context ctx) {
		String lastCommand = getSharedPreferences(ctx).getString("LastCommand", null);
		Log.i(Prefs.TAG, "get LastCommand: " + lastCommand);
		return lastCommand;
	}
	
	
	public static String getWorkFolder() {
		return _workFolder;
	}

	public static void setWorkFolder(String workFolder) {
		_workFolder = workFolder;
	}

	
	public static String getWorkingFolderForNative() {
		//return DEFAULT_WORK_FOLDER.substring(0, DEFAULT_WORK_FOLDER.length() - 1);
		return getWorkFolder().substring(0, getWorkFolder().length() - 1);
	}
	
	// If ffmpeg44android is used as a library, the version canot be taken from the manifest
	public static String getLibraryVersionName() {
		return _versionName;
	}
	
	public static String getVersionName(Context ctx) {
		
		String versionName = "";
			try {
				versionName = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				Log.w(Prefs.TAG, "No version code found, returning -1");
			}
		return versionName;

	}
	
	
	public static String getFfmpeg4androidLogFilePath() {
		return getWorkFolder() + "ffmpeg4android.log";
	}

	

	public static String getVideoKitLogFilePath() {
		return getWorkFolder() + "videokit.log";
	}

	

	public static String getVkLogFilePath() {
		return getWorkFolder() + "vk.log";
	}

	
	public enum Status {
		STATUS_IDLE, STATUS_WORKING, STATUS_FINISHED_OK, STATUS_FINISHED_FAIL, STATUS_NA, STATUS_TRIAL_FINISHED
		
	}

}
