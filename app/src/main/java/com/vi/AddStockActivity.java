
package com.vi;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.vi.adapter.AddStockArrayAdapter;
import com.vi.common.Feed;
import com.vi.common.Item;
import com.vi.method.AddStockMethod;
import com.vi.parser.JSoupHelperAuthen;
import com.vi.parser.JSoupHelperNoAuthen;
import com.vi.storage.DBAdapter;
import com.vi.storage.DBSchema;
import com.vi.storage.SharedPreferencesHelper;
import com.vi.utils.Constants;
import com.vi.utils.LogUtils;
import com.vi.utils.NetworkUtils;
import com.vi.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class AddStockActivity extends Activity implements OnItemClickListener,OnClickListener  {

    private Button backBtn;
    private Button searchBtn;
    ListView stockListView;
    private DBAdapter mDbFeedAdapter;
    //Setting Control
    private String currentBgColor = "bg_color_pantip";
    private String currentFontSize = "19";
    int listViewIndex = 0;
    int listViewTop = 0;
    private String feedTitle  ="";
    private String feedType  ="";
    private boolean dialogShow = false;
    int retryConnect = 2;
    boolean isOnline = false;
    boolean onrotaion=true;
    int mSelectedItem = -1;
    AddStockArrayAdapter arrayItemAdapter = null;
    LogUtils log = new LogUtils("AddStockActivity");
    private JSoupHelperAuthen jsoupAuthen = null;
    private THAIVI thaiVI = null;
    private ProgressDialog dialogDownload;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        log.debug("Create AddStockActivity");
        super.onCreate(savedInstanceState);

        //log.debug("onrotation["+onrotaion+"]");
        if(onrotaion){
            //log.debug("MainActivity:OnCreate");
            //Allow Policy Thread
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            //Load Global Variable
            thaiVI  = ((THAIVI) this.getApplication());
            jsoupAuthen = thaiVI.getThaiviAuthen();

            //Load Setting
            SharedPreferencesHelper settingS = new SharedPreferencesHelper(this);
            currentFontSize = settingS.getSetting("textSize", "18");
            isOnline = NetworkUtils.isOnline(this);

            Bundle extras = getIntent().getExtras(); // Case Pass Param From Another Activity
            if (extras != null) {
                feedTitle =  Utils.isNull(extras.getString("FEED_TITLE"));
                feedType =  Utils.isNull(extras.getString("FEED_TYPE"));

                listViewIndex = extras.getInt("listViewIndex");
                listViewTop = extras.getInt("listViewTop");

            }

            setContentView(R.layout.popup_addstock_layout);
            /** Set Catagory */
            stockListView = (ListView) findViewById(R.id.stockListView);
            backBtn =(Button)findViewById(R.id.backBtn);
            searchBtn =(Button)findViewById(R.id.searchBtn);

            /** register itemListener **/
            stockListView.setOnItemClickListener(this);
            backBtn.setOnClickListener(this);
            searchBtn.setOnClickListener(this);

            if( !isOnline){
                Toast.makeText(AddStockActivity.this, getResources().getString(R.string.no_internet_msg), Toast.LENGTH_LONG).show();
            }

            mDbFeedAdapter = new DBAdapter(this);
            mDbFeedAdapter.open();

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        log.debug("KeyCode:" + keyCode + ",:backCode:" + KeyEvent.KEYCODE_BACK);
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            backAction();
            return true;
        }
        return false;
    }

    public void backAction(){
        mDbFeedAdapter.close();

        Intent intent = new Intent(AddStockActivity.this, FeedMainItemLocal100Activity.class);
        intent.putExtra("FEED_TITLE",feedTitle);
        intent.putExtra("FEED_TYPE",feedType);
        intent.putExtra("listViewIndex",listViewIndex);
        intent.putExtra("listViewTop", listViewTop);

        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if(backBtn.equals(v)){
            backAction();
        }else if(searchBtn.equals(v)){
            log.debug("SearchBTN");
            try {

                final EditText searchCriteria = (EditText) this.findViewById(R.id.searchText);
                List<Item> stockList = AddStockMethod.searchStock100ByKey(mDbFeedAdapter, searchCriteria.getText().toString());

                log.debug("stockList Size:"+stockList.size());
                if(stockList != null && stockList.size() ==0){
                    Feed currentFeed = mDbFeedAdapter.getFeedNoItem(20, "");
                    log.debug("Feed:"+currentFeed);
                    log.debug("feedId:"+currentFeed.getId());
                    log.debug("feedTitle:"+currentFeed.getTitle());
                    log.debug("feedType:"+currentFeed.getType());
                    log.debug("feedLink:"+currentFeed.getLink());

                    new UpdateFeedTask().execute(currentFeed);
                }

                arrayItemAdapter = new AddStockArrayAdapter(this, R.id.title, stockList);
                stockListView.setAdapter(arrayItemAdapter);
            }catch(Exception e){
                log.debug(e.getMessage());
            }finally{

            }
        }
    }

    public void refreshListView(){
        log.debug("refreshListView");
        try {

            final EditText searchCriteria = (EditText) this.findViewById(R.id.searchText);
            List<Item> stockList = AddStockMethod.searchStock100ByKey(mDbFeedAdapter, searchCriteria.getText().toString());

            log.debug("stockList Size:"+stockList.size());

            arrayItemAdapter = new AddStockArrayAdapter(this, R.id.title, stockList);
            stockListView.setAdapter(arrayItemAdapter);
        }catch(Exception e){
            log.debug(e.getMessage());
        }finally{

        }
    }

    //Click Item View Content
    public void onItemClick (AdapterView<?> parent, View v, int position, long id) {
        log.debug("onItemClick:position[" + position + "]");

        if(arrayItemAdapter != null) {
            arrayItemAdapter.setSelectItem(position);
            arrayItemAdapter.notifyDataSetChanged();
        }

        TextView stockId = (TextView) v.findViewById(R.id.stockId);
        TextView stockName = (TextView) v.findViewById(R.id.stockName);
        try {
            mDbFeedAdapter = new DBAdapter(this);
            mDbFeedAdapter.open();

            //Update DB Fav Stock
            ContentValues values = new ContentValues();
            values.put(DBSchema.ItemSchema.COLUMN_FAV, DBSchema.ON);

            mDbFeedAdapter.updateItemByTitle(20,stockName.getText().toString(), values);

        }catch(Exception e){
            log.debug(e.getMessage());
        }finally{
            if(mDbFeedAdapter != null){
                mDbFeedAdapter.close();
            }
        }

        Intent intent = new Intent(AddStockActivity.this, FeedMainItemLocal100Activity.class);
        intent.putExtra("FEED_TITLE",feedTitle);
        intent.putExtra("FEED_TYPE",feedType);
        intent.putExtra("listViewIndex",listViewIndex);
        intent.putExtra("listViewTop",listViewTop);

        startActivity(intent);
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
        mDbFeedAdapter.close();
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
    protected void onResume() {
        super.onResume();

        /*if(dialog != null){
            dialog.dismiss();
        }
        if(dialogShow){
            dialog.dismiss();
            dialogShow = false;
        }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
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
    private class UpdateFeedTask extends AsyncTask<Feed, Void, Boolean> implements DialogInterface.OnDismissListener {
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

                log.debug("feedId:"+feed.getId());
                log.debug("feedTitle:"+feed.getTitle());
                log.debug("feedType:"+feed.getType());
                log.debug("feedLink:"+feed.getLink());

                //log.debug("Get Article List From Dropbox");
                List<Item> items = jsoup.getTopicItemsFromDropboxDB(mDbFeedAdapter,feed);
                //log.debug("Item From Dropbox:"+items.size());

                startTime = new Date();
                feed.setItems(items);
                mDbFeedAdapter.updateFeed(feed);
                //log.debug("updateFeed Time:"+((new Date()).getTime()-startTime.getTime()) );

                //recalc topicCurPage
                String flagType = "ID_SORT_BY_TITLE";

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

                dialogShow = true;
            }catch(Exception e){
                log.debug(e.getMessage());
            }
            return new Boolean(true);
        }

        protected void onPreExecute() {
            showDialog(SharedPreferencesHelper.DIALOG_UPDATE_PROGRESS);
        }
        protected void onPostExecute(Boolean result) {
            /** Search Again and Referesh Lisview **/
            refreshListView();
            //log.debug("dialogDownload isShowing["+dialogDownload.isShowing()+"]");
            if(dialogDownload != null && dialogDownload.isShowing()){
                dismissDialog(SharedPreferencesHelper.DIALOG_UPDATE_PROGRESS);
                long lastItemIdAfterUpdate = -1;
                Item lastItem = mDbFeedAdapter.getLastItem(feed.getId());
                if (lastItem != null)
                    lastItemIdAfterUpdate = lastItem.getId();
                if (lastItemIdAfterUpdate > lastItemIdBeforeUpdate){
                    //Toast.makeText(FeedMainItemActivity.this, R.string.new_item_msg, Toast.LENGTH_LONG).show();
                }else{
                    //Toast.makeText(FeedMainItemActivity.this, R.string.no_new_item_msg, Toast.LENGTH_LONG).show();
                }
            }
            dialogShow = false;
        }
        @Override
        public void onDismiss(DialogInterface dialog) {
            this.cancel(true);
        }
    }
}