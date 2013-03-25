package com.drive.utility.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database_News extends SQLiteOpenHelper {

  public static final String TABLE_NEWS = "news";
  
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_TITLE = "title";
  public static final String COLUMN_SUMM = "summary";
  
  private static final String DATABASE_NAME = "news.db";
  private static final int DATABASE_VERSION = 1;


  //SQL command to create the table
  private static final String DATABASE_CREATE = "create table "
      + TABLE_NEWS 		+ "(" 
      + COLUMN_ID 		+ " integer primary key autoincrement ," 
      + COLUMN_TITLE	+ " text ,"
      + COLUMN_SUMM		+ " text "
      					+ ");";

  public Database_News(Context context) {
	  super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase database) {
	  database.execSQL(DATABASE_CREATE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	  Log.w( Database_News.class.getName(), "Upgrading database from version " + oldVersion + " to "
			  + newVersion + ", which will destroy all old data");
	  db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWS);
	  onCreate(db);
  }

} 