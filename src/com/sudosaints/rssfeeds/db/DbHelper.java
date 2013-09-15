package com.sudosaints.rssfeeds.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.sudosaints.rssfeeds.model.Channel;
import com.sudosaints.rssfeeds.model.WebSite;
import com.sudosaints.rssfeeds.utils.Logger;

/**
 * Should not be used directly by Activity Classes
 * <br>Use {@link DbUtils} for getting object of this class 
 * @author Vishal
 *
 */
public class DbHelper {

	private SQLiteDatabase database;
	private DatabaseHelper databaseHelper;
	private Context context;
	private Logger logger;
	
	public static final String DATABASE_NAME = "rssFeedDb";
	
	/***** Channel Table Fields *****/
	public static final String KEY_CHANNEL_ID = "channelId";
	public static final String KEY_CHANNEL_TITLE = "channelTitle";
	public static final String KEY_CHANNEL_TABLE = "channel";
	/***** End *****/
	
	/***** Site Table Fields *****/
	public static final String KEY_SITE_ID = "siteId";
	public static final String KEY_SITE_URL = "siteUrl";
	public static final String KEY_SITE_RSSURL = "siteRssUrl";
	public static final String KEY_SITE_TITLE = "siteTitle";
	public static final String KEY_SITE_TABLE = "site";
	/***** End *****/
	
	/***** Site-Channel Relationship Table Fields *****/
	public static final String KEY_CS_CHANNEL_ID ="channelId";
	public static final String KEY_CS_SITE_ID = "siteId";
	public static final String KEY_CS_TABLE = "channel_site";
	/***** End *****/
	
	public static final String[] channelColumnList = new String[] {KEY_CHANNEL_ID, KEY_CHANNEL_TITLE};
	public static final String[] siteColumnList = new String[] {KEY_SITE_ID, KEY_SITE_URL, KEY_SITE_RSSURL, KEY_SITE_TITLE};
	public static final String[] channelSiteColumnList = new String[] {KEY_CS_CHANNEL_ID, KEY_CS_SITE_ID};
	
	private static final String CREATE_TABLE_CHANNEL = "create table " + KEY_CHANNEL_TABLE + " (" + KEY_CHANNEL_ID + " integer primary key, " + KEY_CHANNEL_TITLE + " text not null);";
	private static final String CREATE_TABLE_SITE = "create table " + KEY_SITE_TABLE + " (" + KEY_SITE_ID + " integer primary key, " + KEY_SITE_URL + " text not null, " + KEY_SITE_RSSURL + " text not null, " + KEY_SITE_TITLE + " text not null);";
	private static final String CREATE_TABLE_CHANNEL_SITE = "create table " + KEY_CS_TABLE + "(" + KEY_CS_CHANNEL_ID + " integer not null, " + KEY_CS_SITE_ID + " integer not null, foreign key (" + KEY_CS_CHANNEL_ID + ") references " + KEY_CHANNEL_TABLE + " (" + KEY_CHANNEL_ID + "), foreign key (" + KEY_CS_SITE_ID + ") references " + KEY_SITE_TABLE + "(" + KEY_SITE_ID + "));";
	
	private class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			logger.debug("Creating Tables");
			db.execSQL(CREATE_TABLE_CHANNEL);
			db.execSQL(CREATE_TABLE_SITE);
			db.execSQL(CREATE_TABLE_CHANNEL_SITE);
			//addPreDefinedData(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			database.execSQL("drop table if exists " + KEY_CHANNEL_TABLE);
			database.execSQL("drop table if exists " + KEY_SITE_TABLE);
			database.execSQL("drop table if exists " + KEY_CS_TABLE);
			onCreate(db);
		}
	}
	
	private void addPreDefinedData(SQLiteDatabase db) {
		String [] preDefinedChannels = new String [] {"Science", "Technology", "Engineering"};
		
		for (int i = 0; i < preDefinedChannels.length; i++) {
			ContentValues contentValues = new ContentValues();
			contentValues.put(KEY_CHANNEL_TITLE, preDefinedChannels[i]);
			db.insert(KEY_CHANNEL_TABLE, null, contentValues);
		}
		
		List<WebSite> webSites = new ArrayList<WebSite>();
		webSites.add(new WebSite(0, "http://www.techcrunch.com", "", "TechCrunch", 1));
		
		for (WebSite webSite : webSites) {
			ContentValues contentValues = new ContentValues();
			contentValues.put(KEY_SITE_URL, webSite.getLink());
			contentValues.put(KEY_SITE_RSSURL, webSite.getRSSLink());
			contentValues.put(KEY_SITE_TITLE, webSite.getTitle());
			long rowId = db.insert(KEY_SITE_TABLE, null, contentValues);
			
			ContentValues relContentValues = new ContentValues();
			relContentValues.put(KEY_CS_CHANNEL_ID, webSite.get_channelId());
			relContentValues.put(KEY_CS_SITE_ID, rowId);
			db.insert(KEY_CS_TABLE, null, relContentValues);
		}
	}
	
	public DbHelper(Context context) {
		this.context = context;
		logger = new Logger(context);
	}
	
	public DbHelper open() throws SQLException {
		databaseHelper = new DatabaseHelper(context, DATABASE_NAME, null, 1);
		database = databaseHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		databaseHelper.close();
		if(database != null && database.isOpen()) {
			logger.debug("Closing database");
			database.close();
		}
	}
	
	public boolean isDbOpened() {
		if(database != null && database.isOpen()) {
			return true;
		}
		return false;
	}
	
	/***** Channel Specific Methods *****/
	
	public long addChannel(String channelTitle) {
		
		Cursor cursor = getChannel(channelTitle);
		
		if(cursor == null || cursor.getCount() == 0) {
			
			ContentValues contentValues = new ContentValues();
			contentValues.put(KEY_CHANNEL_TITLE, channelTitle);
			if(cursor != null) {
				cursor.close();
			}
			return database.insert(KEY_CHANNEL_TABLE, null, contentValues);
		} else {
			
			long rowId = cursor.getLong(cursor.getColumnIndex(KEY_CHANNEL_ID));
			Channel channel = new Channel(channelTitle);
			cursor.close();
			return updateChannel(rowId, channel) ? rowId : -1; 
		}
	}
	
	public Cursor getChannel(long rowId) {
		
		Cursor cursor = database.query(true, KEY_CHANNEL_TABLE, channelColumnList, KEY_CHANNEL_ID + " = " + rowId, null, null, null, null, null);
		if(cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public Channel getChannelObj(long rowId) {
		
		Cursor cursor = getChannel(rowId);
		Channel channel = new Channel(cursor.getLong(cursor.getColumnIndex(KEY_CHANNEL_ID)), cursor.getString(cursor.getColumnIndex(KEY_CHANNEL_TITLE)));
		cursor.close();
		return channel;
	}
	
	public Cursor getChannel(String channelTitle) {
		
		Cursor cursor = database.query(true, KEY_CHANNEL_TABLE, channelColumnList, KEY_CHANNEL_TITLE + " = '" + channelTitle + "'", null, null, null, null, null);
		if(cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public boolean updateChannel(long rowId, Channel channel) {
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_CHANNEL_ID, channel.getId());
		contentValues.put(KEY_CHANNEL_TITLE, channel.getName());
		return database.update(KEY_CHANNEL_TABLE, contentValues, KEY_CHANNEL_ID + " = " + rowId, null) > 0;
	}
	
	public boolean deleteChannel(long rowId) {
		
		boolean result1 = database.delete(KEY_CHANNEL_TABLE, KEY_CHANNEL_ID + " = " + rowId, null)>0;
		if(result1) {
			deleteChannelFromRelation(rowId);
			return true;
		}
		return false;
	}
	
	public List<Channel> getAllChannels() {
		
		List<Channel> channelList = new ArrayList<Channel>();
		Cursor cursor = database.query(KEY_CHANNEL_TABLE, channelColumnList, null, null, null, null, null);
		if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
			do {
				Channel channel = new Channel(cursor.getLong(cursor.getColumnIndex(KEY_CHANNEL_ID)), cursor.getString(cursor.getColumnIndex(KEY_CHANNEL_TITLE)));
				channelList.add(channel);
			} while (cursor.moveToNext());
			cursor.close();
		}
		return channelList;
	}
	
	/***** End Channel Specific Methods *****/
	
	/***** Site Specific Methods *****/
	
	public long addSite(String siteUrl, String siteRssUrl, String siteTitle, Long channelId) {
		
		Cursor cursor = getSiteByUrl(siteUrl);
		
		if(cursor == null || cursor.getCount() == 0) {

			logger.debug("Did not get anything");
			ContentValues contentValues = new ContentValues();
			contentValues.put(KEY_SITE_URL, siteUrl);
			contentValues.put(KEY_SITE_RSSURL, siteRssUrl);
			contentValues.put(KEY_SITE_TITLE, siteTitle);
			if(cursor != null) {
				cursor.close();
			}
			long siteId = database.insert(KEY_SITE_TABLE, null, contentValues);
			if(siteId != -1) {
				return addChannelSiteEntry(channelId, siteId);
			}
			return -1;
		} else {
			
			
			long siteId = cursor.getLong(cursor.getColumnIndex(KEY_SITE_ID));
			cursor.close();
			Cursor cursor2 = getChannelSiteEntry(channelId, siteId);
			if(cursor2 == null || cursor2.getCount() == 0) {
				addChannelSiteEntry(channelId, siteId);
				if(cursor2 != null) {
					cursor2.close();
				}
			} else {
				cursor2.close();
			}
			logger.debug("Existing Site Id - " + siteId);
			
			WebSite site = new WebSite(siteId, siteUrl, siteRssUrl, siteTitle, channelId);
			return updateSite(site) ? siteId : -1; 
		}
	}
	
	private Cursor getSite(long siteId) {
		
		Cursor cursor = database.query(true, KEY_SITE_TABLE, siteColumnList, KEY_SITE_ID + " = " + siteId, null, null, null, null, null);
		if(cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	private Cursor getSiteByUrl(String siteUrl) {
		
		logger.debug("Trying to get Site with url - '" + siteUrl + "'");
		Cursor cursor = database.query(true, KEY_SITE_TABLE, siteColumnList, KEY_SITE_URL + " = '" + siteUrl + "'", null, null, null, null, null);
		if(cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	/**
	 * <b>The channelId in returned object will always be 0</b>
	 * @param siteId
	 * @return
	 */
	public WebSite getSiteObj(long siteId) {
		
		Cursor cursor = getSite(siteId);
		WebSite website = new WebSite(cursor.getLong(cursor.getColumnIndex(KEY_SITE_ID)), cursor.getString(cursor.getColumnIndex(KEY_SITE_URL)), cursor.getString(cursor.getColumnIndex(KEY_SITE_RSSURL)), cursor.getString(cursor.getColumnIndex(KEY_SITE_TITLE)), 0);
		cursor.close();
		return website;
	}
	
	public boolean updateSite(WebSite website) {
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_SITE_URL, website.getLink());
		contentValues.put(KEY_SITE_RSSURL, website.getRSSLink());
		contentValues.put(KEY_SITE_TITLE, website.getTitle());
		return database.update(KEY_SITE_TABLE, contentValues, KEY_SITE_ID + " = " + website.getId(), null) > 0;
	}
	
	public boolean deleteSite(long siteId) {
		
		boolean result1 = database.delete(KEY_SITE_TABLE, KEY_SITE_ID + " = " + siteId, null)>0;
		if(result1 && deleteSiteFromRelation(siteId)) {
			return true;
		}
		return false;
	}
	
	/***** End Site Specific Methods *****/
	
	/***** Channel-Site Table Specific Methods *****/
	
	private long addChannelSiteEntry(Long channelId, Long siteId) {
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_CS_CHANNEL_ID, channelId);
		contentValues.put(KEY_CS_SITE_ID, siteId);
		return database.insert(KEY_CS_TABLE, null, contentValues);
	}
	
	public List<WebSite> getSitesByChannel(Long channelId) {
		
		List<WebSite> sites = new ArrayList<WebSite>();
		logger.debug("Requested Channel Id - " + channelId);
		Cursor cursor = getSitesForChannel(channelId);
		if(cursor != null && cursor.moveToFirst()) {
			do {
				WebSite site = getSiteObj(cursor.getLong(cursor.getColumnIndex(KEY_CS_SITE_ID)));
				sites.add(site);
			} while (cursor.moveToNext());
		}
		if(cursor != null) {
			cursor.close();
		}
		return sites;
	}

	private Cursor getSitesForChannel(Long channelId) {
		
		Cursor cursor = database.query(true, KEY_CS_TABLE, channelSiteColumnList, KEY_CS_CHANNEL_ID + " = " + channelId, null, null, null, null, null);
		if(cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	private Cursor getChannelSiteEntry(Long channelId, Long siteId) {
		
		Cursor cursor = database.query(true, KEY_CS_TABLE, channelSiteColumnList, KEY_CS_CHANNEL_ID + " = " + channelId + " and " + KEY_CS_SITE_ID + " = " + siteId, null, null, null, null, null);
		if(cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	private boolean deleteSiteFromRelation(long siteId) {
		
		return database.delete(KEY_CS_TABLE, KEY_CS_SITE_ID + " = " + siteId, null)>0;
	}
	
	private boolean deleteChannelFromRelation(long channelId) {
		return database.delete(KEY_CS_TABLE, KEY_CS_CHANNEL_ID + " = " + channelId, null)>0;
	}
	
	/***** End *****/
}
