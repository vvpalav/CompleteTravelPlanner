package com.example.completetravelplanner.controllers;

import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.services.ServiceHandler;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.widget.*;

public class MainActivity extends Activity {
	private int progress = 0;
	public static Context contextOfApplication;
	private SharedPreferences shared;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		contextOfApplication = getApplicationContext();
		shared = PreferenceManager.getDefaultSharedPreferences(contextOfApplication);
		final ProgressBar loading = (ProgressBar) findViewById(R.id.loadingBar);
		final Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {
				if (progress < 100) {
					progress += 30;
					loading.setProgress((progress > 100) ? 100 : progress);
				} else {
					timer.cancel();
					timer.purge();
					Log.i("MainActivity", "starting home screen");
					startActivity(new Intent(MainActivity.this,HomeScreenActivity.class));
					finish();
				}
			}
		};
		
		 if (shared.contains("is_user_reg") && shared.getBoolean("is_user_reg", false)) {
			Log.i("MainActivity", "pulling data from server");
			timer.schedule(task, 10, 700);
			pullDataFromServer();
		} else if (shared.contains("is_user_accepted") && shared.getBoolean("is_user_accepted", false)){
			Log.i("MainActivity", "starting Registration screen");
			startActivity(new Intent(this, InputRegistrationCodeActivity.class));
			finish();
		} else {
			Log.i("MainActivity", "starting login screen");
			startActivity(new Intent(this, LoginActivity.class));
			finish();
		}
		
		if(!shared.contains("maxExpense")){
			shared.edit().putInt("maxExpense", 0).commit();
		}
		
		if(!shared.contains("maxChecklist")){
			shared.edit().putInt("maxChecklist", 0).commit();
		}
	}

	public static Context getContextOfApplication() {
		return contextOfApplication;
	}

	public void pullDataFromServer() {
		try {
			String value = shared.getString("telephone", null);
			JSONObject object = new JSONObject();
			object.put("transaction_type", "MODIFICATION_PULL");
			object.put("telephone", value);
			new ServiceHandler(this).startServices(object.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}