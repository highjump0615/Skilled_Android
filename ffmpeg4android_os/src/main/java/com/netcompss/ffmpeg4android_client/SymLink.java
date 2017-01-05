package com.netcompss.ffmpeg4android_client;



public class SymLink {
	
	public  int createSymLink(String filePath,String linkPath) {
		int result = createSymLinkJNI( filePath, linkPath);
		return result;
	}
	
	public native int  createSymLinkJNI(String filePath,String linkPath);
	
	 static {
	        System.loadLibrary("sym-link");
	 }

}
