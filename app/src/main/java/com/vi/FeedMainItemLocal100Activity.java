
package com.vi;

import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.vi.adapter.FeedMainItemAdapter;
import com.vi.common.Control;
import com.vi.common.Feed;
import com.vi.common.Item;
import com.vi.storage.DBAdapter;
import com.vi.storage.DBSchema;
import com.vi.storage.SharedPreferencesHelper;
import com.vi.utils.Constants;
import com.vi.utils.LogUtils;
import com.vi.utils.PhoneProperty;
import com.vi.utils.Utils;

public class FeedMainItemLocal100Activity extends Activity implements OnItemClickListener ,OnClickListener,OnTouchListener,OnItemLongClickListener {

    private ProgressDialog dialog;
    private DBAdapter mDbFeedAdapter;
    private long currentFeedId = -1;
    private String feedTitle  ="";
    private String feedType  ="";
    private ExtViewFlow viewFlow;
    private ExtDiffItemLocalViewAdapter adapter;
    private boolean customTitleSupported;
    private String currentDateSort = Constants.DB_SORT_DESC;
    private Button backBtn;
    private Button controlMoreControlBtn;
    private Button controlRefreshBtn;

    //Setting Control
    private String currentBgColor = "bg_color_pantip";
    private String currentFontSize = "18";

    List<Item> allLocalList ;
    FeedMainItemAdapter arrayItemAdapter;
    ListView localViewList;
    View footerView;
    int listViewIndex = 0;
    int listViewTop = 0;
    private PopupWindow popControlWindow;
    private PopupWindow popControlWindow2;
    //private JSoupHelperAuthen jsoupAuthen = null;
    private String authenMsg = "";
    boolean verifyLogin =true;
    PhoneProperty phoneProperty = null;
    Item currentItem = null;
    private static LogUtils log = new LogUtils("FeedMainItemLocal100Activity");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // log.debug("FeedMainItemLocal100Activity:OnCreate");

        //Load Phone Preperty
        phoneProperty = new PhoneProperty(this);

        //Load Global Variable
        //jsoupAuthen = ((THAIVI) this.getApplication()).getThaiviAuthen();

        //Load Setting
        SharedPreferencesHelper settingS = new SharedPreferencesHelper(this);
        currentFontSize = settingS.getSetting("textSize","18");
        //log.debug("currentFontSize:"+currentFontSize);

        /** Set Custom Title Bar **/
        customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.swipe_main_item_layout);

        Bundle extras = getIntent().getExtras(); // Case Pass Param From Another Activity
        if (extras != null) {
            feedTitle =  Utils.isNull(extras.getString("FEED_TITLE"));
            feedType =  Utils.isNull(extras.getString("FEED_TYPE"));

            listViewIndex = extras.getInt("listViewIndex");
            listViewTop = extras.getInt("listViewTop");

            //log.debug("feedType["+feedType+"]");
            //log.debug("feedTitle["+feedTitle+"]");
            customTitleBar(feedTitle);
        }
        String[] names = new String[]{"My Stock"};

        viewFlow = (ExtViewFlow) findViewById(R.id.viewflow);
        adapter = new ExtDiffItemLocalViewAdapter(this,names);
        viewFlow.setAdapter(adapter);
        ExtTitleFlowIndicator indicator = (ExtTitleFlowIndicator) findViewById(R.id.viewflowindic);
        indicator.setTitleProvider(adapter);
        viewFlow.setFlowIndicator(indicator);

        /** Open DataBase **/
        mDbFeedAdapter = new DBAdapter(this,currentFeedId,null);
        mDbFeedAdapter.open();

        /** FooterView Load more item **/
        footerView = ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listfooter, null, false);

        /** Set Back Button **/
        backBtn =(Button)findViewById(R.id.controlBackBtn);
        controlMoreControlBtn =(Button)findViewById(R.id.controlConfigBtn);
        controlRefreshBtn = (Button)findViewById(R.id.controlRefreshBtn);
        backBtn.setOnClickListener(this);
        controlMoreControlBtn.setOnClickListener(this);
        controlRefreshBtn.setOnClickListener(this);


        /************ Set Video************************************************/
        localViewList = (ListView) findViewById(R.id.feed_local_list);
        //register itemListener
        localViewList.setOnItemClickListener(this);
        localViewList.setOnItemLongClickListener(this);

        /***********************************************************/
        //Add Data to ListView
        refreshListAllView(currentDateSort);

        if( !Utils.isNull(authenMsg).equals("")){
            Toast.makeText(FeedMainItemLocal100Activity.this, authenMsg, Toast.LENGTH_LONG).show();
        }
        authenMsg = "";
    }

    private void backAction(){
        Intent intent = new Intent(FeedMainItemLocal100Activity.this, MainActivity.class);
        //intent.putExtra("APP_CONFIG_MAP", appConfig);
        startActivity(intent);
        finish();
    }

    //Onhold Long touch for delete
    public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos, long id) { 
    	/* mDbFeedAdapter = new DBAdapter(this,currentFeedId,null);
    	 mDbFeedAdapter.open();*/
        currentItem = mDbFeedAdapter.getItem(id);

        //For Show Control remove topic
        dispControlOnLongTouchItemInListView();

        return true;
    }

    //Click Item View Content
    public void onItemClick (AdapterView<?> parent, View v, int position, long id) {

        arrayItemAdapter.setSelectItem(position);
        arrayItemAdapter.notifyDataSetChanged();

        if(mDbFeedAdapter == null){
            mDbFeedAdapter = new DBAdapter(this,currentFeedId,null);
            mDbFeedAdapter.open();
        }
        Item item = mDbFeedAdapter.getItem(id);
        //log.debug("FeedType:"+item.getFeedType());

        listViewIndex = localViewList.getFirstVisiblePosition();
        View v2 = localViewList.getChildAt(0);
        listViewTop = (v2 == null) ? 0 : v2.getTop();
        //log.debug("OnItemClick:listViewIndex["+listViewIndex+"]listViewTop["+listViewTop+"]");

        Feed currentFeed = new Feed();
        currentFeed.setType(feedType);
        currentFeed.setTitle(feedTitle);

        new StartNewActivityInThread(dialog,currentFeed,item).run();

    }

    public void removeTopic(){
        /** Update to Database **/
        ContentValues values = new ContentValues();
        //values.put(DBSchema.ItemSchema.COLUMN_UPDATE_DATE,(new Date()).getTime());
        values.put(DBSchema.ItemSchema.COLUMN_PATH_FILE, "");
        values.put(DBSchema.ItemSchema.COLUMN_FAV, DBSchema.OFF);
        values.put(DBSchema.ItemSchema.COLUMN_CUR_PAGE, 0);

        if(Constants.FEED_TYPE_BORAD_100_NEW.equalsIgnoreCase(feedType)
                || Constants.FEED_TYPE_BORAD_100.equalsIgnoreCase(feedType)
                ){
            //old data have 2 record
            // mDbFeedAdapter.updateItem(currentItem.getId(), values);

            mDbFeedAdapter.updateItemByTitle(currentItem.getTitle(),values);
        }else{
            mDbFeedAdapter.updateItem(currentItem.getId(), values);
        }
	   
	   /* log.debug("before item id["+currentItem.getId()+"]title["+currentItem.getTitle()+"],fav["+currentItem.isFav()+"]");
 		
 	   	//recheck 
 	   	Item currentItemTest = mDbFeedAdapter.getItem(currentItem.getId());
 		log.debug("after item id["+currentItemTest.getId()+"]title["+currentItemTest.getTitle()+"],fav["+currentItemTest.isFav()+"]");
 		*/

        String msg = "Remove topic success";
        Toast.makeText(FeedMainItemLocal100Activity.this, msg, Toast.LENGTH_LONG).show();

        //Refresh ListView
        refreshListAllView(currentDateSort);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        log.debug("KeyCode:"+keyCode+",:backCode:"+KeyEvent.KEYCODE_BACK);
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            backAction();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if(backBtn.equals(v)){
            backAction();
        }else if(controlMoreControlBtn.equals(v)){
            displayPopupControl();
        }else if(controlRefreshBtn.equals(v)){
            if(verifyLogin){
                refreshFeed(true);
            }
            //Alert Authen Msg
            if( !Utils.isNull(authenMsg).equals("")){
                Toast.makeText(FeedMainItemLocal100Activity.this, authenMsg, Toast.LENGTH_LONG).show();
                authenMsg ="";
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        log.debug("ontouch");
        if(event.getAction()== MotionEvent.ACTION_DOWN){
            return true;
        }
        // positionX = new Double(event.getX()).intValue();
        //positionY = new Double(event.getY()).intValue();

        return false;
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
            //log.debug("text:"+text.length());

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
        super.onResume();
        if(dialog != null){
            dialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbFeedAdapter.close();
        //mDbFeedAdapter = null;
    }

    //Called when device orientation changed (see: android:configChanges="orientation" in manifest file)
    //Avoiding to restart the activity (which causes a crash) when orientation changes during refresh in AsyncTask
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
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
        // log.debug("displayPopupControl");
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

        log.debug("x["+x+"]y["+y+"]");
        popControlWindow.showAtLocation(layout, Gravity.TOP, x, y);

        TableRow tableRowHomeId = (TableRow)layout.findViewById(R.id.tableRowHomeId);
        TableRow tableRowSearchId = (TableRow)layout.findViewById(R.id.tableRowSearchId);
        TableRow tableRowSaveId = (TableRow)layout.findViewById(R.id.tableRowSaveId);
        TableRow tableRowBookmarkId = (TableRow)layout.findViewById(R.id.tableRowBookmarkId);
        TableRow tableRowDelId = (TableRow)layout.findViewById(R.id.tableRowDelId);
        TableRow tableRowRefreshId = (TableRow)layout.findViewById(R.id.tableRowRefreshId);
        TableRow tableRowTopId = (TableRow)layout.findViewById(R.id.tableRowTopId);
        TableRow tableRowBottomId = (TableRow)layout.findViewById(R.id.tableRowBottomId);

        //Hide
        tableRowSearchId.setVisibility(View.GONE);
        tableRowRefreshId.setVisibility(View.GONE);
        tableRowSaveId.setVisibility(View.GONE);
        tableRowBookmarkId.setVisibility(View.GONE);
        tableRowDelId.setVisibility(View.GONE);
        tableRowTopId.setVisibility(View.GONE);
        tableRowBottomId.setVisibility(View.GONE);

        Button homeButton = (Button)layout.findViewById(R.id.controlHomeBtn);
        Button homeButtonImg = (Button)layout.findViewById(R.id.controlHomeBtn_Temp);

        tableRowHomeId.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(FeedMainItemLocal100Activity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        homeButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(FeedMainItemLocal100Activity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        popControlWindow.update();
    }

    public void dispControlOnLongTouchItemInListView(){
        log.debug("dispControlOnLongTouchItemInListView");

        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        TableLayout viewGroup = (TableLayout) findViewById(R.id.popup_list_control_id);
        View layout = layoutInflater.inflate(R.layout.popup_control, viewGroup);

        popControlWindow2 = new PopupWindow(layout,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        popControlWindow2.setBackgroundDrawable(new BitmapDrawable());
        popControlWindow2.setOutsideTouchable(true);
        popControlWindow2.setFocusable(true);
        popControlWindow2.setTouchable(true);

        int y = 150;
        int x = getWindowManager().getDefaultDisplay().getWidth()-300;

        log.debug("x["+x+"]y["+y+"]");
        popControlWindow2.showAtLocation(layout, Gravity.CENTER_HORIZONTAL, 0, 0);

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
        tableRowSaveId.setVisibility(View.GONE);
        tableRowBookmarkId.setVisibility(View.GONE);
        tableRowHomeId.setVisibility(View.GONE);
        tableRowTopId.setVisibility(View.GONE);
        tableRowBottomId.setVisibility(View.GONE);

        Button delButton = (Button)layout.findViewById(R.id.controlDelBtn);
        delButton.setText("ลบบุ็คมาร์ค");

        tableRowDelId.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                removeTopic();
                popControlWindow2.dismiss();
            }
        });
        delButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                removeTopic();
                popControlWindow2.dismiss();
            }
        });
        popControlWindow2.update();
    }

    private void refreshListAllView(String dateSort) {
        log.debug("refreshListAllView:"+dateSort+",feedType:"+feedType);
        Control control = ControlHelper.getControlContent(this, currentFontSize, currentBgColor,0);

        //init get Item All to ArList
        allLocalList =  mDbFeedAdapter.getItems(Constants.MAX_ITEMS,"FAV_BY_FEED_TYPE_NO_DUP",dateSort,null,feedType,null);
        arrayItemAdapter = new FeedMainItemAdapter(this, R.id.title, allLocalList,control);
        localViewList.setAdapter(arrayItemAdapter);

        //restore scroll popsition
        if(listViewIndex !=0 && listViewTop != 0){
            localViewList.setSelectionFromTop(listViewIndex, listViewTop);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void refreshFeed( boolean alwaysDisplayOfflineDialog) {
        refreshListAllView(currentDateSort);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        CharSequence title = null;
        LayoutInflater inflater = null;
        View dialogLayout = null;
        AlertDialog.Builder builder = null;
        switch (id) {
            case SharedPreferencesHelper.DIALOG_UPDATE_PROGRESS:
                dialog = new ProgressDialog(this);
                dialog.setTitle(getResources().getText(R.string.loading));
                //((ProgressDialog)dialog).setIcon(R.drawable.ic_dialog);
                ((ProgressDialog)dialog).setMessage(getResources().getText(R.string.downloading));
                ((ProgressDialog)dialog).setIndeterminate(true);
                dialog.setCancelable(false);
                break;

            case SharedPreferencesHelper.DIALOG_NO_CONNECTION:
                title = getString(R.string.error);
                inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                dialogLayout = inflater.inflate(R.layout.dialog_no_connection, null);
                builder = new AlertDialog.Builder(this);
                builder.setView(dialogLayout)
                        .setTitle(title)
                                //.setIcon(R.drawable.ic_dialog)
                        .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
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

    public class StartNewActivityInThread {
        private Feed feed;
        private Item item;
        public StartNewActivityInThread(ProgressDialog dialog,Feed feed,Item item){
            this.feed = feed;
            this.item = item;
        }
        public void run() {
            try {
                Intent intent = new Intent(FeedMainItemLocal100Activity.this, FeedItemActivity.class);
                intent.putExtra("ACT_", "FeedMainItemLocal100Activity");
                intent.putExtra("FEED",feed);
                intent.putExtra("FEED_ITEM",item);
                intent.putExtra("listViewIndex", listViewIndex);
                intent.putExtra("listViewTop", listViewTop);

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

}