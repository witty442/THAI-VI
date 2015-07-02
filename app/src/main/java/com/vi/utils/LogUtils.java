package com.vi.utils;


public class LogUtils {

  public  String appTag = "";
	public LogUtils(String sappTag){
		this.appTag = sappTag;
	}
	
	public void debug(String msg){
	   //Log.d(appTag, msg);
		System.out.println(appTag+":"+msg);
	}
	public  void info(String msg){
	  //Log.i(appTag, msg);
	  //System.out.println(appTag+":"+msg);
	}
	public  void warn(String msg){
	  //Log.w(appTag, msg);
	  //System.out.println(appTag+":"+msg);
	}
}
