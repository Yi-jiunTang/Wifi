package com.example.wifi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String TEMP_FILE_NAME = "WifiRecord";
	private static final String TAG = "MyActivity";
	private TextView tv;
	private Button mScanButton, mUploadButton, mEnterButton, mConvertButton;
	private EditText position;
	public WifiManager wm;
	public static final int TIME = 30;
	WifiManager.WifiLock wmlock;// ���WIFI�i�J�ίv
	// Handler mHandler;
	String otherwifi, pos;

	int strength;
	int speed;
	int counter;

	private List<ScanResult> results;

	private SQLiteDatabase db = null;
	private DBHelper helper = null;
	public String _DBname = "wifiData.db";

	public static String URI_API = "http://140.116.179.24/wifi_Project/wifi_update.php";
	private ServiceConnection mServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onServiceConnected" + name.getClassName());
			// mUploadService = service.getService();
		}

		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "onServiceDisconnected" + name.getClassName());
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		wm = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		mScanButton = (Button) findViewById(R.id.scan);
		mScanButton.setOnClickListener(scanButtonClickListener);
		mUploadButton = (Button) findViewById(R.id.upload);
		mUploadButton.setOnClickListener(uploadButtonClickListener);
		mEnterButton = (Button) findViewById(R.id.enter);
		mEnterButton.setOnClickListener(enterButtonClickListener);
		mConvertButton = (Button) findViewById(R.id.convert);
		mConvertButton.setOnClickListener(convertButtonClickListener);
		position = (EditText) findViewById(R.id.hint);
		tv = (TextView) findViewById(R.id.wifiSS);
		results = new ArrayList<ScanResult>();

		helper = new DBHelper(this, "wifiData.db", null, 1);

		SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
		counter = preferences.getInt("COUNTER", 0);
	}

	// /���U���s�}�l���y����AP
	// �ñN�ʧ@post��runnable
	View.OnClickListener scanButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Handler lHandler = new Handler();
			Runnable r1 = new InnerRunnable(lHandler);
			lHandler.post(r1);

			if (!wm.isWifiEnabled()) {

				wm.setWifiEnabled(true);
				// Toast.makeText(MainActivity.this, "WiFi�}�Ҥ�",
				// Toast.LENGTH_SHORT).show();
			}
			counter++;
			getPreferences(Context.MODE_PRIVATE).edit()
					.putInt("COUNTER", counter).commit();
			wm.startScan();
			// ����P��WIFI����
			results = wm.getScanResults();
			// �ثe�s�uWIFI��T
			WifiInfo info = wm.getConnectionInfo();
			// configure = wm.getConfiguredNetworks();
			strength = info.getRssi();
			speed = info.getLinkSpeed();
			Toast.makeText(v.getContext(), "Starting Scan. Please Wait 15s",
					Toast.LENGTH_LONG).show();
		}

	};

	// �����s�O��user��ۤv��J�a�I�W��
	View.OnClickListener enterButtonClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			pos = position.getText().toString();
			Toast.makeText(v.getContext(), "The position is " + pos,
					Toast.LENGTH_LONG).show();
			// Log.d(TAG, pos);
		}

	};

	// CONVERT TO XML
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
	// upload���s�O�n��sqlite table�ઽ���W�Ǩ�php���}
	View.OnClickListener uploadButtonClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// // upload(file);
			// Intent intent = new Intent(MainActivity.this,
			// UploadIntentService.class);
			// startService(intent);
			Toast.makeText(v.getContext(), "Upload File Succeed!",
					Toast.LENGTH_LONG).show();
			upload();
			initializeDb();
		}
	};
	int i, d = 1;
	String data, apMac, apId;
	int level;

	private class InnerRunnable implements Runnable {

		Handler mHandler;

		public InnerRunnable(Handler handler) {
			mHandler = handler;
		}

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
				// Log.d(TAG, apMac+apId+level+pos);

				/*
				 * create object of type ContentValue with represents data been
				 * inserted table�@������� _SCANID�O��30�����y�������@��
				 * _BSSID�OAP��MAC ADDRESS _SSID�OAP�W�� _LEVEL���T���j��
				 * _POS�O��e�ϥΪ̿�J���a�I�W��
				 */
				ContentValues values = new ContentValues();
				values.put("Scan_Id", d);
				values.put("Mac_Address", apMac);
				values.put("Ap_Name", apId);
				values.put("RSS", level);
				values.put("Position", pos);
				values.put("Counter", counter);

				db = helper.getWritableDatabase();
				db.insert(DBHelper._TableName, null, values);

			}
			db.close();

			if (d < TIME) {
				d++;
				mHandler.postDelayed(this, 500);
			}

			tv.setText(data);

		}

		/*
		 * public boolean onCreateOptionsMenu(Menu menu) { // Inflate the menu;
		 * this adds items to the action bar if it is present.
		 * getMenuInflater().inflate(R.menu.main, menu); return true; }
		 */
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onDestroy");
		if (db != null)
			db.close();
		super.onDestroy();
	}

	private void initializeDb() {
		helper.getWritableDatabase().delete(DBHelper._TableName, null, null);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.e(TAG, "onPause");
	}

	static class XmlBuilder {

		private static final String XML_OPENING = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		private static final String TUPLENAME = "WifiRecord";

		private Cursor cursor;
		private File file;

		public XmlBuilder(Cursor cursor, File file) {
			this.cursor = cursor;
			this.file = file;
		}

		protected void converToXmlFile() {
			StringBuilder mBuilder = new StringBuilder();
			// mBuilder.append(XmlBuilder.XML_OPENING);
			mBuilder.append(openTag(DBHelper.WIFIRECORDS));
			mBuilder.append("\n");
			while (cursor.moveToNext()) {
				String ColumnOne = cursor.getString(0);
				String ColumnTwo = cursor.getString(1);
				String ColumnThree = cursor.getString(2);
				String ColumnFour = cursor.getString(3);
				String ColumnFive = cursor.getString(4);
				String ColumnSix = cursor.getString(5);

				mBuilder.append(openTag(TUPLENAME));
				mBuilder.append("\n");

				mBuilder.append("\t" + openTag(DBHelper.COUNTER));
				mBuilder.append(ColumnOne);
				mBuilder.append(endTag(DBHelper.COUNTER));
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

				mBuilder.append("\t" + openTag(DBHelper.POSITION));
				mBuilder.append(ColumnSix);
				mBuilder.append(endTag(DBHelper.POSITION));
				mBuilder.append("\n");

				mBuilder.append(endTag(TUPLENAME));
				mBuilder.append("\n");
			}
			mBuilder.append(endTag(DBHelper.WIFIRECORDS));
			Log.v(TAG, mBuilder.toString());
			saveToFile(mBuilder.toString());
		}

		private void saveToFile(String dataxml) {
			try {
				FileWriter fileWriter = new FileWriter(file);
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

}
