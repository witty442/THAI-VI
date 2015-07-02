package com.vi.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class PhoneProperty {

	public int width = 0;
	public int height = 0;
	private static LogUtils log = new LogUtils("PhoneProperty");
	
	public PhoneProperty(Context ctx){
		try{
			WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			width = display.getWidth();
			height = display.getHeight();
			
			//log.debug("PhoneProperty:width["+width+"]");
			///log.debug("PhoneProperty:height["+height+"]");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static int getPopupCorX(int phoneWidth){
		int x = phoneWidth - 150;
		if(phoneWidth >= 1200){ //5.5
			x = phoneWidth - 650;
		}
			
		return x;
	}
}
