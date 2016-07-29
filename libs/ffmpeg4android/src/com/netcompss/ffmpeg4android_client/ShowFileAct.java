package com.netcompss.ffmpeg4android_client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.netcompss.ffmpeg4android.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This is optional activity to send and view log
 * @author user
 *
 */
public class ShowFileAct extends Activity {
	private String log;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.show_file);
				
		//WebView desc = (WebView)findViewById(R.id.ShowFileWebView);
		//desc.loadDataWithBaseURL(null, getHelpText(), "text/html", "utf-8", "about:blank");
		TextView t = (TextView)findViewById(R.id.showFiletextView1);
		log = getLogsText();
		if (log == null || log.equals("")) 
			t.setText(getString(R.string.show_file_text));
		else
			t.setText(log);
		
		
		Button helpDone = (Button) findViewById(R.id.showFileDone);
		helpDone.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//Intent intent = new Intent(ShowFileAct.this, DemoClient.class);
				//startActivity(intent);
				finish();
				
			}

		}); 
		
		final String to = "android@netcompss.com";
		final String subject = getString(R.string.app_name) + " version: " + Prefs.getVersionName(getApplicationContext()) + " Error log";
		
		
		Button sendEmail = (Button) findViewById(R.id.showFileSend);
		sendEmail.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ to });
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, log);

                startActivity(Intent.createChooser(emailIntent, "send email..."));
				
				
			}

		}); 
		
		
		
	}
	
	private String getLogText(String path) {
		String t = null;
		String LINE_SEPARATOR = System.getProperty("line.separator");
		try {
			File f = new File(path); 
			InputStream input = new FileInputStream(f);
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			StringBuffer buf = new StringBuffer();
			
			String line;
			while ((line = reader.readLine()) != null) {
				buf.append(line);
				buf.append(LINE_SEPARATOR);
			}
			t = buf.toString();
			
			
		} catch (IOException e) {
			Log.e(Prefs.TAG, e.getMessage());
			//return getString(R.string.show_file_text);
		}
		return t;

	}
	
	private String getLogsText() {
		String applicationLocalLog = getLogText(Prefs.getVideoKitLogFilePath());
		String ffmpegStreamsLog = getLogText(Prefs.getVkLogFilePath());
		String ffmpegRunLog = getLogText(Prefs.getFfmpeg4androidLogFilePath());
		
		String logs = "";
		if (applicationLocalLog != null) {
			logs += applicationLocalLog;
		}
		else {
			Log.w(Prefs.TAG, "applicationLocalLog is null");
		}
		
		if (ffmpegStreamsLog != null) {
			logs += ffmpegStreamsLog;
		}
		else {
			Log.w(Prefs.TAG, "ffmpegStreamsLog is null");
		}
		if (ffmpegRunLog != null) {
			logs += ffmpegRunLog;
		}
		else {
			Log.w(Prefs.TAG, "ffmpegRunLog is null");
		}
		
		return logs;
		
		
		
		
	}
}

