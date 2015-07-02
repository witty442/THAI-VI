package com.vi;

import com.vi.parser.JSoupHelperAuthen;

import android.app.Application;

public class THAIVI extends Application{
   private JSoupHelperAuthen thaiviAuthen = new JSoupHelperAuthen();

	public JSoupHelperAuthen getThaiviAuthen() {
		return thaiviAuthen;
	}
	
	public void setThaiviAuthen(JSoupHelperAuthen thaiviAuthen) {
		this.thaiviAuthen = thaiviAuthen;
	}
  
  
}
