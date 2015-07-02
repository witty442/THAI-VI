
package com.vi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.vi.adapter.MainAdapter;
import com.vi.common.Catalog;
import com.vi.common.Control;
import com.vi.common.Feed;
import com.vi.parser.JSoupHelperAuthen;
import com.vi.storage.DBAdapter;
import com.vi.storage.SharedPreferencesHelper;
import com.vi.utils.Constants;
import com.vi.utils.FileUtil;
import com.vi.utils.LogUtils;
import com.vi.utils.NetworkUtils;
import com.vi.utils.Utils;


public class MainActivity extends Activity implements OnItemClickListener,OnClickListener  {

    private boolean customTitleSupported;
    private Button backBtn;
    private Button configBtn;

    ProgressDialog dialog;
    ListView mainListView;
    private ExtViewFlow viewFlow;

    //Setting Control
    private String currentBgColor = "bg_color_pantip";
    private String currentFontSize = "19";
    String loginMsg = "";
    private String userName ="";
    private String password ="";
    JSoupHelperAuthen jsoupAuthen = null;
    int retryConnect = 2;
    boolean isOnline = false;
    boolean onrotaion=true;
    THAIVI thaiVI = null;
    String startAppCheck = "";
    int mSelectedItem = -1;
    MainAdapter arrayItemFavAdapter = null;
    LogUtils log = new LogUtils("MainActivity");

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
            //Load Setting
            SharedPreferencesHelper settingS = new SharedPreferencesHelper(this);
            currentFontSize = settingS.getSetting("textSize","18");
            userName = Utils.isNull(settingS.getSetting("username",""));
            password = Utils.isNull(settingS.getSetting("password",""));
            loginMsg = Utils.isNull(settingS.getSetting("loginmsg", ""));

            isOnline = NetworkUtils.isOnline(this);

	    	/*log.debug("currentFontSize:"+currentFontSize);
	    	log.debug("username:"+userName);
	    	log.debug("password:"+password);
	    	log.debug("loginMsg:"+loginMsg);
	    	log.debug("isOnline:"+isOnline);*/

            Bundle extras = getIntent().getExtras(); // Case Pass Param From Another Activity
            startAppCheck = "";
            if(extras != null){
                startAppCheck = extras.getString("START_APP");
                //log.debug("startAppCheck:"+startAppCheck);
            }

            //Load Global Variable
            thaiVI  = ((THAIVI)this.getApplication());

            //Login new Start Application
            new LoginTask().execute();

            /** Set Custom Title Bar **/
            customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
            setContentView(R.layout.swipe_main_layout);
            customTitleBar("THAI-VI");

            viewFlow = (ExtViewFlow) findViewById(R.id.viewflow);
            ExtDiffMainViewAdapter adapter = new ExtDiffMainViewAdapter(this);
            viewFlow.setAdapter(adapter);

            ExtTitleFlowIndicator indicator = (ExtTitleFlowIndicator) findViewById(R.id.viewflowindic);
            indicator.setTitleProvider(adapter);
            viewFlow.setFlowIndicator(indicator);

            /** Set Catagory */
            mainListView = (ListView) findViewById(R.id.main_list);
            backBtn =(Button)findViewById(R.id.controlBackBtn);
            configBtn =(Button)findViewById(R.id.controlConfigBtn);

            /** register menu **/
            registerForContextMenu(mainListView);

            /** register itemListener **/
            mainListView.setOnItemClickListener(this);
            backBtn.setOnClickListener(this);
            configBtn.setOnClickListener(this);

            //initail list view
            initData();
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

    public void backActionBK(){
        new AlertDialog.Builder(this)
                .setTitle("ออกจากโปรแกรม")
                .setMessage("คุณต้องการออกจากโปรแกรม ")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        //Clear Cache (images)
                        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
                        File pathFolder = new File(extStorageDirectory +"/"+ Constants.APP_FOLDER+"/"+Constants.APP_FOLDER_IMAGES);
                        if( pathFolder.exists()){
                            //log.debug("Delete Folder["+pathFolder+"]");
                            if (pathFolder != null && pathFolder.isDirectory()) {
                                FileUtil.deleteDir(pathFolder);
                            }
                        }

                        //Control
                        moveTaskToBack(true);
                        System.exit(0);
                        finish();
                    }})

                .setNegativeButton(android.R.string.no, null).show();
    }

    public void backAction(){
        //Clear Cache (images)
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        File pathFolder = new File(extStorageDirectory +"/"+ Constants.APP_FOLDER+"/"+Constants.APP_FOLDER_IMAGES);
        if( pathFolder.exists()){
            //log.debug("Delete Folder["+pathFolder+"]");
            if (pathFolder != null && pathFolder.isDirectory()) {
                FileUtil.deleteDir(pathFolder);
            }
        }

        //Control
        moveTaskToBack(true);
        System.exit(0);
        finish();
    }

    @Override
    public void onClick(View v) {
        if(backBtn.equals(v)){
            backAction();
        }else if(configBtn.equals(v)){
            startActivity(new Intent(this,FeedPrefActivity.class));
        }
    }

    public void initData(){
        DBAdapter mDbFeedAdapter = null;
        try{
            mDbFeedAdapter = new DBAdapter(this);
            mDbFeedAdapter.open();

            if(mainListView != null ){
                //init get Item All to ArList
                List<Catalog> allFavList =  new ArrayList<Catalog>();
                Catalog c = new Catalog();
                c.setId(1);
                c.setTitle("บทความจาก เซียนหุ้นคุณค่า");
                c.setType(Constants.FEED_TYPE_ARTICLE);
                c.setAuthor("");
                List<Feed> feeds = mDbFeedAdapter.getFeeds(Constants.FEED_TYPE_ARTICLE);
                if(feeds != null){
                    c.setTotalItem(feeds.size());
                }
                allFavList.add(c);

                c = new Catalog();
                c.setId(2);
                c.setTitle("เว็บบอร์ด www.thaivi.org");
                c.setType(Constants.FEED_TYPE_BORAD);
                c.setAuthor("Thaivi.org");
                feeds = mDbFeedAdapter.getFeeds(Constants.FEED_TYPE_BORAD);
                if(feeds != null){
                    c.setTotalItem(feeds.size());
                }
                allFavList.add(c);

                c = new Catalog();
                c.setId(7);
                c.setTitle("คลังมัลติมีเดีย");
                c.setType(Constants.FEED_TYPE_VIDEO);
                c.setAuthor("Thaivi.org");
                feeds = mDbFeedAdapter.getFeeds(Constants.FEED_TYPE_VIDEO);
                if(feeds != null){
                    c.setTotalItem(feeds.size());
                }
                allFavList.add(c);

                c = new Catalog();
                c.setId(4);
                c.setTitle("บทความที่บันทึกไว้");
                c.setType(Constants.FEED_TYPE_ARTICLE);
                c.setAuthor("");
                c.setTotalItem(mDbFeedAdapter.getCountItemByType(Constants.FEED_TYPE_ARTICLE,true,false));
                c.setTopten("10");
                c.setFav(true);
                allFavList.add(c);

                c = new Catalog();
                c.setId(3);
                c.setTitle("กระทู้ที่บุ๊คมาร์คไว้");
                c.setType(Constants.FEED_TYPE_BORAD);
                c.setAuthor("");
                c.setTotalItem(mDbFeedAdapter.getCountItemByType(Constants.FEED_TYPE_BORAD,false,true));
                c.setTopten("10");
                c.setFav(true);
                allFavList.add(c);

                c = new Catalog();
                c.setId(6);
                c.setTitle("หุ้นที่บุ๊คมาร์คไว้ (ห้องร้อยคนร้อยหุ้น สมาชิกสมาคม)");
                c.setType(Constants.FEED_TYPE_BORAD_100_NEW);
                c.setAuthor("");
                c.setTotalItem(mDbFeedAdapter.getCountItemByType(Constants.FEED_TYPE_BORAD_100_NEW,false,true));
                c.setTopten("10");
                c.setFav(true);
                allFavList.add(c);

                c = new Catalog();
                c.setId(5);
                c.setTitle("หุ้นที่บุ๊คมาร์คไว้ (ห้องร้อยคนร้อยหุ้น สมาชิกทั่วไป)");
                c.setType(Constants.FEED_TYPE_BORAD_100);
                c.setAuthor("");
                c.setTotalItem(mDbFeedAdapter.getCountItemByType(Constants.FEED_TYPE_BORAD_100,false,true));
                c.setTopten("10");
                c.setFav(true);
                allFavList.add(c);

                Control control = ControlHelper.getControlContent(this, currentFontSize, currentBgColor,0);
                arrayItemFavAdapter = new MainAdapter(this, R.id.title, allFavList,control);
                mainListView.setAdapter(arrayItemFavAdapter);
            }

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            mDbFeedAdapter.close();
        }
    }

    public void customTitleBar(String text) {
        // set up custom title
        if (customTitleSupported) {
            Window window= getWindow();
            window.setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.titlebar_main);
            Button leftButton = (Button) findViewById( R.id.controlBackBtn);
            Button titleCenter = (Button) findViewById( R.id.titleCenter);

            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();  // desprecated
            int oneBox = display.getWidth()/10;
            int left = oneBox*1;//40
            int center = oneBox*7;//240
            int right = oneBox*1;//40

            //log.debug("Window width:"+width);
            //log.debug("text:"+text.length());

            //leftButton.setWidth(left+20);
            titleCenter.setWidth(center+10);
            //imageRight.getLayoutParams().width = right;
            titleCenter.setText(text);

            //Set Custom Font
            Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Antic.ttf");
            // titleCenter.setTypeface(tf,Typeface.BOLD);
            // leftButton.setTypeface(tf,Typeface.BOLD);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
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

    //Click Item View Content
    public void onItemClick (AdapterView<?> parent, View v, int position, long id) {
        log.debug("onItemClick:position["+position+"]");

        arrayItemFavAdapter.setSelectItem(position);
        arrayItemFavAdapter.notifyDataSetChanged();


        TextView titleView = (TextView) v.findViewById(R.id.title);
        TextView topten = (TextView) v.findViewById(R.id.topten);
        TextView topicTypeView = (TextView) v.findViewById(R.id.topicType);

        //log.debug("topicTypeView["+topicTypeView.getText().toString() +"]topten["+topten.getText().toString()+"]");
        if(Constants.FEED_TYPE_BORAD_100.equalsIgnoreCase(topicTypeView.getText().toString())
                || Constants.FEED_TYPE_BORAD_100_NEW.equalsIgnoreCase(topicTypeView.getText().toString())){

            Intent intent = new Intent(MainActivity.this, FeedMainItemLocal100Activity.class);
            intent.putExtra("FEED_TITLE",titleView.getText().toString());
            intent.putExtra("FEED_TYPE",topicTypeView.getText().toString());

            startActivity(intent);
        }else{
            if( "10".equalsIgnoreCase(topten.getText().toString())){
                Intent intent = new Intent(MainActivity.this, FeedMainItemLocalActivity.class);
                intent.putExtra("FEED_TITLE",titleView.getText().toString());
                intent.putExtra("FEED_TYPE",topicTypeView.getText().toString());
                startActivity(intent);
            }else{
                Intent intent = new Intent(MainActivity.this, FeedMainActivity.class);
                intent.putExtra("FEED_TYPE",topicTypeView.getText());

                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(dialog != null){
            dialog.dismiss();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }


    private class LoginTask extends AsyncTask<Feed, Void, Boolean> implements OnDismissListener{

        public LoginTask() {
            super();
        }
        protected Boolean doInBackground(Feed...params) {
            try {
                if(isOnline && "TRUE".equals(startAppCheck) && loginMsg.equals("login_pass")){
                    //check is logined and Exit and Open again  Relogin
                    boolean passAuthen = true;
                    try{
                        jsoupAuthen = JSoupHelperAuthen.newInstance(userName, password);
                        passAuthen = jsoupAuthen.verifyAuthen();

                        //log.debug("passAuthen:"+passAuthen);

                        if( !passAuthen){
                            String authenMsg = " UserName["+userName+"] และ  Password[*****]ของ(Thaivi.org)ไม่ถูกต้อง หรือไม่มีสิทธิ   passAuthen["+passAuthen+"]";
                            //Toast.makeText(MainActivity.this, authenMsg, Toast.LENGTH_LONG).show();
                            authenMsg = "";
                            JSoupHelperAuthen.jsoupHelperAuthen = null;
                            jsoupAuthen = null;
                        }

                        //set to Global
                        thaiVI.setThaiviAuthen(jsoupAuthen);
                        // log.debug("Relogin jsoupAuthen[Global]:"+jsoupAuthen);

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            return new Boolean(true);
        }

        protected void onPreExecute() {
            showDialog(SharedPreferencesHelper.DIALOG_UPDATE_PROGRESS);
        }

        protected void onPostExecute(Boolean result) {
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
        }
    }
}