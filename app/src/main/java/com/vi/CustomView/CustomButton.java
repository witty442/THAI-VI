package com.vi.CustomView;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomButton extends TextView {

	public CustomButton(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    init();
	}

	public CustomButton(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    init();
	}

	public CustomButton(Context context) {
	    super(context);
	    init();
	}

	public void init() {
	    Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Antic.ttf");
	    setTypeface(tf ,Typeface.BOLD);
	    setTextSize(14);
	 
	}
}
