

package com.vi;

import java.util.Calendar;

import com.vi.utils.LogUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;


public class StartScreenActivity extends Activity {
	
	private static final String LOG_TAG = "SplashScreenActivity";
	private final Handler mHandler = new Handler();
	LogUtils log = new LogUtils("StartScreenActivity");
	
    private final Runnable mPendingLauncherRunnable = new Runnable() {
        public void run() {
        	log.debug("SplashScreenActivity:run");
            Intent intent = new Intent(StartScreenActivity.this, MainActivity.class);
            intent.putExtra("START_APP", "TRUE");
            
            startActivity(intent);
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_layout);
        Drawable backgroundDrawable = getResources().getDrawable(R.drawable.splash_background);
        backgroundDrawable.setDither(true);
        findViewById(android.R.id.content).setBackgroundDrawable(backgroundDrawable);
        
        /** Reset Check Upgrade Topic 15 of month**/
 	    Calendar c = Calendar.getInstance();
 	    int day = c.get(Calendar.DATE);
 	    if(day==15 || day ==30){
 	      SharedPreferences settingMain = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()); 
 		  SharedPreferences.Editor editor = settingMain.edit();
 		  editor.putString("upgradeTopic", "false");
 		  editor.commit();
 	   }
 	   
 	    
        mHandler.postDelayed(mPendingLauncherRunnable, 500);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mPendingLauncherRunnable);
    }
    
   
}
