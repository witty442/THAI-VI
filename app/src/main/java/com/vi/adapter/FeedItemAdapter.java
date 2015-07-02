package com.vi.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.vi.R;
import com.vi.common.ContentBean;
import com.vi.common.Control;
import com.vi.common.Display;
import com.vi.common.Feed;
import com.vi.common.Item;
import com.vi.common.TableRowBean;
import com.vi.parser.JSoupHelperAuthen;
import com.vi.utils.Constants;
import com.vi.utils.LogUtils;
import com.vi.utils.PhoneProperty;
import com.vi.utils.Utils;


public class FeedItemAdapter extends ArrayAdapter<Display> {
    private LayoutInflater mInflater;
    private Context context;
    private Control control;
    private PhoneProperty phoneProperty = null;
    private PopupWindow popupWindow;
    LinearLayout contentMainLayout = null;
    JSoupHelperAuthen jsoupAuthen = null;
    LogUtils log = new LogUtils("FeedItemAdapter");

    private Feed currentFeed ;
    private Item currentItem ;
    private int listViewIndex=0;
    private int listViewTop=0;
    private int item_listViewIndex =0;
    private int item_listViewTop= 0;
    private int listViewIndex_FeedMain = 0;
    private int listViewTop_FeedMain = 0;
    private int topicCurPage =0;

    public FeedItemAdapter(Context context,PopupWindow popupWindow , int textViewResourceId, List<Display> objects ,Control control,JSoupHelperAuthen jsoupAuthen) {
        super(context, textViewResourceId, objects);
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.control = control;
        this.popupWindow = popupWindow;
        //Load Phone Preperty
        phoneProperty = new PhoneProperty(context);
        this.jsoupAuthen = jsoupAuthen;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    /** Set view Feed Item **/
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Display disp = getItem(position);
        int[] item_rows = {R.layout.feed_content_image_row_style};
        int item_row = item_rows[0]; // Default initialization
        View view = convertView;

        // Always inflate view, in order to display correctly the 'read' and 'favorite' states of the items => to apply the right layout+style.
        item_row = item_rows[0];
        view = mInflater.inflate(item_row, null);
        TextView textTitleView = (TextView) view.findViewById(R.id.display_title);
        TextView textAuthorView = (TextView) view.findViewById(R.id.display_author);
        //TextView textPostdateView = (TextView) view.findViewById(R.id.display_postdate);

        if (textTitleView != null){
            textTitleView.setText(disp.getTitle());
            if( !Utils.isNull(disp.getAuthor()).equals("")){
                textAuthorView.setText(disp.getAuthor()+disp.getPostDate());
            }
            // Set Control
            if(control.getTextSize() > 0){
                textTitleView.setTextSize(control.getTextSize()-6);
                textAuthorView.setTextSize(control.getTextSize()-6);
            }
        }

        try{
            contentMainLayout = (LinearLayout)view.findViewById(R.id.content_layout);

            if(disp.isTopicTitle()){
                //Add sub layout to Main layout
                TableLayout tableLayout = new TableLayout(context);
                //tableLayout.set
                tableLayout.setLayoutParams(new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                //Image save or bookmark
                ImageView imageView = new ImageView(context);
                imageView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                if( !"".equals(Utils.isNull(disp.getShowImage())) ){
                    if( "save".equals(Utils.isNull(disp.getShowImage())) ){
                        imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.b_save));
                    }else if( "bookmark".equals(Utils.isNull(disp.getShowImage())) ){
                        imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.b_bookmark));
                    }
                    imageView.getLayoutParams().width = 60;
                    imageView.getLayoutParams().height = 60;

                    TableRow tr = new TableRow(context);
                    tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                    //tr.setGravity(Gravity.CENTER_VERTICAL);
                    tr.setHorizontalGravity(Gravity.LEFT);
                    tr.addView(imageView);

                    //add tot tableLayout
                    tableLayout.addView(tr,new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                }

                //split content by @
                String[] contentArr = disp.getContent().split("@");
                if(contentArr != null && contentArr.length >0){
                    for(int i=0;i<contentArr.length;i++){
                        if( !"".equals(Utils.isNull(contentArr[i]))){
                            TextView textView = new TextView(context);
                            textView.setText(contentArr[i]);
                            if(i==0){
                                textView.setTextAppearance(context.getApplicationContext(), R.style.style_display_content);
                            }else{
                                textView.setTextSize(control.getTextSize()-4);
                                textView.setTextAppearance(context.getApplicationContext(), R.style.item_view_content_title);
                            }

                            textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                            TableRow tr = new TableRow(context);
                            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            // tr.setGravity(Gravity.CENTER_VERTICAL);
                            tr.setHorizontalGravity(Gravity.LEFT);
                            tr.addView(textView);

                            //add tot tableLayout
                            tableLayout.addView(tr,new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                        }//if
                    }//for
                }
                contentMainLayout.addView(tableLayout);

            }else{
                if(Constants.FEED_TYPE_ARTICLE.equals(disp.getType())){

                    TextView textView = new TextView(context);
                    textView.setText(Html.fromHtml(disp.getContent()));
                    //set style
                    textView.setTextAppearance(context.getApplicationContext(), R.style.style_display_content);
                    textView.setTextSize(control.getTextSize());
                    textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                    contentMainLayout.addView(textView);
                }else{

                    //SplitContent Text Or Image Or LinkTag
                    List<ContentBean> contentList = FeedItemAdapterHelper.splitContentToList(jsoupAuthen,disp.getContent());
                    List<TableRowBean> contentTableRowBeanList = new ArrayList<TableRowBean>();

                    TableRowBean tableRowBean = null;
                    int rowIndex = 0;
                    if(contentList != null && contentList.size() > 0){
                        for(int i=0;i<contentList.size();i++){
                            ContentBean c = contentList.get(i);
                            //log.debug("Type["+c.getType()+"]textContent:\n "+c.getContent());

                            if(Constants.ITEM_TYPE_TEXT.equals(c.getType())){
                                //log.debug("Display control.getTextSize(:"+control.getTextSize());

                                TextView textView = new TextView(context);
                                //log.debug("textContent:\n "+c.getContent());
                                textView.setText(Html.fromHtml(c.getContent()));

                                //set style
                                textView.setTextAppearance(context.getApplicationContext(), R.style.style_display_content);
                                textView.setTextSize(control.getTextSize());
                                textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                                if(tableRowBean == null){
                                    TableRow tr = new TableRow(context);
                                    tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                                    tr.setGravity(Gravity.CENTER_VERTICAL);
                                    tr.setHorizontalGravity(Gravity.LEFT);
                                    tr.addView(textView);

                                    tableRowBean = new TableRowBean();
                                    tableRowBean.setType(Constants.ITEM_TYPE_TEXT);
                                    tableRowBean.setTableRowData(textView.getText().toString());
                                    tableRowBean.setLength(textView.getText().length());
                                    tableRowBean.setTableRow(tr);

                                    //add last text view
                                    tableRowBean.setLastTextView(textView);

                                    contentTableRowBeanList.add(tableRowBean);
                                    rowIndex++;
                                }else{

                                    //New Row
                                    TableRow tr = new TableRow(context);
                                    tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                                    tr.setGravity(Gravity.CENTER_VERTICAL);
                                    tr.setHorizontalGravity(Gravity.LEFT);
                                    tr.addView(textView);


                                    tableRowBean = new TableRowBean();
                                    tableRowBean.setTableRowData(textView.getText().toString());
                                    tableRowBean.setType(Constants.ITEM_TYPE_TEXT);
                                    tableRowBean.setLength(textView.getText().length());
                                    tableRowBean.setTableRow(tr);

                                    //add last text view
                                    tableRowBean.setLastTextView(textView);

                                    contentTableRowBeanList.add(tableRowBean);
                                    rowIndex++;

                                }
                            }else if(Constants.ITEM_TYPE_TEXT_LINK.equals(c.getType())){
                                //log.debug("Display control.getTextSize(:"+control.getTextSize());
                                TextView textView = new TextView(context);
                                final String linkUrl = c.getLinkUrl();
                                String linkTextS = "<a href='#'>"+c.getContent()+"</a>";

                                textView.setText(Html.fromHtml(linkTextS));
                                //Add Action Click
                                textView.setOnClickListener(new OnClickListener(){
                                    public void onClick(View v){
			              	            	/*if(linkUrl.indexOf("board.thaivi.org") != -1){
			              	            		
			              	            		Intent intent = new Intent(context, FeedItemLinkActivity.class);
			              	            		intent.putExtra("FEED", currentFeed);
				              	      			intent.putExtra("TOPIC_CUR_PAGE", topicCurPage);
				              	      	        intent.putExtra("ACTION", "NO_FRESH");
				              	      	        intent.putExtra("listViewIndex", listViewIndex);
				              	      	        intent.putExtra("listViewTop", listViewTop);
				              	      	        intent.putExtra("listViewIndex_FeedMain", listViewIndex_FeedMain);
				              	      			intent.putExtra("listViewTop_FeedMain", listViewTop_FeedMain);
				              	      			
				              	      		    context.startActivity(intent);
				              	      		    
			              	            	}else{*/
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl));
                                        context.startActivity(browserIntent);
                                        //}
                                    }
                                });

                                //set style
                                textView.setTextAppearance(context.getApplicationContext(), R.style.style_display_content);
                                textView.setTextSize(control.getTextSize());
                                textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));


                                TableRow tr = new TableRow(context);
                                tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                                tr.setGravity(Gravity.CENTER_VERTICAL);
                                tr.setHorizontalGravity(Gravity.LEFT);
                                tr.addView(textView);

                                tableRowBean = new TableRowBean();
                                tableRowBean.setType(Constants.ITEM_TYPE_TEXT);
                                tableRowBean.setTableRowData(textView.getText().toString());
                                tableRowBean.setLength(textView.getText().length());
                                tableRowBean.setTableRow(tr);

                                //add last text view
                                tableRowBean.setLastTextView(textView);

                                contentTableRowBeanList.add(tableRowBean);
                                rowIndex++;

                            }else if(Constants.ITEM_TYPE_IMAGE.equals(c.getType())){
                                //log.debug("Display imageURL:"+c.getImageUrl());
                                if( !Utils.isNull(c.getImageUrl()).equals("")){
                                    try{
                                        ImageView imageView = new ImageView(context);
                                        // imageView.setPadding(5, 5, 5, 5);
                                        imageView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                                        //boolean isIconThaivi = false;
                                        Bitmap bmp = null;
                                        if( c.isImageLocal()){
                                            if(c.isThaiviIcon()){
                                                bmp = FeedItemAdapterHelper.displayIcon(context,bmp,c.getImageUrl());
                                                imageView.setImageBitmap(bmp);
                                            }
                                        }else if( !c.isImageLocal()){ //Image Online
                                            new DownloadImagesTask().execute(imageView,jsoupAuthen,c);
                                        }
				    			         
				    			        /* if(bmp != null ){
					    			         if(bmp.getWidth() >100 && bmp.getWidth() <= 240){
					    			        	 //not over 240
					    			        	 if(bmp.getWidth() >= 240 ){
						    			            imageView.getLayoutParams().width = 210;
					    			        	 }
					    			         }else  if(bmp.getWidth() >= 241 && bmp.getWidth() <= 480){
					    			        	//not over 480
					    			        	 if(bmp.getWidth() >= 480 ){
						    			            imageView.getLayoutParams().width = 410;
					    			        	 }
					    			         }else  if(bmp.getWidth() >= 481 && bmp.getWidth() <= 1024){
					    			        	//not over 1024
					    			        	 if(bmp.getWidth() >= 1024 ){
						    			            imageView.getLayoutParams().width = 900;
					    			        	 }
					    			         }else  if(bmp.getWidth() >= 1025 && bmp.getWidth() <= 1280){
					    			        	//not over 1280
					    			        	 if(bmp.getWidth() >= 1280 ){
						    			            imageView.getLayoutParams().width = 1000;
					    			        	 }
					    			         }else if(bmp.getWidth() > 1020 ){
					    			        	 imageView.getLayoutParams().width = 1000;
					    			         }else{
					    			        	 //<50
					    			         }
				    			         }*/

                                        // log.debug("Resize Image width:"+imageView.getLayoutParams().width );
                                        // log.debug("Resize Image Height:"+imageView.getLayoutParams().height);

                                        TableRow tr  = new TableRow(context);
                                        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                                        tr.setGravity(Gravity.CENTER_VERTICAL);
                                        tr.setHorizontalGravity(Gravity.LEFT);
                                        tr.addView(imageView);

                                        tableRowBean = new TableRowBean();
                                        tableRowBean.setTableRowData(c.getImageUrl());
                                        tableRowBean.setType(Constants.ITEM_TYPE_IMAGE);
                                        tableRowBean.setLength(bmp!=null?bmp.getWidth():0);

                                        tableRowBean.setTableRow(tr);

                                        //add last text view
                                        tableRowBean.setLastImageView(imageView);

                                        contentTableRowBeanList.add(tableRowBean);
                                        rowIndex++;

                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }//if
                        }//for

                        //Add sub layout to Main layout
                        TableLayout tableLayout = new TableLayout(context);
                        tableLayout.setClickable(true);
                        //tableLayout.setBackgroundResource(R.color.red);
                        tableLayout.setLayoutParams(new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT));

                        //Line Split Reply Comment Author
		                 /*TextView textAuthorView1 = new TextView(context);
		                 if( !Utils.isNull(disp.getAuthor()).equals("")){
		                  	  textAuthorView1.setText(disp.getAuthor()+disp.getPostDate());
		                  	  textAuthorView1.setTextSize(control.getTextSize()-4);
		                 }*/

                        if(contentTableRowBeanList != null && contentTableRowBeanList.size() >0){
                            for(int t=0;t<contentTableRowBeanList.size();t++){
                                TableRowBean tableRowBean1 = contentTableRowBeanList.get(t);
                                //log.debug("Title:"+feed.getTitle()+"]row["+t+"]tableRowData["+tableRowBean1.getTableRowData()+"] column count:"+tableRowBean1.getTableRow().getChildCount());

                                TableRow trow = tableRowBean1.getTableRow();
                                //trow.addView(textAuthorView1);
                                //trow.setBackgroundResource(R.drawable.border);


                                //Set Property Trow
                                //trow.setClickable(true);
                                //Alter method 2
                                //trow.setOnClickListener(tablerowOnClickListener);

                                //Add Trow to TableLayout
                                tableLayout.addView(trow,
                                        new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                                                TableLayout.LayoutParams.WRAP_CONTENT));
                            }//for
                        }
                        contentMainLayout.addView(tableLayout);

                    }//if
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return view;
    }


    public PopupWindow popupWindowDogs() {

        // initialize a pop up window type
        PopupWindow popupWindow = new PopupWindow(context);

        TextView textView = new TextView(context);
        textView.setText("TestXXXXXX");

        // the drop down list is a list view
        // ListView listViewDogs = new ListView(this);

        // set our adapter and pass our pop up window contents
        // listViewDogs.setAdapter(dogsAdapter(popUpContents));

        // set the item click listener
        // listViewDogs.setOnItemClickListener(new DogsDropdownOnItemClickListener());

        // some other visual settings
        popupWindow.setFocusable(true);
        popupWindow.setWidth(250);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        // set the list view as pop up window content
        popupWindow.setContentView(textView);

        // popupWindow.showAsDropDown(v, -5, 0);

        return popupWindow;
    }

    @Override
    public boolean isEnabled(int position){
        return false;
    }

    public Feed getCurrentFeed() {
        return currentFeed;
    }

    public void setCurrentFeed(Feed currentFeed) {
        this.currentFeed = currentFeed;
    }

    public Item getCurrentItem() {
        return currentItem;
    }

    public void setCurrentItem(Item currentItem) {
        this.currentItem = currentItem;
    }

    public int getListViewIndex() {
        return listViewIndex;
    }

    public void setListViewIndex(int listViewIndex) {
        this.listViewIndex = listViewIndex;
    }

    public int getListViewTop() {
        return listViewTop;
    }

    public void setListViewTop(int listViewTop) {
        this.listViewTop = listViewTop;
    }

    public int getItem_listViewIndex() {
        return item_listViewIndex;
    }

    public void setItem_listViewIndex(int item_listViewIndex) {
        this.item_listViewIndex = item_listViewIndex;
    }

    public int getItem_listViewTop() {
        return item_listViewTop;
    }

    public void setItem_listViewTop(int item_listViewTop) {
        this.item_listViewTop = item_listViewTop;
    }

    public int getListViewIndex_FeedMain() {
        return listViewIndex_FeedMain;
    }

    public void setListViewIndex_FeedMain(int listViewIndex_FeedMain) {
        this.listViewIndex_FeedMain = listViewIndex_FeedMain;
    }

    public int getListViewTop_FeedMain() {
        return listViewTop_FeedMain;
    }

    public void setListViewTop_FeedMain(int listViewTop_FeedMain) {
        this.listViewTop_FeedMain = listViewTop_FeedMain;
    }

    public int getTopicCurPage() {
        return topicCurPage;
    }

    public void setTopicCurPage(int topicCurPage) {
        this.topicCurPage = topicCurPage;
    }


}