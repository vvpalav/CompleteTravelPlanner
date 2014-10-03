package com.example.completetravelplanner.controllers;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.adapter.TripHistoryAdapter;
import com.example.completetravelplanner.helper.DatabaseHelper;
import com.example.completetravelplanner.helper.SharedUtil;
import com.example.completetravelplanner.models.Trip;



public class TripHistoryActivity extends Activity implements OnItemClickListener {
	
	ListView tripHistoryList;
	ArrayList<Trip> tripListData;
	TripHistoryAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.triphistory_screen);
		
		initUI();
		fetchdata();
	}

	void fetchdata() {

		DatabaseHelper dbHelper = new DatabaseHelper(TripHistoryActivity.this);
		tripListData = dbHelper.getTripList();
		
		/*for(int i=1;i<3;i++)
		{
			Trip trip = new Trip();
			trip.setTripName("Summer Vacation");
			trip.setStartDate("03-Mar-2014");
			trip.setEndDate("15-Mar-2013");
			tripListData.add(trip);
		}*/
		if (tripListData != null) {

			adapter = new TripHistoryAdapter(this,tripListData);
			tripHistoryList.setAdapter(adapter);

		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("null data");
			builder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
			builder.show();
		}
	}
	
	private void initUI() {
		// TODO Auto-generated method stub
		tripHistoryList = (ListView) findViewById(R.id.trip_history_listID);
		tripHistoryList.setOnItemClickListener(this);
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Trip localTripListData = tripListData.get(position);
		Context applicationContext = MainActivity.getContextOfApplication();
		PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
			.putLong(SharedUtil.TRIP_ID, localTripListData.getTripId()).commit();
		
		Intent intent = new Intent(TripHistoryActivity.this,ManageTourActivity.class);
		startActivity(intent);

	}
}
