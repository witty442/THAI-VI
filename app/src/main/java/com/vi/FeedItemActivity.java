package com.vi;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Configuration;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.vi.adapter.FeedItemAdapter;
import com.vi.adapter.GotoPageArrayAdapter;
import com.vi.common.Control;
import com.vi.common.Display;
import com.vi.common.Feed;
import com.vi.common.Item;
import com.vi.parser.JSoupHelperAuthen;
import com.vi.parser.JSoupHelperNoAuthen;
import com.vi.storage.DBAdapter;
import com.vi.storage.DBSchema;
import com.vi.storage.SharedPreferencesHelper;
import com.vi.utils.Constants;
import com.vi.utils.DebugUtils;
import com.vi.utils.FileUtil;
import com.vi.utils.LogUtils;
import com.vi.utils.NetworkUtils;
import com.vi.utils.PhoneProperty;
import com.vi.utils.Utils;


public class FeedItemActivity extends Activity implements OnItemClickListener,OnClickListener{

    private ProgressDialog dialog = null;
    private ProgressDialog dialogDownload = null;
    private DBAdapter mDbFeedAdapter;
    private int totalPage = 0;
    private int currentPage = 1;
    //private boolean firstBookmarkCurPageLoad = false;
    private boolean customTitleSupported;
    private Feed currentFeed ;
    private Item currentItem ;

    private Button controlBackBtn;
    private Button controlMorePageBtn;
    private TextView titleCenterTxtView;
    private Button gotoPageBtn;
    private Button controlMoreControlBtn;
    private Button controlRefreshBtn;
    private int windowWidth = 0;
    StringBuffer contentDesc = new StringBuffer("");
    String ACT_ = "FeedMainItemActivity";

    //Setting Control
    private String currentBgColor = "bg_color_pantip";
    private String currentFontSize = "18";
    private String userName = "";
    private String password = "";
    private String loginMsg = "";

    private boolean dialogShow = false;
    private int topicCurPage = 0;
    //Logs
    StringBuffer errorMsg = new StringBuffer("");
    View footerView;
    //Dymamic TextView 
    String[] contentArray = null;
    int contentArraySize = 0; // total number of textviews to add
    TextView[] contentTextViews = null; // create an empty array;
    ListView content_list_view;
    List<Display> contentArrayAdapterList =  new ArrayList<Display>();
    List<Display> contentArrayAllAdapterList =  new ArrayList<Display>();
    private PopupWindow popGotoPageWindow;
    private PopupWindow popControlWindow;
    private PopupWindow popLinkWindow;
    LinearLayout gotopage_layout = null;

    private int listViewIndex=0;
    private int listViewTop=0;
    private int item_listViewIndex =0;
    private int item_listViewTop= 0;
    //Prev Position ListView FeedMainActivity
    private int listViewIndex_FeedMain = 0;
    private int listViewTop_FeedMain = 0;

    private JSoupHelperAuthen jsoupAuthen = null;
    private String auhtenMsg = "";
    THAIVI thaiVI = null;
    boolean isOnline = false;
    PhoneProperty phoneProperty = null;
    boolean onrotaion=true;
    LogUtils log = new LogUtils("FeedItemActivity");
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //log.debug("onrotation["+onrotaion+"]");
        if(onrotaion){

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
            userName = settingS.getSetting("username","");
            password = settingS.getSetting("password","");
            loginMsg = settingS.getSetting("loginmsg","");

            //log.debug("currentFontSize:"+currentFontSize);

            mDbFeedAdapter = new DBAdapter(this);
            mDbFeedAdapter.open();

            Bundle extras = getIntent().getExtras();
            if(extras != null){
                currentFeed = extras.get("FEED")!= null?(Feed)extras.get("FEED") : null;
                currentItem = extras.get("FEED_ITEM")!= null?(Item)extras.get("FEED_ITEM") : null;
                topicCurPage = extras.getInt("TOPIC_CUR_PAGE");

                listViewIndex = extras.getInt("listViewIndex");
                listViewTop = extras.getInt("listViewTop");

                listViewIndex_FeedMain = extras.getInt("listViewIndex_FeedMain");
                listViewTop_FeedMain = extras.getInt("listViewTop_FeedMain");

                //log.debug("listViewIndex:"+listViewIndex);
                //log.debug("listViewTop:"+listViewTop);

                //Last_position ->10,111
                //log.debug("LastPosition["+Utils.isNull(currentItem.getLastPosition())+"]");

                if(currentItem != null && !Utils.isNull(currentItem.getLastPosition()).equals("")){
                    String[] lastPositionStr = Utils.isNull(currentItem.getLastPosition()).split(",");
                    item_listViewIndex = Utils.isNullInt(lastPositionStr[0]);
                    item_listViewTop = Utils.isNullInt(lastPositionStr[1]);

                    //log.debug("item_listViewIndex["+item_listViewIndex+"]item_listViewTop["+item_listViewTop+"]");
                }

                //log.debug("FeedItemActivity listViewIndex["+listViewIndex+"]listViewTop["+listViewTop+"] topicCurPage["+topicCurPage+"]");

                /** Get Current Page **/
                currentPage = Utils.isNullInt(extras.getString("CURRENT_PAGE"),1); //default 1
                ACT_ = Utils.isNull(extras.getString("ACT_"),"FeedMainItemActivity");//default : "FeedMainItemActivity";
            }

            //relogin Authen Thaivi Case Stock100 New and jsoupAuthen==null
            if(Constants.FEED_TYPE_BORAD_100_NEW.equalsIgnoreCase(currentFeed.getType())){
                log.debug("jsoupAuthen:"+jsoupAuthen);
                if(jsoupAuthen==null){
                    if( isOnline && "login_pass".equals(loginMsg) && !"".equals(userName) && !"".equals(password)){
                        //Relogin Case Object Fail
                        try{
                            jsoupAuthen = JSoupHelperAuthen.newInstance(userName, password);
                            log.debug("Relogin jsoupAuthen[Global]:"+jsoupAuthen);
                            thaiVI.setThaiviAuthen(jsoupAuthen);//Update Global
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            /** Set Custom Title Bar **/
            customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
            setContentView(R.layout.feed_item_layout);
            customTitleBar((currentPage)+"");

            /** Get Object */
            //titleView = (TextView) findViewById(R.id.title);
            content_list_view = (ListView) findViewById(R.id.content_list_view);
            controlBackBtn =(Button)findViewById(R.id.controlBackBtn);

            gotopage_layout =(LinearLayout)findViewById(R.id.gotopage_layout);
            gotoPageBtn = (Button)findViewById(R.id.controlGotoPageBtn);
            controlMoreControlBtn = (Button)findViewById(R.id.controlConfigBtn);
            //imageDispStatusId =(Button)findViewById(R.id.imageDispStatusId);
            controlRefreshBtn = (Button)findViewById(R.id.controlRefreshBtn);
            titleCenterTxtView =(TextView)findViewById(R.id.titleCenter);

            /** Set Listener Button or View **/
            controlBackBtn.setOnClickListener(this);
            gotoPageBtn.setOnClickListener(this);
            controlMoreControlBtn.setOnClickListener(this);
            controlRefreshBtn.setOnClickListener(this);
            titleCenterTxtView.setOnClickListener(this);

            /** Innit popLinkWindow */
            popLinkWindow = initLinkPopup();

            /** Custom Width **/
            customWidth();

            isOnline = NetworkUtils.isOnline(this);
            if( !isOnline){
                if( !Constants.FEED_TYPE_ARTICLE.equals(currentFeed.getType())){
                    Toast.makeText(FeedItemActivity.this, getResources().getString(R.string.no_internet_msg), Toast.LENGTH_LONG).show();
                }
            }

            /** Load Content **/
            new UpdateFeedTask().execute();

        }//check Rotation

    }

    public void onClick(View v) {
        if(v.equals(controlBackBtn)){
            backAction();
        }else if(v.equals(controlMorePageBtn)){
            //Reset Position ListView
            item_listViewIndex =0;
            item_listViewTop= 0;

            morePageTopic();
        }else if(v.equals(gotoPageBtn) || v.equals(titleCenterTxtView)){
            if(totalPage > 1){
                item_listViewIndex =0;
                item_listViewTop= 0;
                displayGoToPagePopup();
            }
        }else if(v.equals(controlMoreControlBtn)){
            displayPopupControl();
        }else if(v.equals(controlRefreshBtn)){
            refreshTopic();
        }else{
            //throw new IllegalArgumentException("What was clicked?");
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

    public PopupWindow initLinkPopup(){
        log.debug("displayLinkPopup");

        LayoutInflater layoutInflater = (LayoutInflater)this.getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_open_link, null);

        popLinkWindow = new PopupWindow(layout, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        popLinkWindow.setBackgroundDrawable(new BitmapDrawable());
        popLinkWindow.setOutsideTouchable(true);
        popLinkWindow.setFocusable(true);
        popLinkWindow.setTouchable(true);

        int y = 150;
        int x = 20;
        //log.debug("x["+x+"]y["+y+"]");
        //popLinkWindow.showAtLocation(layout, Gravity.TOP, x, y);

        return popLinkWindow;
    }

    public void displayPopupControl(){
        log.debug("displayPopupControl");

        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_control, null);

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


        TableRow tableRowSaveId = (TableRow)layout.findViewById(R.id.tableRowSaveId);
        TableRow tableRowBookmarkId = (TableRow)layout.findViewById(R.id.tableRowBookmarkId);
        TableRow tableRowDelId = (TableRow)layout.findViewById(R.id.tableRowDelId);
        TableRow tableRowRefreshId = (TableRow)layout.findViewById(R.id.tableRowRefreshId);
        TableRow tableRowHomeId = (TableRow)layout.findViewById(R.id.tableRowHomeId);
        TableRow tableRowTopId = (TableRow)layout.findViewById(R.id.tableRowTopId);
        TableRow tableRowBottomId = (TableRow)layout.findViewById(R.id.tableRowBottomId);

        //Show
        tableRowSaveId.setVisibility(View.VISIBLE);
        tableRowBookmarkId.setVisibility(View.VISIBLE);
        tableRowDelId.setVisibility(View.VISIBLE);
        tableRowRefreshId.setVisibility(View.VISIBLE);
        tableRowHomeId.setVisibility(View.VISIBLE);
        tableRowTopId.setVisibility(View.VISIBLE);
        tableRowBottomId.setVisibility(View.VISIBLE);

        Button saveButton = (Button)layout.findViewById(R.id.controlSaveBtn);
        Button bookmarkButton = (Button)layout.findViewById(R.id.controlBookmarkBtn);
        Button delButton = (Button)layout.findViewById(R.id.controlDelBtn);
        Button refreshButton = (Button)layout.findViewById(R.id.controlRefreshBtn);
        Button homeButton = (Button)layout.findViewById(R.id.controlHomeBtn);
        Button controlTopBtn = (Button)layout.findViewById(R.id.controlTopBtn);
        Button controlBottomBtn = (Button)layout.findViewById(R.id.controlBottomBtn);

        //Hide Del Button
        if( !Utils.isNull(currentItem.getPathFile()).equals("") || currentItem.isFav()){
        }else{
            tableRowDelId.setVisibility(View.GONE);
        }

        if(Constants.FEED_TYPE_ARTICLE.equalsIgnoreCase(currentFeed.getType())){
            tableRowSaveId.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    saveTopic();
                    popControlWindow.dismiss();
                }
            });
            saveButton.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    saveTopic();
                    popControlWindow.dismiss();
                }
            });

            //hide bookmark
            tableRowBookmarkId.setVisibility(View.GONE);
            delButton.setText("ลบข้อมูล");
        }else{
            tableRowBookmarkId.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    saveBookmark();
                    popControlWindow.dismiss();
                }
            });
            bookmarkButton.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    saveBookmark();
                    popControlWindow.dismiss();
                }
            });

            //hide save
            tableRowSaveId.setVisibility(View.GONE);
            delButton.setText("ลบบุ็คมาร์ค");
        }

        tableRowDelId.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                removeTopic();
                popControlWindow.dismiss();
            }
        });
        delButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                removeTopic();
                popControlWindow.dismiss();
            }
        });

        tableRowRefreshId.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                refreshTopic();
                popControlWindow.dismiss();
            }
        });

        refreshButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                refreshTopic();
                popControlWindow.dismiss();
            }
        });

        tableRowHomeId.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(FeedItemActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        homeButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(FeedItemActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        /** GotoTop **/
        tableRowTopId.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                gotoTopTopic();
                popControlWindow.dismiss();
            }
        });
        controlTopBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                gotoTopTopic();
                popControlWindow.dismiss();
            }
        });

        /** GotoBottom **/
        tableRowBottomId.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                gotoBottomTopic();
                popControlWindow.dismiss();
            }
        });
        controlBottomBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                gotoBottomTopic();
                popControlWindow.dismiss();
            }
        });

    }

    public void displayGoToPagePopup(){
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_goto_page, null);

        popGotoPageWindow = new PopupWindow(layout, phoneProperty.width-150,LayoutParams.WRAP_CONTENT);
        popGotoPageWindow.setBackgroundDrawable(new BitmapDrawable());
        popGotoPageWindow.setOutsideTouchable(true);
        popGotoPageWindow.setFocusable(true);
        popGotoPageWindow.setTouchable(true);

        int y = 150;
        int x = 20;
        //log.debug("x["+x+"]y["+y+"]");
        popGotoPageWindow.showAtLocation(layout, Gravity.TOP, x, y);

        ListView gotoPageListView = (ListView)layout.findViewById(R.id.gotoPageListView);
        List<String> gotoPageArray = new ArrayList<String>();
        for(int p=0;p<totalPage;p++){
            gotoPageArray.add((p+1)+"");
        }

        GotoPageArrayAdapter arrayAdapter = new GotoPageArrayAdapter(this, R.layout.goto_page_row,gotoPageArray);
        gotoPageListView.setAdapter(arrayAdapter);

        gotoPageListView.setOnItemClickListener(new OnItemClickListener() {
            //Click Item View Content
            public void onItemClick (AdapterView<?> parent, View v, int position, long id) {
                log.debug("onItemClick:ID["+id+"] parentID["+parent.getId()+"viewId["+v.getId()+"]");
                TextView gotoPageID = (TextView) v.findViewById(R.id.gotoPageId);
                log.debug("gotoPageID:"+gotoPageID.getText().toString());
                int gotoPage = Utils.isNullInt(gotoPageID.getText().toString());
                log.debug("gotoPage:"+gotoPage);
                if(currentPage != gotoPage){
                    /** Load Next Page  **/
                    currentPage = gotoPage;
                    currentItem.setCurPage(currentPage);

                    /** Load Content **/
                    new UpdateFeedTask().execute();
                }
                popGotoPageWindow.dismiss();
            }
        });
    }

    private void initControlContent() {
        log.debug("currentFontSize:"+currentFontSize);
        Control control = new Control();
        currentBgColor = "bg_color_day";
        //Set Control
        control.setCurrentStyle(currentBgColor);
        control.setTextSize(Integer.parseInt(currentFontSize));
        control.setTextColor(getResources().getColor(R.color.font_color_day));
        control.setBgColor(getResources().getColor(R.color.bg_color_day));
        setListViewControl(control);

        /** reset Cutom title bar **/
        customTitleBar2((currentPage)+"");

        //restore scroll popsition
        if(item_listViewIndex !=0 && item_listViewTop != 0){
            content_list_view.setSelectionFromTop(item_listViewIndex, item_listViewTop);
        }
    }

    //Change Font Size Or Color
    private void setListViewControl(Control c) {
        Date startTime = new Date();

        FeedItemAdapter arrayContentAdapter = new FeedItemAdapter(this,popLinkWindow, R.id.title, contentArrayAllAdapterList,c,jsoupAuthen);
        //log.debug("content_list_view:"+content_list_view);
        //log.debug("contentArrayAdapterList Size:"+contentArrayAdapterList.size());

        content_list_view.setAdapter(arrayContentAdapter);
        arrayContentAdapter.setCurrentFeed(currentFeed);
        arrayContentAdapter.setCurrentItem(currentItem);
        arrayContentAdapter.setListViewIndex(item_listViewIndex);
        arrayContentAdapter.setListViewTop(item_listViewTop);

        arrayContentAdapter.setListViewIndex_FeedMain(listViewIndex_FeedMain);
        arrayContentAdapter.setListViewTop_FeedMain(listViewTop_FeedMain);
        arrayContentAdapter.setTopicCurPage(topicCurPage);

        /** Notify data Change **/
        arrayContentAdapter.notifyDataSetChanged();
        //content_list_view.setOnItemClickListener(this);

        log.debug("** Time setListViewControl Display Adapter:"+((new Date().getTime())-startTime.getTime()));
    }

    private void addButtonMorePageInListView(){
        int footerViewCount  = content_list_view.getFooterViewsCount();
        log.debug("totalPage:"+totalPage);
        log.debug("footerViewCount:"+footerViewCount);

        if(totalPage >1 && footerViewCount <= 0){

            if(footerView ==null){
                footerView = ((LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.feed_item_list_footer, null, false);
            }

            controlMorePageBtn = (Button)footerView.findViewById(R.id.controlMorePageBtn);
            controlMorePageBtn.setOnClickListener(this);

            // Adding button to listview at footer
            content_list_view.addFooterView(footerView);
        }
    }

    /**
     * Load Content All
     */
    private void loadContent(){
        JSoupHelperNoAuthen jsoup = new JSoupHelperNoAuthen();
        contentArrayAdapterList = new ArrayList<Display>();
        Date st = new Date();
        try{
            //log.debug("CurrentPage["+currentPage+"]currentItem.getCurPage("+currentItem.getCurPage()+")");
            //log.debug("PathFile["+currentItem.getPathFile()+"]");
            //log.debug("fullURL["+currentItem.getLink()+"]");
            //log.debug("jsoupAuthen["+jsoupAuthen+"]");

            if( Utils.isNull(currentItem.getPathFile()).equals("")){
                try{
                    /** Case 1 Load From Dropbox (Article ONLY)**/
                    if(currentItem.getLink().indexOf("dropbox") != -1){
                        log.debug("Get From Dropbox");

                        st = new Date();
                        contentDesc = jsoup.getContentFromDropBox(currentItem);
                        log.debug("** Time loadContent.jsoup.getContentFromDropBox:"+((new Date().getTime())-st.getTime()));

                        //contentDesc =  ArticleUtils.customArticle(contentDesc.toString());
                        //contentDesc = new StringBuffer(StringEscapeUtils.unescapeHtml(contentDesc.toString()));

                        /** Add To Adapter **/
                        Display t = new Display();
                        t.setContent(Utils.trim(contentDesc.toString()));
                        t.setType(Constants.FEED_TYPE_ARTICLE);

                        contentArrayAdapterList.add(t);
                    }else{
                        /**Case 2 (ThaiVI.org) NEED AUTHEN**/
                        if(Constants.FEED_TYPE_BORAD_100_NEW.equalsIgnoreCase(currentFeed.getType())){
                            currentPage = currentItem.getCurPage()==0?currentPage:currentItem.getCurPage(); //Start
                            if(jsoupAuthen != null){
                                log.debug("1.totalReply:"+currentItem.getTotalReply());
                                //load content

                                /** Check Topic is Save db **/

                                //default to id 22 (member general -order by letter)
                                if(currentItem.getFeedId() == 22){
                                    //update 14(order by stock) =21(old) both
                                   Item Item14=  mDbFeedAdapter.getItemDBByTitle(20,currentItem.getTitle());
                                    System.out.println("Item14:"+Item14);
                                    if(Item14 != null) {
                                        System.out.println("Title:"+Item14.getTitle());
                                        //set property to display
                                        currentItem.setTopicCurPage(Item14.getTopicCurPage());
                                        currentItem.setLastPosition(Item14.getLastPosition());
                                        currentPage = Item14.getCurPage();

                                        String[] lastPositionStr = Utils.isNull(currentItem.getLastPosition()).split(",");
                                        item_listViewIndex = Utils.isNullInt(lastPositionStr[0]);
                                        item_listViewTop = Utils.isNullInt(lastPositionStr[1]);
                                    }
                                }

                                /** Get Data From Web **/
                                st = new Date();
                                contentArrayAdapterList = jsoupAuthen.getContentFromWeb(userName,password,currentItem,currentPage);
                                log.debug("** Time loadContent.jsoupAuthen.getContentFromWeb:"+((new Date().getTime())-st.getTime()));

                                //log.debug("2.totalReply:"+currentItem.getTotalReply());

                                //Update Item
                                ContentValues values = new ContentValues();
                                //	values.put(DBSchema.ItemSchema.COLUMN_UPDATE_DATE,(new Date()).getTime());
                                values.put(DBSchema.ItemSchema.COLUMN_TOTAL_REPLY, currentItem.getTotalReply());

                                if(mDbFeedAdapter != null){
                                    mDbFeedAdapter.updateItem(currentItem.getId(), values);
                                }

                                //Calculate TotalPage
                                totalPage = jsoupAuthen.calcTotalPage(currentItem.getTotalReply(), Constants.THAIVI_ROW_PER_PAGE);

                            }else{
                                auhtenMsg = getResources().getString(R.string.no_authen_thaivi_msg);// "";
                            }
                        }else{
                            /**Case 2 (ThaiVI.org) NO AUTHEN**/
                            currentPage = currentItem.getCurPage()==0?currentPage:currentItem.getCurPage(); //Start
                            log.debug("before.totalReply:"+currentItem.getTotalReply());

                            //load content
                            st = new Date();
                            contentArrayAdapterList = jsoup.getContentFromWeb(currentItem,currentPage);
                            log.debug("** Time loadContent.jsoup.getContentFromWeb:"+((new Date().getTime())-st.getTime()));

                            log.debug("after.totalReply:"+currentItem.getTotalReply());

                            //Update Item
                            ContentValues values = new ContentValues();
                            //values.put(DBSchema.ItemSchema.COLUMN_UPDATE_DATE,(new Date()).getTime());
                            values.put(DBSchema.ItemSchema.COLUMN_TOTAL_REPLY, currentItem.getTotalReply());

                            if(mDbFeedAdapter != null){
                                mDbFeedAdapter.updateItem(currentItem.getId(), values);
                            }
                            //Calculate TotalPage
                            totalPage = jsoup.calcTotalPage(currentItem.getTotalReply(), Constants.THAIVI_ROW_PER_PAGE);
                        }
                        log.debug("Content List Size:"+contentArrayAdapterList.size());

                    }//if
                }catch(Exception e){
                    e.printStackTrace();
                    errorMsg.append("\n Error Get Content Web \n "+DebugUtils.genStackErrorMsg(e.getStackTrace()));
                }
            }else{
                /**Case 3 Load From File in Local (article only) **/
                try{
                    /** Case 3.2 Load data from local and show **/
                    log.debug("Get Content From pathFile:"+currentItem.getPathFile());
                    contentDesc = new StringBuffer(FileUtil.readFile(currentItem.getPathFile(),"UTF-8"));

                    //contentDesc =  ArticleUtils.customArticle(contentDesc.toString());
                    //contentDesc = new StringBuffer(StringEscapeUtils.unescapeHtml(contentDesc.toString()));

                    /** Add To Adapter **/
                    Display t = new Display();
                    t.setType(Constants.FEED_TYPE_ARTICLE);
                    t.setContent(Utils.trim(contentDesc.toString()));
                    contentArrayAdapterList.add(t);

                }catch(Exception e){
                    e.printStackTrace();
                }
            }//end if

            /** Set Topic Title **/
            if(contentArrayAdapterList != null && contentArrayAdapterList.size() >0){
                //reste Array ALL
                contentArrayAllAdapterList = new ArrayList<Display>();

                //add title topic
                String formatDate = getResources().getString(R.string.date_format_pattern);
                SimpleDateFormat df = new SimpleDateFormat(formatDate,new Locale("TH","th"));

                Display topicTitle = new Display();
                topicTitle.setType(Constants.FEED_TYPE_ARTICLE);
                topicTitle.setTopicTitle(true);

                String content = currentItem.getTitle();//StringEscapeUtils.unescapeHtml(currentItem.getTitle());
                if(Constants.FEED_TYPE_ARTICLE.equalsIgnoreCase(currentFeed.getType())){
                    content +="\n @เจ้าของบทความ "+ (( Utils.isNull(currentItem.getAuthor()).equals(""))?Utils.isNull(currentFeed.getAuthor()):Utils.isNull(currentItem.getAuthor()));
                }else{
                    content +="\n @เจ้าของกระทู้ "+ (( Utils.isNull(currentItem.getAuthor()).equals(""))?Utils.isNull(currentFeed.getAuthor()):Utils.isNull(currentItem.getAuthor()));
                }
                content +=" วันที่ "+ df.format(currentItem.getCreateDate());
                content +="\n @แหล่งข้อมูล "+ currentItem.getOrgLink() +"\n";

                topicTitle.setContent(Utils.trim(content));
                //chekc show image

                log.debug("PathFile:"+currentItem.getPathFile());
                if( !Utils.isNull(currentItem.getPathFile()).equals("") || currentItem.isFav()){
                    if( !Utils.isNull(currentItem.getPathFile()).equals("")){
                        topicTitle.setShowImage("save");
                    }else{
                        if(currentItem.isFav()){
                            topicTitle.setShowImage("bookmark");
                        }
                    }
                }else{
                    topicTitle.setShowImage("");
                }

                contentArrayAllAdapterList.add(topicTitle);
                contentArrayAllAdapterList.addAll(contentArrayAdapterList);
            }

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(jsoup != null){
                jsoup = null;
            }
        }
    }

    private boolean displayItemView() {
        StringBuffer errorMsg = new StringBuffer("");
        log.debug("*****DisplayItemView*********");
        try{
            // set item as read (case when item is displayed from next/previous contextual menu or buttons)
            ContentValues values = new ContentValues();
            values.put(DBSchema.ItemSchema.COLUMN_READ, DBSchema.ON);
            //values.put(DBSchema.ItemSchema.COLUMN_UPDATE_DATE, new Date().getTime());
            values.put(DBSchema.ItemSchema.COLUMN_COUNT_OPEN, currentItem.getCountOpen()+1);

            mDbFeedAdapter.updateItem(currentItem.getId(), values);
        }catch(Exception e){
            errorMsg.append("\n Get Content Error: \n "+ DebugUtils.genStackErrorMsg(e.getStackTrace()));
        }finally{
            try{
                DebugUtils.debugToFile(errorMsg.toString());
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Control Back Button
     */
    private void backAction(){
        if("FeedMainItemLocalActivity".equals(ACT_)){
            Intent intent = new Intent(FeedItemActivity.this, FeedMainItemLocalActivity.class);
            intent.putExtra("FEED_TITLE", currentFeed.getTitle());
            intent.putExtra("FEED_TYPE", currentFeed.getType());
            intent.putExtra("TOPIC_CUR_PAGE", topicCurPage);
            intent.putExtra("listViewIndex", listViewIndex);
            intent.putExtra("listViewTop", listViewTop);
            startActivity(intent);
        }else if("FeedMainItemLocal100Activity".equals(ACT_)){
            Intent intent = new Intent(FeedItemActivity.this, FeedMainItemLocal100Activity.class);
            intent.putExtra("FEED_TITLE", currentFeed.getTitle());
            intent.putExtra("FEED_TYPE", currentFeed.getType());
            intent.putExtra("TOPIC_CUR_PAGE", topicCurPage);
            intent.putExtra("listViewIndex", listViewIndex);
            intent.putExtra("listViewTop", listViewTop);
            startActivity(intent);
        }else{
            Intent intent = new Intent(FeedItemActivity.this, FeedMainItemActivity.class);
            //FEED_TITLE,FEED_ID
            intent.putExtra("FEED", currentFeed);
            intent.putExtra("TOPIC_CUR_PAGE", topicCurPage);
            intent.putExtra("ACTION", "NO_FRESH");
            intent.putExtra("listViewIndex", listViewIndex);
            intent.putExtra("listViewTop", listViewTop);
            intent.putExtra("listViewIndex_FeedMain", listViewIndex_FeedMain);
            intent.putExtra("listViewTop_FeedMain", listViewTop_FeedMain);

            startActivity(intent);
        }

        //save last position read
        if(currentItem != null && currentItem.isFav() && !Constants.FEED_TYPE_ARTICLE.equals(currentFeed.getType()) ){
            saveBookmark(false);
        }

        finish();
    }

    public void onItemClick (AdapterView<?> parent, View v, int position, long id) {
        log.debug("ItemClick");
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        log.debug("KeyCode:"+keyCode+",:backCode:"+KeyEvent.KEYCODE_MENU);
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

    private void customWidth(){
        if(windowWidth != 0){
            //Set author ,pubDate
            //authorView.setWidth(windowWidth/2);
            //pubdateView.setWidth(windowWidth/2);
        }
    }

    public void saveBookmark(){
        saveBookmark(true);
    }
    //Save Bookmark
    public void saveBookmark(boolean showMsg){

        item_listViewIndex = content_list_view.getFirstVisiblePosition();
        View v2 = content_list_view.getChildAt(0);
        item_listViewTop = (v2 == null) ? 0 : v2.getTop();
        //log.debug("saveBookmark:item_listViewIndex["+item_listViewIndex+"]item_listViewTop["+item_listViewTop+"]");

        if(showMsg){
            String msg = "Bookmark หน้าที่ "+(currentPage)+" Success ";
            Toast.makeText(FeedItemActivity.this, msg, Toast.LENGTH_LONG).show();
        }
        /** Update to Database **/
        ContentValues values = new ContentValues();
        //values.put(DBSchema.ItemSchema.COLUMN_UPDATE_DATE,(new Date()).getTime());
        values.put(DBSchema.ItemSchema.COLUMN_FAV, DBSchema.ON);
        values.put(DBSchema.ItemSchema.COLUMN_CUR_PAGE, currentPage);
        values.put(DBSchema.ItemSchema.COLUMN_LAST_POSITION, item_listViewIndex+","+item_listViewTop);

        if(Constants.FEED_TYPE_BORAD_100.equals(currentFeed.getType()) ||
                Constants.FEED_TYPE_BORAD_100_NEW.equals(currentFeed.getType())){

            if(Constants.FEED_TYPE_BORAD_100.equals(currentFeed.getType())){ //21,14
                //default to id 14 (member general -order by letter)
                if(currentItem.getFeedId() == 21){
                    //update 14 both
                    mDbFeedAdapter.updateItemByTitle(14,currentItem.getTitle(), values);
                }else{
                    //update cur
                    mDbFeedAdapter.updateItem(currentItem.getId(), values);
                }
            }else{
                //default to id 20 (member Assosiation -order by letter)
                if(currentItem.getFeedId() == 22){ //22,20
                    //update 20 both
                    mDbFeedAdapter.updateItemByTitle(20,currentItem.getTitle(), values);
                }else{
                    //update cur 20
                    mDbFeedAdapter.updateItem(currentItem.getId(), values);
                }
            }
        }else{
            mDbFeedAdapter.updateItem(currentItem.getId(), values);

        }
    }

    public void removeTopic(){
        log.debug("RemoveTopic currentItem.getId["+currentItem.getId()+"]");
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
            //mDbFeedAdapter.updateItem(currentItem.getId(), values);
            mDbFeedAdapter.updateItemByTitle(currentItem.getTitle(),values);
        }else{
            mDbFeedAdapter.updateItem(currentItem.getId(), values);
        }
 	   
 	   	/*log.debug("before item id["+currentItem.getId()+"]title["+currentItem.getTitle()+"],fav["+currentItem.isFav()+"]");
 		
 	   	//recheck 
 	   	Item currentItemTest = mDbFeedAdapter.getItem(currentItem.getId());
 		log.debug("after item id["+currentItemTest.getId()+"]title["+currentItemTest.getTitle()+"],fav["+currentItemTest.isFav()+"]");
 		*/

        String msg = "Remove topic success";
        Toast.makeText(FeedItemActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    //Save File to Local
    public void saveTopic(){
        String msg = "Please waiting... downloading";
        Toast.makeText(FeedItemActivity.this, msg, Toast.LENGTH_LONG).show();

        if(Constants.FEED_TYPE_BORAD.equals(currentFeed.getType())
                || Constants.FEED_TYPE_BORAD_100.equalsIgnoreCase(currentFeed.getType())){

            JSoupHelperNoAuthen jsoup = new JSoupHelperNoAuthen();
            contentDesc = jsoup.getContentThaiVIBoardAllPageToSave(currentFeed,currentItem);
        }

        log.debug("content_description:"+contentDesc.toString());
        String pathFile = saveFile(String.valueOf(currentItem.getId()),contentDesc.toString());
        log.debug("pathFile:"+pathFile);

        msg = "Save Topic success";
        Toast.makeText(FeedItemActivity.this, msg, Toast.LENGTH_LONG).show();

        /** Update to Database **/
        ContentValues values = new ContentValues();
        values.put(DBSchema.ItemSchema.COLUMN_PATH_FILE, pathFile);
        values.put(DBSchema.ItemSchema.COLUMN_FAV, DBSchema.ON);
        values.put(DBSchema.ItemSchema.COLUMN_CUR_PAGE, currentPage);
        mDbFeedAdapter.updateItem(currentItem.getId(), values);
    }

    public void gotoTopTopic(){
        content_list_view.setSelectionFromTop(0, 0);
    }

    public void gotoBottomTopic(){

        int lastListViewIndex = 0;
        int lastListViewTop = 0;
        if(contentArrayAllAdapterList != null && contentArrayAllAdapterList.size() > 0){
            lastListViewIndex =contentArrayAllAdapterList.size();
        }

        log.debug("lastListViewIndex["+lastListViewIndex+"]lastListViewTop["+lastListViewTop+"]");
        content_list_view.setSelectionFromTop(lastListViewIndex, lastListViewTop);
    }
   
  /* public void deleteTopic(){
		String msg = "Delete Topic success";	
	   	Toast.makeText(FeedItemActivity.this, msg, Toast.LENGTH_LONG).show();
	   	mDbFeedAdapter.removeItem(currentItem.getId());
   }*/



    public void morePageTopic(){
        log.debug("MorePageTopic currentPage["+currentPage+":totalPage["+totalPage+"]");
        if(currentPage < (totalPage)){
            /** Load Next Page  **/
            currentPage += 1;
            currentItem.setCurPage(currentPage);

            /** Load Content **/
            new UpdateFeedTask().execute();
        }
    }

    public void refreshTopic(){
        /** refresh **/
        new UpdateFeedTask().execute();
    }

    public String saveFile(String fileName,String data){
        // TODO Auto-generated method stub
        String pathFile = "";
        try {
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File myNewFolder = new File(extStorageDirectory +"/"+ Constants.APP_FOLDER);
            log.debug("extStorageDirectory["+extStorageDirectory+"] appFolder Exist["+myNewFolder.exists()+"]");
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

    public void customTitleBar(String currentPage) {
        log.debug("customTitleBar currentPage["+currentPage+"]");
        // set up custom title
        if (customTitleSupported) {
            Window window = getWindow();
            window.setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.titlebar_display_items);
            // Button titleCenter = (Button) findViewById( R.id.titleCenter);
            // titleCenter.setText("หน้าปัจจุบัน :"+currentPage);
        }
    }

    public void customTitleBar2(String currentPage) {
        if(Constants.FEED_TYPE_ARTICLE.equalsIgnoreCase(currentFeed.getType())){
            //gotopage_layout.setVisibility(View.GONE);
            Button titleCenter = (Button) findViewById( R.id.titleCenter);
            Button controlGotoPageBtn = (Button) findViewById( R.id.controlGotoPageBtn);
            titleCenter.setVisibility(View.GONE);
            controlGotoPageBtn.setVisibility(View.GONE);
        }else{
            log.debug("customTitleBar2 currentPage["+currentPage+"]");
            Button titleCenter = (Button) findViewById( R.id.titleCenter);
            titleCenter.setText("หน้า "+currentPage +"/"+totalPage);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        log.debug("onStart");
    }

    @Override
    protected void onStop() {
        log.debug("onStop");
        super.onStop();
    }

    @Override
    protected void onResume() {
        //log.debug("onResume");
        super.onResume();
        displayItemView();
        if(dialogShow){
            dialogShow = false;
        }
    }

    @Override
    protected void onDestroy() {
        //log.debug("onDestroy");
        super.onDestroy();
        mDbFeedAdapter.close();
        errorMsg = null;
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(DBSchema.ItemSchema._ID, currentItem.getId());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    	/*switch(requestCode) {
	    	case KILL_ACTIVITY_CODE:
	    	    if (resultCode == RESULT_OK)
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

    /**
     * UpdateFeedTask
     * @author aa
     *
     */
    private class UpdateFeedTask extends AsyncTask<Feed, Void, Boolean> implements OnDismissListener{

        public UpdateFeedTask() {
            super();
        }
        protected Boolean doInBackground(Feed...params) {
            try {
                /** Load Content From Web **/
                loadContent();

                /** add more page in ListView **/
                addButtonMorePageInListView();

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
            /** Init Control Content **/
            initControlContent();

            // log.debug("dialogDownload isShowing["+dialogDownload.isShowing()+"]");
            if(dialogDownload != null && dialogDownload.isShowing()){
                dismissDialog(SharedPreferencesHelper.DIALOG_UPDATE_PROGRESS);
            }
            dialogShow = false;
            String msg = "";

            /** Check can load content **/
            if(contentArrayAllAdapterList == null || (contentArrayAllAdapterList != null && contentArrayAllAdapterList.size()==0)){
                //Check User Login
                if(Constants.FEED_TYPE_BORAD_100_NEW.equalsIgnoreCase(currentFeed.getType())){
                    boolean passAuthen = false;
                    log.debug("isOnline:"+isOnline);

                    if( isOnline && "login_pass".equals(loginMsg) && !"".equals(userName) && !"".equals(password)){
                        //Relogin Case Object Fail
                        try{
                            jsoupAuthen = JSoupHelperAuthen.newInstance(userName, password);
                            passAuthen = jsoupAuthen.verifyAuthen();

                            log.debug("passAuthen:"+passAuthen);

                            if( !passAuthen){
                                // auhtenMsg = " UserName["+userName+"] และ  Password[*******]ของ(Thaivi.org)ไม่ถูกต้อง หรือไม่มีสิทธิ   passAuthen[****]";
                                //jsoupAuthen.jsoupHelperAuthen = null;
                                jsoupAuthen = null;
                            }else{
                                //relogin
                                jsoupAuthen = JSoupHelperAuthen.newInstance(userName, password);
                            }

                            /** Load Content From Web **/
                            loadContent();

                            /** add more page in ListView **/
                            addButtonMorePageInListView();

                            dialogShow = true;

                            log.debug("Relogin jsoupAuthen[Global]:"+jsoupAuthen);
                            thaiVI.setThaiviAuthen(jsoupAuthen);//Update Global

                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    msg = auhtenMsg+ getResources().getString(R.string.refresh_again_msg);// "";
                }else{
                    msg = getResources().getString(R.string.refresh_again_msg);// "";
                }
                Toast.makeText(FeedItemActivity.this, msg, Toast.LENGTH_LONG).show();
                auhtenMsg = "";
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            this.cancel(true);
        }
    }
}