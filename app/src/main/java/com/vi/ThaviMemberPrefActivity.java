
package com.vi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vi.common.Feed;
import com.vi.parser.JSoupHelperAuthen;
import com.vi.storage.SharedPreferencesHelper;
import com.vi.utils.LogUtils;
import com.vi.utils.NetworkUtils;
import com.vi.utils.Utils;


public class ThaviMemberPrefActivity extends Activity implements OnClickListener  {

    private boolean customTitleSupported;
    private Button backBtn;
    private Button loginBtn;
    private Button logoutBtn;
    private TextView loginMsgTextView;
    private TextView userNameView;
    private TextView passwordView;
    ProgressDialog dialog;
    //Setting Control
    JSoupHelperAuthen jsoupAuthen = null;
    int retryConnect = 2;
    boolean isOnline = false;
    boolean onrotaion=true;
    THAIVI thaiVI = null;
    public SharedPreferences settingS = null;
    SharedPreferences.Editor editor = null;
    private String userName ="";
    private String password ="";
    private String loginMsg ="";
    private static LogUtils log = new LogUtils("ThaiviMemberPrefActivity");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //log.debug("onrotation["+onrotaion+"]");
        if(onrotaion){
            //log.debug("MainActivity:OnCreate");
            //Allow Policy Thread
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            //Check internet online
            isOnline = NetworkUtils.isOnline(this);

            //Load Global Variable
            thaiVI  = ((THAIVI) this.getApplication());

            //Load Setting
            settingS = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
            editor = settingS.edit();

            setContentView(R.layout.thaivi_member_layout);
            loginMsgTextView = (TextView) findViewById(R.id.loginMsgText);
            userNameView = (TextView) findViewById(R.id.username);
            passwordView = (TextView) findViewById(R.id.password);

            loginBtn = (Button)findViewById(R.id.login);
            logoutBtn = (Button)findViewById(R.id.logout);
            backBtn = (Button)findViewById(R.id.backBtn);

            loginBtn.setOnClickListener(this);
            logoutBtn.setOnClickListener(this);
            backBtn.setOnClickListener(this);

            userName = Utils.isNull(settingS.getString("username",""));
            password = Utils.isNull(settingS.getString("password",""));
            loginMsg = Utils.isNull(settingS.getString("loginmsg", ""));

            if(loginMsg.equals("login_pass")){
                loginMsgTextView.setText(getString(R.string.login_action_success));
                userNameView.setText(userName);
                passwordView.setText(password);
            }else{
                loginMsgTextView.setText(getString(R.string.login_action_no));
            }

        }
    }

    public void backAction(){
        Intent intent = new Intent(ThaviMemberPrefActivity.this, FeedPrefActivity.class);
        // intent.putExtra("listViewTop", listViewTop);
        startActivity(intent);
    }

    public void loginAction(){
        //Login new Start Application
        if(isOnline ){
            //check is logined and Exit and Open again  Relogin
            boolean passAuthen = true;
            try{
                userName = userNameView.getText().toString();
                password = passwordView.getText().toString();

                jsoupAuthen = JSoupHelperAuthen.newInstance(userName, password);
                passAuthen = jsoupAuthen.verifyAuthen();

                //log.debug("passAuthen:"+passAuthen);

                if( !passAuthen){
                    loginMsgTextView.setText(getString(R.string.login_action_fail));
                    String authenMsg = " UserName["+userName+"] และ  Password[*****]ของ(Thaivi.org)ไม่ถูกต้อง หรือไม่มีสิทธิเข้าห้องสมาชิกสมาคม ";
                    Toast.makeText(ThaviMemberPrefActivity.this, authenMsg, Toast.LENGTH_LONG).show();
                    authenMsg = "";
                    JSoupHelperAuthen.jsoupHelperAuthen = null;
                    jsoupAuthen = null;
                }else{
                    loginMsgTextView.setText(getString(R.string.login_action_success));
                    editor.putString("username", userName);
                    editor.putString("password", password);
                    editor.putString("loginmsg", "login_pass");
                    editor.commit();

                    String authenMsg = "Login เรียบร้อยแล้ว ";
                    Toast.makeText(ThaviMemberPrefActivity.this, authenMsg, Toast.LENGTH_LONG).show();
                }

                //set to Global
                thaiVI.setThaiviAuthen(jsoupAuthen);
                // log.debug("Relogin jsoupAuthen[Global]:"+jsoupAuthen);

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void logoutAction(){
        try{
            userNameView.setText("");
            passwordView.setText("");

            editor.putString("username", "");
            editor.putString("password", "");
            editor.putString("loginmsg", "");
            editor.commit();

            thaiVI.setThaiviAuthen(null);

            String authenMsg = "Logout เรียบร้อยแล้ว ";
            Toast.makeText(ThaviMemberPrefActivity.this, authenMsg, Toast.LENGTH_LONG).show();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if(backBtn.equals(v)){
            backAction();
        }else if(loginBtn.equals(v)){
            loginAction();
        }else if(logoutBtn.equals(v)){
            logoutAction();
        }
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(dialog != null){
            dialog.dismiss();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        //mDbFeedAdapter.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mDbFeedAdapter.close();
        //Toast.makeText(MainActivity.this, "onDestroy()", Toast.LENGTH_SHORT).show();
    }

    //Called when device orientation changed (see: android:configChanges="orientation" in manifest file)
    //Avoiding to restart the activity (which causes a crash) when orientation changes during refresh in AsyncTask
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);

        //Change flag  here 
        onrotaion=false;

        //Toast.makeText(MainActivity.this,  "onConfigurationChanged(): " + newConfig.toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

}