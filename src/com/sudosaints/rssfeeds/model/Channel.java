package com.sudosaints.rssfeeds.model;

public class Channel {
	long channelID;
	String channelName;
	
	public Channel(String channelName) {
		super();
		this.channelName = channelName;
	}

	public Channel(long channelID, String channelName) {
		super();
		this.channelID = channelID;
		this.channelName = channelName;
	}

	/*int getChannelId(String chName){
		int chNo = 0;
		return chNo;
	}
	
	String getChannelName(int id){
		String name = "";
		return name;
	}*/
	
	public void setId(long id){
		this.channelID = id;
	}
	
	public void setName(String name){
		this.channelName = name;
	}
	
	public Long getId(){
		return this.channelID;
	}
	
	public String getName(){
		return this.channelName;
	}
	
	@Override
	public boolean equals(Object o) {
		Channel object = (Channel) o;
		if(this.channelName.equals(object.channelName)) {
			return true;
		}
		return false;
	}
	
}
