package com.example.completetravelplanner.controllers;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.adapter.TourPlacesAdapter;
import com.example.completetravelplanner.helper.DatabaseHelper;
import com.example.completetravelplanner.helper.SharedUtil;
import com.example.completetravelplanner.models.TourPlaces;
import com.example.completetravelplanner.services.ServiceHandler;

public class AddLocationActivity extends Activity implements OnClickListener, OnItemClickListener{
	
	Button addLocationBtn;
	public static final int MAP_RESPONCE = 2;
	private String location,itemId;
	private TourPlaces tourPlacesObj;
	ListView tourPlacesList;
	ArrayList<TourPlaces> tourPlacesdata; 
	TourPlacesAdapter tourPlacesAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_location_screen);
		
		initUI();
		fetchData();
	}

	private void fetchData() {

		DatabaseHelper dbHelper = new DatabaseHelper(AddLocationActivity.this);
		long tripId = SharedUtil.getActiveTripId();
		tourPlacesdata = dbHelper.getTourPlaces(tripId);
		if (tourPlacesdata != null) {

			tourPlacesAdapter = new TourPlacesAdapter(this,tourPlacesdata);
			tourPlacesList.setAdapter(tourPlacesAdapter);
			
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
		addLocationBtn = (Button) findViewById(R.id.addLoaction_ButtonID);
		
		addLocationBtn.setOnClickListener(AddLocationActivity.this);
		tourPlacesList = (ListView) findViewById(R.id.location_listID);
		tourPlacesList.setOnItemClickListener(this);
		
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		if (v == addLocationBtn) {
			Intent intent = new Intent(AddLocationActivity.this,MapViewActivity.class);
            startActivityForResult(intent, MAP_RESPONCE);
		}
	}
	
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
		
		switch (reqCode) {
                
        case (MAP_RESPONCE):
        	if (resultCode == Activity.RESULT_OK){
        		 if (data.hasExtra("city")) {
        		      //Toast.makeText(this, data.getExtras().getString("city"),Toast.LENGTH_SHORT).show();
        		      addLocation(data.getExtras().getString("city"));
        		      fetchData();
        		    }
        	}
		}	
	}
	private int getMaxTourPlacesId(){
		
		int tourPlacesId = SharedUtil.getMaxTourPlacesId();
		tourPlacesId = tourPlacesId+1;
		return tourPlacesId;
	}
	
	private TourPlaces getTourPlaces(String location)
 {
		long tripId = SharedUtil.getActiveTripId();
		String telephone = SharedUtil.getActiveTelephone();

		int tempItemId = getMaxTourPlacesId();
		this.itemId = telephone + "_" + tempItemId;
		this.location = location;
		SharedUtil.updateTourPlacesID(tempItemId);
		if (this.location.length() > 0) {

			tourPlacesObj = new TourPlaces();
			tourPlacesObj.setTripId(tripId);
			tourPlacesObj.setItemID(this.itemId);
			tourPlacesObj.setLocation(this.location);
			return tourPlacesObj;
		}
		// TODO Auto-generated method stub
		return null;
	}

	private void addLocation(String location) {
		TourPlaces tourPlaces = getTourPlaces(location);
		if (tourPlaces != null) {
			ServiceHandler handle = new ServiceHandler(this);
			JSONObject tourPlacesJSON = tourPlaces.toInsertJSON(tourPlaces);
			if(tourPlacesJSON!=null)
			{
				handle.startServices(tourPlacesJSON.toString());
			}	
			
//			DatabaseHelper dbHelper = new DatabaseHelper(AddLocationActivity.this);
//			long tourPlacesId = dbHelper.createTourPlaces(tourPlaces);
//			dbHelper.close();
//			return;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
	}

}
