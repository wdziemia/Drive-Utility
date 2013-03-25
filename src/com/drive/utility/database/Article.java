package com.drive.utility.database;

import com.google.gson.annotations.SerializedName;

public class Article {
	
	@SerializedName("id")
	private long id;
	
	@SerializedName("title")
	private String title;
	
	@SerializedName("desc")
	private String desc;
	
	
	public Article(String title, String desc ) {
		this.title = title;
		this.desc = desc;
	}
	
	public Article() { }
	
	public long getId() {
	    return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSumm() {
	    return desc;
	}

	public void setSumm(String desc) {
		this.desc = desc;
	}
	
	public String getTitle() {
	    return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
}
