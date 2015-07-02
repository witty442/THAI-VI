
package com.vi;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.TextView;
import android.widget.Toast;

import com.vi.common.Feed;
import com.vi.parser.JSoupHelperAuthen;
import com.vi.parser.JSoupHelperNoAuthen;
import com.vi.storage.DBAdapter;
import com.vi.storage.SharedPreferencesHelper;
import com.vi.utils.Constants;
import com.vi.utils.FileUtil;
import com.vi.utils.LogUtils;
import com.vi.utils.NetworkUtils;

public class FeedPrefActivity extends PreferenceActivity {

    private DBAdapter mDbFeedAdapter;
    private Context context;
    public String textSize ="";
    public SharedPreferences settings = null;
    SharedPreferences.Editor editor = null;
    private JSoupHelperAuthen jsoupAuthen = null;
    String authenMsg = "";
    boolean pass = false;
    Preference login = null;
    Preference userNameEdit = null;
    Preference passwordEdit = null;
    String loginMsg = null;
    String userName = "";
    String password = "";
    EditTextPreference loginMsgEditText = null;
    THAIVI thaiVI = null;
    boolean isOnline = false;
    boolean onrotaion=true;
    private static LogUtils log = new LogUtils("FeedPrefActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        log.debug("onrotation["+onrotaion+"]");
        if(onrotaion){

            context = this;

            //Open DB Connection
            mDbFeedAdapter = new DBAdapter(this);
            mDbFeedAdapter.open();

            //Set Title Name
            CharSequence title = getString(R.string.pref_name);
            setTitle(title);

            isOnline = NetworkUtils.isOnline(this);

            addPreferencesFromResource(R.xml.settings);

            //Link to Thaivi member
            Preference thaiviMemberPref = findPreference("thaivi_member");
            thaiviMemberPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(FeedPrefActivity.this, ThaviMemberPrefActivity.class);
                    // intent.putExtra("listViewTop", listViewTop);
                    startActivity(intent);
                    return true;
                }
            });


            //Set Clear Data All
            Preference clearCachePref = findPreference("clearCache");
            clearCachePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getBaseContext(), "Clear Cache ",Toast.LENGTH_LONG).show();
                    try{

                        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
                        File pathFolder = new File(extStorageDirectory +"/"+ Constants.APP_FOLDER+"/"+Constants.APP_FOLDER_IMAGES);
                        if( pathFolder.exists()){
                            log.debug("Delete Folder["+extStorageDirectory +"/"+ Constants.APP_FOLDER+"]:"+pathFolder.delete());
                            if (pathFolder != null && pathFolder.isDirectory()) {
                                FileUtil.deleteDir(pathFolder);
                            }
                        }
                        Toast.makeText(getBaseContext(), "Clear Cache เรียบร้อยแล้ว",Toast.LENGTH_LONG).show();

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    return true;
                }
            });

            //Set Clear Data All
            Preference clearPref = findPreference("clearDataAll");
            clearPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getBaseContext(), "กำลัง ลบข้อมูลทั้งหมด กรุณาราสักครู่ ",Toast.LENGTH_LONG).show();
                    try{
                        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
                        File pathFolder = new File(extStorageDirectory +"/"+ Constants.APP_FOLDER);
                        if( pathFolder.exists()){
                            log.debug("Delete Folder["+extStorageDirectory +"/"+ Constants.APP_FOLDER+"]:"+pathFolder.delete());
                            if (pathFolder != null && pathFolder.isDirectory()) {
                                FileUtil.deleteDir(pathFolder);
                            }
                        }

                        // Drop table and create table
                        mDbFeedAdapter.truncate();

                        //initail table and data
                        JSoupHelperNoAuthen jsoup = new JSoupHelperNoAuthen();
                        List<Feed> resourceFeeds = jsoup.getFeedCatalogsFromDropboxDB(null);// FeedXMLHelper.getOPMLResourceFeedsModelWeb(context,0,null);
                        for(Feed f:resourceFeeds){
                            mDbFeedAdapter.addFeed(f);
                        }

                        Toast.makeText(getBaseContext(), "ลบข้อมูลทั้งหมด เรียบร้อยแล้ว",Toast.LENGTH_LONG).show();

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    return true;
                }
            });

            //Show About
            Preference showAbout = findPreference("showAbout");
            showAbout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    try{
                        showDialog(SharedPreferencesHelper.DIALOG_ABOUT);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    return true;
                }
            });

        }
    }

    //Called when device orientation changed (see: android:configChanges="orientation" in manifest file)
    //Avoiding to restart the activity (which causes a crash) when orientation changes during refresh in AsyncTask
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        onrotaion=false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //TrackerAnalyticsHelper.startTracker(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TrackerAnalyticsHelper.trackPageView(this,"/preferenceView");
    }

    @Override
    protected void onStop() {
        super.onStop();
        //TrackerAnalyticsHelper.stopTracker(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbFeedAdapter.close();
        //saveSettings();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        log.debug("KeyCode:"+keyCode+",:backCode:"+KeyEvent.KEYCODE_BACK);
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            backAction();
            return true;
        }
        return false;
    }

    public void backAction(){

        Intent intent = new Intent(FeedPrefActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        CharSequence title = null;
        LayoutInflater inflater = null;
        View dialogLayout = null;
        AlertDialog.Builder builder = null;
        log.debug("Dialog id["+id+"]:"+SharedPreferencesHelper.DIALOG_UPDATE_PROGRESS);
        switch (id) {
            case SharedPreferencesHelper.DIALOG_UPDATE_PROGRESS:
                dialog = new ProgressDialog(this);
                dialog.setTitle(getResources().getText(R.string.loading));
                // ((ProgressDialog)dialog).setIcon(R.drawable.ic_dialog);
                ((ProgressDialog)dialog).setMessage(getResources().getText(R.string.downloading));
                ((ProgressDialog)dialog).setIndeterminate(true);
                dialog.setCancelable(false);
                break;
            case SharedPreferencesHelper.DIALOG_ABOUT:
                //title = getResources().getText(R.string.app_name) + " - " + getResources().getText(R.string.version) + " " + SharedPreferencesHelper.getVersionName(this);
                title = getString(R.string.app_name) + " " + getString(R.string.version) + " ";// + SharedPreferencesHelper.getVersionName(this);

                inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                dialogLayout = inflater.inflate(R.layout.dialog_about, null);
                TextView childView = null;
                if (getString(R.string.website).equals("")) {
                    childView = (TextView) dialogLayout.findViewById(R.id.website);
                    childView.setVisibility(View.GONE);
                }
                if (getString(R.string.email).equals("")) {
                    childView = (TextView) dialogLayout.findViewById(R.id.email);
                    childView.setVisibility(View.GONE);
                }
                if (getString(R.string.contact).equals("")) {
                    childView = (TextView) dialogLayout.findViewById(R.id.contact);
                    childView.setVisibility(View.GONE);
                }
                if (getString(R.string.powered).equals("")) {
                    childView = (TextView) dialogLayout.findViewById(R.id.powered);
                    childView.setVisibility(View.GONE);
                }
                builder = new AlertDialog.Builder(this);
                builder.setView(dialogLayout)
                        .setTitle(title)
                                //.setIcon(R.drawable.ic_dialog)
                        .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                dialog = builder.create();
                break;
            default:
                dialog = null;
        }
        return dialog;
    }
}
