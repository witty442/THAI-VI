package com.vi.utils;

import java.io.File;

import android.os.Environment;

public class DebugUtils {

	private static LogUtils log = new LogUtils("DebugUtils");
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
       try{
    	  int r = Integer.parseInt("-");
       }catch(Exception e){
    	   genStackErrorMsg(e.getStackTrace());
       }
	}
	 public static StringBuffer genStackErrorMsg(StackTraceElement[] s){
		 StringBuffer stackMessage = new StringBuffer("");
		 try{
			 for(int i =0;i<s.length;i++){
				 StackTraceElement t = s[i];
				 log.debug("Error["+i+"]:"+t.toString());
				 stackMessage.append(t.toString()) ;
			 }
			 
		 }catch(Exception e){
			 e.printStackTrace();
		 }
		 return stackMessage;
	 }
	
	 public static  void debugToFile(String errorMsg){
		  // TODO Auto-generated method stub
	       String pathFile = "";
		   try {
			   String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
			   File myNewFolder = new File(extStorageDirectory +"/"+ Constants.APP_FOLDER);
			   log.debug("extStorageDirectory["+extStorageDirectory+"] appFolder Exist["+myNewFolder.exists()+"]");
			   if( !myNewFolder.exists()){
			      log.debug("mkDir:"+myNewFolder.mkdir());
			   }
			   pathFile = extStorageDirectory+"/"+Constants.APP_FOLDER+"/error_msg.txt";
			   
			   FileUtil.writeFile(pathFile, errorMsg.getBytes());

		   } catch (Exception e) {
		      // TODO Auto-generated catch block
		      e.printStackTrace();
		   }
	   }

}
