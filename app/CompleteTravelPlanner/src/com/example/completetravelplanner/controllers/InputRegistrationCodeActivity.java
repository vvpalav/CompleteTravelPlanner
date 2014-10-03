package com.example.completetravelplanner.controllers;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.services.ServiceHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextUtils;

public class InputRegistrationCodeActivity extends Activity {
	
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private String SENDER_ID = "80266458481";
	public static final String TAG = "GCM Services";
	private GoogleCloudMessaging gcm;
	private Context context;
	
	String regid;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_input_registration_code);
		context = getApplicationContext();
		final Intent input = getIntent();
		setTitle("Registration Authentication");
		if(checkPlayServices()){
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(context);
			
			if(regid.isEmpty()){
				registerInBackground();
			}
			}
			else{
				Log.i(TAG, "No valid google play services apk was found");
			}
		
		try {
			final EditText enterCode = (EditText) findViewById(R.id.enter_code);
			enterCode.setError(null);
			final SharedPreferences shared = PreferenceManager
					.getDefaultSharedPreferences(MainActivity.getContextOfApplication());
			final JSONObject inJson = new JSONObject(shared.getString("user_login_info", null));
			Button submit = (Button) findViewById(R.id.submit_code);
			submit.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					View focusView = null;
					if (TextUtils.isDigitsOnly(enterCode.getText().toString()
							.trim())) {
						JSONObject json = new JSONObject();
						int code = Integer.parseInt(enterCode.getText()
								.toString().trim());
						try {
							json.put("transaction_type", "USER_PASSCODE_AUTH");
							json.put("name", inJson.getString("name"));
							json.put("telephone", inJson.getString("telephone"));
							json.put("email_id", inJson.getString("email_id"));
							json.put("TYPE_USER", input.getStringExtra("TYPE_USER"));
							json.put("gcm_id", regid);
							json.put("code", code);
							new ServiceHandler(InputRegistrationCodeActivity.this)
								.startServices(json.toString());
							while(true){
								Thread.sleep(1000);
								if (shared.contains("is_user_reg")) {
									if (!shared.getBoolean("is_user_reg", false)) {
										enterCode.setError(getString(R.string.error_incorrect_code));
										enterCode.requestFocus();
										shared.edit().remove("is_user_reg").commit();
										break;
									} else {
										shared.edit().remove("user_login_info").commit();
										startActivity(new Intent(InputRegistrationCodeActivity.this, 
												HomeScreenActivity.class));
										finish();
										break;
									}
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} catch(InterruptedException ex){
							ex.printStackTrace();
						}
					} else {
						enterCode.setError(getString(R.string.error_incorrect_code));
						focusView = enterCode;
						focusView.requestFocus();
					}
				}
			});
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void registerInBackground() {
		// TODO Auto-generated method stub
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				// TODO Auto-generated method stub
				String msg="";
				try{
					if(gcm == null){
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID = " + regid;
					Log.i(TAG, msg);
					sendRegistrationToBackend();
					storeRegistrationId(context, regid);
				} catch(IOException e){
					msg = "Error" + e.getMessage();
					Log.i(TAG, msg);
				}
				return msg;
			}
			
			protected void onPostExecute(String result) {
				// we can send the registration id to the server here.
				
			};
			
		}.execute(null,null,null);
		
	}
	
	
	//pass the registration Id to our server via the service handler
	private void storeRegistrationId(Context context, String regid) {
		// TODO Auto-generated method stub
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG,"Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID,regid);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
		
	}

	
	private void sendRegistrationToBackend() {
		// TODO Auto-generated method stub
		/*
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving the registration ID on the app version - " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regid);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
		*/
	}
			
		
	

	private String getRegistrationId(Context context) {
		// TODO Auto-generated method stub
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if(registrationId.isEmpty()){
			Log.i(TAG, "Registration not found");
			return "";
		}
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if(registeredVersion != currentVersion){
			Log.i(TAG, "App version changed");
			return "";
		}
		return registrationId;
	}

	private int getAppVersion(Context context) {
		// TODO Auto-generated method stub
		try{
			PackageInfo packageinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageinfo.versionCode;
		} catch(NameNotFoundException e){
			throw new RuntimeException("Could not get the package name :" + e);
			}
		
	}

	private SharedPreferences getGCMPreferences(Context context2) {
		// TODO Auto-generated method stub
		return getSharedPreferences(InputRegistrationCodeActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	private boolean checkPlayServices() {
		// TODO Auto-generated method stub
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if(resultCode != ConnectionResult.SUCCESS){
			if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			}
		else {
			Log.i(TAG, "This device is not supported");
			finish();
		}
		return false;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.input_registration_code, menu);
		return true;
	}

}
