package com.sudosaints.rssfeeds.db;

import android.content.Context;

public class DbUtils {

	Context context;

	DbHelper dbHelper;
	
	public DbUtils(Context context) {
		super();
		this.context = context;
	}
	
	public DbHelper getDbHelper() {

		if(dbHelper != null && dbHelper.isDbOpened()) {
			return dbHelper;
		}
		dbHelper = new DbHelper(context);
		dbHelper.open();
		return dbHelper;
	}
	
}
