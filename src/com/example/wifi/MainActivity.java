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
	WifiManager.WifiLock wmlock;//阻止WIFI進入睡眠
	Handler mHandler;
	String otherwifi, pos;
	
	private WifiInfo info; 
	int strength;
	int speed;
	private List<ScanResult> results;
	
	private SQLiteDatabase db = null;
	private DBHelper helper = null;
	public String _DBname = "wifiData.db";//資料庫名
	public String _TABLEname = "data";//表名
	
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
		
	///按下按鈕開始掃描附近AP
	//並將動作post給runnable
	OnClickListener startClick = new OnClickListener(){
		@Override
		public void onClick(View v){
			mHandler.post(r1);
			
			if(!wm.isWifiEnabled()){
				
				wm.setWifiEnabled(true);
				//Toast.makeText(MainActivity.this, "WiFi開啟中", Toast.LENGTH_SHORT).show();
			} 	
						
			wm.startScan();
			//偵測周圍WIFI環境
			results = wm.getScanResults();
			//目前連線WIFI資訊
			WifiInfo info = wm.getConnectionInfo();
			//configure = wm.getConfiguredNetworks();
			strength = info.getRssi();
			speed = info.getLinkSpeed();
			
		}
		
	};
	
	//此按鈕是讓user能自己輸入地點名稱
	OnClickListener okayClick = new OnClickListener(){
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			pos = position.getText().toString();
			//Log.d(TAG, pos);
		}
		
	};
	
	//upload按鈕是要讓sqlite table能直接上傳到php網址
	//目前還沒有開發
	OnClickListener uploadClick = new OnClickListener(){
		
		@Override
		public void onClick(View v) {
			
		}
	};
	
	
		int i, d=1;
		String data, apMac, apId ;
		int level;
		//將每次掃描的資料存入txt檔 每一次scan會得到附近所有AP的訊號 
		//整個runnable會每delay 0.5秒再run，run30個循環，因此會得到附近所有AP各30個訊號
		
		final Runnable r1 = new Runnable(){
			@Override
			public void run(){
					
				wm.startScan(); //每RUN一次都重新掃描一次訊號
				results = wm.getScanResults();
				for (int i = 0; i < results.size(); i++){
					
					data +=  results.get(i).BSSID+ "\n" 
					+ results.get(i).SSID +"\n" +results.get(i).level+ "\n"; //SSID為AP名稱，level為訊號強度
					apMac = results.get(i).BSSID;
					apId = results.get(i).SSID;
					level = results.get(i).level;
					//Log.d(TAG, apMac+apId+level+pos);
					
			   /*create object of type ContentValue with represents data been inserted
				* table共五個欄位 _SCANID是指30次掃描中的哪一次
				*				 _BSSID是AP的MAC ADDRESS
				*				 _SSID是AP名稱
				*				 _LEVEL為訊號強度
				*				 _POS是先前使用者輸入的地點名稱
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
					
				//每delay 0.5秒 RUN一次
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
	


