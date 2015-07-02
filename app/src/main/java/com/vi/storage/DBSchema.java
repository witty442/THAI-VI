
package com.vi.storage;

import android.provider.BaseColumns;

public final class DBSchema {
	
	public static final String DATABASE_NAME = "VITHAI_DB";
	public static final int DATABASE_VERSION = 3;
	public static final String SORT_ASC = " ASC";
	public static final String SORT_DESC = " DESC";
	public static final String[] ORDERS = {SORT_ASC,SORT_DESC};
	public static final int OFF = 0;
	public static final int ON = 1;

	public static final class FeedSchema implements BaseColumns {
		public static final String TABLE_NAME = "feeds";
		public static final String COLUMN_ID = "feed_id";
		public static final String COLUMN_URL = "url";
		public static final String COLUMN_TITLE = "title";
		public static final String COLUMN_TYPE = "type"; //article ,board
		public static final String COLUMN_CREATE_DATE = "create_date";
		public static final String COLUMN_UPDATE_DATE = "update_date";
		public static final String COLUMN_ENABLE = "enable";
		public static final String COLUMN_AUTHOR = "author";//Author Article ,board(THaivi)
		public static final String COLUMN_ATTR1 = "attr_1";//for edit after GoLive
		public static final String COLUMN_ATTR2 = "attr_2";
		public static final String COLUMN_ATTR3 = "attr_3";
		public static final String COLUMN_ATTR4 = "attr_4";
		
		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + 
		                                           " (" + COLUMN_ID+ " INTEGER NOT NULL," +
		                                                  COLUMN_URL + " TEXT NOT NULL," + 
		                                                  COLUMN_TITLE + " TEXT NOT NULL," + 
		                                                  COLUMN_TYPE + " TEXT," + //Atrticle,WEBBOARD,STOCK_100
		                                                  COLUMN_CREATE_DATE + " INTEGER," + 
		                                                  COLUMN_UPDATE_DATE + " INTEGER," + 
		                                                  COLUMN_AUTHOR + " TEXT," + 
		                                                  COLUMN_ATTR1 + " TEXT," +
		                                                  COLUMN_ATTR2 + " TEXT," +
		                                                  COLUMN_ATTR3 + " TEXT," +
		                                                  COLUMN_ATTR4 + " TEXT," +
		                                                  COLUMN_ENABLE + " INTEGER NOT NULL);";
		public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}
	
	public static final class ItemSchema implements BaseColumns {
		public static final String TABLE_NAME = "items";
		public static final String COLUMN_FEED_ID = "feed_id";
		public static final String COLUMN_LINK = "link";
		public static final String COLUMN_ORG_LINK = "org_link";
		public static final String COLUMN_TITLE = "title";//Case 100 Stock Save Stock
		public static final String COLUMN_DESCRIPTION = "description"; // not used
		public static final String COLUMN_CONTENT = "content";
		public static final String COLUMN_IMAGE = "image";
		public static final String COLUMN_CREATE_DATE = "create_date";
		public static final String COLUMN_UPDATE_DATE = "update_date";
		public static final String COLUMN_PATH_FILE = "path_file";//Case Local file
		public static final String COLUMN_READ = "read";
		public static final String COLUMN_FAV = "fav";
		public static final String COLUMN_COUNT_OPEN = "count_open";//count open
		public static final String COLUMN_TYPE = "type";//Article,Video
		public static final String COLUMN_AUTHOR = "author";//Author Topic
		public static final String COLUMN_TOTAL_REPLY = "total_reply";//total_reply
		public static final String COLUMN_TOTAL_READ = "total_read";//total_read
		public static final String COLUMN_TOPIC_CUR_PAGE = "topic_cur_page";// topic cur page 
		public static final String COLUMN_CUR_PAGE = "cur_page";//cur page in show detail topic
		public static final String COLUMN_LAST_POSITION = "attr_1";//(ListIndex,lisyFromTop) (10,-744) //for edit after GoLive
		public static final String COLUMN_ATTR2 = "attr_2";
		public static final String COLUMN_ATTR3 = "attr_3";
		public static final String COLUMN_ATTR4 = "attr_4";
		
		public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
		                                         " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		                                                 COLUMN_FEED_ID + " INTEGER NOT NULL," + 
		                                                 COLUMN_LINK + " TEXT NOT NULL," + 
		                                                 COLUMN_ORG_LINK + " TEXT," + 
		                                                 COLUMN_TITLE + " TEXT NOT NULL," + 
		                                                 COLUMN_DESCRIPTION + " TEXT," +
		                                                 COLUMN_CONTENT + " TEXT," + 
		                                                 COLUMN_IMAGE + " TEXT," + 
		                                                 COLUMN_CREATE_DATE + " INTEGER," + 
		                                                 COLUMN_UPDATE_DATE + " INTEGER," + 
		                                                 COLUMN_READ + " INTEGER NOT NULL," + 
		                                                 COLUMN_FAV + " INTEGER NOT NULL," + 
		                                                 COLUMN_PATH_FILE + " TEXT," + 
		                                                 COLUMN_COUNT_OPEN + " INTEGER NOT NULL," + 
		                                                 COLUMN_TOTAL_REPLY + " INTEGER ," + 
		                                                 COLUMN_TOTAL_READ + " INTEGER ," + 
		                                                 COLUMN_AUTHOR + " TEXT," + 
		                                                 COLUMN_TYPE + " TEXT," +
		                                                 COLUMN_TOPIC_CUR_PAGE + " INTEGER ,"+
		                                                 COLUMN_LAST_POSITION + " TEXT," +
		                                                 COLUMN_ATTR2 + " TEXT," +
		                                                 COLUMN_ATTR3 + " TEXT," +
		                                                 COLUMN_ATTR4 + " TEXT," +
		                                                 COLUMN_CUR_PAGE + " INTEGER);";
		public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
	}
	
}
