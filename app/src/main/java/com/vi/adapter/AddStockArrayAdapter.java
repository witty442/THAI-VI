package com.vi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vi.R;
import com.vi.common.Item;
import com.vi.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;


public class AddStockArrayAdapter extends ArrayAdapter<Item> {
    private LayoutInflater mInflater;
    private Context context;
    private int selectedItem = -1; // no item selected by default
    LogUtils log = new LogUtils("AddStockArrayAdapter");

    public AddStockArrayAdapter(Context context, int textViewResourceId, List<Item> objects) {
        super(context, textViewResourceId, objects);
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);
        this.context = context;

    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    /** Set view Feed Item **/
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        view = mInflater.inflate(R.layout.addstock_page_row, null);
        Item item = getItem(position);

        TextView stockID = (TextView) view.findViewById(R.id.stockId);
        TextView stockTitle = (TextView) view.findViewById(R.id.stockName);

        if (stockID != null){
            stockID.setText(""+item.getId());
        }
        if(stockTitle != null){
            stockTitle.setText(item.getTitle());
        }
        return view;
    }

    public void setSelectItem(int selectItem){
        this.selectedItem = selectItem;
    }
}
	    

