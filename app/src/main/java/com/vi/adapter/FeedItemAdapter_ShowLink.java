package com.vi.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
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
import android.webkit.WebView;
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

import com.vi.FeedItemActivity;
import com.vi.MainActivity;
import com.vi.R;
import com.vi.common.ContentBean;
import com.vi.common.Control;
import com.vi.common.Display;
import com.vi.common.TableRowBean;
import com.vi.parser.JSoupHelperNoAuthen;
import com.vi.parser.JSoupUtils;
import com.vi.utils.Constants;
import com.vi.utils.LogUtils;
import com.vi.utils.PhoneProperty;
import com.vi.utils.Utils;


public class FeedItemAdapter_ShowLink extends ArrayAdapter<Display> {
    private LayoutInflater mInflater;
    private Context context;
    private Control control;
    private PhoneProperty phoneProperty = null;
    private PopupWindow popupWindow;
    LinearLayout contentMainLayout = null;
    LogUtils log = new LogUtils("FeedItemAdapter_ShowLink");

    public FeedItemAdapter_ShowLink(Context context,PopupWindow popupWindow , int textViewResourceId, List<Display> objects ,Control control) {
        super(context, textViewResourceId, objects);
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.control = control;
        this.popupWindow = popupWindow;
        //Load Phone Preperty
        phoneProperty = new PhoneProperty(context);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    /** Set view Feed Item **/
        /*@Override*/
    public View getView_ORG(int position, View convertView, ViewGroup parent) {
        Display disp = getItem(position);
        int[] item_rows = {R.layout.feed_content_row_style};
        int item_row = item_rows[0]; // Default initialization

        View view = convertView;
        // Always inflate view, in order to display correctly the 'read' and 'favorite' states of the items => to apply the right layout+style.
        item_row = item_rows[0];
        view = mInflater.inflate(item_row, null);
        TextView textTitleView = (TextView) view.findViewById(R.id.display_title);
        TextView textContentView = (TextView) view.findViewById(R.id.display_content);
        TextView textAuthorView = (TextView) view.findViewById(R.id.display_author);
        TextView textPostdateView = (TextView) view.findViewById(R.id.display_postdate);

        if (textTitleView != null){
            textTitleView.setText(disp.getTitle());
            textContentView.setText(Html.fromHtml(disp.getContent()));
            textAuthorView.setText(disp.getAuthor());
            textPostdateView.setText(disp.getPostDate());

            // Set Control
            if(control.getTextSize() > 0){
                textTitleView.setTextSize(control.getTextSize()-4);
                textContentView.setTextSize(control.getTextSize());
                textAuthorView.setTextSize(control.getTextSize()-4);
                textPostdateView.setTextSize(control.getTextSize()-4);
            }
        }
        return view;
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
            //textPostdateView.setText(disp.getPostDate());

            // Set Control
            if(control.getTextSize() > 0){
                textTitleView.setTextSize(control.getTextSize()-4);
                textAuthorView.setTextSize(control.getTextSize()-4);
                //textPostdateView.setTextSize(control.getTextSize()-4);
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
                    imageView.getLayoutParams().width = 35;
                    imageView.getLayoutParams().height = 35;

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

                    //SplitContent Text Or Image
                    List<ContentBean> contentList = splitContentToList(disp.getContent());
                    List<TableRowBean> contentTableRowBeanList = new ArrayList<TableRowBean>();
                    //Split Content to LinkTag
                    //contentLinkTagList = JSoupUtils.getLinkTag(disp.getContent().toString());

                    TableRowBean tableRowBean = null;
                    int rowIndex = 0;
                    if(contentList != null && contentList.size() > 0){
                        for(int i=0;i<contentList.size();i++){
                            ContentBean c = contentList.get(i);

                            if(Constants.ITEM_TYPE_TEXT.equals(c.getType())){
                                //log.debug("Display control.getTextSize(:"+control.getTextSize());
                                //TagLinkView
                                TextView tagLinkView = new TextView(context);
                                List<String[]> tagLinkList = JSoupUtils.getLinkTagList(c.getContent().toString());
                                if(tagLinkList != null && tagLinkList.size() >0){
                                    String textLink = "";
                                    for(int l=0;l<tagLinkList.size();l++){
                                        String[] linkArray = tagLinkList.get(l);
                                        textLink +=linkArray[0]+","+linkArray[1]+"|";
                                    }
                                    //log.debug("textLink:"+textLink);

                                    tagLinkView.setId(999);
                                    tagLinkView.setText(textLink);
                                    tagLinkView.setVisibility(View.GONE);
                                }

                                TextView textView = new TextView(context);
                                //log.debug("textContent:\n "+textContent);
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
                                    tr.addView(tagLinkView);

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
                                    //Check Prev Data
                                    if(i != 0 && contentTableRowBeanList != null && contentTableRowBeanList.size() >0){
                                        TableRowBean prevDataBean = contentTableRowBeanList.get(rowIndex-1);

                                        //Check for new Row leng   pre+cur length
                                        int totalRowLength = (prevDataBean != null?prevDataBean.getLength():0)+textView.getText().length();
                                        if(totalRowLength > 100 || prevDataBean.getType().equals(Constants.ITEM_TYPE_IMAGE)){
                                            //New Row
                                            TableRow tr = new TableRow(context);
                                            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                                            tr.setGravity(Gravity.CENTER_VERTICAL);
                                            tr.setHorizontalGravity(Gravity.LEFT);
                                            tr.addView(textView);
                                            tr.addView(tagLinkView);

                                            tableRowBean = new TableRowBean();
                                            tableRowBean.setTableRowData(textView.getText().toString());
                                            tableRowBean.setType(Constants.ITEM_TYPE_TEXT);
                                            tableRowBean.setLength(textView.getText().length());
                                            tableRowBean.setTableRow(tr);

                                            //add last text view
                                            tableRowBean.setLastTextView(textView);

                                            contentTableRowBeanList.add(tableRowBean);
                                            rowIndex++;
                                        }else{
                                            //add to Prev Row
                                            prevDataBean.getTableRow().addView(textView);//add next Column in same row
                                            prevDataBean.getTableRow().addView(tagLinkView);

                                            prevDataBean.setLength(totalRowLength);// old +new (same row)
                                            prevDataBean.setTableRowData(prevDataBean.getTableRowData()+","+textView.getText().toString());

                                            //add last text view
                                            prevDataBean.setLastTextView(textView);

                                            contentTableRowBeanList.set(rowIndex-1, prevDataBean);
                                        }
                                    }
                                }

                            }else if(Constants.ITEM_TYPE_IMAGE.equals(c.getType())){
                                //log.debug("Display imageURL:"+c.getImageUrl());
                                if( !Utils.isNull(c.getImageUrl()).equals("")){
                                    try{
                                        ImageView imageView = new ImageView(context);
                                        // imageView.setPadding(5, 5, 5, 5);
                                        imageView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

                                        //boolean isIconThaivi = false;
                                        Bitmap bmp = null;
                                        if(c.getImageUrl().indexOf("images_") != -1){
                                            bmp = displayIcon(bmp, c.getImageUrl());
                                            imageView.setImageBitmap(bmp);
                                            //isIconThaivi = true;
                                        }else{
                                            FileInputStream fis = null;
                                            File file = null;
                                            try{
                                                file = new File(c.getImageUrl());
                                                if(file.exists()){
                                                    fis = new FileInputStream(file);

                                                    bmp = BitmapFactory.decodeStream(fis);
                                                    //log.debug("image:"+bmp.getWidth());
                                                    imageView.setImageBitmap(bmp);
                                                }
                                            }catch(Exception e){
                                                e.printStackTrace();
                                            }finally{
                                                if(fis != null){
                                                    fis.close();
                                                }
                                            }
                                        }

                                        if(bmp != null ){
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
                                        }


                                        // log.debug("Resize Image width:"+imageView.getLayoutParams().width );
                                        // log.debug("Resize Image Height:"+imageView.getLayoutParams().height);

                                        if(tableRowBean == null){
                                            TableRow tr = new TableRow(context);
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
                                        }else{
                                            //Check Prev Data
                                            if(i != 0 && contentTableRowBeanList != null && contentTableRowBeanList.size() >0){
                                                TableRowBean prevDataBean = contentTableRowBeanList.get(rowIndex-1);

                                                //Check for new Row leng   pre+cur length
                                                int totalRowLength = (prevDataBean != null?prevDataBean.getLength():0)+ (bmp !=null?bmp.getWidth():0);
                                                if(imageView != null){
                                                    //log.debug("Image Url:"+c.getImageUrl()+",imageView Width:"+imageView.getWidth());
                                                }
                                                if(bmp !=null){
                                                    //log.debug("Image Url:"+c.getImageUrl()+",bmp width:"+bmp.getWidth());
                                                }
                                                // log.debug("totalRowLength:"+totalRowLength);

                                                /*** Case add icon to TextView  add to old Line***/
                                                if( ( (bmp != null && bmp.getWidth() <= 25) && prevDataBean.getType().equals(Constants.ITEM_TYPE_TEXT))
                                                        ){
                                                    // log.debug("*** Case add icon to TextView **");

                                                    //Get last text view Table Row
                                                    TextView prevTextView = prevDataBean.getLastTextView();
                                                    //Remove Old View
                                                    prevDataBean.getTableRow().removeView(prevTextView);
                                                    //log.debug("prevTextView["+prevTextView+"]");

                                                    //ImageSpan imageSpan = new ImageSpan(bmp);
                                                    ImageSpan imageSpan = new ImageSpan(context, bmp, ImageSpan.ALIGN_BASELINE);

                                                    SpannableStringBuilder builder = new SpannableStringBuilder();
                                                    builder.append(prevTextView.getText()+" [icon]");
                                                    builder.setSpan(imageSpan, builder.length()-"[icon]".length(), builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                                    prevTextView.setText(builder, BufferType.SPANNABLE);

                                                    //Replace new View
                                                    prevDataBean.getTableRow().addView(prevTextView);//add next Column in same row
                                                    prevDataBean.setLength(totalRowLength);// old +new (same row)

                                                    //add last text view
                                                    prevDataBean.setLastImageView(imageView);

                                                    contentTableRowBeanList.set(rowIndex-1, prevDataBean);

                                                }else{
                                                    //Add new Line
                                                    if(totalRowLength > 100){
                                                        //New Row
                                                        TableRow tr = new TableRow(context);
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

                                                    }else{
                                                        //add to Prev Row
                                                        prevDataBean.getTableRow().addView(imageView);//add next Column in same row
                                                        prevDataBean.setLength(totalRowLength);// old +new (same row)
                                                        prevDataBean.setTableRowData(prevDataBean.getTableRowData()+","+c.getImageUrl());

                                                        //add last text view
                                                        prevDataBean.setLastImageView(imageView);

                                                        contentTableRowBeanList.set(rowIndex-1, prevDataBean);
                                                    }
                                                }
                                            }
                                        }
                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }//if
                        }//for

                        //Add sub layout to Main layout
                        TableLayout tableLayout = new TableLayout(context);
                        tableLayout.setClickable(true);
                        tableLayout.setLayoutParams(new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT));

                        if(contentTableRowBeanList != null && contentTableRowBeanList.size() >0){
                            for(int t=0;t<contentTableRowBeanList.size();t++){
                                TableRowBean tableRowBean1 = contentTableRowBeanList.get(t);
                                //log.debug("Title:"+feed.getTitle()+"]row["+t+"]tableRowData["+tableRowBean1.getTableRowData()+"] column count:"+tableRowBean1.getTableRow().getChildCount());

                                TableRow trow = tableRowBean1.getTableRow();
                                //Set Property Trow
                                trow.setClickable(true);
                                //Alter method 2
                                trow.setOnClickListener(tablerowOnClickListener);

                                //Add Trow to TableLayout
                                tableLayout.addView(trow,
                                        new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT,
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

    private OnClickListener tablerowOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            ListView linkListView =  new ListView(context);
            linkListView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));

            linkListView.setBackgroundResource(R.color.color_green_1);
            List<String[]> linkArrayList = new ArrayList<String[]>();
            linkArrayList.add(new String[]{"Link","Link"});

            //GET TEXT HERE ,OR IMAGEVIEW
            TableRow trRow = (TableRow)v;
            int maxObj = trRow.getChildCount();
            //log.debug("Onclick MaxObj["+maxObj+"]");

            for(int i=0;i<maxObj;i++){
                if(trRow.getChildAt(i) instanceof TextView){
                    TextView chkTag = ((TextView)trRow.getChildAt(i));
                    if(chkTag.getId()==999){

                        String currentLinkText = ((TextView)trRow.getChildAt(i)).getText().toString();
                        log.debug("Onclick currentLinkText:"+currentLinkText);

                        if( !Utils.isNull(currentLinkText).equals("")){
                            String[] linkArray = currentLinkText.split("\\|");

                            for(int n=0;n<linkArray.length;n++){
                                log.debug("linkSubText:"+linkArray[n]);
                                String[] linkSubArray = linkArray[n].split("\\,");
                                linkArrayList.add(linkSubArray);

                            }//for
                        }//if
                    }//if
                }//if
            }//for

            //Add to Adapter
            LinkArrayAdapter arrayAdapter = new LinkArrayAdapter(context, R.layout.goto_page_row,linkArrayList);
            linkListView.setAdapter(arrayAdapter);

            popupWindow.setContentView(linkListView);

            int y = phoneProperty.height/2;
            int x = 0;//context.getWindowManager().getDefaultDisplay().getWidth()-150;
            log.debug("x:"+x+",y:"+y);

            popupWindow.showAtLocation(contentMainLayout, Gravity.TOP, x, y);
        }
    };

    private OnClickListener tablerowOnClickListenerEx1 = new OnClickListener() {
        public void onClick(View v) {
            LinearLayout layoutLink = new LinearLayout(context);
            layoutLink.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
            	
            	
            	/*TableLayout layoutLink = new TableLayout(context);
            	layoutLink.setLayoutParams(new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 
            			TableRow.LayoutParams.WRAP_CONTENT));*/

            layoutLink.setBackgroundResource(R.color.color_green_1);

            //GET TEXT HERE ,OR IMAGEVIEW
            TableRow trRow = (TableRow)v;
            int maxObj = trRow.getChildCount();
            log.debug("Onclick MaxObj["+maxObj+"]");

            for(int i=0;i<maxObj;i++){
                if(trRow.getChildAt(i) instanceof TextView){
                    TextView chkTag = ((TextView)trRow.getChildAt(i));
                    if(chkTag.getId()==999){

                        String currentLinkText = ((TextView)trRow.getChildAt(i)).getText().toString();
                        log.debug("Onclick currentLinkText:"+currentLinkText);

                        if( !Utils.isNull(currentLinkText).equals("")){
                            String[] linkArray = currentLinkText.split("\\|");

                            for(int n=0;n<linkArray.length;n++){
                                log.debug("linkSubText:"+linkArray[n]);

                                String[] linkSubArray = linkArray[n].split("\\,");
                                final String linkUrl = linkSubArray[0];
                                String linktext = linkSubArray[1];

                                TextView linkTagView = new TextView(context);
                                linkTagView.setText(linktext);
                                //Property
                                linkTagView.setTextSize(20.0f);
                                linkTagView.setTextColor(Color.BLACK);

                                linkTagView.setOnClickListener(new OnClickListener(){
                                    public void onClick(View v){
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl));
                                        context.startActivity(browserIntent);
                                    }
                                });
			                	   
			                	    /*TableRow tr = new TableRow(context);
		        					tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
				        	        tr.setGravity(Gravity.CENTER_VERTICAL);
				        	        tr.setHorizontalGravity(Gravity.LEFT);
		        					tr.addView(linkTagView);
		        					
		        					tableLayout.addView(tr,
			            					new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, 
			            					TableLayout.LayoutParams.WRAP_CONTENT));*/

                                layoutLink.addView(linkTagView);
                            }//for
                        }//if
                    }//if
                }//if
            }//for

            popupWindow.setContentView(layoutLink);


            int y = phoneProperty.height/2;
            int x = 0;//context.getWindowManager().getDefaultDisplay().getWidth()-150;
            log.debug("x:"+x+",y:"+y);

            popupWindow.showAtLocation(contentMainLayout, Gravity.TOP, x, y);
        }
    };

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


    /**
     *
     * @param s  -> 'xxxx^๑yyyyyy^๑/mnt/sdcard/THAIVI/images/xx.gif'
     * @return
     */
    private static List<ContentBean> splitContentToList(String s){
        List<ContentBean> contentList = new ArrayList<ContentBean>();
        try{
    			/*log.debug("******splitContentToList.Content ALL************************" +
    					" \n"+s);
    			log.debug("*****************************************");*/

            if(s.indexOf("images") != -1){
                String content ="";
                String[] contentArr = s.split("\\"+Constants.content_image_delimeter);
                for(int i=0;i<contentArr.length;i++){
                    content  = contentArr[i];
                    //log.debug("******contentArr["+i+"]:********************" +
                    //"\n "+content +"\n *****************************************");

                    ContentBean b = new ContentBean();
                    if(content.indexOf("images") != -1){
                        b.setType(Constants.ITEM_TYPE_IMAGE);
                        b.setImageUrl(content.substring(0,content.length()));
                    }else{
                        b.setContent(content.substring(0,content.length()));

                        //log.debug("ContentSplit:"+b.getContent());

                        b.setType(Constants.ITEM_TYPE_TEXT);
                    }
                    contentList.add(b);
                }//while
            }else{
                ContentBean b = new ContentBean();
                b.setContent(s.substring(0,s.length()-2));
                b.setType(Constants.ITEM_TYPE_TEXT);
                contentList.add(b);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return contentList;
    }
    @Override
    public boolean isEnabled(int position){
        return false;
    }

    public Bitmap displayIcon(Bitmap bm,String iconName){
        if(iconName.indexOf("cheers") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.cheers);
        }else if(iconName.indexOf("cool") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.cool);
        }else if(iconName.indexOf("doi") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.doi);
        }else if(iconName.indexOf("drink") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.drink);
        }else if(iconName.indexOf("icon_arrow") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_arrow);
        }else if(iconName.indexOf("icon_bigcry") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_bigcry);
        }else if(iconName.indexOf("icon_biggrin") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_biggrin);
        }else if(iconName.indexOf("icon_bigsmile") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_bigsmile);
        }else if(iconName.indexOf("icon_bow") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_bow);
        }else if(iconName.indexOf("icon_confused") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_confused);
        }else if(iconName.indexOf("icon_cool") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_cool);
        }else if(iconName.indexOf("icon_cry") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_cry);
        }else if(iconName.indexOf("icon_donottdothat") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_donottdothat);
        }else if(iconName.indexOf("icon_eek") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_eek);
        }else if(iconName.indexOf("icon_evil") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_evil);
        }else if(iconName.indexOf("icon_evilplan") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_evilplan);
        }else if(iconName.indexOf("icon_exclaim") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_exclaim);
        }else if(iconName.indexOf("icon_idea") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_idea);
        }else if(iconName.indexOf("icon_juju") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_juju);
        }else if(iconName.indexOf("icon_keepquiet") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_keepquiet);
        }else if(iconName.indexOf("icon_look") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_look);
        }else if(iconName.indexOf("icon_mad") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_mad);
        }else if(iconName.indexOf("icon_mrgreen") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_mrgreen);
        }else if(iconName.indexOf("icon_neutral") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_neutral);
        }else if(iconName.indexOf("icon_nevermind") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_nevermind);
        }else if(iconName.indexOf("icon_ohno") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_ohno);
        }else if(iconName.indexOf("icon_pray") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_pray);
        }else if(iconName.indexOf("icon_question") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_question);
        }else if(iconName.indexOf("icon_razz") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_razz);
        }else if(iconName.indexOf("icon_redface") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_redface);
        }else if(iconName.indexOf("icon_rofl") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_rofl);
        }else if(iconName.indexOf("icon_rolleyes") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_rolleyes);
        }else if(iconName.indexOf("icon_sad") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_sad);
        }else if(iconName.indexOf("icon_smile") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_smile);
        }else if(iconName.indexOf("icon_surprised") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_surprised);
        }else if(iconName.indexOf("icon_twisted") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_twisted);
        }else if(iconName.indexOf("icon_vomit") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_vomit);
        }else if(iconName.indexOf("icon_wal") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_wall);
        }else if(iconName.indexOf("icon_wink") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_wink);
        }else if(iconName.indexOf("liverpool") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.liverpool);
        }else if(iconName.indexOf("pantipman") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.pantipman);
        }else if(iconName.indexOf("pig") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.pig);
        }else if(iconName.indexOf("welcome") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.welcome);
        }
        return bm;
    }
}