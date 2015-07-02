package com.vi.adapter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vi.R;
import com.vi.common.Control;
import com.vi.common.Item;
import com.vi.utils.Constants;
import com.vi.utils.LogUtils;
import com.vi.utils.Utils;

public class FeedMainItemAdapter extends ArrayAdapter<Item>{

    private LayoutInflater mInflater;
    private Context context;
    private Control control;
    private int selectedItem = -1; // no item selected by default
    LogUtils log = new LogUtils("FeedMainItemAdapter");

    public FeedMainItemAdapter(Context context, int textViewResourceId, List<Item> objects,Control control) {
        super(context, textViewResourceId, objects);
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.control = control;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    /** Set view Feed Item **/
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        int currentPage = control.getCurrentPage()==0?1:control.getCurrentPage();
        try{
            // log.debug"currentPage["+currentPage+"]");

            Item item = getItem(position);
            if(view ==null)
                view = mInflater.inflate(R.layout.feed_main_item_row, null);

            TextView titleIdView = (TextView) view.findViewById(R.id.title_id);
            TextView titleView = (TextView) view.findViewById(R.id.title);
            TextView topicFooterView = (TextView) view.findViewById(R.id.topicFooter);

            if (titleIdView != null){
                titleIdView.setText((position+1)+"");
                titleIdView.setVisibility(View.GONE);
            }
            if (titleView != null){
                int no = ((currentPage-1)*Constants.THAIVI_TOPIC_ROW_PER_PAGE)+position+1;
                titleView.setText(no+"."+StringEscapeUtils.unescapeHtml(item.getTitle()));
            }
            if (topicFooterView != null){
                String msg ="";
                if(Constants.FEED_TYPE_BORAD.equals(item.getFeedType())){
                    msg += " จำนวนคนตอบ("+item.getTotalReply()+")";
                    msg += " จำนวนคนอ่าน("+item.getTotalRead()+")";
                    if(item.getCurPage() != 0){
                        msg += " หน้าล่าสุดที่อ่าน("+item.getCurPage()+")";
                    }
                }else{
                    msg += "ผู้แต่ง("+item.getAuthor()+")";
                    msg += "จำนวนครั้งที่อ่าน("+item.getCountOpen()+")";
                }
                String formatDate = context.getResources().getString(R.string.date_format_pattern);
                SimpleDateFormat df = new SimpleDateFormat(formatDate,new Locale("TH","th"));
                msg = msg +" วันที่ "+df.format(item.getCreateDate());

                //log.debug"msg:"+msg);
                topicFooterView.setText(msg);


                //hilight item click
                //log.debug"View Position:["+position+"] selectedItem["+selectedItem+"],isSelected:"+view.isSelected());
                if(selectedItem == position){
                    view.setBackgroundColor(context.getResources().getColor(R.color.bg_listview_selected));
                    topicFooterView.setBackgroundColor(context.getResources().getColor(R.color.bg_listview_selected));
                }else{
                    view.setBackgroundColor(context.getResources().getColor(R.color.bg_color_mainstyle));
                    topicFooterView.setBackgroundColor(context.getResources().getColor(R.color.bg_color_mainstyle));
                }
            }
            // Set Control
            if(control.getTextSize() > 0){
                titleView.setTextSize(control.getTextSize());
                topicFooterView.setTextSize(control.getTextSize()-4);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return view;
    }

    public void setSelectItem(int selectItem){
        this.selectedItem = selectItem;
    }
}
