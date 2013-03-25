package com.drive.utility.database;

import java.util.ArrayList;
import java.util.List;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class Database_DataSource_News {

	private SQLiteDatabase database;
	private Database_News dbHelper;
  
	private String[] allColumns = { 
			Database_News.COLUMN_ID,
			Database_News.COLUMN_TITLE,
			Database_News.COLUMN_SUMM,};

	public Database_DataSource_News(Context context) {
		dbHelper = new Database_News(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
	  	dbHelper.close();
	}

  
	public Article createArticle( String title, String summ ) {
		ContentValues values = new ContentValues();
	
		values.put( Database_News.COLUMN_TITLE, title);
		values.put( Database_News.COLUMN_SUMM, summ);
   
		long insertId = database.insert(Database_News.TABLE_NEWS, null, values);
    
		Cursor cursor = database.query(Database_News.TABLE_NEWS,
			  allColumns, Database_News.COLUMN_ID + " = " + insertId, 
			  null, null, null, null);
	  
		cursor.moveToFirst();
		Article article = cursorToArticle(cursor);
		cursor.close();
    
	  	return article;
	}
  
 
	public void deleteArticle(Article article) {
		database.delete(Database_News.TABLE_NEWS, Database_News.COLUMN_ID + " = " + article.getId(), null); // delete article from database with specifc ID
	}

	public ArrayList <Article> getAllArticle() {
	
		ArrayList <Article> entries = new ArrayList<Article>();

		Cursor cursor = database.query(Database_News.TABLE_NEWS, allColumns, 
			  null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Article entry = cursorToArticle(cursor);
			entries.add(entry); // add to arraylist
			cursor.moveToNext();
		}
		cursor.close();
		return entries; 
	}

	private Article cursorToArticle(Cursor cursor) {

		Article article = new Article();
		article.setId(cursor.getLong(0));
		article.setTitle(cursor.getString(1));
		article.setSumm(cursor.getString(2));
		return article;
	}

  public void deleteAllArticles() {
	  List <Article> articles = getAllArticle(); 
	  for (Article article : articles){ 
		  database.delete(Database_News.TABLE_NEWS, Database_News.COLUMN_ID + " = " + article.getId(), null);
	  }
  }

  public void setArticles(List<Article> newArticles) {
	  open();
	  List <Article> articles = getAllArticle(); 
	  
	  for (Article article : articles){ 
		  database.delete(Database_News.TABLE_NEWS, Database_News.COLUMN_ID + " = " + article.getId(), null);
	  }
	  
	  for (Article article : newArticles){ 
		  createArticle(article.getTitle(), article.getSumm());
	  }
	  close();
  }
} 
