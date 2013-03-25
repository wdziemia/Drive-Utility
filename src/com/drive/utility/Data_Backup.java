package com.drive.utility;

import java.util.ArrayList;

import com.drive.utility.database.Article;
import com.google.gson.annotations.SerializedName;

public class Data_Backup {
	
	public Data_Backup(ArrayList<Article> articles){
		this.articles = articles;
	}
	
	@SerializedName("articles")
	public ArrayList<Article> articles;

}
