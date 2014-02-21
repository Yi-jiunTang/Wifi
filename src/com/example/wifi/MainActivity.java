package com.example.wifi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.TelephonyManager;

;

public class MainActivity extends Activity {
	public static String URI_API = "http://140.116.39.172/wifi_data/wifi_update.php";
	// public static String URI_API =
	// "http://140.116.179.24/wifi_Project/wifi_update.php";
	public static final String TEMP_FILE_NAME = "WifiRecord";
	private static final String TUPLENAME = "WifiRecord";
	private static final String TAG = "MyActivity";
	// public static final int TIME = 30;
	private TextView tv;
	private Button mScanButton, mUploadButton, mEnterButton, mConvertButton;
	private EditText location, frequency;
	public WifiManager wm;
	WifiManager.WifiLock unlock;// avoid wifi falling sleep
	private List<ScanResult> results;
	private SQLiteDatabase db = null;
	public static DBHelper helper = null;
	public String _DBname = "wifiData.db";
	private TelephonyManager telephonyManager;
	int strength;
	int speed;
	int Sequence;
	int level;
	int i;
	int pos, freq;
	String data, apMac, apId;
	String otherwifi, iMEIString, MACString;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// setting all neeeded buttons
		wm = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		mScanButton = (Button) findViewById(R.id.scan); // start to scan
		mScanButton.setOnClickListener(scanButtonClickListener);
		mUploadButton = (Button) findViewById(R.id.upload); // upload to sever
		mUploadButton.setOnClickListener(uploadButtonClickListener);
		mEnterButton = (Button) findViewById(R.id.enter); // check the location
		mEnterButton.setOnClickListener(enterButtonClickListener);
		mConvertButton = (Button) findViewById(R.id.convert); // convert to xml
		mConvertButton.setOnClickListener(convertButtonClickListener);
		location = (EditText) findViewById(R.id.hint);
		frequency = (EditText) findViewById(R.id.scanFreq);
		tv = (TextView) findViewById(R.id.wifiSS);
		// the wifi scan results is stored in arraylist
		results = new ArrayList<ScanResult>();
		// start my database class
		this.helper = new DBHelper(this, "wifiData.db", null, 1);

		// calculate how many times the process is executed
		SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
		Sequence = preferences.getInt("Sequence", 0);

		telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		String iMEIString = telephonyManager.getDeviceId();
		Log.d(TAG, "IMEI:" + iMEIString);

	}

	// click scan button it will post to r1 execute the scan cycle
	View.OnClickListener scanButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// lHandler is manager, r1 is its employee
			// one manager, one employee, one job
			Handler lHandler = new Handler();
			Runnable r1 = new InnerRunnable(lHandler);
			lHandler.post(r1);

			// ensure wifi is open
			if (!wm.isWifiEnabled()) {

				wm.setWifiEnabled(true);

			}

			// click scan once, calculate +1
			Sequence++;
			getPreferences(Context.MODE_PRIVATE).edit()
					.putInt("Sequence", Sequence).commit();

			wm.startScan();
			results = wm.getScanResults();
			WifiInfo info = wm.getConnectionInfo();
			strength = info.getRssi();
			speed = info.getLinkSpeed();
			String MACString = info.getMacAddress();
			Log.d(TAG, "MAC ADDRESS:" + MACString);
			Toast.makeText(
					v.getContext(),
					"Starting Scan." + "Please Wait " + (freq / 2) + " seconds",
					Toast.LENGTH_LONG).show();
		}

	};

	// record the location name
	View.OnClickListener enterButtonClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			pos = Integer.parseInt(location.getText().toString());
			freq = Integer.parseInt(frequency.getText().toString());
			Toast.makeText(v.getContext(),
					"The location is " + pos + ": " + freq + "times",
					Toast.LENGTH_LONG).show();
			Log.d(TAG, "pos:" + pos + "freq:" + freq);
		}

	};

	/*
	 * CONVERT TO XML convert string is stored in WifiRecord first the resource
	 * comes from sqlite database, it rawQuery all columns and append to xml the
	 * xml results temporarily stored in file
	 */
	View.OnClickListener convertButtonClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Cursor cursor;
			File file = new File(getFilesDir(), TEMP_FILE_NAME);
			SQLiteDatabase readDatabase = helper.getReadableDatabase();
			cursor = readDatabase.rawQuery(
					String.format("SELECT * FROM %s", DBHelper._TableName),
					null);

			XmlBuilder mXmlBuilder = new XmlBuilder(cursor, file);
			mXmlBuilder.converToXmlFile();
			Toast.makeText(v.getContext(), "Convert to XML Succeed!",
					Toast.LENGTH_LONG).show();
		}
	};
	// upload to server, click button post to uoload();
	// initialzeDB clean the uploaded data
	View.OnClickListener uploadButtonClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			Toast.makeText(v.getContext(), "Upload File Succeed!",
					Toast.LENGTH_LONG).show();
			upload();
			// initializeDb();
		}
	};

	// mHandler is a manager inside lHndler, it's responsible for counting scan
	// times
	private class InnerRunnable implements Runnable {
		Handler mHandler;
		int scanCnt = 1;

		public InnerRunnable(Handler handler) {
			mHandler = handler;
		}

		// scan all around wifi APs 0.5second a time till 30 times
		// insert data into sqlite database
		@Override
		public void run() {
			db = helper.getWritableDatabase();
			wm.startScan();
			results = wm.getScanResults();
			data = "";
			for (int i = 0; i < results.size(); i++) {

				data += results.get(i).BSSID + "\n" + results.get(i).SSID
						+ "\n" + results.get(i).level + "\n";
				apMac = results.get(i).BSSID;
				apId = results.get(i).SSID;
				level = results.get(i).level;

				ContentValues values = new ContentValues();
				values.put("Scan_Id", scanCnt);
				values.put("Mac_Address", apMac);
				values.put("Ap_Name", apId);
				values.put("RSS", level);
				values.put("Location", pos);
				values.put("Sequence", Sequence);
				// values.put("Device_Id", MACString);//or MACString

				db = helper.getWritableDatabase();
				db.insert(DBHelper._TableName, null, values);

			}
			db.close();

			if (scanCnt < freq) {
				scanCnt++;
				mHandler.postDelayed(this, 500);
			}
			if (scanCnt == freq) {
				Toast.makeText(getApplicationContext(), freq + "次掃描結束",
						Toast.LENGTH_LONG).show();
			}

			tv.setText(data);

		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onDestroy");
		if (db != null)
			db.close();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}

	// build a file store cursor results
	static class XmlBuilder {

		// private static final String XML_OPENING =
		// "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		private Cursor mCursor;
		private File mFileXmlFile;

		public XmlBuilder(Cursor cursor, File file) {
			this.mCursor = cursor;
			this.mFileXmlFile = file;
		}

		// the cursor results append tags for xml
		protected void converToXmlFile() {
			StringBuilder mBuilder = new StringBuilder();
			// mBuilder.append(XmlBuilder.XML_OPENING);
			mBuilder.append(openTag(DBHelper.WIFIRECORDS));
			mBuilder.append("\n");
			while (mCursor.moveToNext()) {
				// string every column, add tags between them
				String ColumnOne = mCursor.getString(0);
				String ColumnTwo = mCursor.getString(1);
				String ColumnThree = mCursor.getString(2);
				String ColumnFour = mCursor.getString(3);
				String ColumnFive = mCursor.getString(4);
				String ColumnSix = mCursor.getString(5);
				// String columnSeven = mCursor.getString(6);

				mBuilder.append(openTag(TUPLENAME));
				mBuilder.append("\n");

				mBuilder.append("\t" + openTag(DBHelper.SEQUENCE));
				mBuilder.append(ColumnOne);
				mBuilder.append(endTag(DBHelper.SEQUENCE));
				mBuilder.append("\n");

				mBuilder.append("\t" + openTag(DBHelper.SCAN_ID));
				mBuilder.append(ColumnTwo);
				mBuilder.append(endTag(DBHelper.SCAN_ID));
				mBuilder.append("\n");

				mBuilder.append("\t" + openTag(DBHelper.MAC_ADDRESS));
				mBuilder.append(ColumnThree);
				mBuilder.append(endTag(DBHelper.MAC_ADDRESS));
				mBuilder.append("\n");

				mBuilder.append("\t" + openTag(DBHelper.AP_NAME));
				mBuilder.append(ColumnFour);
				mBuilder.append(endTag(DBHelper.AP_NAME));
				mBuilder.append("\n");

				mBuilder.append("\t" + openTag(DBHelper.RSS));
				mBuilder.append(ColumnFive);
				mBuilder.append(endTag(DBHelper.RSS));
				mBuilder.append("\n");

				mBuilder.append("\t" + openTag(DBHelper.LOCATION));
				mBuilder.append(ColumnSix);
				mBuilder.append(endTag(DBHelper.LOCATION));
				mBuilder.append("\n");

				// mBuilder.append("\t" + openTag(DBHelper.DEVICE_ID));
				// mBuilder.append(columnSeven);
				// mBuilder.append(endTag(DBHelper.DEVICE_ID));
				// mBuilder.append("\n");

				mBuilder.append(endTag(TUPLENAME));
				mBuilder.append("\n");
			}
			mBuilder.append(endTag(DBHelper.WIFIRECORDS));
			Log.v(TAG, mBuilder.toString());
			// append end, results save to file
			saveToFile(mBuilder.toString());
		}

		// write the append results into mFileXmlFile
		private void saveToFile(String dataxml) {
			try {
				FileWriter fileWriter = new FileWriter(mFileXmlFile);
				fileWriter.write(dataxml);
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		static String openTag(String tag) {
			return "<" + tag + ">";
		}

		static String endTag(String tag) {
			return "</" + tag + ">";
		}

	}

	// TEST MODE
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Start");
		menu.add("Stop");
		menu.add("test");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v(TAG, String.format("item.getTitle().equals(TEST) = %B ", item
				.getTitle().equals("TEST")));
		if (item.getTitle().equals("Start")) {
			start();
			return true;
		} else if (item.getTitle().equals("Stop")) {
			stop();
			return true;
		} else if (item.getTitle().equals("test")) {
			test();
			return true;
		}
		return false;
	}

	@SuppressLint("NewApi")
	private void test() {

		Log.v(TAG,
				Integer.toString(helper
						.getReadableDatabase()
						.query(false, DBHelper._TableName, null, null, null,
								null, null, null, null, null).getCount()));
	}

	private void stop() {
		Log.v(TAG, "stop()");
		Intent intent = new Intent();
		intent.setClass(this, UploadIntentService.class);
		stopService(intent);
	}

	private void start() {
		Log.v(TAG, String.format("start(), file is %s", new File(getFilesDir(),
				TEMP_FILE_NAME)));
		Intent intent = new Intent();
		intent.setClass(this, UploadIntentService.class);
		startService(intent);
	}

	private void upload() {
		Intent intent = new Intent();
		intent.setClass(this, UploadIntentService.class);
		startService(intent);
	}
	// TEST MODE
}
