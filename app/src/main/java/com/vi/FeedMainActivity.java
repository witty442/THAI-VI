
package com.vi;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.vi.adapter.FeedMainAdapter;
import com.vi.adapter.FeedMainItemAdapter;
import com.vi.common.Control;
import com.vi.common.Feed;
import com.vi.common.Item;
import com.vi.helper.ThreadHelper;
import com.vi.parser.JSoupHelperNoAuthen;
import com.vi.storage.DBAdapter;
import com.vi.storage.SharedPreferencesHelper;
import com.vi.utils.Constants;
import com.vi.utils.LogUtils;
import com.vi.utils.PhoneProperty;
import com.vi.utils.Utils;


public class FeedMainActivity extends Activity implements OnItemClickListener,OnClickListener{
	private DBAdapter mDbFeedAdapter;
	private ExtViewFlow viewFlow;
	private boolean customTitleSupported;
	private String currentDateSort = Constants.DB_SORT_DESC;
	private Button backBtn;
	private Button controlMoreBtn;
	private Button controlRefreshBtn;
	private ImageView favBtn;
	ProgressDialog dialog;
	ProgressDialog dialogDownload;
	
	ListView listCatagoryView;
	ListView localViewList;
	ListView favViewList;
	View footerView;
	
	int itemsPerPage = Constants.MAX_ITEMS_PER_PAGE;
    int currentPageFav = 1;
    int currentPageNew = 1;
    
  //Setting Control 
	private String currentBgColor = "bg_color_pantip";
	private String currentFontSize = "18";
	
	List<Item> allFavList ;
	//FeedMainItemAdapter arrayItemFavAdapter;
	FeedMainAdapter arrayCatagoryAdapter;
	String curFeedType = "";
	private boolean dialogShow = false;
	private PopupWindow popControlWindow;
	String upgradeTopic = "false";
	boolean onrotaion=true; 
	
	private int listViewIndex_FeedMain = 0;
	private int listViewTop_FeedMain = 0;
	LogUtils log = new LogUtils("FeedMainActivity");
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        log.debug("onrotation["+onrotaion+"]");
        if(onrotaion){
	        log.debug("FeedMainActivity:OnCreate");
	        
	        //Load Setting
	        SharedPreferences settingMain = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
	    	//SharedPreferencesHelper settingGet = new SharedPreferencesHelper(this);
	    	currentFontSize = settingMain.getString("textSize","18");
	    	upgradeTopic = settingMain.getString("upgradeTopic","false");

			//log.debug("currentFontSize:"+currentFontSize);
	    	//log.debug("upgradeTopic:"+upgradeTopic);
	    	
	        //Allow Policy Thread
	    	if (android.os.Build.VERSION.SDK_INT > 9) {
	    	     StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    	     StrictMode.setThreadPolicy(policy);
	    	 }
	    	
	    	Bundle extras = getIntent().getExtras(); // Case Pass Param From Another Activity
			if (extras != null) {
				curFeedType =  Utils.isNull(extras.getString("FEED_TYPE"));
				log.debug("MainItem>>>curFeedType["+curFeedType+"]");

				listViewIndex_FeedMain = extras.getInt("listViewIndex_FeedMain");
				listViewTop_FeedMain = extras.getInt("listViewTop_FeedMain");
				
				log.debug("listViewIndex_FeedMain:"+listViewIndex_FeedMain);
				log.debug("listViewTop_FeedMain:"+listViewTop_FeedMain);
			}
			
	        /** Open DataBase **/
			mDbFeedAdapter = new DBAdapter(this);
			mDbFeedAdapter.open();

			/** Update Script Sql Case Bug **/
			Calendar c = Calendar.getInstance();
			int day = c.get(Calendar.DATE);
			String scriptUpdateSql ="";
			if(day==15 || day ==30) {
				JSoupHelperNoAuthen jsoup = new JSoupHelperNoAuthen();
				scriptUpdateSql = jsoup.getScriptFromDropbox();
			}
			log.debug("scriptSql:"+Utils.isNull(scriptUpdateSql));
		    if( !Utils.isNull(scriptUpdateSql).equals("")){
				mDbFeedAdapter.updateByScriptSql(Utils.isNull(scriptUpdateSql));
			}

	        /** Set Custom Title Bar **/
	        customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);  
	        setContentView(R.layout.swipe_main_layout);
	        customTitleBar("THAI-VI");
	        
	        viewFlow = (ExtViewFlow) findViewById(R.id.viewflow);
	        ExtDiffViewAdapter adapter = new ExtDiffViewAdapter(this);
	        viewFlow.setAdapter(adapter);
	        
			ExtTitleFlowIndicator indicator = (ExtTitleFlowIndicator) findViewById(R.id.viewflowindic);
			indicator.setTitleProvider(adapter);
			viewFlow.setFlowIndicator(indicator);
			
			/** FooterView Load more item **/
		    footerView = ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listfooter, null, false);
	
			/** Set Catagory */
			listCatagoryView = (ListView) findViewById(R.id.feed_catagory_list);
			
	        //register itemListener
	        listCatagoryView.setOnItemClickListener(this);
	        
	       
	        /** Set Back Button **/
	        backBtn =(Button)findViewById(R.id.controlBackBtn);
	        controlMoreBtn =(Button)findViewById(R.id.controlConfigBtn);
	        controlRefreshBtn = (Button)findViewById(R.id.controlRefreshBtn);
	        	
	        backBtn.setOnClickListener(this);
	        controlMoreBtn.setOnClickListener(this);
	        controlRefreshBtn.setOnClickListener(this);
			
		    /** check database exist **/
		   int countFeeds = mDbFeedAdapter.countFeedsByLike(curFeedType);
		   String firstFeedUpdateDate = mDbFeedAdapter.countFirstUpdateDateFeedsByLike(curFeedType);
		   String currentDate = Utils.convertToString(new Date());
		   log.debug("currentDate["+currentDate+"]firstFeedUpdateDate["+firstFeedUpdateDate+"]");
		   
		   /** Check Upgrade Topic **/
		   boolean updateTopicFlag = false;
		   if("false".equals(upgradeTopic) && !firstFeedUpdateDate.equals(currentDate)){
			  updateTopicFlag = true;
			  
			  SharedPreferences.Editor editor = settingMain.edit();
			  editor.putString("upgradeTopic", "true");
			  editor.commit();
		   }
			
		   if(countFeeds==0 || updateTopicFlag){
		      log.debug("First Time Initial data ");
		      updateFeedDB();
		   }
        }//Check Rotation
    }   
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		//log.debug("KeyCode:"+keyCode+",:backCode:"+KeyEvent.KEYCODE_BACK);
	    if(keyCode == KeyEvent.KEYCODE_BACK) {     
	    	if(dialog != null && dialog.isShowing()){
			   dialog.dismiss();
			}
	    	backAction();
            return true;
	    }
	    return false;
	}

	@Override
	public void onClick(View v) {
		if(backBtn.equals(v)){
			backAction();
		}else if(controlRefreshBtn.equals(v)){
			updateFeedDB();
		}else if(controlMoreBtn.equals(v)){
			displayPopupControl();
		}
	}
	
	public void backAction(){
		Intent intent = new Intent(FeedMainActivity.this, MainActivity.class);
		//intent.putExtra("APP_CONFIG_MAP", appConfig);
        startActivity(intent);
        finish();
	}
	
	public void displayPopupControl(){
	      //log.debug("displayPopupControl");
	     //popupControl_flag = true;
  	
		  LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);   
		  TableLayout viewGroup = (TableLayout) findViewById(R.id.popup_list_control_id);
	      View layout = layoutInflater.inflate(R.layout.popup_control, viewGroup);

		  int popControlWindowWidth =getWindowManager().getDefaultDisplay().getWidth()-100;
		  int popControlWindowHeight = LayoutParams.WRAP_CONTENT;

		  popControlWindow = new PopupWindow(layout,popControlWindowWidth,popControlWindowHeight);
		  popControlWindow.setBackgroundDrawable(new BitmapDrawable());
		  popControlWindow.setOutsideTouchable(true);
		  popControlWindow.setFocusable(true);
		  popControlWindow.setTouchable(true);

		  int y = getWindowManager().getDefaultDisplay().getHeight()/5;
		  int x = 50;//getWindowManager().getDefaultDisplay().getWidth()-150;

		  log.debug("x["+x+"]y["+y+"]");
		  popControlWindow.showAtLocation(layout, Gravity.NO_GRAVITY, x, y);
	      
	      TableRow tableRowHomeId = (TableRow)layout.findViewById(R.id.tableRowHomeId);
	      TableRow tableRowRefreshId = (TableRow)layout.findViewById(R.id.tableRowRefreshId);

	      //Show
		  tableRowRefreshId.setVisibility(View.VISIBLE);
		  tableRowHomeId.setVisibility(View.VISIBLE);

	      Button refreshButton = (Button)layout.findViewById(R.id.controlRefreshBtn);
	      Button homeButton = (Button)layout.findViewById(R.id.controlHomeBtn);
	      
	      tableRowRefreshId.setOnClickListener(new OnClickListener(){
	            public void onClick(View v){
	            	updateFeedDB();
	            	popControlWindow.dismiss();
	            }
	       }); 
	      refreshButton.setOnClickListener(new OnClickListener(){
	            public void onClick(View v){
	            	updateFeedDB();
	            	popControlWindow.dismiss();
	            }
	       }); 
	      
	      tableRowHomeId.setOnClickListener(new OnClickListener(){
	            public void onClick(View v){
	            	Intent intent = new Intent(FeedMainActivity.this, MainActivity.class);
	            	startActivity(intent);
	            }
	      });  
	      homeButton.setOnClickListener(new OnClickListener(){
	            public void onClick(View v){
	            	Intent intent = new Intent(FeedMainActivity.this, MainActivity.class);
	            	startActivity(intent);
	            }
	      });  
	      
	      popControlWindow.update();
	}
  
	public void updateFeedDB(){
		try{
   	        //Update feed item in Background Process
   	        new UpdateFeedTask().execute("NEW",curFeedType);
       	}catch(Exception e){
       		e.printStackTrace();
       	}
	}
	
	public void customTitleBar(String text) {
        // set up custom title
        if (customTitleSupported) {
    	    Window window= getWindow();
            window.setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.titlebar_items);  
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
    protected void onResume() {
    	super.onResume();
    	
    	//log.debug("OnResume :"+curFeedType);
    	//Init List data
        refreshListAllView(currentDateSort,curFeedType);
        
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
        
    //Called when device orientation changed (see: android:configChanges="orientation" in manifest file)
    //Avoiding to restart the activity (which causes a crash) when orientation changes during refresh in AsyncTask
    @Override
    public void onConfigurationChanged(Configuration newConfig){        
        super.onConfigurationChanged(newConfig);
        onrotaion=false;
    }
    
    private void refreshListAllView(String dateSort,String curFeedType){
       log.debug("refreshListView:all :curFeedType["+curFeedType+"]");

        Control control = ControlHelper.getControlContent(this, currentFontSize, currentBgColor,0);
        List<Feed> catagoryFeed = null;
        if(Constants.FEED_TYPE_BORAD.equalsIgnoreCase(curFeedType)){
           String[] feedTypes = new String[]{Constants.FEED_TYPE_BORAD_100_NEW,Constants.FEED_TYPE_BORAD_100,curFeedType};
           catagoryFeed = mDbFeedAdapter.getFeeds(feedTypes);//get Type BOARD_100_NEW
        }else if(Constants.FEED_TYPE_BORAD_100.equalsIgnoreCase(curFeedType)){
        	String[] feedTypes = new String[]{Constants.FEED_TYPE_BORAD_100_NEW,curFeedType,Constants.FEED_TYPE_BORAD};
            catagoryFeed = mDbFeedAdapter.getFeeds(feedTypes);//get Type BOARD_100_NEW
        }else if(Constants.FEED_TYPE_BORAD_100_NEW.equalsIgnoreCase(curFeedType)){
        	String[] feedTypes = new String[]{curFeedType,Constants.FEED_TYPE_BORAD_100,Constants.FEED_TYPE_BORAD};
            catagoryFeed = mDbFeedAdapter.getFeeds(feedTypes);//get Type BOARD_100_NEW
        }else{
           catagoryFeed = mDbFeedAdapter.getFeeds(curFeedType);//get Type BOARD
        }
        
        log.debug("catagoryFeed Size:"+catagoryFeed.size());
	    
		arrayCatagoryAdapter = new FeedMainAdapter(this, R.id.title, catagoryFeed,control);
		listCatagoryView.setAdapter(arrayCatagoryAdapter);
		
	   //restore scroll popsition
       if(listViewIndex_FeedMain !=0 && listViewTop_FeedMain != 0){
    	   log.debug("set position listView ,listViewIndex_FeedMain["+listViewIndex_FeedMain+"]listViewTop_FeedMain["+listViewTop_FeedMain+"]");
    	   listCatagoryView.setSelectionFromTop(listViewIndex_FeedMain, listViewTop_FeedMain);
       }
	       
    }
  
    //Click Item View Content
    public void onItemClick (AdapterView<?> parent, View v, int position, long id) {
    	//log.debug("onItemClick:ID["+id+"] parentID["+parent.getId()+"viewId["+v.getId()+"]");
    	
    	//Get Position in ListView
    	listViewIndex_FeedMain = listCatagoryView.getFirstVisiblePosition();
    	View v2 = listCatagoryView.getChildAt(0);
    	listViewTop_FeedMain = (v2 == null) ? 0 : v2.getTop();
    	
    	arrayCatagoryAdapter.setSelectItem(position);
    	arrayCatagoryAdapter.notifyDataSetChanged();
         
    	TextView titleView = (TextView) v.findViewById(R.id.title);
        TextView titleId = (TextView) v.findViewById(R.id.title_id); 
        TextView topicTypeView = (TextView) v.findViewById(R.id.topic_type);
        TextView authorView = (TextView) v.findViewById(R.id.author);
        
        //log.debug("topicTypeView:"+topicTypeView.getText().toString());
        
        if(topicTypeView != null){	
        	Feed feed = new Feed();
        	feed.setId(Utils.isNullLong(titleId.getText().toString()));
        	feed.setTitle(Utils.isNull(titleView.getText().toString()));
        	feed.setAuthor(Utils.isNull(titleView.getText().toString()));
        	feed.setType(Utils.isNull(topicTypeView.getText().toString()));
        	
        	new StartNewActivityInThread( dialog,feed).run();
        }
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);

    	/*switch(requestCode) {
	    	case NEXT_ACTIVITY_CODE:
	    	    if (resultCode == RESULT_OK)
	    	    	log.debug("NEXT_ACTIVITY_CODE["+NEXT_ACTIVITY_CODE+",resultCode["+resultCode+"]");
	    	    	dialog.dismiss();
	    	    	finish();
	    	    break;
	    	}*/
    }

    @Override
    protected Dialog onCreateDialog(int id) {
    	dialogDownload = null;
        switch (id) {
        	case SharedPreferencesHelper.DIALOG_UPDATE_PROGRESS:
        		dialogDownload = new ProgressDialog(this);
	            dialogDownload.setTitle(getResources().getText(R.string.loading));
	           // ((ProgressDialog)dialog).setIcon(R.drawable.ic_dialog);
	            dialogDownload.setMessage(getResources().getText(R.string.downloading));
	            dialogDownload.setIndeterminate(true);
	            dialogDownload.setCancelable(false);
	            break;
            default:
            	dialogDownload = null;
        }
        return dialogDownload;
    }

    public class StartNewActivityInThread {
	  private ProgressDialog dialog;
	  private Feed feed;

      public StartNewActivityInThread(ProgressDialog dialog,Feed feed){
	    this.feed = feed;
      }
	  public void run() {
        try {
        	Intent intent = null;
        	
        	intent = new Intent(FeedMainActivity.this, FeedMainItemActivity.class);
        	intent.putExtra("ACT_","FeedMainActivity");
        	intent.putExtra("FEED",feed);
	    	intent.putExtra("ACTION","NEW");
	    	intent.putExtra("listViewIndex_FeedMain", listViewIndex_FeedMain);
			intent.putExtra("listViewTop_FeedMain", listViewTop_FeedMain);
	    	//intent.putExtra("APP_CONFIG_MAP", appConfig);
	        startActivity(intent);

            finish();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	  }
	  
	  public void release(){
		  try{
			  log.debug("Thread release");
		     // Thread.sleep(1000);
		      //dialog.dismiss();
		      
		      ThreadHelper.timerDelayRemoveDialog(10, dialog);
		  }catch(Exception e){
			  e.printStackTrace();
		  }
	  }
    }
    
    private class UpdateFeedTask extends AsyncTask<String,Void, Boolean> {
    	private long feedId = -1;
    	private long lastItemIdBeforeUpdate = -1;
    	private String curFeedType1 = "";
    	
    	public UpdateFeedTask() {
    		super();
    	}
        protected Boolean doInBackground(String...params) {
        	try{
        		// String updateType = (String)params[0];//NEW,ALL,FAV
        		 curFeedType1 = params[1];//Article , Board
        		 JSoupHelperNoAuthen jsoup = new JSoupHelperNoAuthen();
    			 List<Feed> resourceFeeds = jsoup.getFeedCatalogsFromDropboxDB(curFeedType1); 
	       	     if(resourceFeeds != null && resourceFeeds.size() >0){
	    			 for(Feed f:resourceFeeds){
		       	    	 boolean feedExist = mDbFeedAdapter.isFeedExist(f.getId());
		       	    	 log.debug("feedId:"+f.getId()+",Exist:"+feedExist+",Link:"+f.getLink()+",OrgLink:"+f.getOrgLink());
		       	    	 if(feedExist == false){
		       	    		log.debug("Insert");
		       	    		mDbFeedAdapter.addFeed(f);
		       	    	 }else{
		       	    		log.debug("Update");
		       	    		f.setUpdateDate(new Date());
		       	    		mDbFeedAdapter.updateFeedHead(f);
		       	    	 }
		       	     }//for
	       	    }
	       	  dialogShow = true;
        	}catch(Exception e){
        		e.printStackTrace();
        	}
            return new Boolean(true);
        }
        
        protected void onPreExecute() {
        	//log.debug("onPreExecute");
        	showDialog(SharedPreferencesHelper.DIALOG_UPDATE_PROGRESS);
        }

        protected void onPostExecute(Boolean result) {		
        	//log.debug("onPostExecute");
        	
            refreshListAllView(currentDateSort,curFeedType1);
            //log.debug("dialogDownload isShowing["+dialogDownload.isShowing()+"]");
            if(dialogDownload != null && dialogDownload.isShowing()){
                dismissDialog(SharedPreferencesHelper.DIALOG_UPDATE_PROGRESS);
            }
        	dialogShow = false;
        } 
    }
}