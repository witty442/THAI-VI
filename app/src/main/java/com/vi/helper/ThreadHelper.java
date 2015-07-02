package com.vi.helper;

import android.app.Dialog;
import android.os.Handler;
import android.os.Message;

public class ThreadHelper {

	

	public static void timerDelayRemoveDialog(long time, final Dialog d){
	    new Handler().postDelayed(new Runnable() {
	        public void run() {                
	            d.dismiss();         
	        }
	    }, time); 
	}
}
