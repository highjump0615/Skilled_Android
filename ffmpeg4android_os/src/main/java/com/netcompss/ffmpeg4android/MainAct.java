package com.netcompss.ffmpeg4android;

//import com.netcompss.ffmpeg4android.IMyRemoteService;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.example.DemoClient;
import com.netcompss.ffmpeg4android_client.BaseWizard;
import com.netcompss.ffmpeg4android_client.Prefs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainAct  extends BaseWizard {

	private boolean started = false;
	private static final int DIALOG_ABOUT = 0;
	private static final int DIALOG_CONTACT = 1;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.remoteserviceclient);
		
		_prefs = new Prefs();
	    _prefs.setContext(this);
	    
	    //Prefs.setWorkFolder("/sdcard/videokit3/");
		//copyLicenseAndDemoFilesFromAssetsToSDIfNeeded();

		Button about = (Button)findViewById(R.id.aboutButton);
		Button contactus = (Button)findViewById(R.id.contactButton);
		Button purchase =  (Button)findViewById(R.id.purchase);
		
		
		Button startDemoAct = (Button)findViewById(R.id.startDemoAct);

		Prefs p = new Prefs();
		p.setContext(getApplicationContext());

		TextView t = (TextView)findViewById(R.id.license);
		String licenseText;
		


		// personal license 4
		LicenseCheckJNI lic = new LicenseCheckJNI();
		int licenseCheckRC = lic.licenseCheck(Prefs.getWorkingFolderForNative(), getApplicationContext());
		if (licenseCheckRC == 4 ) {
			 licenseText = " Personal";
		}
		else if (licenseCheckRC == 1) {
			 licenseText = " OEM";
			
		}
		else if (licenseCheckRC == 0 || licenseCheckRC == 2) {
			 licenseText = " Trial";
		}
		else if (licenseCheckRC == -1) {
			 licenseText = " Trial Expired";
		}
		else if (licenseCheckRC == -3) {
			 licenseText = " Trial (not validated) ";
			 Log.w(Prefs.TAG, "License file not created, possible first time");
		}
		else {
			licenseText = "Not Valid";
		}
		t.setText("License: " + licenseText);


		about.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				showDialog(DIALOG_ABOUT);
			}
		});

		contactus.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				showDialog(DIALOG_CONTACT);
			}
		});

	

		startDemoAct.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				startDemoAct();
			}
		});
		
		purchase.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("http://ffmpeg4android.netcompss.com/"));  
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});

		
				
	}

	
	@Override
	protected Dialog onCreateDialog(final int id) {
		
		Builder builder;
		switch (id) {
		case DIALOG_ABOUT:
			builder = new AlertDialog.Builder(this);
			builder.setCustomTitle(null);
			builder.setPositiveButton(R.string.ui_ok, null);

			View contentView = getLayoutInflater().inflate(R.layout.about_dialog, null, false);
			WebView webView = (WebView) contentView.findViewById(R.id.about_content);
			webView.loadData(getAboutText(), "text/html", "utf-8");
			builder.setView(contentView);

			Dialog d = builder.create();
			return d;
			
		case DIALOG_CONTACT:
			builder = new AlertDialog.Builder(this);
			builder.setCustomTitle(null);
			builder.setPositiveButton(R.string.ui_ok, null);

			View contentView2 = getLayoutInflater().inflate(R.layout.contact_us_dialog, null, false);
			WebView webView2 = (WebView) contentView2.findViewById(R.id.contact_us_content);
			webView2.loadData(getContactUsText(), "text/html", "utf-8");
			builder.setView(contentView2);

			Dialog d2 = builder.create();
			return d2;
			
		default:
			return null;
		}

		
	}

	

	

	

	@Override
	protected void onResume() {
		super.onResume();
		//updateServiceStatus();
	}

	protected void onDestroy() {
		super.onDestroy();
		Log.d( Prefs.TAG, "Client onDestroy()" );
	}

	private void startDemoAct() {
		Intent intent = new Intent(this, DemoClient.class);
		Log.d(Prefs.TAG, "Starting demo act");
		this.startActivity(intent);
	}
	
	
	
	public String getAboutText() {
		try {
			InputStream input = getResources().getAssets().open("about.html");
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			StringBuffer buf = new StringBuffer();
			String line;
			while ((line = reader.readLine()) != null) {
				buf.append(line);
			}
			String aboutText = buf.toString();
			aboutText = String.format(aboutText, getString(R.string.app_name), Prefs.getVersionName(getApplicationContext()));
			
			return aboutText;
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.getMessage(), e);
			return "An error occured while reading about.html";
		}

	}
	
	public String getContactUsText() {
		try {
			InputStream input = getResources().getAssets().open("contact_us.html");
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			StringBuffer buf = new StringBuffer();
			String line;
			while ((line = reader.readLine()) != null) {
				buf.append(line);
			}
			String aboutText = buf.toString();
			aboutText = String.format(aboutText, getString(R.string.app_name), Prefs.getVersionName(getApplicationContext()));
			
			return aboutText;
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.getMessage(), e);
			return "An error occured while reading about.html";
		}

	}
	
	

}