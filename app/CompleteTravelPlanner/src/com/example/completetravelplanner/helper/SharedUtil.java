package com.example.completetravelplanner.helper;

import java.util.Date;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.completetravelplanner.controllers.MainActivity;

public class SharedUtil {
	public static final String TRIP_ID = "trip_id";
	public static final String TELEPHONE = "telephone";
	public static final String NAME = "name";
	public static final String CHECKLIST_ITEM_ID = "maxChecklist";
	public static final String TOUR_PLACES_ID ="maxTourPlaces";
	public static final String EXPENSE_ITEM_ID = "maxExpense";
	public static String CHECKLIST_PRIVATE= "Private";
	public static String CHECKLIST_SHARED= "Shared";
	Context context;
	
	public static long getActiveTripId(){
		Context applicationContext = MainActivity.getContextOfApplication();
		long activeTripId = PreferenceManager.getDefaultSharedPreferences(
				applicationContext).getLong(TRIP_ID, 0);
		return activeTripId;
	}
	public static String getTime(){
		Date now = new Date();
		String[] strSplits = now.toString().split(" ");
		String strTime = strSplits[2] + strSplits[3];
		strTime = strTime.replace(":", "");
		return strTime;
	}
	public static String getCurrentUser(Context context){
		Context applicationContext = MainActivity.getContextOfApplication();
		String name = PreferenceManager.getDefaultSharedPreferences(
				applicationContext).getString(NAME, "");
		return name;
	}
	public static long getPhoneNumber(String ph){
		String str = ph;
		str = str.replace("+", "");
		str = str.replace("(", "");
		str = str.replace(")", "");
		str = str.replace("-", "");
		str = str.replace(" ", "");
		return Long.parseLong(str);
	}
	
	public static String getActiveTelephone(){
		Context applicationContext = MainActivity.getContextOfApplication();
		String activeTelephone = PreferenceManager.getDefaultSharedPreferences(
				applicationContext).getString(TELEPHONE, "Failure");
		return activeTelephone;
	}
	
	public static int getMaxCheckListID(){
		Context applicationContext = MainActivity.getContextOfApplication();
		int maxCheckListId = PreferenceManager.getDefaultSharedPreferences(
				applicationContext).getInt(CHECKLIST_ITEM_ID, 0);
		return maxCheckListId;
	}
	public static int getMaxTourPlacesId(){
		Context applicationContext = MainActivity.getContextOfApplication();
		int tourPlacesID = PreferenceManager.getDefaultSharedPreferences(
				applicationContext).getInt(TOUR_PLACES_ID, 0);
		return tourPlacesID;
	}
	
	public static int getMaxExpenseID(){
		Context applicationContext = MainActivity.getContextOfApplication();
		int maxExpenseId = PreferenceManager.getDefaultSharedPreferences(
				applicationContext).getInt(EXPENSE_ITEM_ID, 0);
		return maxExpenseId;
	}
	
	public static void updateCheckListId(int maxId)
	{
		Context applicationContext = MainActivity.getContextOfApplication();
		PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
		.putInt(SharedUtil.CHECKLIST_ITEM_ID, maxId).commit();
	}
	
	public static void updateTourPlacesID(int maxId)
	{
		Context applicationContext = MainActivity.getContextOfApplication();
		PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
		.putInt(SharedUtil.TOUR_PLACES_ID, maxId).commit();
	}
	
	public static void updateExpenseId(int maxId)
	{
		Context applicationContext = MainActivity.getContextOfApplication();
		PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
		.putInt(SharedUtil.EXPENSE_ITEM_ID, maxId).commit();
	}
	

	
}
