package com.example;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.netcompss.ffmpeg4android.R;
import com.netcompss.ffmpeg4android_client.BaseWizard;
import com.netcompss.ffmpeg4android_client.FileUtils;
import com.netcompss.ffmpeg4android_client.Prefs;


public class DemoClient extends BaseWizard {
	

	
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	      super.onCreate(savedInstanceState);
	      setContentView(R.layout.ffmpeg_demo_client2);
	      
	      String lastCommandText = Prefs.getLastCommand(getApplicationContext());
	      EditText commandText =  (EditText)findViewById(R.id.CommandText);
	      if (lastCommandText != null) {
	    	  Log.d(Prefs.TAG, "Setting command text as the last command: " + lastCommandText);
	    	  commandText.setText(lastCommandText);
	      }
	      else {
	    	  Log.d(Prefs.TAG, "No last command using default");
	      }
	     

	      // if you want to change the default work location (/sdcard/videokit/) use the uncomment the below method.
	      // It must be defined before calling the copyLicenseAndDemoFilesFromAssetsToSDIfNeeded method,
	      // in order for this method to copy the assets to the correct location.
	      //setWorkingFolder("/sdcard/videokit3/");
	      
	      // this will copy the license file and the demo video file 
	      // to the videokit work folder location 
	      copyLicenseAndDemoFilesFromAssetsToSDIfNeeded();
	      
	      if (Prefs.transcodingIsRunning) {
				Log.i(Prefs.TAG, "Currently transcoding is running, not running.");
				Toast.makeText(this, getString(R.string.trascoding_running_background_message), Toast.LENGTH_LONG).show();
				finish();
				return;
		  }
	      

	      Button invoke =  (Button)findViewById(R.id.invokeButton);
	      invoke.setOnClickListener(new OnClickListener() {
				public void onClick(View v){
					
					EditText commandText = (EditText)findViewById(R.id.CommandText);
					String commandStr = commandText.getText().toString();
					
					//String commandStr = "ffmpeg -y -i /sdcard/videokit/inputvideo.mp4 -strict experimental -vcodec libx264 -preset ultrafast -crf 24 /sdcard/videokit/out3.mp4";
					// String[] complexCommand = {"ffmpeg","-y" ,"-i", "/sdcard/videokit/inputvideo.mp4","-strict","experimental", "-s", "320x240", "-r", "25", "-b", "2097k", "-vcodec", "mpeg4", "-ab", "48000", "-ac", "2", "-ar", "22050","/sdcard/videokit/out2.mp4"};
					
					
					setCommand(commandStr);
					String outputPath = FileUtils.getOutputFileFromCommandStr(commandStr);
					setOutputFilePath(outputPath);
					
					//setCommandComplex(complexCommand);
					        			
					///optional////
					setProgressDialogTitle("Transcoding...");
					setProgressDialogMessage("Depends on your video size, transcoding can take a few minutes");
					setNotificationIcon(R.drawable.notification_icon);
					setNotificationMessage("Demo is running...");
					setNotificationTitle("DemoClient");
					setNotificationfinishedMessageTitle("Transcoding finished");
					setNotificationfinishedMessageDesc("Click to play");
					setNotificationStoppedMessage("Transcoding stopped");
					///////////////
				
					runTranscoing();
        			///////////////////////////////////////////////////////////////////////////////
        			
        			
        			
        			
				}
			});
	      
	      Button showLog =  (Button)findViewById(R.id.showLastRunLogButton);
	      showLog.setOnClickListener(new OnClickListener() {
				public void onClick(View v){
					startAct(com.netcompss.ffmpeg4android_client.ShowFileAct.class);				
				}
			});
	      
	      
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(Prefs.TAG, "DemoClient onDestroy()");
	}
	
	/*
	@Override
	public void onBackPressed() {
		Log.d(Prefs.TAG, "DemoClient onBackPressed");
		super.onDestroy();
		
	}
	*/
	
	
	 


}
