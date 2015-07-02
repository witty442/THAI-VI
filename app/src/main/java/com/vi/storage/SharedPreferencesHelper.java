

package com.vi.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.vi.utils.Constants;


public final class SharedPreferencesHelper {
	
	private static final String LOG_TAG = "SharedPreferencesHelper";
	private static long errorId = 0;
	
	// Dialogs Id
	public static final int DIALOG_ABOUT = 0;
	public static final int DIALOG_NO_CONNECTION = 1;
	public static final int DIALOG_UPDATE_PROGRESS = 2;
	public static final int DIALOG_REMOVE_CHANNEL = 3;
	public static final int DIALOG_MIN_CHANNEL_REQUIRED = 4;
	public static final int DIALOG_ADD_CHANNEL = 5;
	public static final int DIALOG_ADD_CHANNEL_ERROR_URL = 6;
	public static final int DIALOG_ADD_CHANNEL_ERROR_PARSING = 7;
	public static final int DIALOG_STARTUP = 8;
	public static final int DIALOG_OPEN_ITEM_PROGRESS = 9;
	
	// Menu Groups Id
	public static final int CHANNEL_SUBMENU_GROUP = 0;
	
	// App Preferences
	protected static final String PREFS_FILE_NAME = Constants.PREF_NAME;
	private static final String PREF_UNIQUE_ID = "uuid";
	private static final String PREF_STARTUP_DIALOG_ON_INSTALL_KEY = "startupDialogOnInstall";
	private static final String PREF_STARTUP_DIALOG_ON_UPDATE_KEY = "startupDialogOnUpdate";
	private static final String PREF_VERSION_CODE_KEY = "version";
	private static final String PREF_BG_CURRENT_COLOR = "currentBgColor";
	private static final String PREF_CURRENT_FONT_SIZE = "textSize";
	private static final String PREF_CURRENT_BRIGHTNESS = "currentBrightness";
	
	public static final int DEFAULT_SPLASH_SCREEN_DURATION = 800;
	public static final boolean DEFAULT_DYNAMIC_MODE = true;
	public static final boolean DEFAULT_SHOW_UPDATE_DIALOG = false;
	
	// Min content length to display item view
	public static final int MIN_CONTENT_LENGTH = 80;
	public static SharedPreferences settings = null;
	
	public SharedPreferencesHelper(Context context){
		 //settings = context.get
		settings = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public  String getSetting(String key,String defaultS){
		String value = "";
		value = settings.getString(key,defaultS );
		return value;
	}
	
}
