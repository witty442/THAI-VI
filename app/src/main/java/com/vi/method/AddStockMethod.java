package com.vi.method;

import com.vi.ControlHelper;
import com.vi.R;
import com.vi.adapter.FeedMainItemAdapter;
import com.vi.common.Control;
import com.vi.common.Feed;
import com.vi.common.Item;
import com.vi.storage.DBAdapter;
import com.vi.utils.Constants;

import java.util.List;

/**
 * Created by WITTY-HP on 12/11/2015.
 */
public class AddStockMethod {

    public static List<Item> searchStock100ByKey(DBAdapter mDbFeedAdapter,String key) {
        //Control control = ControlHelper.getControlContent(this, currentFontSize, currentBgColor, topicCurPage);

        //init get Item All to ArList
        int feedId =20 ;//stock_100_new //default
        List<Item> stockList = mDbFeedAdapter.getItemsDBByTitle(feedId,key);

        return stockList;
    }
}
