package com.example.wifi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.os.Handler;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


public class MainActivity extends Activity {
	
	private static final String TAG = "MyActivity";
	private TextView tv;
	private Button scan, upload, enter;
	private EditText position;
	public WifiManager wm;
	WifiManager.WifiLock wmlock;//����WIFI�i�J�ίv
	Handler mHandler;
	String otherwifi, pos;
	
	private WifiInfo info; 
	int strength;
	int speed;
	private List<ScanResult> results;
	
	private SQLiteDatabase db = null;
	private DBHelper helper = null;
	public String _DBname = "wifiData.db";//��Ʈw�W
	public String _TABLEname = "data";//��W
	
	public String uriAPI = "http://140.116.39.172/wifi_data/wifi_update.php";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
					
		wm = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		scan = (Button) findViewById(R.id.scan);
		scan.setOnClickListener(startClick);
		upload = (Button) findViewById(R.id.upload);
		upload.setOnClickListener(uploadClick);
		enter = (Button) findViewById(R.id.enter);
		enter.setOnClickListener(okayClick);
		position = (EditText) findViewById(R.id.hint);
		tv = (TextView) findViewById(R.id.wifiSS);
		List<ScanResult> results = new ArrayList<ScanResult>();
		mHandler = new Handler();
		
		helper = new DBHelper(this, "wifiData.db", null,1);
		db = helper.getWritableDatabase();
		
	}
		
	///���U���s�}�l���y����AP
	//�ñN�ʧ@post��runnable
	OnClickListener startClick = new OnClickListener(){
		@Override
		public void onClick(View v){
			mHandler.post(r1);
			
			if(!wm.isWifiEnabled()){
				
				wm.setWifiEnabled(true);
				//Toast.makeText(MainActivity.this, "WiFi�}�Ҥ�", Toast.LENGTH_SHORT).show();
			} 	
						
			wm.startScan();
			//�����P��WIFI����
			results = wm.getScanResults();
			//�ثe�s�uWIFI��T
			WifiInfo info = wm.getConnectionInfo();
			//configure = wm.getConfiguredNetworks();
			strength = info.getRssi();
			speed = info.getLinkSpeed();
			
		}
		
	};
	
	//�����s�O��user��ۤv��J�a�I�W��
	OnClickListener okayClick = new OnClickListener(){
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			pos = position.getText().toString();
			//Log.d(TAG, pos);
		}
		
	};
	
	//upload���s�O�n��sqlite table�ઽ���W�Ǩ�php���}
	//�ثe�٨S���}�o
	OnClickListener uploadClick = new OnClickListener(){
		
		@Override
		public void onClick(View v) {
			
		}
	};
	
	
		int i, d=1;
		String data, apMac, apId ;
		int level;
		//�N�C�����y����Ʀs�Jtxt�� �C�@��scan�|�o�����Ҧ�AP���T�� 
		//���runnable�|�Cdelay 0.5��Arun�Arun30�Ӵ`���A�]���|�o�����Ҧ�AP�U30�ӰT��
		
		final Runnable r1 = new Runnable(){
			@Override
			public void run(){
					
				wm.startScan(); //�CRUN�@�������s���y�@���T��
				results = wm.getScanResults();
				for (int i = 0; i < results.size(); i++){
					
					data +=  results.get(i).BSSID+ "\n" 
					+ results.get(i).SSID +"\n" +results.get(i).level+ "\n"; //SSID��AP�W�١Alevel���T���j��
					apMac = results.get(i).BSSID;
					apId = results.get(i).SSID;
					level = results.get(i).level;
					//Log.d(TAG, apMac+apId+level+pos);
					
			   /*create object of type ContentValue with represents data been inserted
				* table�@������� _SCANID�O��30�����y�������@��
				*				 _BSSID�OAP��MAC ADDRESS
				*				 _SSID�OAP�W��
				*				 _LEVEL���T���j��
				*				 _POS�O���e�ϥΪ̿�J���a�I�W��
				*/
					ContentValues values = new ContentValues();
					values.put("_SCANID", d);
					values.put("_BSSID", apMac);
					values.put("_SSID", apId);
					values.put("_LEVEL", level);
					values.put("_POS", pos);
					
					db = helper.getWritableDatabase();
					db.insert(_TABLEname, null, values);
					db.close();
				}
					
				//�Cdelay 0.5�� RUN�@��
					if(d<30){
						d++;
						mHandler.postDelayed(r1, 500);
					}
					
					tv.setText(data);
		
			}
	
	/*public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}*/

};
	@Override
	protected void onDestroy(){
		//TODO Auto-generated method stub
		Log.e(TAG, "onDestroy");
		if(db != null)
			db.close();
		super.onDestroy();
	}
	@Override
	protected void onPause(){
		//TODO Auto-generated method stub
		Log.e(TAG, "onPause");
	}
		
}
	


