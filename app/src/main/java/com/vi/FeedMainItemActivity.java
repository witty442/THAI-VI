
package com.vi;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.vi.adapter.FeedMainItemAdapter;
import com.vi.common.Control;
import com.vi.common.Feed;
import com.vi.common.Item;
import com.vi.parser.JSoupHelperAuthen;
import com.vi.parser.JSoupHelperNoAuthen;
import com.vi.storage.DBAdapter;
import com.vi.storage.DBSchema;
import com.vi.storage.SharedPreferencesHelper;
import com.vi.utils.Constants;
import com.vi.utils.FileUtil;
import com.vi.utils.LogUtils;
import com.vi.utils.NetworkUtils;
import com.vi.utils.PhoneProperty;
import com.vi.utils.Utils;

public class FeedMainItemActivity extends Activity implements OnItemClickListener ,OnClickListener,OnItemLongClickListener {

    private ProgressDialog dialog;
    private ProgressDialog dialogDownload;
    private DBAdapter mDbFeedAdapter;
    private Feed currentFeed;
    private Item currentItem;
    private String action = "";
    private ExtViewFlow viewFlow;
    private ExtDiffItemViewAdapter adapter;
    private boolean customTitleSupported;
    private String currentDateSort = Constants.DB_SORT_DESC;
    private Button controlMoreBtn;
    private Button backBtn;
    private Button controlCurrentBtn;
    private Button controlPrevBtn;
    private Button controlNextBtn;
    private Button controlRefreshBtn;
    int topicCurPage = 1;
    int itemsPerPage =  Constants.MAX_ITEMS_PER_PAGE;
    View footerView;
    private boolean dialogShow = false;
    List<Item> allArticleList ;
    List<Item> allLocalList ;
    FeedMainItemAdapter arrayItemAdapter;
    FeedMainItemAdapter arrayItemLocalAdapter;

    //Setting Control
    private String currentBgColor = "bg_color_pantip";
    private String currentFontSize = "18";

    ListView articleListView ;
    ListView localViewList;

    private PopupWindow popWindow;
    private PopupWindow popControlWindow;
    private PopupWindow popMainWindow;

    private int listViewIndex = 0;
    private int listViewTop = 0;
    //Prev Position ListView FeedMainActivity
    private int listViewIndex_FeedMain = 0;
    private int listViewTop_FeedMain = 0;

    private String userName ="";
    private String password ="";
    THAIVI thaiVI = null;
    private JSoupHelperAuthen jsoupAuthen = null;
    Map<String ,Object> params = new HashMap<String ,Object>();
    private String authenMsg = "";
    boolean verifyLogin = true;
    String loginMsg = "";
    boolean isOnline = false;
    private PhoneProperty phoneProperty = null;
    boolean onrotaion=true;
    LogUtils log = new LogUtils("FeedMainItemActivity");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // log.debug("onrotation["+onrotaion+"]");
        if(onrotaion){
            //log.debug("FeedMainItemActivity:OnCreate");

            //Allow Policy Thread
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            //Load Phone Preperty
            phoneProperty = new PhoneProperty(this);

            //Load Global Variable
            thaiVI  = ((THAIVI) this.getApplication());
            jsoupAuthen = thaiVI.getThaiviAuthen();

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
	    	log.debug("isOnline:"+isOnline);
	    	log.debug("Phone Width:"+phoneProperty.width+",height:"+phoneProperty.height);*/

            /** Set Custom Title Bar **/
            customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
            setContentView(R.layout.swipe_main_item_layout);

            Bundle extras = getIntent().getExtras(); // Case Pass Param From Another Activity
            if (extras != null) {
                currentFeed = extras.get("FEED")!= null?(Feed) extras.get("FEED"):null;
                action =  extras.getString("ACTION");
                topicCurPage = extras.getInt("TOPIC_CUR_PAGE")==0?1:extras.getInt("TOPIC_CUR_PAGE");

                if(currentFeed !=null)
                    customTitleBar(currentFeed.getTitle());

                listViewIndex = extras.getInt("listViewIndex");
                listViewTop = extras.getInt("listViewTop");

                listViewIndex_FeedMain = extras.getInt("listViewIndex_FeedMain");
                listViewTop_FeedMain = extras.getInt("listViewTop_FeedMain");

                //log.debug("listViewIndex:"+listViewIndex);
                //log.debug("listViewTop:"+listViewTop);
                //log.debug("listViewIndex_FeedMain:"+listViewIndex_FeedMain);
                //log.debug("listViewTop_FeedMain:"+listViewTop_FeedMain);

                //log.debug("FeedMainItemActivity:OnCreate listViewIndex["+listViewIndex+"]listViewTop["+listViewTop+"]");
                //log.debug("FeedID:"+currentFeed.getId());
                //log.debug("FeedType:"+currentFeed.getType());
                //log.debug("topicCurPage:"+topicCurPage);
                //log.debug("currentFeed.getTitle():"+currentFeed.getTitle());
            }
            String[] names = new String[2];
            if(Constants.FEED_TYPE_BORAD.equalsIgnoreCase(currentFeed.getType()) ||
                    Constants.FEED_TYPE_BORAD_100.equalsIgnoreCase(currentFeed.getType()) ||
                    Constants.FEED_TYPE_BORAD_100_NEW.equalsIgnoreCase(currentFeed.getType())){

                names[0] = "Article";
                names[1] = "MyBookmark";
            }else{
                names[0] = "Article";
                names[1] = "MyLocal";
            }

            viewFlow = (ExtViewFlow) findViewById(R.id.viewflow);
            adapter = new ExtDiffItemViewAdapter(this,names);
            viewFlow.setAdapter(adapter);

            ExtTitleFlowIndicator indicator = (ExtTitleFlowIndicator) findViewById(R.id.viewflowindic);
            indicator.setTitleProvider(adapter);
            viewFlow.setFlowIndicator(indicator);

            /** Open DataBase **/
            mDbFeedAdapter = new DBAdapter(this,currentFeed.getId(),null);
            mDbFeedAdapter.open();

            /** Load more property CurrentFeed **/
            currentFeed = mDbFeedAdapter.getFeed(currentFeed.getId(),false,currentFeed.getType());

            /** Set Back Button **/
            backBtn =(Button)findViewById(R.id.controlBackBtn);
            controlMoreBtn =(Button)findViewById(R.id.controlConfigBtn);
            controlRefreshBtn = (Button)findViewById(R.id.controlRefreshBtn);

            backBtn.setOnClickListener(this);
            controlMoreBtn.setOnClickListener(this);
            controlRefreshBtn.setOnClickListener(this);

            /******************* Article ********************************************************/
            articleListView = (ListView) findViewById(R.id.feed_article_list);
            //register menu
            registerForContextMenu(articleListView);
            //register itemListener
            articleListView.setOnItemClickListener(this);
            articleListView.setOnItemLongClickListener(this);
            /*********************************************************************/

            /************ Set Video************************************************/
            localViewList = (ListView) findViewById(R.id.feed_local_list);
            //register menu
            registerForContextMenu(localViewList);
            //register itemListener
            localViewList.setOnItemClickListener(this);
            localViewList.setOnItemLongClickListener(this);
            /***********************************************************/

            boolean isOnline = NetworkUtils.isOnline(this);
            if( !isOnline){
                if( !Constants.FEED_TYPE_ARTICLE.equals(currentFeed.getType())){
                    Toast.makeText(FeedMainItemActivity.this, getResources().getString(R.string.no_internet_msg), Toast.LENGTH_LONG).show();
                }
            }

            /** case Relogin ThaiVI **/
            if(isOnline && loginMsg.equals("login_pass") && jsoupAuthen==null){
                //check is logined and Exit and Open again  Relogin
                boolean passAuthen = true;
                try{
                    jsoupAuthen = JSoupHelperAuthen.getInstance(userName, password);
                    passAuthen = jsoupAuthen.verifyAuthen();
                    //log.debug("passAuthen:"+passAuthen);

                    if( !passAuthen){
                        String authenMsg = " UserName["+userName+"] และ  Password[********]ของ(Thaivi.org)ไม่ถูกต้อง หรือไม่มีสิทธิ ";
                        Toast.makeText(FeedMainItemActivity.this, authenMsg, Toast.LENGTH_LONG).show();
                        authenMsg = "";
                        JSoupHelperAuthen.jsoupHelperAuthen = null;
                        jsoupAuthen = null;
                    }
                    //set to Global
                    ((THAIVI) this.getApplication()).setThaiviAuthen(jsoupAuthen);
                    //log.debug("Relogin jsoupAuthen[Global]:"+jsoupAuthen);

                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            //Check UserName and Password ThaiVi and Internet Connection
            boolean verifyLogin = true;
            if( Constants.FEED_TYPE_BORAD_100_NEW.equals(currentFeed.getType()) && jsoupAuthen == null){
                authenMsg = getResources().getString(R.string.no_authen_thaivi_msg);
                verifyLogin = false;
            }

            /** check database exist **/
		   /* log.debug("Feed Item:"+currentFeed.getTotalItem());
		    log.debug("Action In Activity["+action+"]");
		    log.debug("jsoupAuthen:"+jsoupAuthen);
		    log.debug("verifyLogin:"+verifyLogin);
		    log.debug("FeedOrgLink:"+currentFeed.getOrgLink());
			log.debug("FeedLink:"+currentFeed.getLink());*/

            if("NEW".equalsIgnoreCase(action)){
                boolean isFeedExist = mDbFeedAdapter.isFeedItemExist(currentFeed.getId());
                //log.debug("FeedId["+currentFeed.getId()+"]Type["+currentFeed.getType()+"]isFeedExist["+isFeedExist+"]");
                if(isFeedExist){
                    if(verifyLogin ==true){
                        if( Constants.FEED_TYPE_BORAD.equals(currentFeed.getType()) ||
                                (Constants.FEED_TYPE_BORAD_100.equals(currentFeed.getType()) && currentFeed.getLink().indexOf("dropbox")== -1) ||
                                (Constants.FEED_TYPE_BORAD_100_NEW.equals(currentFeed.getType()) && currentFeed.getLink().indexOf("dropbox")== -1)
                                ){
                            new UpdateFeedTask().execute(currentFeed);
                        }else{
                            addButtonLoadMoreInListView();
                            refreshListAllView(currentDateSort);
                        }
                    }
                }else{
                    if(verifyLogin ==true){
                        new UpdateFeedTask().execute(currentFeed);
                    }
                }
            }else{
                if(verifyLogin ==true){
                    addButtonLoadMoreInListView();
                    refreshListAllView(currentDateSort);
                }
            }

            //Alert Authen Msg
            if( !Utils.isNull(authenMsg).equals("")){
                Toast.makeText(FeedMainItemActivity.this, authenMsg, Toast.LENGTH_LONG).show();
            }
            authenMsg = "";

        }//check Rotation 
    }

    public void customTitleBar(String text) {
        // set up custom title
        if (customTitleSupported) {
            Window window= getWindow();
            window.setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.titlebar_items);
            Button leftButton = (Button) findViewById( R.id.controlBackBtn);
            Button titleCenter = (Button) findViewById( R.id.titleCenter);

            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();  // deprecated
            int oneBox = display.getWidth()/10;
            int left = oneBox*1;//40
            int center = oneBox*7;//240
            int right = oneBox*1;//40

            // log.debug("Window width:"+width);
            //  log.debug("text:"+text.length());

            //leftButton.setWidth(left+20);
            titleCenter.setWidth(center+10);
            titleCenter.setText(text);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        //log.debug("OnResume");
        super.onResume();
        if(dialog != null){
            dialog.dismiss();
        }
        if(dialogShow){
            dialog.dismiss();
            dialogShow = false;
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
        mDbFeedAdapter.close();
    }

    @Override
    public void onClick(View v) {
        //log.debug("View:"+v);
        if(backBtn != null && backBtn.equals(v)){
            backAction();
        }else if(controlNextBtn != null && controlNextBtn.equals(v)){
            //Reset Position ListView
            listViewIndex = 0;
            listViewTop = 0;

            nextPageTopic();
        }else if(controlPrevBtn != null && controlPrevBtn.equals(v)){
            //Reset Position ListView
            listViewIndex = 0;
            listViewTop = 0;

            prevPageTopic();
        }else if(controlMoreBtn != null && controlMoreBtn.equals(v)){
            displayPopupControl();
        }else if(controlRefreshBtn.equals(v)){
            if(verifyLogin){
                refreshPageTopic();
            }
            //Alert Authen Msg
            if( !Utils.isNull(authenMsg).equals("")){
                Toast.makeText(FeedMainItemActivity.this, authenMsg, Toast.LENGTH_LONG).show();
                authenMsg ="";
            }
        }
    }

    private int getDisplayPopControl(){
        int w = 300;
        if( phoneProperty.width-100 > 400){
            //big screen
            w = 400;
        }
        return w;
    }

    public void displayPopupControl(){
        //log.debug("displayPopupControl");
        //popupControl_flag = true;

        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        TableLayout viewGroup = (TableLayout) findViewById(R.id.popup_list_control_id);
        View layout = layoutInflater.inflate(R.layout.popup_control, viewGroup);

        popControlWindow = new PopupWindow(layout,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        popControlWindow.setBackgroundDrawable(new BitmapDrawable());
        popControlWindow.setOutsideTouchable(true);
        popControlWindow.setFocusable(true);
        popControlWindow.setTouchable(true);

        int y = 150;
        int x = getWindowManager().getDefaultDisplay().getWidth()-150;

        //log.debug("x["+x+"]y["+y+"]");
        popControlWindow.showAtLocation(layout, Gravity.TOP, x, y);

        TableRow tableRowSearchId = (TableRow)layout.findViewById(R.id.tableRowSearchId);
        TableRow tableRowSaveId = (TableRow)layout.findViewById(R.id.tableRowSaveId);
        TableRow tableRowBookmarkId = (TableRow)layout.findViewById(R.id.tableRowBookmarkId);
        TableRow tableRowDelId = (TableRow)layout.findViewById(R.id.tableRowDelId);
        TableRow tableRowRefreshId = (TableRow)layout.findViewById(R.id.tableRowRefreshId);
        TableRow tableRowTopId = (TableRow)layout.findViewById(R.id.tableRowTopId);
        TableRow tableRowBottomId = (TableRow)layout.findViewById(R.id.tableRowBottomId);
        TableRow tableRowHomeId = (TableRow)layout.findViewById(R.id.tableRowHomeId);
        TableRow tableRowAnnounceId = (TableRow)layout.findViewById(R.id.tableRowAnnounceId);

        tableRowRefreshId.setVisibility(View.GONE);
        if( Constants.FEED_TYPE_BORAD_100.equals(currentFeed.getType()) ||
                Constants.FEED_TYPE_BORAD_100_NEW.equals(currentFeed.getType())){
            tableRowAnnounceId.setVisibility(View.GONE);
        }else{
            tableRowSearchId.setVisibility(View.GONE);
        }

        //Hide
        tableRowSaveId.setVisibility(View.GONE);
        tableRowBookmarkId.setVisibility(View.GONE);
        tableRowDelId.setVisibility(View.GONE);
        tableRowTopId.setVisibility(View.GONE);
        tableRowBottomId.setVisibility(View.GONE);

        Button searchButton = (Button)layout.findViewById(R.id.controlSearchBtn);
        Button searchButtonImg = (Button)layout.findViewById(R.id.controlSearchBtn_Temp);
        Button saveButton = (Button)layout.findViewById(R.id.controlSaveBtn);
        Button saveButtonImg = (Button)layout.findViewById(R.id.controlSaveBtn_Temp);
        Button bookmarkButton = (Button)layout.findViewById(R.id.controlBookmarkBtn);
        Button bookmarkButtonImg = (Button)layout.findViewById(R.id.controlBookmarkBtn_Temp);
        Button delButton = (Button)layout.findViewById(R.id.controlDelBtn);
        Button delButtonImg = (Button)layout.findViewById(R.id.controlDelBtn_Temp);
        Button refreshButton = (Button)layout.findViewById(R.id.controlRefreshBtn);
        Button refreshButtonImg = (Button)layout.findViewById(R.id.controlRefreshBtn_Temp);
        Button homeButton = (Button)layout.findViewById(R.id.controlHomeBtn);
        Button homeButtonImg = (Button)layout.findViewById(R.id.controlHomeBtn_Temp);
        Button announceButton = (Button)layout.findViewById(R.id.controlAnnounceBtn);


        tableRowSearchId.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                displaySearchPopup();
                popControlWindow.dismiss();
            }
        });
        searchButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                displaySearchPopup();
                popControlWindow.dismiss();
            }
        });
        tableRowRefreshId.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                refreshFeed(currentFeed, true);
                popControlWindow.dismiss();
            }
        });
        refreshButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                refreshFeed(currentFeed, true);
                popControlWindow.dismiss();
            }
        });

        tableRowHomeId.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(FeedMainItemActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        homeButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(FeedMainItemActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        tableRowAnnounceId.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                refreshFeedWithTopicAnnounce(currentFeed, true);
                popControlWindow.dismiss();
            }
        });
        announceButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                refreshFeedWithTopicAnnounce(currentFeed, true);
                popControlWindow.dismiss();
            }
        });

        popControlWindow.update();
    }

    public void dispControlItemSelectedActionPage(){
        //log.debug("displayPopMainWindow");

        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        TableLayout viewGroup = (TableLayout) findViewById(R.id.popup_list_control_id);
        View layout = layoutInflater.inflate(R.layout.popup_control, viewGroup);

        popMainWindow = new PopupWindow(layout,getDisplayPopControl(),LayoutParams.WRAP_CONTENT);
        popMainWindow.setBackgroundDrawable(new BitmapDrawable());
        popMainWindow.setOutsideTouchable(true);
        popMainWindow.setFocusable(true);
        popMainWindow.setTouchable(true);

        int y = 150;
        int x = getWindowManager().getDefaultDisplay().getWidth()-150;
        //log.debug("x["+x+"]y["+y+"]");
        popMainWindow.showAtLocation(layout, Gravity.TOP, x, y);

        TableRow tableRowSearchId = (TableRow)layout.findViewById(R.id.tableRowSearchId);
        TableRow tableRowSaveId = (TableRow)layout.findViewById(R.id.tableRowSaveId);
        TableRow tableRowBookmarkId = (TableRow)layout.findViewById(R.id.tableRowBookmarkId);
        TableRow tableRowDelId = (TableRow)layout.findViewById(R.id.tableRowDelId);
        TableRow tableRowRefreshId = (TableRow)layout.findViewById(R.id.tableRowRefreshId);
        TableRow tableRowHomeId = (TableRow)layout.findViewById(R.id.tableRowHomeId);
        TableRow tableRowTopId = (TableRow)layout.findViewById(R.id.tableRowTopId);
        TableRow tableRowBottomId = (TableRow)layout.findViewById(R.id.tableRowBottomId);
        TableRow tableRowAnnounceId = (TableRow)layout.findViewById(R.id.tableRowAnnounceId);

        //Hide
        tableRowSearchId.setVisibility(View.GONE);
        tableRowRefreshId.setVisibility(View.GONE);
        tableRowAnnounceId.setVisibility(View.GONE);
        tableRowHomeId.setVisibility(View.GONE);
        tableRowTopId.setVisibility(View.GONE);
        tableRowBottomId.setVisibility(View.GONE);

        Button saveButton = (Button)layout.findViewById(R.id.controlSaveBtn);
        Button bookmarkButton = (Button)layout.findViewById(R.id.controlBookmarkBtn);
        Button delButton = (Button)layout.findViewById(R.id.controlDelBtn);
	      
	      /*log.debug("itemId["+currentItem.getId()+"]");
	      log.debug("itemTile["+currentItem.getTitle()+"]");
	      log.debug("pathFile["+currentItem.getPathFile()+"]");
	      log.debug("isFav["+currentItem.isFav()+"]");*/

        //save article
        if( !Utils.isNull(currentItem.getPathFile()).equals("")){
            //Show Del ,SaveArticle
            delButton.setText("ลบข้อมูล");
            tableRowBookmarkId.setVisibility(View.GONE);
        }else{
            //save bookmark
            if(currentItem.isFav()){
                //DelBookmark,SaveBookmark
                delButton.setText("ลบบุ็คมาร์ค");
                tableRowSaveId.setVisibility(View.GONE);
            }else{
                //no save
                if(Constants.FEED_TYPE_ARTICLE.equals(currentFeed.getType())){
                    //Show SaveArticle
                    tableRowDelId.setVisibility(View.GONE);
                    tableRowBookmarkId.setVisibility(View.GONE);
                }else{
                    //Show SaveBookmark
                    tableRowDelId.setVisibility(View.GONE);
                    tableRowSaveId.setVisibility(View.GONE);
                }
            }
        }

        tableRowSaveId.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                saveTopic();
                popMainWindow.dismiss();
            }
        });
        saveButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                saveTopic();
                popMainWindow.dismiss();
            }
        });

        tableRowBookmarkId.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                saveBookmark();
                popMainWindow.dismiss();
            }
        });
        bookmarkButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                saveBookmark();
                popMainWindow.dismiss();
            }
        });

        tableRowDelId.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                removeTopic();
                popMainWindow.dismiss();
            }
        });
        delButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                removeTopic();
                popMainWindow.dismiss();
            }
        });
    }

    //Save File to Local
    public void saveBookmark(){
        int currentPage = 1;
        int item_listViewIndex = 0;
        int item_listViewTop = 0;
        //log.debug("saveBookmark:item_listViewIndex["+item_listViewIndex+"]item_listViewTop["+item_listViewTop+"]");

        String msg = "บุคร์มาร์ค กระทู้:"+currentItem.getTitle()+" เรียบร้อยแล้ว ";
        Toast.makeText(FeedMainItemActivity.this, msg, Toast.LENGTH_LONG).show();

        /** Update to Database **/
        ContentValues values = new ContentValues();
        //values.put(DBSchema.ItemSchema.COLUMN_UPDATE_DATE,(new Date()).getTime());
        values.put(DBSchema.ItemSchema.COLUMN_FAV, DBSchema.ON);
        //values.put(DBSchema.ItemSchema.COLUMN_CUR_PAGE, currentPage);
        //values.put(DBSchema.ItemSchema.COLUMN_LAST_POSITION, item_listViewIndex+","+item_listViewTop);

        if(Constants.FEED_TYPE_BORAD_100.equals(currentFeed.getType()) ||
                Constants.FEED_TYPE_BORAD_100_NEW.equals(currentFeed.getType())){

            if(Constants.FEED_TYPE_BORAD_100.equals(currentFeed.getType())){ //21,14
                if(currentItem.getFeedId() == 21){
                    //update cur
                    mDbFeedAdapter.updateItem(currentItem.getId(), values);
                    //update 14 both
                    mDbFeedAdapter.updateItemByTitle(14,currentItem.getTitle(), values);
                }else{
                    //update cur
                    mDbFeedAdapter.updateItem(currentItem.getId(), values);
                    //update 21 both
                    mDbFeedAdapter.updateItemByTitle(21,currentItem.getTitle(), values);
                }
            }else{
                if(currentItem.getFeedId() == 22){ //22,20
                    //update cur
                    mDbFeedAdapter.updateItem(currentItem.getId(), values);
                    //update 14 both
                    mDbFeedAdapter.updateItemByTitle(20,currentItem.getTitle(), values);
                }else{
                    //update cur
                    mDbFeedAdapter.updateItem(currentItem.getId(), values);
                    //update 14 both
                    mDbFeedAdapter.updateItemByTitle(22,currentItem.getTitle(), values);
                }
            }
        }else{
            mDbFeedAdapter.updateItem(currentItem.getId(), values);
        }

        /** Add Load More Button **/
        addButtonLoadMoreInListView();
        /** Set Content To List View **/
        refreshListAllView(currentDateSort);
    }

    public void removeTopic(){
        /** Update to Database **/
        ContentValues values = new ContentValues();
        //values.put(DBSchema.ItemSchema.COLUMN_UPDATE_DATE,(new Date()).getTime());
        values.put(DBSchema.ItemSchema.COLUMN_PATH_FILE, "");
        values.put(DBSchema.ItemSchema.COLUMN_FAV, DBSchema.OFF);
        values.put(DBSchema.ItemSchema.COLUMN_CUR_PAGE, 0);

        if(Constants.FEED_TYPE_BORAD_100_NEW.equalsIgnoreCase(currentFeed.getType())
                || Constants.FEED_TYPE_BORAD_100.equalsIgnoreCase(currentFeed.getType())
                ){
            //old data have 2 record
            // mDbFeedAdapter.updateItem(currentItem.getId(), values);
            mDbFeedAdapter.updateItemByTitle(currentItem.getTitle(),values);
        }else{
            mDbFeedAdapter.updateItem(currentItem.getId(), values);
        }

        mDbFeedAdapter.updateItem(currentItem.getId(), values);

        String msg = "Remove topic success";
        Toast.makeText(FeedMainItemActivity.this, msg, Toast.LENGTH_LONG).show();

        /** Add Load More Button **/
        addButtonLoadMoreInListView();
        /** Set Content To List View **/
        refreshListAllView(currentDateSort);
    }

    //Save File to Local
    public void saveTopic(){
        StringBuffer contentDesc = null;
        String msg = "โปรดรอสักครู่  .....กำลังดึงข้อมูล";
        Toast.makeText(FeedMainItemActivity.this, msg, Toast.LENGTH_LONG).show();

        if(Constants.FEED_TYPE_ARTICLE.equals(currentFeed.getType())){
            JSoupHelperNoAuthen jsoup = new JSoupHelperNoAuthen();
            try{
                contentDesc = jsoup.getContentFromDropBox(currentItem);
            }catch(Exception e){
                log.debug("Err Retry Conn");
                try{
                    contentDesc = jsoup.getContentFromDropBox(currentItem);
                }catch(Exception ee){}
            }
        }

        //log.debug("content_description:"+contentDesc.toString());
        String pathFile = saveFile(String.valueOf(currentItem.getId()),contentDesc!=null?contentDesc.toString():"");
        //log.debug("pathFile:"+pathFile);

        msg = "บันทึกบทความ :"+currentItem.getTitle()+" เรียบร้อยแล้ว";
        Toast.makeText(FeedMainItemActivity.this, msg, Toast.LENGTH_LONG).show();

        /** Update to Database **/
        ContentValues values = new ContentValues();
        values.put(DBSchema.ItemSchema.COLUMN_PATH_FILE, pathFile);
        values.put(DBSchema.ItemSchema.COLUMN_FAV, DBSchema.ON);
        //values.put(DBSchema.ItemSchema.COLUMN_CUR_PAGE, currentPage);
        mDbFeedAdapter.updateItem(currentItem.getId(), values);

        /** Add Load More Button **/
        addButtonLoadMoreInListView();
        /** Set Content To List View **/
        refreshListAllView(currentDateSort);
    }

    public String saveFile(String fileName,String data){
        // TODO Auto-generated method stub
        String pathFile = "";
        try {
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File myNewFolder = new File(extStorageDirectory +"/"+ Constants.APP_FOLDER);
            //log.debug("extStorageDirectory["+extStorageDirectory+"] appFolder Exist["+myNewFolder.exists()+"]");
            if( !myNewFolder.exists()){
                log.debug("mkDir:"+myNewFolder.mkdir());
            }

            myNewFolder = new File(extStorageDirectory+"/"+Constants.APP_FOLDER+"/"+Utils.getValidFolderName(currentItem.getAuthor()));
            if( !myNewFolder.exists()){
                log.debug("mkDir:"+myNewFolder.mkdir());
            }
            pathFile = extStorageDirectory+"/"+Constants.APP_FOLDER+"/"+Utils.getValidFolderName(currentItem.getAuthor())+"/"+Utils.validFileName(fileName)+".txt";
            FileUtil.writeFile(pathFile, data.getBytes());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return pathFile;
    }

    public void displaySearchPopup(){

        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        TableLayout viewGroup = (TableLayout) findViewById(R.id.popup_list_control_id);
        View layout = layoutInflater.inflate(R.layout.popup_search, viewGroup);

        popWindow = new PopupWindow(layout,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        popWindow.setBackgroundDrawable(new BitmapDrawable());
        popWindow.setOutsideTouchable(true);
        popWindow.setFocusable(true);
        popWindow.setTouchable(true);

        int y = 150;
        int x = 40;
        //log.debug("x["+x+"]y["+y+"]");
        popWindow.showAtLocation(layout, Gravity.TOP, x, y);

        final EditText searchCriteria = (EditText)layout.findViewById(R.id.searchText);
        Button searchButton = (Button)layout.findViewById(R.id.popupSearchBtn);

        searchButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                if( !"".equals(searchCriteria.getText().toString())){
                    searchByKey(searchCriteria.getText().toString());
                    popWindow.dismiss();
                }
            }
        });
    }

    private void searchByKey(String key) {
        Control control = ControlHelper.getControlContent(this, currentFontSize, currentBgColor,topicCurPage);

        //init get Item All to ArList
        Feed feedKeySerach  = new Feed();
        feedKeySerach.setId(currentFeed.getId());
        feedKeySerach.setType(currentFeed.getType());
        feedKeySerach.setAuthor(currentFeed.getAuthor());
        feedKeySerach.setTitle(key);

        allArticleList =  mDbFeedAdapter.getItems(Constants.MAX_ITEMS,"KEY_SEARCH","DESC",feedKeySerach,feedKeySerach.getType(),topicCurPage);
        //log.debug("allArticleList Size:"+allArticleList.size());
        arrayItemAdapter = new FeedMainItemAdapter(this, R.id.title, allArticleList,control);
        articleListView.setAdapter(arrayItemAdapter);
    }

    public void nextPageTopic(){
        topicCurPage++;

        //log.debug("nextPageTopic ->topicCurPage:"+topicCurPage);
        if(Constants.FEED_TYPE_BORAD.equals(currentFeed.getType()) ||
                (Constants.FEED_TYPE_BORAD_100.equals(currentFeed.getType()) && currentFeed.getLink().indexOf("dropbox")== -1) ||
                (Constants.FEED_TYPE_BORAD_100_NEW.equals(currentFeed.getType()) && currentFeed.getLink().indexOf("dropbox")== -1)
                ){
            //http://board.thaivi.org/viewforum.php?f=7
            //http://board.thaivi.org/viewforum.php?f=7&start=50
            //Calc Next PageUrl
            if(topicCurPage > 1){
                int start = (topicCurPage-1) * Constants.THAIVI_TOPIC_ROW_PER_PAGE;
                currentFeed.setLink(currentFeed.getOrgLink()+"&start="+start);
            }

            if(topicCurPage==1){
                // int start = topicCurPage * Constants.THAIVI_TOPIC_ROW_PER_PAGE;
                currentFeed.setLink(currentFeed.getOrgLink());
            }

            /** Load More Content by Next Page in Topic **/
            new UpdateFeedTask().execute(currentFeed);
        }else{
            /** Set Content To List View **/
            refreshListAllView(currentDateSort);

            /** Add Load More Button **/
            addButtonLoadMoreInListView();
        }
    }

    public void refreshPageTopic(){

        //log.debug("refreshPageTopic ->topicCurPage:"+topicCurPage);
        if(Constants.FEED_TYPE_BORAD.equals(currentFeed.getType()) ||
                (Constants.FEED_TYPE_BORAD_100.equals(currentFeed.getType()) && currentFeed.getLink().indexOf("dropbox")== -1) ||
                (Constants.FEED_TYPE_BORAD_100_NEW.equals(currentFeed.getType()) && currentFeed.getLink().indexOf("dropbox")== -1)
                ){
            //http://board.thaivi.org/viewforum.php?f=7
            //http://board.thaivi.org/viewforum.php?f=7&start=50
            //Calc Next PageUrl
            if(topicCurPage > 1){
                int start = (topicCurPage-1) * Constants.THAIVI_TOPIC_ROW_PER_PAGE;
                currentFeed.setLink(currentFeed.getOrgLink()+"&start="+start);
            }

            if(topicCurPage==1){
                // int start = topicCurPage * Constants.THAIVI_TOPIC_ROW_PER_PAGE;
                currentFeed.setLink(currentFeed.getOrgLink());
            }

            /** Load More Content by Next Page in Topic **/
            new UpdateFeedTask().execute(currentFeed);
        }else{
            /** Load More Content by Next Page in Topic **/
            new UpdateFeedTask().execute(currentFeed);
        }
    }

    public void prevPageTopic(){
        if(topicCurPage > 1){
            topicCurPage--;

            //log.debug("prevPageTopic->topicCurPage:"+topicCurPage);
            if(Constants.FEED_TYPE_BORAD.equals(currentFeed.getType()) ||
                    (Constants.FEED_TYPE_BORAD_100.equals(currentFeed.getType()) && currentFeed.getLink().indexOf("dropbox")== -1) ||
                    (Constants.FEED_TYPE_BORAD_100_NEW.equals(currentFeed.getType()) && currentFeed.getLink().indexOf("dropbox")== -1)
                    ){
                //http://board.thaivi.org/viewforum.php?f=7
                //http://board.thaivi.org/viewforum.php?f=7&start=50
                //Calc Next PageUrl
                if(topicCurPage > 1){
                    int start = (topicCurPage-1) * Constants.THAIVI_TOPIC_ROW_PER_PAGE;
                    currentFeed.setLink(currentFeed.getOrgLink()+"&start="+start);
                }

                if(topicCurPage==1){
                    // int start = topicCurPage * Constants.THAIVI_TOPIC_ROW_PER_PAGE;
                    currentFeed.setLink(currentFeed.getOrgLink());
                }
                if(topicCurPage >0){
                    /** Load More Content by Next Page in Topic **/
                    new UpdateFeedTask().execute(currentFeed);
                }
            }else{
                /** Set Content To List View **/
                refreshListAllView(currentDateSort);

                /** Add Load More Button **/
                addButtonLoadMoreInListView();
            }
        }
    }

    //Called when device orientation changed (see: android:configChanges="orientation" in manifest file)
    //Avoiding to restart the activity (which causes a crash) when orientation changes during refresh in AsyncTask
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        onrotaion=false;
    }

    private void addButtonLoadMoreInListView(){
        int footerArViewCount  = articleListView.getFooterViewsCount();
        //log.debug("addButtonLoadMoreInListView ->topicCurPage:"+topicCurPage+",footerArViewCount="+footerArViewCount);

        if(footerView ==null){
            footerView = ((LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.feed_main_item_listview_footer, null, false);
        }

        if (footerArViewCount > 0){
            articleListView.removeFooterView(footerView);
        }

        controlPrevBtn = (Button)footerView.findViewById(R.id.controlPrevBtn);
        controlCurrentBtn = (Button)footerView.findViewById(R.id.controlCurrentBtn);
        controlNextBtn = (Button)footerView.findViewById(R.id.controlNextBtn);

        controlCurrentBtn.setText("หน้าที :"+topicCurPage);

        //Add Listener
        controlPrevBtn.setOnClickListener(this);
        //controlCurrentBtn.setOnClickListener(this);
        controlNextBtn.setOnClickListener(this);

        // Adding button to listview at footer
        articleListView.addFooterView(footerView);

    }

    private void refreshListAllView(String dateSort) {
        //log.debug("refreshListAllView:"+dateSort);

        Control control = ControlHelper.getControlContent(this, currentFontSize, currentBgColor,topicCurPage);
        //init get Item All to ArList
        allArticleList =  mDbFeedAdapter.getItems(Constants.MAX_ITEMS,"ID",dateSort,currentFeed,currentFeed.getType(),topicCurPage);
        log.debug("allArticleList Size:"+allArticleList.size());

        arrayItemAdapter = new FeedMainItemAdapter(this, R.id.title, allArticleList,control);
        articleListView.setAdapter(arrayItemAdapter);

        //restore scroll popsition
        if(listViewIndex !=0 && listViewTop != 0){
            articleListView.setSelectionFromTop(listViewIndex, listViewTop);
        }

        //init get Item All to ArList
        if(currentFeed.getId() == 22){ // Case Stock 100 NEW
            currentFeed.setIdSub(20);
        }else if(currentFeed.getId() == 20){ // Case Stock 100 NEW
            currentFeed.setIdSub(22);
            //OLD BOARD
        }else  if(currentFeed.getId() == 21){
            currentFeed.setIdSub(14);
        }else  if(currentFeed.getId() == 14){
            currentFeed.setIdSub(21);
        }

        allLocalList = mDbFeedAdapter.getItems(Constants.MAX_ITEMS,"FAV_BY_FEED_ID",dateSort,currentFeed,currentFeed.getType(),currentFeed.getAuthor());
        log.debug("allLocalList Size:"+allLocalList.size());

        arrayItemLocalAdapter = new FeedMainItemAdapter(this, R.id.title, allLocalList,control);
        localViewList.setAdapter(arrayItemLocalAdapter);
    }

    public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos, long id) {
        //Get Position in ListView
        listViewIndex = articleListView.getFirstVisiblePosition();
        View v2 = articleListView.getChildAt(0);
        listViewTop = (v2 == null) ? 0 : v2.getTop();

        currentItem = mDbFeedAdapter.getItem(id);
        dispControlItemSelectedActionPage();
        return true;
    }

    //Click Item View Content
    public void onItemClick (AdapterView<?> parent, View v, int position, long id) {

        arrayItemAdapter.setSelectItem(position);
        arrayItemAdapter.notifyDataSetChanged();

        arrayItemLocalAdapter.setSelectItem(position);
        arrayItemLocalAdapter.notifyDataSetChanged();

        listViewIndex = articleListView.getFirstVisiblePosition();
        View v2 = articleListView.getChildAt(0);
        listViewTop = (v2 == null) ? 0 : v2.getTop();
        //log.debug("OnItemClick:listViewIndex["+listViewIndex+"]listViewTop["+listViewTop+"]");

        Item item = mDbFeedAdapter.getItem(id);
        // log.debug("FeedType:"+item.getFeedType());
        new StartNewActivityInThread(dialog,currentFeed,item,topicCurPage,listViewIndex,listViewTop).run();

        //log.debug("StartNewActivityInThread:listViewIndex["+listViewIndex+"]listViewTop["+listViewTop+"]");

    }
    /**
     * Control Back Button
     */
    private void backAction(){

        Intent intent = new Intent(FeedMainItemActivity.this, FeedMainActivity.class);
        intent.putExtra("FEED_TYPE", currentFeed.getType());
        intent.putExtra("listViewIndex_FeedMain", listViewIndex_FeedMain);
        intent.putExtra("listViewTop_FeedMain", listViewTop_FeedMain);

        //intent.putExtra("APP_CONFIG_MAP", appConfig);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        //log.debug("KeyCode:"+keyCode+",:backCode:"+KeyEvent.KEYCODE_MENU);
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }
            backAction();
            //return super.onKeyDown(keyCode, event);
        }else if(keyCode ==KeyEvent.KEYCODE_MENU){
        }
        return super.onKeyDown(keyCode, event);
    }

    private void refreshFeed(Feed feed, boolean alwaysDisplayOfflineDialog) {
        //log.debug("refreshFeed topicCurPage["+topicCurPage+"]");
        params.put("FEED", feed);
        new UpdateFeedTask().execute(currentFeed);
    }

    private void refreshFeedWithTopicAnnounce(Feed feed, boolean alwaysDisplayOfflineDialog) {
        //log.debug("refreshFeed topicCurPage["+topicCurPage+"]");
        currentFeed.setShowTopicAnnounce(true);
        new UpdateFeedTask().execute(currentFeed);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        dialogDownload = null;
        switch (id) {
            case SharedPreferencesHelper.DIALOG_UPDATE_PROGRESS:
                dialogDownload = new ProgressDialog(this);
                dialogDownload.setCanceledOnTouchOutside(true);
                dialogDownload.setTitle(getResources().getText(R.string.loading));
                //((ProgressDialog)dialog).setIcon(R.drawable.ic_dialog);
                dialogDownload.setMessage(getResources().getText(R.string.downloading));
                dialogDownload.setIndeterminate(true);
                dialogDownload.setCancelable(true);
                break;
            default:
                dialogDownload = null;
        }
        return dialogDownload;
    }

    public class StartNewActivityInThread {
        private Feed feed;
        private Item item;
        private int topicCurPage;
        private int listViewIndex = 0;
        private int listViewTop = 0;
        public StartNewActivityInThread(ProgressDialog dialog,Feed feed,Item item,int topicCurPage,int listViewIndex,int listViewTop){
            this.feed = feed;
            this.item = item;
            this.topicCurPage = topicCurPage;
            this.listViewIndex = listViewIndex;
            this.listViewTop = listViewTop;
        }
        public void run() {
            try {
                Intent intent = new Intent(FeedMainItemActivity.this, FeedItemActivity.class);
                intent.putExtra("FEED",feed);
                intent.putExtra("FEED_ITEM",item);
                intent.putExtra("TOPIC_CUR_PAGE", topicCurPage);
                intent.putExtra("listViewIndex", listViewIndex);
                intent.putExtra("listViewTop", listViewTop);

                //log.debug("StartNewActivityInThread:listViewIndex["+listViewIndex+"]listViewTop["+listViewTop+"]");
                log.debug("debug click:title["+feed.getTitle()+"]");

                startActivity(intent);
                finish();
                release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void release(){
            try{
                log.debug("Thread release");
                //ThreadHelper.timerDelayRemoveDialog(400, dialog);
                //dialog.dismiss();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * UpdateFeedTask
     * @author aa
     *
     */
    private class UpdateFeedTask extends AsyncTask<Feed, Void, Boolean> implements OnDismissListener{
        private Feed feed;
        private long lastItemIdBeforeUpdate = -1;
        public UpdateFeedTask() {
            super();
        }
        protected Boolean doInBackground(Feed...params) {
            feed = params[0];
            Calendar c = Calendar.getInstance();
            JSoupHelperNoAuthen jsoup = new JSoupHelperNoAuthen();
            Date startTime = new Date();
            try {
                //** Delete Old Topic Item not inLocal
                // mDbFeedAdapter.deleteItemNotInLocal(feed.getId());

                if(feed.getLink().indexOf("dropbox") == -1){

                    //	Update TopicCurPage == 9999 Clear before
                    mDbFeedAdapter.updateTopicCurPage(feed.getId());

                    /** Set defalut load **/
                    if(topicCurPage==1){
                        feed.setLink(feed.getOrgLink());
                    }

                    //Get From Web
                    //log.debug("Get Topic List From Thai VI :topicCurPage["+topicCurPage+"]");
                    //log.debug("Get OrgLink ["+feed.getOrgLink()+"]");
                    //log.debug("Get Link ["+feed.getLink()+"]");


                    List<Item> items = new ArrayList<Item>();

                    if(Constants.FEED_TYPE_BORAD_100_NEW.equals(feed.getType())){
                        items = jsoupAuthen.getFeedsTopicList(feed,topicCurPage);

                        if(items ==null ||(items !=null && items.size() ==0)){
                            if( isOnline && "login_pass".equals(loginMsg) && !"".equals(userName) && !"".equals(password)){
                                //Relogin Case Object Fail
                                try{
                                    jsoupAuthen = JSoupHelperAuthen.newInstance(userName, password);

                                    items = jsoupAuthen.getFeedsTopicList(feed,topicCurPage);

                                    log.debug("Relogin jsoupAuthen[Global]:"+jsoupAuthen);
                                    thaiVI.setThaiviAuthen(jsoupAuthen);//Update Global

                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }else{
                        items = jsoup.getFeedsTopicList(feed,topicCurPage);
                    }

                    //log.debug("jsoup.getFeedsTopicList items["+items.size()+"] Time:"+((new Date()).getTime()-startTime.getTime()) );

                    feed.setItems(items);

                    //Set Org Link
                    feed.setLink(feed.getOrgLink());

                    startTime = new Date();
                    mDbFeedAdapter.updateFeed(feed);
                    //log.debug("updateFeed Time:"+((new Date()).getTime()-startTime.getTime()) );

                }else{

                    //log.debug("Get Article List From Dropbox");
                    List<Item> items = jsoup.getTopicItemsFromDropboxDB(mDbFeedAdapter,feed);
                    //log.debug("Item From Dropbox:"+items.size());

                    startTime = new Date();
                    feed.setItems(items);
                    mDbFeedAdapter.updateFeed(feed);
                    //log.debug("updateFeed Time:"+((new Date()).getTime()-startTime.getTime()) );

                    //recalc topicCurPage
                    String flagType = "ALL_BY_ID_AUTHOR";
                    if(  Constants.FEED_TYPE_BORAD_100.equals(feed.getType())
                            || Constants.FEED_TYPE_BORAD_100_NEW.equals(feed.getType())){

                        flagType ="ID_SORT_BY_TITLE";
                    }

                    //log.debug("FeedType:"+feed.getType()+",flagType:"+flagType);
                    List<Item> allTopicList =  mDbFeedAdapter.getItems(Constants.MAX_ITEMS,flagType,"DESC",feed,feed.getType(),-1);

                    //log.debug("*** process calc topicCurPage and save to db allTopicList:["+allTopicList.size()+"]****");

                    if(allTopicList!= null && allTopicList.size()>0){
                        int no = 0;
                        int topicCurPage = 1;
                        for(int i=0;i<allTopicList.size();i++){
                            no++;
                            Item it = allTopicList.get(i);
                            if(no ==Constants.THAIVI_TOPIC_ROW_PER_PAGE+1){
                                no=0;
                                topicCurPage++;
                            }
                            /** Set for  order by create_desc */
                            c.set(Calendar.MILLISECOND, -100);
                            it.setUpdateDate(c.getTime());

                            it.setTopicCurPage(topicCurPage);
                            //log.debug("Title:"+it.getTitle()+":"+it.getTopicCurPage());
                            /** update Item in List **/
                            allTopicList.set(i, it);
                        }//for

                        //update topicCurPage
                        feed.setItems(allTopicList);
                        mDbFeedAdapter.updateFeed(feed);
                    }

                    //log.debug("*** process calc topicCurPage and save to db ****");
                }

                dialogShow = true;
            }catch(Exception e){
                e.printStackTrace();
            }
            return new Boolean(true);
        }

        protected void onPreExecute() {
            showDialog(SharedPreferencesHelper.DIALOG_UPDATE_PROGRESS);
        }
        protected void onPostExecute(Boolean result) {
            /** Add Load More Button **/
            addButtonLoadMoreInListView();
            /** Set Content To List View **/
            refreshListAllView(currentDateSort);

            //log.debug("dialogDownload isShowing["+dialogDownload.isShowing()+"]");
            if(dialogDownload != null && dialogDownload.isShowing()){
                dismissDialog(SharedPreferencesHelper.DIALOG_UPDATE_PROGRESS);
                long lastItemIdAfterUpdate = -1;
                Item lastItem = mDbFeedAdapter.getLastItem(feed.getId());
                if (lastItem != null)
                    lastItemIdAfterUpdate = lastItem.getId();
                if (lastItemIdAfterUpdate > lastItemIdBeforeUpdate){
                    Toast.makeText(FeedMainItemActivity.this, R.string.new_item_msg, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(FeedMainItemActivity.this, R.string.no_new_item_msg, Toast.LENGTH_LONG).show();
                }
            }
            dialogShow = false;

            /** Check can load content **/
            if(allArticleList == null || (allArticleList != null && allArticleList.size()==0)){
                String msg = getResources().getString(R.string.refresh_again_msg);
                Toast.makeText(FeedMainItemActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        }
        @Override
        public void onDismiss(DialogInterface dialog) {
            this.cancel(true);
        }
    }

}