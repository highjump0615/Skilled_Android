package com.netcompss.ffmpeg4android_client;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class CopyFileBackground extends AsyncTask<String, Integer, Integer>
{
	private Activity _act;
	ProgressDialog progressDialog;
	public CopyFileBackground(Activity act) {
		_act = act;
	}
	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(_act);
		progressDialog.setMessage("Copying target file to working folder");
		progressDialog.show();
		
	}

	protected Integer doInBackground(String... paths) {
		Log.d(Prefs.TAG, "CopyFileBackground doInBackground started");
		String filePath = paths[0];
		String destinationPath = paths[1];
		FileUtils.copyFileToFolder(filePath, destinationPath);
		return new Integer(0);
	}

	protected void onProgressUpdate(Integer... progress) {

	}

	@Override
	protected void onCancelled() {
		Log.d(Prefs.TAG, "CopyFileBackground onCancelled");
		progressDialog.dismiss();
		super.onCancelled();
	}


	@Override
	protected void onPostExecute(Integer result) {
		Log.d(Prefs.TAG, "CopyFileBackground onPostExecute");
		progressDialog.dismiss();
		super.onPostExecute(result);

	}
}