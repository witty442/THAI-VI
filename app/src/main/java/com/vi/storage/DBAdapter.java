/*
 * Copyright (C) 2010-2011 Mathieu Favez - http://mfavez.com
 *
 *
 * This file is part of FeedGoal.
 * 
 * FeedGoal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FeedGoal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FeedGoal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vi.storage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;

import com.vi.FeedException;
import com.vi.common.Feed;
import com.vi.common.Item;
import com.vi.parser.JSoupHelperNoAuthen;
import com.vi.utils.Constants;
import com.vi.utils.LogUtils;
import com.vi.utils.Utils;


public class DBAdapter {
	
	private static final String LOG_TAG = "DbFeedAdapter";
	private static long errorId = 0;
	
	private final Context mCtx;
	private long  feedId;
	private String curFeedType;
	private DbHelper mDbHelper;
	private SQLiteDatabase mDb;
	private static LogUtils log = new LogUtils("DBAdapter");
	
	private static class DbHelper extends SQLiteOpenHelper {
		private static final String LOG_TAG = "DbHelper";
    	private DBAdapter mDbfa;
        private long feedId;
        private String curFeedType;
        
        DbHelper(DBAdapter dbfa,long feedId,String curFeedType) {
           super(dbfa.mCtx, DBSchema.DATABASE_NAME, null, DBSchema.DATABASE_VERSION);
           mDbfa = dbfa;
           this.feedId = feedId;
           this.curFeedType = curFeedType;
        }
        DbHelper(DBAdapter dbfa) {
            super(dbfa.mCtx, DBSchema.DATABASE_NAME, null, DBSchema.DATABASE_VERSION);
            mDbfa = dbfa;
         }
         
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DBSchema.FeedSchema.CREATE_TABLE);
            // Read and populate OPML feeds
            log.debug("DbHelper onCreate");
            try {
                // Get resource feeds
            	JSoupHelperNoAuthen jsoup = new JSoupHelperNoAuthen();
                List<Feed> resourceFeeds = jsoup.getFeedCatalogsFromDropboxDB(curFeedType);
                
                // Populate resource feeds
                initFeeds(db, resourceFeeds);
            } catch (Exception ioe) {
                Log.e(LOG_TAG,"",ioe);
                errorId = errorId + 1;
        		//TrackerAnalyticsHelper.trackError(mDbfa.mCtx, Long.toString(errorId), ioe.getMessage(), LOG_TAG);
            }
            db.execSQL(DBSchema.ItemSchema.CREATE_TABLE);
       
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	 log.debug("DbHelper onUpgrade");
        	 
            if (oldVersion <= 2) {
            	Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
            	//String alter_table = "ALTER TABLE " + DBSchema.FeedSchema.TABLE_NAME + " ADD " + DBSchema.FeedSchema.COLUMN_HOMEPAGE + " TEXT;";
            	//db.execSQL(alter_table);
            	String alter_table = "ALTER TABLE " + DBSchema.ItemSchema.TABLE_NAME + " ADD " + DBSchema.ItemSchema.COLUMN_CONTENT + " TEXT;";
            	db.execSQL(alter_table);
            	
            	
            	// Reformat and update already existing items description in db for offline reading
            	long feedId = -1;
            	Cursor feedCursor = null;
            	long itemId = -1;
            	String itemDescription = null;
            	String itemContent = null;
            	Cursor itemCursor = null;
            	ContentValues values = null;
            	
            	feedCursor = db.query(DBSchema.FeedSchema.TABLE_NAME, new String[]{DBSchema.FeedSchema._ID}, null, null, null, null, DBSchema.FeedSchema._ID + DBSchema.SORT_ASC);
            	feedCursor.moveToFirst();
        		while (!feedCursor.isAfterLast()) {
        			feedId = feedCursor.getLong(feedCursor.getColumnIndex(DBSchema.FeedSchema._ID));
        			itemCursor = db.query(DBSchema.ItemSchema.TABLE_NAME, null, DBSchema.ItemSchema.COLUMN_FEED_ID + "=?",new String[]{Long.toString(feedId)}, null, null, DBSchema.ItemSchema._ID + DBSchema.SORT_ASC);
        			itemCursor.moveToFirst();
        			
        			while (!itemCursor.isAfterLast()) {
        				itemId = itemCursor.getLong(itemCursor.getColumnIndex(DBSchema.ItemSchema._ID));
        				if (!itemCursor.isNull(itemCursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_DESCRIPTION)))
        					itemDescription = itemCursor.getString(itemCursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_DESCRIPTION));
        				
        				// Remove HTML format from item description
        				if (itemDescription != null) {
		                	SpannableStringBuilder spannedStr = (SpannableStringBuilder)Html.fromHtml(itemDescription.toString().trim());
			        		Object[] spannedObjects = spannedStr.getSpans(0,spannedStr.length(),Object.class);
			        		for (int i = 0; i < spannedObjects.length; i++) {
			        			if (spannedObjects[i] instanceof ImageSpan)
			        				spannedStr.replace(spannedStr.getSpanStart(spannedObjects[i]), spannedStr.getSpanEnd(spannedObjects[i]), "");
			        		}
			        		
			        		itemDescription = spannedStr.toString().trim() + System.getProperty("line.separator" );
			        		itemContent = spannedStr.toString().trim() + System.getProperty("line.separator" );
			        		
			        		values = new ContentValues();
			                values.put(DBSchema.ItemSchema.COLUMN_DESCRIPTION, itemDescription);
			                values.put(DBSchema.ItemSchema.COLUMN_CONTENT, itemContent);
			                
			        		db.update(DBSchema.ItemSchema.TABLE_NAME, values, DBSchema.ItemSchema._ID + "=?", new String[]{Long.toString(itemId)});
			        		
			        		itemCursor.moveToNext();
        				}
        			}
        			
        			if (itemCursor != null)
            			itemCursor.close();
        			
        			feedCursor.moveToNext();
        		}
        		
        		if (feedCursor != null)
        			feedCursor.close();
            }

            //Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            //db.execSQL(DbSchema.FeedSchema.DROP_TABLE);
            //db.execSQL(DbSchema.ItemSchema.DROP_TABLE);
            
            try {
                // Get resource feeds
            	JSoupHelperNoAuthen jsoup = new JSoupHelperNoAuthen();
                List<Feed> resourceFeeds = jsoup.getFeedCatalogsFromDropboxDB(curFeedType);
                // Populate resource feeds
                initFeeds(db, resourceFeeds);
            } catch (Exception ioe) {
                Log.e(LOG_TAG,"",ioe);
                errorId = errorId + 1;
        		//TrackerAnalyticsHelper.trackError(mDbfa.mCtx, Long.toString(errorId), ioe.getMessage(), LOG_TAG);
            }
        }

        private void initFeeds(SQLiteDatabase db, List<Feed> feeds) {
            if (feeds != null) {
                Iterator<Feed> feedsIterator = feeds.iterator();
                Feed feed = null;
                long feedId = -1;
                boolean populated = false;
                while (feedsIterator.hasNext()) {
	                feed = feedsIterator.next();
	                feedId = hasFeed(db,feed);
	                
	                if (feedId == -1)
	                	populated = insertFeed(db, feed);               
	                else {
                        feed.setId(feedId);
                        populated = updateFeed(db, feed);
	                }
	                
	                if (!populated)
	                    Log.e(LOG_TAG, "Feed with title '"+feed.getTitle()+"' cannot be populated into the database. Feed URL: " + feed.getLink().toString());
                }
            }
        }
        
        private boolean insertFeed(SQLiteDatabase db, Feed feed) {
            boolean inserted = false;
            ContentValues values = mDbfa.getContentValues(feed);
            inserted = (db.insert(DBSchema.FeedSchema.TABLE_NAME, null, values) != -1);     
            return inserted;
        }
        
        private boolean updateFeed(SQLiteDatabase db, Feed feed) {
            boolean updated = false;
            ContentValues values = mDbfa.getContentValues(feed);
            updated = (db.update(DBSchema.FeedSchema.TABLE_NAME, values, DBSchema.FeedSchema.COLUMN_ID + "=?", new String[]{Long.toString(feed.getId())}) > 0);  
            return updated;
        }
        
        // check if feed URL already exists in the DB
        // if exists, returns feed id
        // if does not exist, returns -1
        private long hasFeed(SQLiteDatabase db, Feed feed) {
            long feedId = -1;
            Cursor cursor = db.query(DBSchema.FeedSchema.TABLE_NAME, null, DBSchema.FeedSchema.COLUMN_ID + "=?", new String[]{Long.toString(feed.getId())}, null, null, null);
            if (cursor.moveToFirst())
                    feedId = cursor.getLong(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_ID));
            
            if (cursor != null)
                    cursor.close();
            
            return feedId;
        }
    }
    
	public DBAdapter(Context ctx) {
	    this.mCtx = ctx;
	}
	
	public DBAdapter(Context ctx,long feedId,String curFeedType) {
	    this.mCtx = ctx;
	    this.feedId = feedId;
	    this.curFeedType = curFeedType;
	}
	
	/* public boolean isOpen() {
	    	
	   //  mDbHelper.
	     
	 }
*/
    public DBAdapter open() {
    	//log.debug("Open");
        mDbHelper = new DbHelper(this , feedId,curFeedType);
        mDb = mDbHelper.getWritableDatabase();
     
        return this;
    }
    
    public void truncate() {
        log.debug("Drop Table");
        mDb.execSQL(DBSchema.FeedSchema.DROP_TABLE);
        mDb.execSQL(DBSchema.ItemSchema.DROP_TABLE);
        
        //log.debug("Create Table");
        mDb.execSQL(DBSchema.FeedSchema.CREATE_TABLE);
        mDb.execSQL(DBSchema.ItemSchema.CREATE_TABLE);
    }

    public void close() {
        mDbHelper.close();
    }
    
    public void cleanFeedItemsALL(String feedType) {
    	log.debug("Clear Feed Item ALL CurrentFeedId:");
    	List<Feed> items;
    	ListIterator<Feed> itemListIterator;
    	
    	items = getFeeds(feedType);
    	itemListIterator = items.listIterator();
		while (itemListIterator.hasNext()) {
			long feedId  = itemListIterator.next().getId();
			log.debug("remove feed id:"+feedId);
			cleanFeedItemsALL(feedId,feedType);
		}
    }
    
    public void cleanFeedItemsALL(long feedId,String feedType) {
    	log.debug("Clear Feed Item CurrentFeedId:"+feedId);
    	List<Item> items;
    	ListIterator<Item> itemListIterator;
    	
    	Feed feed = getFeed(feedId,feedType);
    	items = feed.getItems();
    	if(items != null && items.size() > 0){
	    	itemListIterator = items.listIterator();
			while (itemListIterator.hasNext()) {
				long id  = itemListIterator.next().getId();
				log.debug("remove feed item id:"+id);
				removeItem(id);
			}
    	}
    }
    
    public void deleteItemNotInLocal(long feedId,String feedType) {
    	log.debug("Clear Feed Item CurrentFeedId:"+feedId);
    	List<Item> items;
    	ListIterator<Item> itemListIterator;
    	
    	Feed feed = getFeed(feedId,feedType);
    	items = feed.getItems();
    	if(items != null && items.size() > 0){
	    	itemListIterator = items.listIterator();
			while (itemListIterator.hasNext()) {
			    Item itemFind  = itemListIterator.next();
				if(Utils.isNull(itemFind.getPathFile()).equals("")){
				  removeItem(itemFind.getId());
				}
			}
    	}
    }
    
    public void deleteItemNotInLocal(long feedId) {
    	try{
    	   StringBuffer sql = new StringBuffer("");
    	   sql.append("delete from "+DBSchema.ItemSchema.TABLE_NAME+" where "+DBSchema.ItemSchema.COLUMN_FEED_ID+"="+feedId+" and fav <> 1 ");
    	   log.debug("deleteItemNotInLocal sql:"+sql.toString());
    	   
    	   mDb.execSQL(sql.toString());
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public void updateTopicCurPage(long feedId) {
    	try{
    	   StringBuffer sql = new StringBuffer("");
    	   sql.append("UPDATE "+DBSchema.ItemSchema.TABLE_NAME +
    	        " set "+DBSchema.ItemSchema.COLUMN_TOPIC_CUR_PAGE +"=9999 "+
    	   		" where "+DBSchema.ItemSchema.COLUMN_FEED_ID+"="+feedId);
    	   
    	   log.debug("updateTopicCurPage sql:"+sql.toString());
    	   mDb.execSQL(sql.toString());
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    public boolean isFeedExist(String feedType) {
    	boolean exist = false;
    	Cursor cursor = null;
    	if( !Utils.isNull(feedType).equals("")){
		   cursor = mDb.query(DBSchema.FeedSchema.TABLE_NAME, new String[]{DBSchema.FeedSchema.COLUMN_ID}, DBSchema.FeedSchema.COLUMN_TYPE + "=?", new String[]{feedType}, null, null, DBSchema.FeedSchema.COLUMN_ID + DBSchema.SORT_ASC);
    	}else{
    	   cursor = mDb.query(DBSchema.FeedSchema.TABLE_NAME, new String[]{DBSchema.FeedSchema.COLUMN_ID}, null, null, null, null, DBSchema.FeedSchema.COLUMN_ID + DBSchema.SORT_ASC);
    	}
    	
		//log.debug("cursor:"+cursor);
		int c = cursor.getCount();
		//log.debug("Count:"+c);
		if(c > 0){
			exist = true;
		}
		if (cursor != null)
			cursor.close();
		
		return exist;
    }
    
    public int countFeedsByLike(String feedType) {
        int c = 0;
    	Cursor cursor = null;
    	String sql = " select count(*) as c from feeds  ";
 	          sql += " where type LIKE '"+feedType+"%'";
 	    
 	    cursor = mDb.rawQuery(sql,null);
 	    cursor.moveToFirst();
		if(!cursor.isAfterLast()) {
			c = cursor.getInt(cursor.getColumnIndex("c"));
		}
		if (cursor != null)
			cursor.close();
		
		return c;
    }
    
    public String countFirstUpdateDateFeedsByLike(String feedType) {
        String c = "";
    	Cursor cursor = null;
    	String sql = " select update_date from feeds  ";
 	           sql += " where type LIKE '"+feedType+"%'";
 	    
 	    log.debug("sql:"+sql);
 	    cursor = mDb.rawQuery(sql,null);
 	    cursor.moveToFirst();
		if(!cursor.isAfterLast()) {
			if (!cursor.isNull(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_UPDATE_DATE))){
				Date upDate = new Date(cursor.getLong(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_UPDATE_DATE)));
				c = Utils.convertToString(upDate);
			}
		}
		if (cursor != null)
			cursor.close();
		
		return c;
    }

    public List<Feed> getFeeds(String feedType) {
		List<Feed> feeds = new ArrayList<Feed>();
		Cursor cursor = null;
		if( !Utils.isNull(feedType).equals("")){
			cursor = mDb.query(DBSchema.FeedSchema.TABLE_NAME, new String[]{DBSchema.FeedSchema.COLUMN_ID}, 
					DBSchema.FeedSchema.COLUMN_TYPE + "=?", 
					new String[]{feedType}, null, null, 
					DBSchema.FeedSchema.COLUMN_CREATE_DATE + DBSchema.SORT_ASC);
		}else{
			cursor = mDb.query(DBSchema.FeedSchema.TABLE_NAME, new String[]{DBSchema.FeedSchema.COLUMN_ID}, 
					null, null, null, null, 
					DBSchema.FeedSchema.COLUMN_CREATE_DATE + DBSchema.SORT_ASC);
		}
		cursor.moveToFirst();
		
		while (!cursor.isAfterLast()) {
			Feed feed = getFeed(cursor.getLong(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_ID)),false,feedType);
			if (feed != null)
				feeds.add(feed);
			cursor.moveToNext();
		}
		
		if (cursor != null)
			cursor.close();
		
		return feeds;
    }
    
    private String makePlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }
    
    public List<Feed> getFeeds(String[] feedTypes) {
		List<Feed> feeds = new ArrayList<Feed>();
		Cursor cursor = null;
		if( feedTypes != null && feedTypes.length >0){
			String query = "SELECT * FROM feeds "
			    + " WHERE type IN (" + makePlaceholders(feedTypes.length) + ") ORDER BY CREATE_DATE ASC ";
			//log.debug("sql:"+query);
			//log.debug("feedTypes :"+feedTypes.toString());
			
			cursor = mDb.rawQuery(query, feedTypes);
		}
		if(cursor != null){
		   cursor.moveToFirst();
		
			while (!cursor.isAfterLast()) {
				Feed feed = getFeed(cursor.getLong(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_ID)),false,"");
				if (feed != null){
					//log.debug("Feed Title:"+feed.getTitle()+",create Date:"+feed.getCreateDate());
					feeds.add(feed);
				}
				cursor.moveToNext();
			}
		}
		if (cursor != null)
			cursor.close();
		
		return feeds;
    }
    
    public Feed getFirstFeed(String feedType) {
    	Feed firstFeed = null;
    	boolean found = false;
    	Cursor cursor = null;
		if( !Utils.isNull(feedType).equals("")){
			cursor = mDb.query(DBSchema.FeedSchema.TABLE_NAME, new String[]{DBSchema.FeedSchema.COLUMN_ID}, DBSchema.FeedSchema.COLUMN_TYPE + "=?", new String[]{feedType}, null, null, DBSchema.FeedSchema.COLUMN_ID + DBSchema.SORT_ASC);
		}else{
			cursor = mDb.query(DBSchema.FeedSchema.TABLE_NAME, new String[]{DBSchema.FeedSchema.COLUMN_ID}, null, null, null, null, DBSchema.FeedSchema.COLUMN_ID + DBSchema.SORT_ASC);
		}
		cursor.moveToFirst();
		while (!cursor.isAfterLast() && !found) {
			firstFeed = getFeed(cursor.getLong(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_ID)),feedType);
			cursor.moveToNext();
			found = true;
		}
		
		if (cursor != null)
			cursor.close();
		
		return firstFeed;
    }
    
    public Feed getFeed(long id,String feedType) {
    	return getFeedDB(id, true,feedType);
    }
    public Feed getFeedNoItem(long id,String feedType) {
    	return getFeedDB(id, false,feedType);
    }
    
    public Feed getFeed(long id,boolean getItems,String feedType) {
    	return getFeedDB(id, getItems,feedType);
    }
    
    public Feed getFeedNotInLocal(long id,String feedType) {
    	Feed feed = null;
		Cursor cursor = null;
		log.debug("getFeedNotInLocal");
		try {
	
			 cursor = mDb.query(DBSchema.FeedSchema.TABLE_NAME, null, 
				       DBSchema.FeedSchema.COLUMN_ID + "=? and "+DBSchema.FeedSchema.COLUMN_TYPE +"= ?", new String[]{Long.toString(id),feedType}, null, null, null);
				
			if (cursor.moveToFirst()) {
				feed = new Feed();
				while (!cursor.isAfterLast()) {
					feed.setId(cursor.getLong(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_ID)));
					feed.setLink(cursor.getString(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_URL)));
					feed.setOrgLink(feed.getLink());
					feed.setTitle(cursor.getString(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_TITLE)));
					if (!cursor.isNull(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_TYPE)))
						feed.setType(cursor.getString(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_TYPE)));
					
					if (!cursor.isNull(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_CREATE_DATE))){
						feed.setCreateDate(new Date(cursor.getLong(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_CREATE_DATE))));
					}
					//Calendar.getInstance().setTimeInMillis(cursor.getInt(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_CREATE_DATE)));
					//feed.setCreateDate(Calendar.getInstance().getTime());
					
					if (!cursor.isNull(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_UPDATE_DATE))){
						feed.setUpdateDate(new Date(cursor.getLong(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_UPDATE_DATE))));
					}
					//Calendar.getInstance().setTimeInMillis(cursor.getInt(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_UPDATE_DATE)));
					//feed.setUpdateDate(Calendar.getInstance().getTime());
					
					feed.setEnabled(cursor.getInt(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_ENABLE)));
					feed.setAuthor(cursor.getString(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_AUTHOR)));
					//get Item
					feed.setItems(getItems( -1,"default","DESC",feed,feedType,-1)); 
					
					//getTotal Items
					feed.setTotalItem(getTotalItemDB(id));
					
					cursor.moveToNext();
				}
			} else {
				//throw new FeedException("Feed with id " + id + " not found in the database.");
				return null;
			}
		}catch(Exception e){
			Log.e(LOG_TAG,"",e);
			errorId = errorId + 1;
    		//TrackerAnalyticsHelper.trackError(mCtx, Long.toString(errorId), e.getMessage(), LOG_TAG);
		}
		
		if (cursor != null)
			cursor.close();
		
		return feed;
    }
    
	public Feed getFeedDB(long id,boolean getItems,String feedType) {
		Feed feed = null;
		Cursor cursor = null;
		//log.debug("getFeedDB");
		String sql = "";
		try {
			
			 sql += "\n select f.* from feeds f where 1=1 ";
			 if(!Utils.isNull(feedType).equals("")){
			    sql +=" \n and f.type ='"+feedType+"'";
			 }
			 if(id !=0){
				 sql +=" \n and f.feed_id ="+id+""; 
			 }
			sql +="\n order by f.update_date desc ";

			log.debug("sql:\n"+sql);
		    cursor = mDb.rawQuery(sql, null);
		    	
			/*if(id ==0){
				//by type
				if(!Utils.isNull(feedType).equals("")){
					cursor = mDb.query(DBSchema.FeedSchema.TABLE_NAME, null, 
					       DBSchema.FeedSchema.COLUMN_ID + "=? and "+DBSchema.FeedSchema.COLUMN_TYPE +"= ?", new String[]{Long.toString(id),feedType}, null, null, null);
				}else{
					//all 
					cursor = mDb.query(DBSchema.FeedSchema.TABLE_NAME, null,null, null, null, null, null);
				}
			}else{
				if(Utils.isNull(feedType).equals("")){
				   cursor = mDb.query(DBSchema.FeedSchema.TABLE_NAME, null, 
						DBSchema.FeedSchema.COLUMN_ID + "=? ", new String[]{Long.toString(id)}, null, null, null);
				}else{
				   cursor = mDb.query(DBSchema.FeedSchema.TABLE_NAME, null, 
						DBSchema.FeedSchema.COLUMN_ID + "=? and "+DBSchema.FeedSchema.COLUMN_TYPE +"= ?", new String[]{Long.toString(id),feedType}, null, null, null);
				}
			}*/
		    	
			if (cursor.moveToFirst()) {
				feed = new Feed();
				while (!cursor.isAfterLast()) {
					feed.setId(cursor.getLong(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_ID)));
					feed.setLink(Utils.isNull(cursor.getString(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_URL))));
					feed.setOrgLink(feed.getLink());
					feed.setTitle(Utils.isNull(cursor.getString(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_TITLE))));
				    feed.setType(Utils.isNull(cursor.getString(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_TYPE))));
					
					if (!cursor.isNull(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_CREATE_DATE))){
						feed.setCreateDate(new Date(cursor.getLong(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_CREATE_DATE))));
					}
					Calendar.getInstance().setTimeInMillis(cursor.getInt(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_CREATE_DATE)));
					feed.setCreateDate(Calendar.getInstance().getTime());
					
					if (!cursor.isNull(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_UPDATE_DATE))){
						feed.setUpdateDate(new Date(cursor.getLong(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_UPDATE_DATE))));
					}
					Calendar.getInstance().setTimeInMillis(cursor.getInt(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_UPDATE_DATE)));
					feed.setUpdateDate(Calendar.getInstance().getTime());
					
					feed.setEnabled(cursor.getInt(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_ENABLE)));
					feed.setAuthor(Utils.isNull(cursor.getString(cursor.getColumnIndex(DBSchema.FeedSchema.COLUMN_AUTHOR))));
					if(getItems){
					  feed.setItems(getItems( -1,"default","DESC",feed,feedType,-1)); 
					}
					//getTotal Items
					feed.setTotalItem(getTotalItemDB(id));
					
					cursor.moveToNext();
				}
			} else {
				//throw new FeedException("Feed with id " + id + " not found in the database.");
				return null;
			}
		}catch(Exception e){
			Log.e(LOG_TAG,"",e);
			errorId = errorId + 1;
    		//TrackerAnalyticsHelper.trackError(mCtx, Long.toString(errorId), e.getMessage(), LOG_TAG);
		}
		
		if (cursor != null)
			cursor.close();
		
		return feed;
	}
	
	public boolean isFeedExist(long id) {
		Cursor cursor = null;
		try {
			cursor = mDb.query(DBSchema.FeedSchema.TABLE_NAME, null, DBSchema.FeedSchema.COLUMN_ID + "=?", new String[]{Long.toString(id)}, null, null, null);
			//throw new FeedException("Feed with id " + id + " not found in the database.");
			return cursor.moveToFirst();
		}catch(Exception e){
			Log.e(LOG_TAG,"",e);
			errorId = errorId + 1;
		}
		
		if (cursor != null)
			cursor.close();
		
		return false;
	}
	
	public boolean isFeedItemExist(long id) {
		Cursor cursor = null;
		try {
			cursor = mDb.query(DBSchema.ItemSchema.TABLE_NAME, null, DBSchema.ItemSchema.COLUMN_FEED_ID + "=?", new String[]{Long.toString(id)}, null, null, null);
			//throw new FeedException("Feed with id " + id + " not found in the database.");
			return cursor.moveToFirst();
		}catch(Exception e){
			Log.e(LOG_TAG,"",e);
			errorId = errorId + 1;
		}
		
		if (cursor != null)
			cursor.close();
		
		return false;
	}
	
    public ContentValues getContentValues(Feed feed) {
    	ContentValues values = new ContentValues();
        values.put(DBSchema.FeedSchema.COLUMN_URL, feed.getLink().toString());
        
        //set id 
        values.put(DBSchema.FeedSchema.COLUMN_ID, feed.getId());
        
        if (feed.getTitle() == null)
        	values.putNull(DBSchema.FeedSchema.COLUMN_TITLE);
        else
        	values.put(DBSchema.FeedSchema.COLUMN_TITLE, feed.getTitle());
        if (feed.getType() == null)
        	values.putNull(DBSchema.FeedSchema.COLUMN_TYPE);
        else
        	values.put(DBSchema.FeedSchema.COLUMN_TYPE, feed.getType());
		
        if (feed.getCreateDate() == null)
    		values.putNull(DBSchema.FeedSchema.COLUMN_CREATE_DATE);
    	else
    		values.put(DBSchema.FeedSchema.COLUMN_CREATE_DATE, feed.getCreateDate().getTime());  
        
        if (feed.getUpdateDate() == null)
    		values.putNull(DBSchema.FeedSchema.COLUMN_UPDATE_DATE);
    	else
    		values.put(DBSchema.FeedSchema.COLUMN_UPDATE_DATE, feed.getUpdateDate().getTime());  
		
    	int state = DBSchema.ON;
    	if (!feed.isEnabled())
    		state = DBSchema.OFF;
    	values.put(DBSchema.FeedSchema.COLUMN_ENABLE, state);
     
    	if (feed.getAuthor() == null)
          values.putNull(DBSchema.FeedSchema.COLUMN_AUTHOR);
        else
          values.put(DBSchema.FeedSchema.COLUMN_AUTHOR, feed.getAuthor());
    	 
    	return values;
    }
    
    public long addFeed(Feed feed) {
        return addFeed(getContentValues(feed),feed.getItems());
    }
    
    public long addFeed(ContentValues values, List<Item> items) {
    	long feedId = mDb.insert(DBSchema.FeedSchema.TABLE_NAME, null, values);
        if (feedId == -1)
        	Log.e(LOG_TAG, "Feed '" + values.getAsString(DBSchema.ItemSchema.COLUMN_TITLE) + "' could not be inserted into the database. Feed values: " + values.toString());
        
        if (items != null && feedId != -1) {
    		Iterator<Item> iterator = items.iterator();
    		while (iterator.hasNext()) {
    			addItem(feedId,iterator.next());
    		}
    	}
        return feedId;
    }
    
    public ContentValues getUpdateContentValuesHeader(Feed feed) {
    	ContentValues values = new ContentValues();

    	if (feed.getTitle() == null){
           values.putNull(DBSchema.FeedSchema.COLUMN_TITLE);
    	}else{
           values.put(DBSchema.FeedSchema.COLUMN_TITLE, feed.getTitle());
    	}
        if (feed.getType() == null){
           values.putNull(DBSchema.FeedSchema.COLUMN_TYPE);
        }else{
           values.put(DBSchema.FeedSchema.COLUMN_TYPE, feed.getType());
        }
        
        if (feed.getLink() == null){
           values.putNull(DBSchema.FeedSchema.COLUMN_URL);
        }else{
           values.put(DBSchema.FeedSchema.COLUMN_URL, feed.getLink());
        }

     	if (feed.getAuthor() == null){
           values.putNull(DBSchema.FeedSchema.COLUMN_AUTHOR);
     	}else{
           values.put(DBSchema.FeedSchema.COLUMN_AUTHOR, feed.getAuthor());
     	}
     	
		if (feed.getUpdateDate() != null){
    	   values.put(DBSchema.FeedSchema.COLUMN_UPDATE_DATE, feed.getUpdateDate().getTime());  	
		}
		if (feed.getCreateDate() != null){
	    	values.put(DBSchema.FeedSchema.COLUMN_CREATE_DATE, feed.getCreateDate().getTime());  	
		}
    	return values;
    }
    
    public ContentValues getUpdateContentValuesFeed(Feed feed) {
    	ContentValues values = new ContentValues();

    	if (feed.getTitle() == null){
           values.putNull(DBSchema.FeedSchema.COLUMN_TITLE);
    	}else{
           values.put(DBSchema.FeedSchema.COLUMN_TITLE, feed.getTitle());
    	}
        if (feed.getType() == null){
           values.putNull(DBSchema.FeedSchema.COLUMN_TYPE);
        }else{
           values.put(DBSchema.FeedSchema.COLUMN_TYPE, feed.getType());
        }
        if (feed.getLink() == null){
           values.putNull(DBSchema.FeedSchema.COLUMN_URL);
        }else{
           values.put(DBSchema.FeedSchema.COLUMN_URL, feed.getLink());
        }
     	if (feed.getAuthor() == null){
           values.putNull(DBSchema.FeedSchema.COLUMN_AUTHOR);
     	}else{
           values.put(DBSchema.FeedSchema.COLUMN_AUTHOR, feed.getAuthor());
     	}
    	return values;
    }
    
    public boolean updateFeed(Feed feed) {  	
    	return updateFeed(feed.getId(),feed.getTitle(), getUpdateContentValuesFeed(feed), feed.getItems());
    }
    
    public boolean updateFeedHead(Feed feed) {  	
    	return updateFeedHead(feed.getId(), getUpdateContentValuesHeader(feed));
    }
    
    public boolean updateFeed(long feedId,String title, ContentValues values, List<Item> items) {
    	boolean feedUpdated = (mDb.update(DBSchema.FeedSchema.TABLE_NAME, values, 
    			DBSchema.FeedSchema.COLUMN_ID + "=?",
    			new String[]{Long.toString(feedId)}) > 0);
        int r = 0;
        int u = 0;
        int i = 0;
    	if (feedUpdated && items != null) {
    		Iterator<Item> iterator = items.listIterator();
    		Item item = null;
    		while (iterator.hasNext()) {
    			item = iterator.next();
    			boolean hasItem = isExist(feedId,item);
    			//log.debug("hasItem:"+hasItem);
    			if (!hasItem){
    				r++;
    				i++;
    				addItem(feedId, item);
    				//log.debug("*** Insert Title["+r+"]:"+item.getTitle()+",feedId["+feedId+"]createDate["+item.getCreateDate()+"]");
    			}else{
    				r++;
    				u++;
    				updateItemByTitleAndFeedId(feedId,item);
    				//log.debug("*** Update Title["+r+"]:"+item.getTitle()+",feedId["+feedId+"]createDate["+item.getCreateDate()+"]");
    			}
    			//log.debug("*************************");
    		}
    	}
    	
    	log.debug("Item db all["+r+"],insert["+i+"]update["+u+"]");
    	return feedUpdated;
    }
    
    public boolean updateFeedHead(long feedId,ContentValues values) {
    	boolean feedUpdated = (mDb.update(DBSchema.FeedSchema.TABLE_NAME, values, 
    			DBSchema.FeedSchema.COLUMN_ID + "=?",
    			new String[]{Long.toString(feedId)}) > 0);
    	//log.debug("Update Feed Head:feedId["+feedId+"]title["+values.getAsString(DBSchema.FeedSchema.COLUMN_TITLE)+"]feedUpdated["+feedUpdated+"]");
    	return feedUpdated;
    }
    
    public boolean hasItem(long feedId, Item item) {
    	Cursor cursor = mDb.query(DBSchema.ItemSchema.TABLE_NAME, null, DBSchema.ItemSchema.COLUMN_FEED_ID + "=? AND (" + DBSchema.ItemSchema.COLUMN_LINK + "=?  OR " + DBSchema.ItemSchema.COLUMN_TITLE + "=?)", new String[]{Long.toString(feedId),item.getLink().toString(), item.getTitle()}, null, null, null);
    	boolean hasItem = cursor.moveToFirst();
    	//log.debug("hasItem:"+hasItem);
    	if (cursor != null)
			cursor.close();
    	
    	return hasItem;
    }
    
    public boolean isExist(long feedId,Item item) {
    	Cursor cursor = mDb.query(DBSchema.ItemSchema.TABLE_NAME, null,
    			DBSchema.ItemSchema.COLUMN_FEED_ID +"=? and "+DBSchema.ItemSchema.COLUMN_TITLE + "=? ", 
    			new String[]{Long.toString(feedId),item.getTitle()}, null, null, null);
    	boolean hasItem = cursor.moveToFirst();
    	//log.debug("hasItem:"+hasItem);
    	if (cursor != null)
			cursor.close();
    	
    	return hasItem;
    }
    
    public boolean removeFeed(Feed feed) {
    	return removeFeed(feed.getId(),feed.getType());
    }
    
    public boolean removeFeed(long id,String feedType) {
    	removeItems(id,feedType);
    	return (mDb.delete(DBSchema.FeedSchema.TABLE_NAME, DBSchema.FeedSchema.COLUMN_ID + "=?", new String[]{Long.toString(id)}) > 0);
    }
    
	public long getItemFeedId(long itemId) {
		long feedId = -1;
		Cursor cursor = null;
		try {
			cursor = mDb.query(DBSchema.ItemSchema.TABLE_NAME, null, DBSchema.ItemSchema._ID + "=?", new String[]{Long.toString(itemId)}, null, null, null);
			if (cursor.moveToFirst()) {
				while (!cursor.isAfterLast()) {
					feedId = cursor.getLong(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_FEED_ID));
					cursor.moveToNext();
				}
			} else {
				throw new FeedException("Feed id for item id " + itemId + " not found in the database.");
			}
		} catch (FeedException fe) {
			Log.e(LOG_TAG,"",fe);
			errorId = errorId + 1;	
    	}
    	if (cursor != null)
    		cursor.close();
		return feedId;
	}
	
	public Item getItem(long id) {
		Item item = getItemDB(id);
		if("".equals(Utils.isNull(item.getAuthor()))){
			Feed feed = getFeedNoItem(item.getFeedId(), "");
			item.setAuthor(feed.getAuthor());
		}
		return item;
	}
	
    public Item getItemDB(long id) {
		Item item = null;
		Cursor cursor = null;
		try {
			cursor = mDb.query(DBSchema.ItemSchema.TABLE_NAME, null, DBSchema.ItemSchema._ID + "=?", new String[]{Long.toString(id)}, null, null, null);
			if (cursor.moveToFirst()) {
				item = new Item();
				while (!cursor.isAfterLast()) {
					item.setFeedId(cursor.getLong(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_FEED_ID)));
					item.setId(cursor.getLong(cursor.getColumnIndex(DBSchema.ItemSchema._ID)));
					item.setLink(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_LINK)));
					item.setOrgLink(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_ORG_LINK)));
					item.setTitle(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_TITLE)));
					if (!cursor.isNull(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_DESCRIPTION)))
						item.setDescription(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_DESCRIPTION)));
					if (!cursor.isNull(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_CONTENT)))
						item.setContent(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_CONTENT)));
					if (!cursor.isNull(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_IMAGE)))
						item.setImage(new URL(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_IMAGE))));
					
					if (!cursor.isNull(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_CREATE_DATE))){
						item.setCreateDate(new Date(cursor.getLong(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_CREATE_DATE))));
					}
					
					if (!cursor.isNull(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_UPDATE_DATE))){
						item.setUpdateDate(new Date(cursor.getLong(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_UPDATE_DATE))));
					}
					
					item.setPathFile(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_PATH_FILE)));
					
					//log.debug("Item fav int:"+cursor.getInt(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_FAV)));
					
					item.setFav(cursor.getInt(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_FAV))==0?false:true);
					
					item.setRead(cursor.getInt(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_READ))==0?false:true);
					item.setCountOpen(cursor.getInt(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_COUNT_OPEN)));
					
					item.setAuthor(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_AUTHOR)));
					item.setType(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_TYPE)));
					
					item.setTotalReply(cursor.getInt(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_TOTAL_REPLY)));
					item.setTotalRead(cursor.getInt(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_TOTAL_READ)));
					item.setCurPage(cursor.getInt(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_CUR_PAGE)));
					item.setTopicCurPage(cursor.getInt(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_TOPIC_CUR_PAGE)));
					item.setLastPosition(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_LAST_POSITION)));
					
					cursor.moveToNext();
				}
			} else {
				throw new FeedException("Item with id " + id + " not found in the database.");
			}
		} catch (FeedException fe) {
			Log.e(LOG_TAG,"",fe);
			errorId = errorId + 1;
    	} catch (MalformedURLException mue) {
			Log.e(LOG_TAG,"",mue);
			errorId = errorId + 1;
		}
    	
    	if (cursor != null)
    		cursor.close();
    	
		return item;
	}
	public Item getItemDBByTitle(long feedId,String title) {
		Item item = null;
		Cursor cursor = null;
		try {
			//cursor = mDb.query(DBSchema.ItemSchema.TABLE_NAME, null, DBSchema.ItemSchema.COLUMN_TITLE + "=?", new String[]{title}, null, null, null);
			cursor = mDb.rawQuery("select * from items where feed_id =? and title = ?", new String[]{Long.valueOf(feedId).toString(),title});
			if (cursor.moveToFirst()) {
				item = new Item();
				while (!cursor.isAfterLast()) {
					item.setFeedId(cursor.getLong(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_FEED_ID)));
					item.setId(cursor.getLong(cursor.getColumnIndex(DBSchema.ItemSchema._ID)));
					item.setLink(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_LINK)));
					item.setOrgLink(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_ORG_LINK)));
					item.setTitle(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_TITLE)));
					if (!cursor.isNull(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_DESCRIPTION)))
						item.setDescription(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_DESCRIPTION)));
					if (!cursor.isNull(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_CONTENT)))
						item.setContent(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_CONTENT)));
					if (!cursor.isNull(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_IMAGE)))
						item.setImage(new URL(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_IMAGE))));

					if (!cursor.isNull(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_CREATE_DATE))){
						item.setCreateDate(new Date(cursor.getLong(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_CREATE_DATE))));
					}

					if (!cursor.isNull(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_UPDATE_DATE))){
						item.setUpdateDate(new Date(cursor.getLong(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_UPDATE_DATE))));
					}

					item.setPathFile(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_PATH_FILE)));

					//log.debug("Item fav int:"+cursor.getInt(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_FAV)));

					item.setFav(cursor.getInt(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_FAV))==0?false:true);

					item.setRead(cursor.getInt(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_READ))==0?false:true);
					item.setCountOpen(cursor.getInt(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_COUNT_OPEN)));

					item.setAuthor(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_AUTHOR)));
					item.setType(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_TYPE)));

					item.setTotalReply(cursor.getInt(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_TOTAL_REPLY)));
					item.setTotalRead(cursor.getInt(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_TOTAL_READ)));
					item.setCurPage(cursor.getInt(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_CUR_PAGE)));
					item.setTopicCurPage(cursor.getInt(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_TOPIC_CUR_PAGE)));
					item.setLastPosition(cursor.getString(cursor.getColumnIndex(DBSchema.ItemSchema.COLUMN_LAST_POSITION)));

					cursor.moveToNext();
				}
			} else {
				throw new FeedException("Item with feed_id " + feedId+ " not found in the database.");
			}
		} catch (FeedException fe) {
			Log.e(LOG_TAG,"",fe);
			errorId = errorId + 1;
		} catch (MalformedURLException mue) {
			Log.e(LOG_TAG,"",mue);
			errorId = errorId + 1;
		}

		if (cursor != null)
			cursor.close();

		return item;
	}
    
    public boolean isItemExistDB(String title) {
		Cursor cursor = null;
		//log.debug("Title["+title+"]");
		try {
			cursor = mDb.query(DBSchema.ItemSchema.TABLE_NAME, null, DBSchema.ItemSchema.COLUMN_TITLE + "=?", new String[]{title}, null, null, null);
			if (cursor.moveToFirst()) {
				return true;
			} 
    	} catch (Exception mue) {
			Log.e(LOG_TAG,"",mue);
			errorId = errorId + 1;
		}
    	
    	if (cursor != null)
    		cursor.close();
    	
		return false;
	}
    
    public int getCountItemByType(String type,boolean local,boolean fav) {
    	//log.debug("getTotalItemDB");
        int totalItem = 0;
        Cursor cursor = null;
        String sql = "";
        Map<String, String> noDup = new HashMap<String, String>();
        try{
	        sql += "\n select i.title from feeds f,items i where f.feed_id = i.feed_id "+
			((local)?" \n and i.path_file <> ''":"")+((fav)?" and i.fav=1":"");
	        sql+=" \n and f.type ='"+type+"'";
			//log.debug("sql:"+sql);
			
	    	cursor = mDb.rawQuery(sql, null);
	    	
	        cursor.moveToFirst();
	        while (!cursor.isAfterLast()) {
			   String title = cursor.getString(cursor.getColumnIndex("title"));
			   if(noDup.get(title) == null){
			     totalItem++;
			     noDup.put(title,title);
			   }
			   cursor.moveToNext();
	        }
	        
			if (cursor != null)
				cursor.close();
        }catch(Exception e){
        	e.printStackTrace();
        }
		return totalItem;
    }
    
    public Item getFirstItem(long feedId) {
        Cursor cursor = mDb.query(DBSchema.ItemSchema.TABLE_NAME, null, DBSchema.ItemSchema.COLUMN_FEED_ID + "=?",new String[]{Long.toString(feedId)}, null, null, DBSchema.ItemSchema._ID + DBSchema.SORT_ASC, "1");
        boolean hasItem = cursor.moveToFirst();
        Item firstItem = null;
        if (hasItem)
        	firstItem = getItem(cursor.getLong(cursor.getColumnIndex(DBSchema.ItemSchema._ID)));
        
        if (cursor != null)
        	cursor.close();
        
		return firstItem;
    }
    
    public Item getLastItem(long feedId) {
        Cursor cursor = mDb.query(DBSchema.ItemSchema.TABLE_NAME, null, DBSchema.ItemSchema.COLUMN_FEED_ID + "=?",new String[]{Long.toString(feedId)}, null, null, DBSchema.ItemSchema._ID + DBSchema.SORT_DESC, "1");
        boolean hasItem = cursor.moveToFirst();
        Item lastItem = null;
        if (hasItem)
        	lastItem = getItem(cursor.getLong(cursor.getColumnIndex(DBSchema.ItemSchema._ID)));
        
        if (cursor != null)
        	cursor.close();
        
		return lastItem;
    }
    
    public long getNextItemId(long feedId, long currentItemId) {
    	long itemId = -1;
    	boolean isCurrentItem = false;
    	boolean nextItemFound = false;
    	long nextItemId = -1;
    	Cursor cursor = null;
    	
    	try {
    		cursor = mDb.query(DBSchema.ItemSchema.TABLE_NAME, null, DBSchema.ItemSchema.COLUMN_FEED_ID + "=?",new String[]{Long.toString(feedId)}, null, null, DBSchema.ItemSchema._ID + DBSchema.SORT_ASC);
			if (cursor.moveToFirst()) {
				while (!cursor.isAfterLast() && !nextItemFound) {
					itemId = cursor.getLong(cursor.getColumnIndex(DBSchema.ItemSchema._ID));
					
					if (isCurrentItem) {
						nextItemId = itemId;
						nextItemFound = true;
					}

					isCurrentItem = itemId == currentItemId;

					cursor.moveToNext();
				}
			} else {
				throw new FeedException("Feed id " + feedId + " not found in the database.");
			}
		} catch (FeedException fe) {
			Log.e(LOG_TAG,"",fe);
			errorId = errorId + 1;
    	}
    	
    	if (cursor != null)
    		cursor.close();

    	return nextItemId;
    }
    
    public long getPreviousItemId(long feedId, long currentItemId) {
    	long itemId = -1;
    	boolean isCurrentItem = false;
    	boolean previousItemFound = false;
    	long previousItemId = -1;
    	Cursor cursor = null;
    	
    	try {
    		cursor = mDb.query(DBSchema.ItemSchema.TABLE_NAME, null, DBSchema.ItemSchema.COLUMN_FEED_ID + "=?",new String[]{Long.toString(feedId)}, null, null, DBSchema.ItemSchema._ID + DBSchema.SORT_ASC);
			if (cursor.moveToLast()) {
				while (!cursor.isBeforeFirst() && !previousItemFound) {
					itemId = cursor.getLong(cursor.getColumnIndex(DBSchema.ItemSchema._ID));
					
					if (isCurrentItem) {
						previousItemId = itemId;
						previousItemFound = true;
					}

					isCurrentItem = itemId == currentItemId;

					cursor.moveToPrevious();
				}
			} else {
				throw new FeedException("Feed id " + feedId + " not found in the database.");
			}
		} catch (FeedException fe) {
			Log.e(LOG_TAG,"",fe);
			errorId = errorId + 1;
    		//TrackerAnalyticsHelper.trackError(mCtx, Long.toString(errorId), fe.getMessage(), LOG_TAG);
    	}
    	
    	if (cursor != null)
    		cursor.close();

    	return previousItemId;
    }

    public int getTotalItemDB(long feedId) {
    	//log.debug("getTotalItemDB");
        int totalItem = 0;
    	Cursor cursor = mDb.rawQuery("select count(*) as total_items from items where feed_id =?", new String[]{Long.valueOf(feedId).toString()});
    		
        cursor.moveToFirst();
		totalItem = cursor.getInt(cursor.getColumnIndex("total_items"));
		//log.debug("totalItem:"+totalItem);
		
		if (cursor != null)
			cursor.close();
		
		return totalItem;
    }
   
    public List<Item> getItems(int maxItems,String flagType,String dateSort ,Feed feed,String feedType,String author) {
    	if(feed == null || (feed!= null && feed.getId() ==0))
    	   feed = getFeed(feedId,false,feedType);
    	return getItemsDB( maxItems, flagType, dateSort, feed,feedType,author,-1);   
    }
    
    public List<Item> getItems(int maxItems,String flagType,String dateSort ,Feed feed,String feedType,int topicCurPage) {
    	if(feed == null || (feed!= null && feed.getId() ==0))
    	   feed = getFeed(feedId,false,feedType);
    	return getItemsDB( maxItems, flagType, dateSort, feed,feedType,null,topicCurPage);   
    }

	public List<Item> getItemsDBByTitle(int feedId,String title){
		List<Item> items = new ArrayList<Item>();
		String sql = "";
		Cursor cursor = null;
		try{
			sql = "select i.*  from items i where i.feed_id= "+feedId+" and i.title like '%"+title+"%' order by i.title asc ";
			log.debug("sql:"+sql);

			cursor = mDb.rawQuery(sql,null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast() ) {
				Item item = getItem(cursor.getLong(cursor.getColumnIndex(DBSchema.ItemSchema._ID)));

				items.add(item);
				cursor.moveToNext();

			}

			if (cursor != null)
				cursor.close();
		}catch(Exception e){
			log.debug(e.getMessage());
		}
        return items;
    }

    public List<Item> getItemsDB(
    		int maxItems,String flagType,
            String dateSort ,Feed feed,
    		String feedType,String author,
    	    int topicCurPage) {
    	
    	List<Item> items = new ArrayList<Item>();
    	String sql = "";
    	try{
	    	String sortStr = DBSchema.ItemSchema.COLUMN_UPDATE_DATE + " "+Constants.DB_SORT_DESC;
	    	if(Constants.DB_SORT_ASC.equals(dateSort)){
	    		sortStr = DBSchema.ItemSchema.COLUMN_UPDATE_DATE + " "+Constants.DB_SORT_ASC;
	    	}else if(Constants.DB_SORT_DESC.equals(dateSort)){
	    		sortStr = DBSchema.ItemSchema.COLUMN_UPDATE_DATE + " "+Constants.DB_SORT_DESC;
	    	}
	    	
	    	/** debug ***********/
    		/*log.debug("maxItems["+maxItems+"]");
    		log.debug("flagType["+flagType+"]");
    		log.debug("dateSort["+dateSort+"]");
    		log.debug("feed["+feed+"]");
    		log.debug("feedType["+feedType+"]");
    		log.debug("author["+author+"]");
    		log.debug("sortStr["+sortStr+"]");*/
    		/********************/
    		
	    	String maxItemStr = String.valueOf(Constants.MAX_ITEMS);
	    	Cursor cursor = null;
	    	
	    	if("NEW".equals(flagType)){
	    		Calendar startDate = Calendar.getInstance();
	    		Calendar endDate = Calendar.getInstance();
	    		startDate.set(Calendar.DATE, -60);
	    	    
	    		log.debug("starDate:"+startDate);
	    		log.debug("endDate:"+endDate);

	    		cursor = mDb.rawQuery("select i.* ,,f.author as author_main from feeds f inner join items i on f.feed_id = i.feed_id where f.create_date >= ? and f.create_date <= ?",
	    				 new String[]{Long.toString(startDate.getTimeInMillis()),Long.toString(endDate.getTimeInMillis())});
	    		
	    	}else if("LOCAL".equals(flagType)){
	    		 log.debug("Local feedId["+feedId+"] and path_file is not null ");
		    	    sql =  " select i.* ,f.author as author_main from feeds f inner join items i on f.feed_id = i.feed_id ";
		    	    sql += " where (i.path_file is not null or i.path_file <> '')" ;
		    	    sql += " and f.feed_id = "+feed.getId();
		    	    if( !Utils.isNull(author).equals("")){
		   	    		sql += " and f.author ='"+author+"' \n";
		   	    	 }
		    	    sql += " order by i.create_date desc";
		    	    
			        cursor = mDb.rawQuery(sql,null);
	    		
	    	}else if("FAV".equals(flagType)){	
	    		sql = "select i.* ,f.author as author_main  from feeds f " +
	    				"inner join items i on f.feed_id = i.feed_id where i.fav <> 0 \n";
	    		if( !Utils.isNull(author).equals("")){
	    		   sql += " and f.author ='"+author+"' \n";
	    		}
	    	    sql += " and f.feed_id  ="+feed.getId()+" \n";
	    		sql +=" order by i.count_open desc LIMIT 20 \n";
		    	cursor = mDb.rawQuery(sql,null);
		    	
            }else if("FAV_BY_FEED_TYPE".equals(flagType)){	
	    		sql = "select i.* ,f.author as author_main from feeds f " +
	    				"inner join items i on f.feed_id = i.feed_id where f.type= '"+feedType+"' and i.fav <> 0 order by i.create_date desc ";
	    		
	    		cursor = mDb.rawQuery(sql,null);
	    		
            }else if("FAV_BY_FEED_ID".equals(flagType)){
            	if(feed.getIdSub() != 0){
            		sql = "select i.* ,f.author as author_main from feeds f " +
            			 "inner join items i on f.feed_id = i.feed_id " +
            			 "where f.feed_id in( "+feed.getId()+","+feed.getIdSub()+") and i.fav <> 0 order by i.create_date desc ";
            	}else{
	    		   sql = "select i.* ,f.author as author_main from feeds f " +
	    		   		"inner join items i on f.feed_id = i.feed_id " +
	    		   		"where f.feed_id= "+feed.getId()+" and i.fav <> 0 order by i.create_date desc ";
            	}
            	
	    		cursor = mDb.rawQuery(sql,null);
	    		
            }else if("FAV_BY_FEED_TYPE_NO_DUP".equals(flagType)){	
	    		sql = "select i.* ,f.author as author_main from feeds f " +
	    				"inner join items i on f.feed_id = i.feed_id where f.type= '"+feedType+"' and i.fav <> 0 order by i.create_date desc ";
	    		
	    		cursor = mDb.rawQuery(sql,null);	
            }else if("KEY_SEARCH".equals(flagType)){	
		    	sql = "select i.* ,f.author as author_main from feeds f " +
		    			"inner join items i on f.feed_id = i.feed_id where 1=1 and i.title like '%"+feed.getTitle()+"%'  ";
		    	sql += " and f.feed_id  ="+feed.getId()+" \n";
		    	sql += "order by i.create_date desc \n ";
		    	
		    	cursor = mDb.rawQuery(sql,null);	
		    		
            }else if("LOCAL_BY_FEED_TYPE".equals(flagType)){	
	    		sql = "select i.* ,f.author as author_main from feeds f " +
	    				"inner join items i on f.feed_id = i.feed_id where f.type= '"+feedType+"' and i.path_file is not null order by i.create_date desc ";
	    		cursor = mDb.rawQuery(sql,null);
	    		
	    	}else if("ALL".equals(flagType)){	
		        cursor = mDb.query(DBSchema.ItemSchema.TABLE_NAME, null, null,null, null, null, sortStr, maxItemStr);
		        
	    	}else if("ID_SORT_BY_TITLE".equals(flagType)){
	    		sql = "select i.* from items i where i.feed_id ="+feed.getId()+" \n";
	    		if( topicCurPage != -1){
	    		   sql += " and i.topic_cur_page ="+topicCurPage+" \n";
	    		}
	    		sql +=" order by i.title asc \n";
		    	cursor = mDb.rawQuery(sql,null);
		    	
	    	}else if("ALL_BY_ID_AUTHOR".equals(flagType)){
	    		sql = " select i.* , f.author as author_main  " +
	    			  " from feeds f ,items i where 1=1 \n"+
	    			  " and f.feed_id = i.feed_id \n";
	    		if( !Utils.isNull(author).equals("")){
	    		   sql += " and f.author ='"+author+"' \n";
	    		}
	    		if( feed != null && feed.getId() != 0){
		    	   sql += " and i.feed_id  ="+feed.getId()+" \n";
		    	}
	    		sql +=" order by i.create_date desc \n";
		    	cursor = mDb.rawQuery(sql,null);
		    	
	    	}else if("ID".equals(flagType)){
	    		sql = "select i.* ,f.author as author_main from feeds f inner join items i on f.feed_id = i.feed_id where i.feed_id ="+feed.getId()+" \n";
	    		if( topicCurPage != -1){
	    		   sql += " and i.topic_cur_page ="+topicCurPage+" \n";
	    		}
	    		sql +=" order by i.update_date desc \n";
		    	cursor = mDb.rawQuery(sql,null);	
		    	
	    	}else if("TOPTEN".equals(flagType)){
		    	cursor = mDb.rawQuery("select i.* ,f.author as author_main from feeds f inner join items i on f.feed_id = i.feed_id where f.type = ? order by i.count_open desc",
		    		     new String[]{feedType});
	    	}else{
	    	    //cursor = mDb.query(DBSchema.ItemSchema.TABLE_NAME, null, DBSchema.ItemSchema.COLUMN_FEED_ID + "=?",
			             //new String[]{Long.toString(feed.getId())}, null, null, sortStr, maxItemStr);
	    	    
	    	    cursor = mDb.rawQuery("select i.* ,f.author as author_main from feeds f inner join items i on f.feed_id = i.feed_id where f.feed_id = ? order by i.create_date desc ",
		    		     new String[]{Long.toString(feed.getId())});
	    	}
	    	
	    	log.debug("sql:"+sql);
	    	
	    	int maxRows = 99999;
	    	int row = 0;
	    	if("FAV_BY_FEED_TYPE".equals(flagType)){
	    	   maxRows = 20;
	    	}
	        cursor.moveToFirst();
	       
	        if("FAV_BY_FEED_TYPE_NO_DUP".equalsIgnoreCase(flagType)){
	        	Map<String, String> titleNoDupMap = new HashMap<String, String>();
	        	while (!cursor.isAfterLast() && row <= maxRows) {
					Item item = getItem(cursor.getLong(cursor.getColumnIndex(DBSchema.ItemSchema._ID)));
					item.setFeedId(cursor.getInt(cursor.getColumnIndex("feed_id")));
	
					if("".equals(Utils.isNull(item.getAuthor()))){
					  item.setAuthor(cursor.getString(cursor.getColumnIndex("author_main")));
					}
					
					if( !"".equals(feedType)){
					  item.setFeedType(feedType);	
					}
					if(titleNoDupMap.get(item.getTitle()) ==null){
						//log.debug("item id["+item.getId()+"]title["+item.getTitle()+"],fav["+item.isFav()+"]");
						
					   items.add(item);
					   titleNoDupMap.put(item.getTitle(), item.getTitle());
					}
					cursor.moveToNext();
				}//while
	        	
	        	titleNoDupMap.clear();
	        	titleNoDupMap = null;
	        }else{
				while (!cursor.isAfterLast() && row <= maxRows) {
					Item item = getItem(cursor.getLong(cursor.getColumnIndex(DBSchema.ItemSchema._ID)));
	
					if("FAV_BY_FEED_TYPE".equals(flagType)){
		              row++;
					}
					item.setFeedId(cursor.getInt(cursor.getColumnIndex("feed_id")));
	
					//log.debug("author_main:"+cursor.getString(cursor.getColumnIndex("author_main")));
					//log.debug("author_item:"+cursor.getString(cursor.getColumnIndex("author")));
					
					if("".equals(Utils.isNull(item.getAuthor()))){
					  item.setAuthor(cursor.getString(cursor.getColumnIndex("author_main")));
					}
					
					if( !"".equals(feedType)){
					  item.setFeedType(feedType);	
					}
					
					//log.debug("Select Item Title["+item.getTitle()+"]createDate["+item.getCreateDate()+"]");
					
					items.add(item);
					
					cursor.moveToNext();
				}//while
	        }//if
			if (cursor != null)
				cursor.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
		return items;
    }
    
    private ContentValues getContentValues(long feedId, Item item) {
    	ContentValues values = new ContentValues();
        values.put(DBSchema.ItemSchema.COLUMN_FEED_ID, feedId);
        values.put(DBSchema.ItemSchema.COLUMN_LINK, item.getLink());
        values.put(DBSchema.ItemSchema.COLUMN_ORG_LINK, item.getOrgLink());
        values.put(DBSchema.ItemSchema.COLUMN_TITLE, item.getTitle());
        
        if (item.getDescription() == null){
        	values.putNull(DBSchema.ItemSchema.COLUMN_DESCRIPTION);
        }else{
        	values.put(DBSchema.ItemSchema.COLUMN_DESCRIPTION, item.getDescription());
        }
        if (item.getContent() == null){
        	values.putNull(DBSchema.ItemSchema.COLUMN_CONTENT);
        }else{
        	values.put(DBSchema.ItemSchema.COLUMN_CONTENT, item.getContent());
        }
        
    	if (item.getImage() == null){
    		values.putNull(DBSchema.ItemSchema.COLUMN_IMAGE);
    	}else{
    		values.put(DBSchema.ItemSchema.COLUMN_IMAGE, item.getImage().toString()); 
    	}
    	if (item.getCreateDate() != null){
    		values.put(DBSchema.ItemSchema.COLUMN_CREATE_DATE, item.getCreateDate().getTime()); 
    	}
    	
    	if (item.getUpdateDate() != null){
    		values.put(DBSchema.ItemSchema.COLUMN_UPDATE_DATE, item.getUpdateDate().getTime()); 
    	}
    	
    	int state = DBSchema.ON;
    	if (!item.isRead()){
    		state = DBSchema.OFF;
    	}else{
    		state = DBSchema.ON;
    	}
    	values.put(DBSchema.ItemSchema.COLUMN_READ, state);
    	
    	state = DBSchema.ON;
    	if (!item.isFav()){
    		state = DBSchema.OFF;
    	}else{
    		state = DBSchema.ON;
    	}
    	
    	values.put(DBSchema.ItemSchema.COLUMN_FAV, state);
    	
    	values.put(DBSchema.ItemSchema.COLUMN_TYPE, item.getType());
    	values.put(DBSchema.ItemSchema.COLUMN_COUNT_OPEN, item.getCountOpen());

    	if (item.getPathFile() == null)
    		values.putNull(DBSchema.ItemSchema.COLUMN_PATH_FILE);
    	else
    		values.put(DBSchema.ItemSchema.COLUMN_PATH_FILE, item.getPathFile()); 
    	
    	values.put(DBSchema.ItemSchema.COLUMN_AUTHOR, item.getAuthor());
    	values.put(DBSchema.ItemSchema.COLUMN_TOTAL_REPLY, item.getTotalReply());
    	values.put(DBSchema.ItemSchema.COLUMN_TOTAL_READ, item.getTotalRead());
    	values.put(DBSchema.ItemSchema.COLUMN_TOPIC_CUR_PAGE, item.getTopicCurPage());
    
    	
    	return values;
    }
    
    private ContentValues getContentValuesUpdateTopicCurPage(long feedId, Item item) {
    	ContentValues values = new ContentValues();
        values.put(DBSchema.ItemSchema.COLUMN_FEED_ID, feedId);
    	values.put(DBSchema.ItemSchema.COLUMN_TOPIC_CUR_PAGE, item.getTopicCurPage());
    	values.put(DBSchema.ItemSchema.COLUMN_TOTAL_REPLY, item.getTotalReply());
    	values.put(DBSchema.ItemSchema.COLUMN_TOTAL_READ, item.getTotalRead());
    	
    	if (item.getCreateDate() != null){
    		values.put(DBSchema.ItemSchema.COLUMN_CREATE_DATE, item.getCreateDate().getTime()); 
    	}
    	
    	if (item.getUpdateDate() != null){
    		values.put(DBSchema.ItemSchema.COLUMN_UPDATE_DATE, item.getUpdateDate().getTime()); 
    	}
    	
    	return values;
    }
    
    public long addItem(long feedId, Item item) {
    	return addItem(feedId,getContentValues(feedId, item));
    }
    
    public long addItem(long feedId, ContentValues values) {
    	long itemId = mDb.insert(DBSchema.ItemSchema.TABLE_NAME, null, values);
    	if (itemId == -1)
    		Log.e(LOG_TAG,"Item '" + values.getAsString(DBSchema.ItemSchema.COLUMN_TITLE) + "' for Feed id " + values.getAsLong(DBSchema.ItemSchema.COLUMN_FEED_ID) + " could not be inserted into the database. Item values: " + values.toString());

    	return itemId;
    }
    
    //Update By item_id
    public boolean updateItem(long feedId, Item item) {
    	return updateItem(item.getId(), getContentValues(feedId, item));
    }
    //Update By item_id
    public boolean updateItem(long id, ContentValues values) {
    	try{
    	   boolean itemUpdated = (mDb.update(DBSchema.ItemSchema.TABLE_NAME, values, DBSchema.ItemSchema._ID + "=?", new String[]{Long.toString(id)}) > 0);
    	   return itemUpdated;
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return false;
    }

	public void updateByScriptSql(String scriptSql) {
		try{
			//mDb.update(DBSchema.ItemSchema.TABLE_NAME, null, , null);
			mDb.execSQL(scriptSql);

		}catch(Exception e){
			log.debug(e.getMessage());
		}
	}

    //Update By item_title
    public boolean updateItemByTitleAndFeedId(long feedId,Item item) {
    	return updateItemByTitle(feedId,item.getTitle(), getContentValuesUpdateTopicCurPage(feedId, item));
    }
    //Update By item_title
    public boolean updateItemByTitle(long feedId ,String title, ContentValues values) {
    	boolean itemUpdated = (mDb.update(DBSchema.ItemSchema.TABLE_NAME, values, DBSchema.ItemSchema.COLUMN_FEED_ID +"=? and "+DBSchema.ItemSchema.COLUMN_TITLE + "=?", new String[]{Long.toString(feedId),title}) > 0);
    	return itemUpdated;
    }
  //Update By item_title
    public boolean updateItemByTitle(String title, ContentValues values) {
    	boolean itemUpdated = (mDb.update(DBSchema.ItemSchema.TABLE_NAME, values, DBSchema.ItemSchema.COLUMN_TITLE + "=?", new String[]{title}) > 0);
    	return itemUpdated;
    }
    public boolean removeItems(long feedId,String feedType) {
    	Feed feed  = new Feed();
    	feed.setId(feedId);
    	List<Item> items = getItems( -1,"Default","DESC",feed,feedType,-1);
    	return removeItems(items);
    	//return (mDb.delete(DbSchema.ItemSchema.TABLE_NAME, DbSchema.ItemSchema.COLUMN_FEED_ID + "=?", new String[]{Long.toString(feedId)}) > 0);
    }
    
    public boolean removeItems(List<Item> items) {
    	boolean allItemRemoved = true;
    	Item item;
    	Iterator<Item> iterator = items.iterator();
    	while (iterator.hasNext()) {
    		item = iterator.next();
    		if (!removeItem(item.getId()))
    			allItemRemoved = false;
    	}
    	return allItemRemoved;
    }
    
    public boolean removeItem(long id) {
    	return (mDb.delete(DBSchema.ItemSchema.TABLE_NAME, DBSchema.ItemSchema._ID + "=?", new String[]{Long.toString(id)}) > 0);
    }
    
}