package com.vi.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vi.R;


 public class LinkArrayAdapter extends ArrayAdapter<String[]> {
    	private LayoutInflater mInflater;
    	private Context context;
    	private List<String[]> listData = new ArrayList<String[]>(); //list ในการเก็บข้อมูลของ DataShow
    	 

    	public LinkArrayAdapter(Context context, int textViewResourceId, List<String[]> objects) {
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
            view = mInflater.inflate(R.layout.link_page_row, null);
            TextView pageID = (TextView) view.findViewById(R.id.gotoPageId);
            TextView pageText = (TextView) view.findViewById(R.id.gotoPageText);
      
            if (pageID != null){
            	final String url = listData.get(position)[0];
            	pageID.setText(url);
            	pageID.setVisibility(View.GONE);
            }
            if(pageText != null){
            	final String url = listData.get(position)[0];
            	pageText.setText(listData.get(position)[1]);
            	pageText.setOnClickListener(new OnClickListener(){
      	            public void onClick(View v){
      	            	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      	            	context.startActivity(browserIntent);
      	            }
      	        });   
            }
            return view;
        }
    }
	    

