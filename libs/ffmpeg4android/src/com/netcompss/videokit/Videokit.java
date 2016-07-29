package com.netcompss.videokit;

public final class Videokit {

  static {
    System.loadLibrary("videokit");
  }
	

  public native void run(String[] args, String andExternalpath);
  public native void fexit();
  

}
