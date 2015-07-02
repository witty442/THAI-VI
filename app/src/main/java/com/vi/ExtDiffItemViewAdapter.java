package com.vi;

import com.vi.utils.LogUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ExtDiffItemViewAdapter extends BaseAdapter implements ExtTitleProvider {

    private static final int VIEW1 = 0;
    private static final int VIEW2 = 1;
    private static final int VIEW_MAX_COUNT = VIEW2 + 1;
	private String[] names = {"Article","MyLocal"};
    private LayoutInflater mInflater;
    private Context context ;
    private String currentTab;
    private static LogUtils log = new LogUtils("ExtDiffItemViewAdapter");

    public ExtDiffItemViewAdapter(Context context,String[] names) {
    	this.context = context;
    	this.names = names;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_MAX_COUNT;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int view = getItemViewType(position);
        //log.debug("view:"+view);
        if (convertView == null) {
          
            switch (view) {
            case VIEW1:
                convertView = mInflater.inflate(R.layout.main_item_article_layout, null);
                currentTab  = names[position];
                convertView.setTag(new String("Article"));
                break;
            case VIEW2:
                convertView = mInflater.inflate(R.layout.main_item_local_layout, null);
                currentTab  = names[position];
                convertView.setTag(new String("Video"));
                break;
        }
        }
        return convertView;
    }

    /* (non-Javadoc)
	 * @see org.taptwo.android.widget.TitleProvider#getTitle(int)
	 */
	public String getTitle(int position) {
		return names[position];
	}
	
	public String getCurrentTab(){
    	return currentTab;
    }

}
