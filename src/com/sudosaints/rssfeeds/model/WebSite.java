package com.sudosaints.rssfeeds.model;


/**
 * This class file used while inserting data or retrieving data from 
 * SQLite database
 * **/
public class WebSite {
	long _id;
	String _title;
	String _link;
	String _rss_link;
	long _ch_id;
	
	// constructor
	public WebSite(){
		
	}

	// constructor with parameters
	public WebSite(long id, String link, String rss_link, String title, long channelId){
		this._id = id;
		this._title = title;
		this._link = link;
		this._rss_link = rss_link;
		this._ch_id = channelId;
	}
	
	/**
	 * All set methods
	 * */
	public void setId(long id){
		this._id = id;
	}
	
	public void setTitle(String title){
		this._title = title;
	}
	
	public void setLink(String link){
		this._link = link;
	}
	
	public void setRSSLink(String rss_link){
		this._rss_link = rss_link;
	}
	
	public void setChannel(long channelId){
		this._ch_id = channelId;
	}
	
	/**
	 * All get methods
	 * */
	public long getId(){
		return this._id;
	}
	
	public String getTitle(){
		return this._title;
	}
	
	public String getLink(){
		return this._link;
	}
	
	public String getRSSLink(){
		return this._rss_link;
	}
	
	public long get_channelId() {
		return this._ch_id;
	}
}
