package com.vi.adapter;

import java.util.ArrayList;
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


public class GotoPageArrayAdapter extends ArrayAdapter<String> {
    private LayoutInflater mInflater;
    private Context context;
    private List<String> listData = new ArrayList<String>(); //list ในการเก็บข้อมูลของ DataShow


    public GotoPageArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
        super(context, textViewResourceId, objects);
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.listData = objects;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    /** Set view Feed Item **/
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        view = mInflater.inflate(R.layout.goto_page_row, null);
        TextView pageID = (TextView) view.findViewById(R.id.gotoPageId);
        TextView pageText = (TextView) view.findViewById(R.id.gotoPageText);

        if (pageID != null){
            pageID.setText(listData.get(position));
        }
        if(pageText != null){
            pageText.setText("");
        }
        return view;
    }
}
	    

