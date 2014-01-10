package com.example.wifi;



import android.content.Context;

//import android.content.ContentValues;

import android.database.Cursor;

import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteDatabase.CursorFactory;

import android.database.sqlite.SQLiteOpenHelper;

import android.util.Log;



class DBHelper extends SQLiteOpenHelper{

	private static final String TAG = "DB";

	private final static int _DBversion = 1; //版本

	private final static String _DBname = "wifiData.db";

	private final static String _TableName = "data";

	public static final String COUNTING_NUMBER = "_SCANID";

	public static final String MAC_ADDRESS = "_BSSID";

	public static final String AP_NAME = "_SSID";

	public static final String SIGNAL_STRENGTH = "_LEVEL";

	public static final String POSITION = "_POS";

	//private SQLiteDatabase db;

	

	public DBHelper(Context context){

		super(context, _DBname, null, _DBversion);	

	}

	public DBHelper(Context context, String name, CursorFactory factory, int version){

		super(context, name, factory, version);

	}

	

	

	@Override

	public void onCreate(SQLiteDatabase db){

		//TODO Auto-generated method stub

		

		final String SQL = "CREATE TABLE " 

				+ _TableName +"(" 

				+ COUNTING_NUMBER + " INTEGER NOT NULL,"

				+ MAC_ADDRESS + " TEXT NOT NULL,"

				+ AP_NAME + " TEXT NOT NULL," 

				+ SIGNAL_STRENGTH + " TEXT NOT NULL,"

				+ POSITION + " TEXT NOT NULL,"

				+"PRIMARY KEY(_SCANID, _BSSID)"+

				");";

		db.execSQL(SQL);

		Log.e(TAG, "creat table succeed!");

	}

	

	@Override

	public void onOpen(SQLiteDatabase db){

		//TODO Auto-generated method stub

		Log.e(TAG, "SQLiteHelper onOpen");

		super.onOpen(db);

	}

	@Override

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

		//TODO Auto-generated method stub

		db.execSQL("DROP TABLE IF EXISTS");

		onCreate(db);

		Log.e(TAG, "SQLiteHelper onUpgrade!");

	}
	 /*public void clean (){  
	        this.getWritableDatabase().execSQL("DROP TABLE IF EXISTS "+_TableName);  
	        System.out.println("clean删除表");  
	        this.onCreate(this.getWritableDatabase());  
	        this.getWritableDatabase().close();  
	    }  */

}

	