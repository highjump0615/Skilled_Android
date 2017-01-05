package com.netcompss.ffmpeg4android;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.DemoClient;
import com.netcompss.ffmpeg4android_client.Prefs;
import com.netcompss.loader.LoadJNI;

public class FFMpegRemoteServiceBridge extends Service {
	
	private Handler serviceHandler;
	//private int status = Prefs.Status.STATUS_FINISHED_OK.ordinal();
	private Task myTask = new Task();
	private String _commandStr = null;
	private String[] _command  = null;
	
	private boolean _run = false;
	private String _cat = null;
	private int _transcodingProgress;
	ProgressBackgroundRemote _progressRemote;
	
	private String _notificationTitle = null;
	private String _notificationMessage = null;
	private String _workingFolder = Prefs.DEFAULT_WORK_FOLDER;
	
	public String getNotificationTitle() {
		return _notificationTitle;
	}


	public String getNotificationMessage() {
		return _notificationMessage;
	}

	public int getTranscodingProgress() {
		Log.d(Prefs.TAG, "get _transcodingProgress: " + _transcodingProgress);
		return _transcodingProgress;
	}


	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(Prefs.TAG, "=======service onBind()=======");
		return myRemoteServiceStub;
	}

	private IFfmpgefRemoteServiceBridge.Stub myRemoteServiceStub = new IFfmpgefRemoteServiceBridge.Stub() {
				
		public void setComplexFfmpegCommand(String[] command) throws RemoteException {
			_command = command;
			Log.d(Prefs.TAG, "==========printing complex commad========");
			for (int i = 0; i < _command.length; i++) {
				Log.d(Prefs.TAG, command[i]);
			}
			Log.d(Prefs.TAG, "=========================================");
		}
		
		public void setFfmpegCommand(String command) throws RemoteException {
			 _commandStr = command;
			 _command = _commandStr.split(" ");
			 Log.d(Prefs.TAG, "command items num: " + _command.length);
//			 Log.d(Prefs.TAG, "command: " + _commandStr);
		}

		public void setNotificationMessage(String notificationMessage) {
			_notificationMessage = notificationMessage;
		}
		
		public void setNotificationTitle(String notificationTitle) {
			Log.d(Prefs.TAG, "notificationTitle: " + notificationTitle);
			_notificationTitle = notificationTitle;
		}
		
		public void setWorkingFolder(String workingFolder) {
			_workingFolder =  workingFolder.substring(0, workingFolder.length() - 1);
//			Log.d(Prefs.TAG, "workingFolder from remote: " + _workingFolder);
			
		}
		
		
		public void setTranscodingProgress(int transcodingProgress) {
			// Log.d(Prefs.TAG, "set transcodingProgress: " + transcodingProgress);
			_transcodingProgress = transcodingProgress;
		}
		
		public void fexit() throws RemoteException  {
			
			LoadJNI vk = new LoadJNI();
			try {
				Log.e(Prefs.TAG, "Calling fexit() via loader");
				vk.fExit(getApplicationContext());
			} catch (Exception e) {
				Log.e(Prefs.TAG, e.getMessage(), e);
			}
			
			//Log.e(Prefs.TAG, "Calling fexit() -  deprecated.");
			
			
		}
		
		public void runTranscoding() throws RemoteException  {
			Log.i(Prefs.TAG,"=======remote service runTranscoding ======");
				_run = true;
		}

        public int getTranscodingProgress() {
            Log.d(Prefs.TAG, "transcoding Progress: " + _transcodingProgress);
            return _transcodingProgress;
        }
	};
	
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(Prefs.TAG,"=======service onCreate(): Stopping forground (to overcome 2.3.x bug)" );
		stopForeground(true);
		
	}
	
	
	
	@Override
	public void onDestroy() {
		Log.i(Prefs.TAG,"=======service onDestroy()======");
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	
		Log.i(Prefs.TAG, "===onStartCommand called");
		
		if (intent != null && intent.getCategories() != null) {
		  _cat = intent.getCategories().iterator().next();
		  Log.i(Prefs.TAG, "===onStartCommand cat: " + _cat);
		}
		else {
			_cat = "Ignore";
		}
		
		if (_cat.equals("Base")) {
			Log.d(Prefs.TAG, "onStartCommand, START_STICKY, base Command");
			
			int tempIconId = Prefs.getRemoteNotificationIconId(getApplicationContext());
			int notifIcon = R.drawable.icon;
			if (tempIconId != -1) {
				notifIcon = tempIconId;
				Log.i(Prefs.TAG, "notifIcon is set");
			}
			else 
				Log.w(Prefs.TAG, "notifIcon is not set reverting to default");
			
			Intent pintent = new Intent(getApplicationContext(), DemoClient.class);
			pintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pendIntent = PendingIntent.getActivity(getApplicationContext(), 0, pintent, 0);

			Notification _notification = new Notification(notifIcon, "", System.currentTimeMillis());
			
			
			//This method is deprecated. Use Notification.Builder instead.
			Log.i(Prefs.TAG, "Start RemoteService with notification id: " + Prefs.NOTIFICATION_ID );
			_notification.setLatestEventInfo(getApplicationContext(), "", "", pendIntent);
			_notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
			startForeground(Prefs.NOTIFICATION_ID, _notification);
			
			_progressRemote = new ProgressBackgroundRemote( Prefs.NOTIFICATION_ID, getApplicationContext(), _notification, this);

			serviceHandler = new Handler();
			serviceHandler.post(myTask);
			return START_STICKY;
		}
		else if (_cat.equals("Info")) {
			Log.d(Prefs.TAG, "onStartCommand, base info");
			return START_STICKY;
		}
		else {
			Log.d(Prefs.TAG, "Not running since the OS auto started the service after crash");
			NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			nm.cancel(Prefs.NOTIFICATION_ID);
			Log.i(Prefs.TAG, "Cancel notification: " + Prefs.NOTIFICATION_ID);
			stopForeground(true);
			stopSelf();
			return START_STICKY;
		}


		
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(Prefs.TAG, "service onStart()");
		//serviceHandler = new Handler();
		//serviceHandler.post(myTask);

	}
	
	class Task implements Runnable {
		int sleepCounter = 0;
		public void run() {
			Log.i(Prefs.TAG, "Run called.");
			//android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
			while (true) {
				if (_command != null && _run) {
					LoadJNI vk = new LoadJNI();
					try {
						//ProgressBackgroundRemote p = new ProgressBackgroundRemote( _notificationId, getApplicationContext(), _notification);
						_progressRemote.execute();
						Log.i(Prefs.TAG, "===============Running command from thread path: " + _workingFolder);
						vk.run(_command, _workingFolder, getApplicationContext());
						
						_run = false;
					} catch (Exception e) {
						Log.e(Prefs.TAG, "FFMPEG finished with errors..");
					}
					break;
				}
				else {
					sleepCounter++;
					if (sleepCounter > 10)
						break;
					Log.d(Prefs.TAG, "Sleeping, waiting for command");
					try {Thread.sleep(300);} catch (InterruptedException e) {}
				}
			}
			Log.i(Prefs.TAG, "RemoteService: FFMPEG finished.");
		}
	}
	
	
	
	
	

}
