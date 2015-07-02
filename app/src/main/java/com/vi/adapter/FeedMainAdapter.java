package com.vi.adapter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vi.R;
import com.vi.common.Control;
import com.vi.common.Feed;
import com.vi.utils.Constants;


public class FeedMainAdapter extends ArrayAdapter<Feed> {
    private LayoutInflater mInflater;
    private Context context;
    private Control control;
    private int selectedItem = -1; // no item selected by default

    public FeedMainAdapter(Context context, int textViewResourceId, List<Feed> objects,Control control) {
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
        try{
            Feed feed = getItem(position);
            if(view ==null)
                view = mInflater.inflate(R.layout.feed_main_row, null);

            if(selectedItem == position){
                view.setBackgroundColor(context.getResources().getColor(R.color.bg_listview_selected));
                //view.setBackgroundColor(0xFF00FF00);
            }else{
                view.setBackgroundColor(context.getResources().getColor(R.color.bg_color_mainstyle));
                //view.setBackgroundColor(0xFF00FF00);
            }

            TextView titleNoView = (TextView) view.findViewById(R.id.title_no);
            TextView titleView = (TextView) view.findViewById(R.id.title);
            TextView titleId = (TextView) view.findViewById(R.id.title_id);
            TextView pubdateView = (TextView) view.findViewById(R.id.pubdate);
            TextView totalView = (TextView) view.findViewById(R.id.total);
            TextView topicType = (TextView) view.findViewById(R.id.topic_type);
            TextView authorView = (TextView) view.findViewById(R.id.author);

            if (titleNoView != null){
                titleNoView.setText(""+(position+1)+".");
            }
            if (titleView != null){
                titleView.setText(feed.getTitle());
            }
            if (totalView != null ){
                String msg = "จำนวนรายการ("+feed.getTotalItem()+")";
                totalView.setText(msg);
            }
            if (titleId != null){
                titleId.setText(feed.getId()+"");
                titleId.setVisibility(View.GONE);
            }
            if (pubdateView != null) {
                String formatDate = context.getResources().getString(R.string.date_format_pattern);
                SimpleDateFormat df = new SimpleDateFormat(formatDate,new Locale("TH","th"));

                String msg ="วันที่ "+df.format(feed.getUpdateDate());
                pubdateView.setText(msg);
            }

            if (topicType != null){
                topicType.setText(feed.getType());
                topicType.setVisibility(View.GONE);
            }
            if (authorView != null){
                authorView.setText(feed.getAuthor());
                authorView.setVisibility(View.GONE);
            }

            // Set Control
            if(control.getTextSize() > 0){
                titleNoView.setTextSize(control.getTextSize());
                titleView.setTextSize(control.getTextSize());
                totalView.setTextSize(control.getTextSize()-4);
                pubdateView.setTextSize(control.getTextSize()-4);
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
	    

