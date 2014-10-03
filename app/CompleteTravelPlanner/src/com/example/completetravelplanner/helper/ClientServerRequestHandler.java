package com.example.completetravelplanner.helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.completetravelplanner.controllers.*;

public class ClientServerRequestHandler {
	private static DatabaseHelper db;

	public static void handleRequest(String line) {
		SharedPreferences shared = PreferenceManager
				.getDefaultSharedPreferences(MainActivity.getContextOfApplication());
		try {
			if (db == null) {
				db = new DatabaseHelper(MainActivity.getContextOfApplication());
			}
			JSONObject object = new JSONObject(line);
			String action = object.getString("transaction_type");
			if (action.equals("MODIFICATION_PUSH")) {
				if (object.getString("action").equals("INSERT")) {
					db.insertDataIntoDB(object);
				} else if (object.getString("action").equals("UPDATE")) {
					db.updateDataIntoDB(object);
				} else if (object.getString("action").equals("DELETE")) {
					db.deleteDataFromDB(object);
				}
			} else if (action.equals("TRIP_DELETION")){
				db.deleteTrip(object.getLong("trip_id"));
			} else if (action.equals("NEW_USER_REGISTRATION") || 
					action.equals("EXISTING_USER_REGISTRATION") ) {
				shared.edit().putString("user_login_info", line).commit();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void handleServerResponse(String line) {
		try {
			Log.i("from_server", line);
			if (db == null) {
				db = new DatabaseHelper(MainActivity.getContextOfApplication());
			}
			SharedPreferences shared = PreferenceManager
					.getDefaultSharedPreferences(MainActivity
							.getContextOfApplication());
			JSONObject object = new JSONObject(line);
			String action = object.getString("transaction_type");
			if (action.equals("MODIFICATION_PULL_RESPONSE")) {
				JSONArray data = object.getJSONArray("data");
				for (int i = 0; i < data.length(); i++) {
					JSONObject mod = data.getJSONObject(i);
					if (mod.getString("action").equals("INSERT")) {
						db.insertDataIntoDB(mod);
					} else if (mod.getString("action").equals("UPDATE")) {
						db.updateDataIntoDB(mod);
					} else if (mod.getString("action").equals("DELETE")) {
						db.deleteDataFromDB(mod);
					}
				}
			} else if (action.equals("USER_PASSCODE_AUTH_RESPONSE")){
				boolean flag = false;
				if(object.getString("response").equalsIgnoreCase("success")) {
					flag = true;
					shared.edit().putString("telephone", object.getString("telephone"))
						.putString("email_id", object.getString("email_id"))
						.putString("name", object.getString("name")).commit();
				}
				shared.edit().putBoolean("is_user_reg", flag).commit();
			} else if (action.equals("TRIP_CREATION_RESPONSE")
					&& object.getString("response").equalsIgnoreCase("success")) {
				db.createTrip(object);
			} else if (action.equals("TRIP_ADD_MEMBER_RESPONSE")
					&& object.getString("response").equalsIgnoreCase("success")) {
				db.insertTripMembers(object);
			} else if (action.equals("NEW_USER_REGISTRATION_RESPONSE")) {
				boolean flag = true;
				if (object.getString("response").equals("Failure")) {
					shared.edit().putString("server_res", object.getString("message")).commit();
					flag = false;
				} 
				shared.edit().putBoolean("is_user_accepted", flag).commit();
			} else if (action.equals("EXISTING_USER_REGISTRATION_RESPONSE")) {
				boolean flag = true;
				if (object.getString("response").equals("Failure")) {
					flag = false;
				} 
				shared.edit().putBoolean("is_user_accepted", flag).commit();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
