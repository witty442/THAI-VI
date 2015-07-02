package com.vi.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vi.R;
import com.vi.common.Catalog;
import com.vi.common.Control;
import com.vi.utils.LogUtils;


public class MainAdapter extends ArrayAdapter<Catalog> {
    private LayoutInflater mInflater;
    private Context context;
    private Control control;
    private int selectedItem = -1; // no item selected by default
    LogUtils log = new LogUtils("MainAdapter");

    public MainAdapter(Context context, int textViewResourceId, List<Catalog> objects,Control control) {
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
        Catalog feed = getItem(position);
        View view = convertView;
        if(view ==null)
            view = mInflater.inflate(R.layout.main_row, null);

        //log.debug("View Position:["+position+"] selectedItem["+selectedItem+"],isSelected:"+view.isSelected());
        if(selectedItem == position){
            view.setBackgroundColor(context.getResources().getColor(R.color.bg_listview_selected));
            //view.setBackgroundColor(0xFF00FF00);
        }else{
            view.setBackgroundColor(context.getResources().getColor(R.color.bg_color_mainstyle));
            //view.setBackgroundColor(0xFF00FF00);
        }

        TextView titleView = (TextView) view.findViewById(R.id.title);
        TextView titleId = (TextView) view.findViewById(R.id.title_id);
        TextView totalView = (TextView) view.findViewById(R.id.main_total);
        TextView topicTypeView = (TextView) view.findViewById(R.id.topicType);
        TextView toptenView = (TextView) view.findViewById(R.id.topten);

        // log.debug("totalView:"+totalView);

        if (titleView != null){
            titleView.setText(feed.getTitle());
            if(control.getTextSize() > 0){
                titleView.setTextSize(control.getTextSize());
            }
        }
        if (titleId != null){
            titleId.setText(feed.getId()+"");
            titleId.setVisibility(View.GONE);
        }
        if (totalView != null){
            totalView.setText("จำนวน ("+feed.getTotalItem()+")");
            if(control.getTextSize() > 0){
                totalView.setTextSize(control.getTextSize()-2);
            }
        }
        if (topicTypeView != null){
            topicTypeView.setText(feed.getType());
            topicTypeView.setVisibility(View.GONE);
        }
        if (toptenView != null){
            toptenView.setText(feed.getTopten());
            toptenView.setVisibility(View.GONE);
        }
        return view;
    }

    public void setSelectItem(int selectItem){
        this.selectedItem = selectItem;
    }

}
	    

