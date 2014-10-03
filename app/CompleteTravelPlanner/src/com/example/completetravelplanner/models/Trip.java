package com.example.completetravelplanner.models;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.completetravelplanner.helper.SharedUtil;

import android.util.Log;

public class Trip{
	//Unique id which will be auto incremented every time a new trip is created
	private long tripId;
	//Name of the trip
	private String tripName;
	//Destination of the trip
	private String destination;
	//Start trip Date
	private String startDate;
	//End Trip Date
	private String endDate;

	public String getTripName(){
		return tripName;
	}
	public String getDestination(){
		return destination;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate(){
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public long getTripId(){
		return tripId;
	}
	public void setTripId(long tripId){
		this.tripId = tripId;
	}
	public void setTripName(String tripName){
		this.tripName = tripName;
	}
	public void setDestination(String destination){
		this.destination = destination;
	}
	
	@Override
	public String toString() {
		return tripName;
	}
	public Trip()
	{
		
	}
	
	public Trip(int tripId, String tripName, String startDate, String endDate){
		this.tripId = tripId;
		this.tripName = tripName;
		this.startDate = startDate;
		this.endDate = endDate;
		Log.i("In Trip constructor", "New trip id: " + tripId);
	}
	public JSONObject toDeleteJSON() {
		try{
			JSONObject object = new JSONObject();
	        object.put("transaction_type", "TRIP_DELETION");
	        object.put("trip_id", SharedUtil.getActiveTripId());
	        object.put("telephone", SharedUtil.getActiveTelephone());
        	return object;
		} catch(JSONException e) {
			e.printStackTrace();
		}
		return null;	
	}
}
