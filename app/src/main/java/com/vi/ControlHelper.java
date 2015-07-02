package com.vi;

import android.content.Context;

import com.vi.common.Control;
import com.vi.utils.LogUtils;

public class ControlHelper {
	private static LogUtils log = new LogUtils("ControlHelper");
	
	public static Control getControlContent(Context context,String currentFontSize,String currentBgColor,int currentPage) {
    	log.debug("currentFontSize:"+currentFontSize);
    	Control control = new Control();    
		//Set Control
	    control.setCurrentStyle(currentBgColor);
		control.setTextSize(Integer.parseInt(currentFontSize));
		control.setTextColor(context.getResources().getColor(R.color.font_color_day));
        control.setBgColor(context.getResources().getColor(R.color.bg_color_day));
        
        control.setCurrentPage(currentPage);
        return control;
    }
}
