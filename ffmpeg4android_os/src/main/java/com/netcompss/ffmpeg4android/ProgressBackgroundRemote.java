package com.netcompss.ffmpeg4android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.example.DemoClient;
import com.netcompss.ffmpeg4android_client.Prefs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProgressBackgroundRemote extends AsyncTask<Void, Integer, Integer> {
	
	private Notification _notification;
	private Context _context;
	int _notificationId;
	private SimpleDateFormat _simpleDateFormat;
	long _timeRef = -1;
	int _prevProgress;
	FFMpegRemoteServiceBridge _bridge;
	
	public ProgressBackgroundRemote(int notificationId, Context context, Notification notification, FFMpegRemoteServiceBridge bridge) {
		_notification = notification;
		_context = context;
		_notificationId = notificationId;
		_simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SS");
		_bridge = bridge;
		try {
			Date ref = _simpleDateFormat.parse("00:00:00.00");
			ref.setYear(112);
			_timeRef = ref.getTime();
		} catch (ParseException e) {
			Log.w(Prefs.TAG, "failed to set _timeRef");
		}
	
	}
	
	protected Integer doInBackground(Void... paths)  {
		Log.i(Prefs.TAG, "=======ProgressBackgroundRemote doInBackground=========");
		showNotificationInProgressNew();
        Intent intent = new Intent("action_transcoding_complete");
        _context.sendBroadcast(intent);
		return 0;
	}
	
	private void showNotificationInProgressNew() {
		try {Thread.sleep(2000);} catch (InterruptedException e) {}
		Log.i(Prefs.TAG, "Trying to: _bridge.getTranscodingProgress()");
		int	progress = -1;
		
		//int profress = calcProgress();
		NotificationManager nm = null;
		while ( (progress = _bridge.getTranscodingProgress() ) < 100) {
			// Log.i(Prefs.TAG, "======================== progress from remote: " +  progress);
			nm = (NotificationManager) _context.getSystemService(_context.NOTIFICATION_SERVICE);
			Intent intent = new Intent(_context, DemoClient.class); 
			PendingIntent pIntent = PendingIntent.getActivity(_context.getApplicationContext(), 0, intent, 0);
			
			String notificationTitle =  (_bridge.getNotificationTitle() == null) ? _context.getString(R.string.notif_progress_title): _bridge.getNotificationTitle();
			String notificationMessage = (_bridge.getNotificationMessage() == null) ? _context.getString(R.string.notif_progress_desc): _bridge.getNotificationMessage();
			
			
			_notification.setLatestEventInfo(_context, notificationTitle + " " + progress + "%" , notificationMessage, pIntent);
			nm.notify(_notificationId, _notification);
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
		}

		Log.i(Prefs.TAG, "== Cancel notification (remote): " + _notificationId);
		if (nm == null) nm = (NotificationManager) _context.getSystemService(_context.NOTIFICATION_SERVICE);
		nm.cancel(_notificationId);

	}
	

	

	protected void onProgressUpdate(Integer... progress) {

	}
	
	@Override
	protected void onPostExecute(Integer result) {
		
	}
	
	

}

