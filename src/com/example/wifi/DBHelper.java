package com.example.wifi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//import android.content.ContentValues;

public class DBHelper extends SQLiteOpenHelper {

	static final String TAG = DBHelper.class.getSimpleName();

	final static int _DBversion = 2; // ��本

	final static String _DBname = "wifiData.db";

	final static String _TableName = "data";

	static final String COUNTER = "Counter";

	public static final String SCAN_ID = "Scan_Id";

	public static final String MAC_ADDRESS = "Mac_Address";

	public static final String AP_NAME = "Ap_Name";

	public static final String RSS = "RSS";

	public static final String POSITION = "Position";

	public static final String WIFIRECORDS = "WifiRecords";

	// private SQLiteDatabase db;

	public DBHelper(Context context) {

		super(context, _DBname, null, _DBversion);

	}

	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {

		super(context, name, factory, version);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		// TODO Auto-generated method stub

		final String SQL = "CREATE TABLE "

				+ _TableName
				+ "("

				+ COUNTER
				+ " INTEGER NOT NULL,"

				+ SCAN_ID
				+ " INTEGER NOT NULL,"

				+ MAC_ADDRESS
				+ " TEXT NOT NULL,"

				+ AP_NAME
				+ " TEXT NOT NULL,"

				+ RSS
				+ " TEXT NOT NULL,"

				+ POSITION
				+ " TEXT NOT NULL,"

				+ String.format("PRIMARY KEY(%s,%s,%s)", COUNTER,
						SCAN_ID, MAC_ADDRESS) +

				");";

		db.execSQL(SQL);

		Log.d(TAG, "creat table succeed!");

	}

	@Override
	public void onOpen(SQLiteDatabase db) {

		// TODO Auto-generated method stub

		Log.d(TAG, "SQLiteHelper onOpen");

		super.onOpen(db);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		// TODO Auto-generated method stub

		db.execSQL("DROP TABLE IF EXISTS");

		onCreate(db);

		Log.d(TAG, "SQLiteHelper onUpgrade!");

	}
	/*
	 * public void clean (){
	 * this.getWritableDatabase().execSQL("DROP TABLE IF EXISTS "+_TableName);
	 * System.out.println("clean��除���);
	 * this.onCreate(this.getWritableDatabase());
	 * this.getWritableDatabase().close(); }
	 */

}
