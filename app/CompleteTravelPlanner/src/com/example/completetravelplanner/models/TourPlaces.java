package com.example.completetravelplanner.models;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.completetravelplanner.helper.DatabaseHelper;
import com.example.completetravelplanner.helper.SharedUtil;

public class TourPlaces {
	
	private String destination;
	private String location;
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	private long tripId;	
	
	public long getTripId() {
		return tripId;
	}

	public void setTripId(long tripId) {
		this.tripId = tripId;
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	private String itemID;

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public JSONObject toInsertJSON(TourPlaces tourPlaces) {
		
		try{
			JSONObject object = new JSONObject();
	        object.put("transaction_type", "MODIFICATION_PUSH");
	        object.put("action", "INSERT");
	        object.put("tablename", DatabaseHelper.TABLE_BEST_PLACES);
	        
	        String telephone = SharedUtil.getActiveTelephone();
	        long activeTripId = SharedUtil.getActiveTripId();
	        object.put("trip_id", activeTripId);
	        object.put("telephone", telephone);
	        
	        JSONArray array = new JSONArray();
	        array.put(0, new JSONObject().put("column", DatabaseHelper.KEY_TRIP_ID)
	                .put("value", tourPlaces.getTripId()));
	        array.put(1, new JSONObject().put("column", DatabaseHelper.KEY_ITEM_ID)
	                .put("value", tourPlaces.getItemID()));
	        array.put(2, new JSONObject().put("column", DatabaseHelper.KEY_lOCATION)
	                .put("value", tourPlaces.getLocation()));
	        
	        object.put("data", array);
        	return object;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;	
	}
	
	public JSONObject toDeleteJSON(String itemId) {

		try{
			JSONObject object = new JSONObject();
	        object.put("transaction_type", "MODIFICATION_PUSH");
	        object.put("action", "DELETE");
	        object.put("tablename", DatabaseHelper.TABLE_BEST_PLACES);
	       
	        String telephone = SharedUtil.getActiveTelephone();
	        long activeTripId = SharedUtil.getActiveTripId();
	        object.put("trip_id", activeTripId);
	        object.put("telephone", telephone);
	        
	        JSONArray array = new JSONArray();
	        array.put(0, new JSONObject().put("column", DatabaseHelper.KEY_TRIP_ID)
	                .put("value", activeTripId));
	        array.put(1, new JSONObject().put("column", DatabaseHelper.KEY_ITEM_ID)
	                .put("value", itemId));
	       
	        object.put("data", array);
        	return object;
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;	
	}

}
