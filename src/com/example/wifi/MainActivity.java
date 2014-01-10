package com.example.wifi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends Activity {

	private static final String TAG = "MyActivity";
	private TextView tv;
	private Button mScanButton, mUploadButton, mEnterButton;
	private EditText position;
	public WifiManager wm;
	WifiManager.WifiLock wmlock;// ����WIFI�i�J�ίv
	Handler mHandler;
	String otherwifi, pos;

	int strength;
	int speed;
	int counter;

	private List<ScanResult> results;

	private SQLiteDatabase db = null;
	private DBHelper helper = null;
	public String _DBname = "wifiData.db";// ��Ʈw�W

	public String uriAPI = "http://140.116.39.172/wifi_data/wifi_update.php";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		wm = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		mScanButton = (Button) findViewById(R.id.scan);
		mScanButton.setOnClickListener(startClick);
		mUploadButton = (Button) findViewById(R.id.upload);
		mUploadButton.setOnClickListener(uploadClick);
		mEnterButton = (Button) findViewById(R.id.enter);
		mEnterButton.setOnClickListener(okayClick);
		position = (EditText) findViewById(R.id.hint);
		tv = (TextView) findViewById(R.id.wifiSS);
		results = new ArrayList<ScanResult>();
		mHandler = new Handler();

		helper = new DBHelper(this, "wifiData.db", null, 1);
		db = helper.getWritableDatabase();

		SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
		counter = preferences.getInt("COUNTER", 0);
	}

	// /���U���s�}�l���y����AP
	// �ñN�ʧ@post��runnable
	View.OnClickListener startClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mHandler.post(r1);

			if (!wm.isWifiEnabled()) {

				wm.setWifiEnabled(true);
				// Toast.makeText(MainActivity.this, "WiFi�}�Ҥ�",
				// Toast.LENGTH_SHORT).show();
			}
			counter++;
			getPreferences(Context.MODE_PRIVATE).edit()
					.putInt("COUNTER", counter).commit();
			wm.startScan();
			// �����P��WIFI����
			results = wm.getScanResults();
			// �ثe�s�uWIFI��T
			WifiInfo info = wm.getConnectionInfo();
			// configure = wm.getConfiguredNetworks();
			strength = info.getRssi();
			speed = info.getLinkSpeed();

		}

	};

	// �����s�O��user��ۤv��J�a�I�W��
	View.OnClickListener okayClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			pos = position.getText().toString();
			// Log.d(TAG, pos);
		}

	};

	// upload���s�O�n��sqlite table�ઽ���W�Ǩ�php���}
	// �ثe�٨S���}�o
	View.OnClickListener uploadClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Cursor cursor;
			File file = new File(getFilesDir(), "temp");
			SQLiteDatabase readDatabase= helper.getReadableDatabase();
			cursor = readDatabase.rawQuery(String.format("SELECT * FROM %s", DBHelper._TableName), null);
			 
			XmlBuilder mXmlBuilder = new XmlBuilder(cursor, file);
			mXmlBuilder.converToXmlFile();
		}
	};

	int i, d = 1;
	String data, apMac, apId;
	int level;
	// �N�C�����y����Ʀs�Jtxt�� �C�@��scan�|�o�����Ҧ�AP���T��
	// ���runnable�|�Cdelay 0.5��Arun�Arun30�Ӵ`���A�]���|�o�����Ҧ�AP�U30�ӰT��

	final Runnable r1 = new Runnable() {
		@Override
		public void run() {

			wm.startScan(); // �CRUN�@�������s���y�@���T��
			results = wm.getScanResults();
			for (int i = 0; i < results.size(); i++) {

				data += results.get(i).BSSID + "\n" + results.get(i).SSID
						+ "\n" + results.get(i).level + "\n"; // SSID��AP�W�١Alevel���T���j��
				apMac = results.get(i).BSSID;
				apId = results.get(i).SSID;
				level = results.get(i).level;
				// Log.d(TAG, apMac+apId+level+pos);

				/*
				 * create object of type ContentValue with represents data been
				 * inserted table�@������� _SCANID�O��30�����y�������@�� _BSSID�OAP��MAC ADDRESS
				 * _SSID�OAP�W�� _LEVEL���T���j�� _POS�O���e�ϥΪ̿�J���a�I�W��
				 */
				ContentValues values = new ContentValues();
				values.put("Scan_id", d);
				values.put("Mac_address", apMac);
				values.put("Ap_name", apId);
				values.put("RSS", level);
				values.put("Position", pos);
				values.put("Counter", counter);

				db = helper.getWritableDatabase();
				db.insert(DBHelper._TableName, null, values);
				db.close();
			}

			// �Cdelay 0.5�� RUN�@��
			if (d < 30) {
				d++;
				mHandler.postDelayed(r1, 500);
			}

			tv.setText(data);

		}

		/*
		 * public boolean onCreateOptionsMenu(Menu menu) { // Inflate the menu;
		 * this adds items to the action bar if it is present.
		 * getMenuInflater().inflate(R.menu.main, menu); return true; }
		 */

	};

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
		// TODO Auto-generated method stub
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
			mBuilder.append(XmlBuilder.XML_OPENING);
			while (cursor.moveToNext()) {
				int ColumnOne = cursor.getInt(0);
				int ColumnTwo = cursor.getInt(1);
				int ColumnThree = cursor.getInt(2);
				int ColumnFour = cursor.getInt(3);
				int ColumnFive = cursor.getInt(4);
				int ColumnSix = cursor.getInt(5);

				mBuilder.append(openTag(TUPLENAME));
				mBuilder.append("\n");
				
				mBuilder.append("\t"+openTag(DBHelper.COUNTER));
				mBuilder.append(ColumnOne);
				mBuilder.append(endTag(DBHelper.COUNTER));
				mBuilder.append("\n");
				
				mBuilder.append("\t"+openTag(DBHelper.SCAN_ID));
				mBuilder.append(ColumnTwo);
				mBuilder.append(endTag(DBHelper.SCAN_ID));
				mBuilder.append("\n");
				
				mBuilder.append("\t"+openTag(DBHelper.MAC_ADDRESS));
				mBuilder.append(ColumnThree);
				mBuilder.append(endTag(DBHelper.MAC_ADDRESS));
				mBuilder.append("\n");
								
				mBuilder.append("\t"+openTag(DBHelper.AP_NAME));
				mBuilder.append(ColumnFour);
				mBuilder.append(endTag(DBHelper.AP_NAME));
				mBuilder.append("\n");
				
				mBuilder.append("\t"+openTag(DBHelper.RSS));
				mBuilder.append(ColumnFive);
				mBuilder.append(endTag(DBHelper.RSS));
				mBuilder.append("\n");
				
				mBuilder.append("\t"+openTag(DBHelper.POSITION));
				mBuilder.append(ColumnSix);
				mBuilder.append(endTag(DBHelper.POSITION));
				mBuilder.append("\n");
				
				mBuilder.append(endTag(TUPLENAME));
				mBuilder.append("\n");
			}
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

}
