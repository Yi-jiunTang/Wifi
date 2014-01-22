package com.example.wifi;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class UploadIntentService extends IntentService {

	private static final String TAG = UploadIntentService.class.getSimpleName();

	public UploadIntentService(String name) {
		super(name);
	}

	public UploadIntentService() {
		super(null);
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		upload();

	}

	void upload() {
		try {
			HttpURLConnection httpUrlConnection = (HttpURLConnection) new URL(
					MainActivity.URI_API).openConnection();
			httpUrlConnection.setDoOutput(true);
			httpUrlConnection.setRequestMethod("POST");
			OutputStream os = httpUrlConnection.getOutputStream();
			Thread.sleep(1000);
			
			BufferedInputStream fis = new BufferedInputStream(openFileInput("WifiRecord"));

			byte[] temp = new byte[1024 * 4];
			int count;
			while ((count = fis.read(temp)) != -1) {
				os.write(temp, 0, count);
				Log.v(TAG, new String(temp, 0, count));
			}

			fis.close();
			os.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					httpUrlConnection.getInputStream()));

			String s = null;
			while ((s = in.readLine()) != null) {
				System.out.println(s);
			}

			in.close();
			fis.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}
	}

}
