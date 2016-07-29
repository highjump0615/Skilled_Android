package com.netcompss.ffmpeg4android;

interface IFfmpgefRemoteServiceBridge {

	
	void runTranscoding();
	void setFfmpegCommand(String command);
	void fexit();
	void setTranscodingProgress(int transcodingProgress);
	void setNotificationMessage(String notificationMessage);
	void setNotificationTitle(String notificationTitle);
	void setComplexFfmpegCommand(in String[] command);
	void setWorkingFolder(String workingFolder);
	int getTranscodingProgress();
	
}