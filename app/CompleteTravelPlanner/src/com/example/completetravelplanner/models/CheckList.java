package com.example.completetravelplanner.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.preference.PreferenceManager;

import com.example.completetravelplanner.controllers.MainActivity;
import com.example.completetravelplanner.helper.DatabaseHelper;
import com.example.completetravelplanner.helper.SharedUtil;

public class CheckList {

	private long tripId;
	
	
	public long getTripId() {
		return tripId;
	}

	public void setTripId(long tripId) {
		this.tripId = tripId;
	}

	private String itemId;
	
	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	private String itemName;
	
	private String checkListType;
	
	private String quantity;
	
	private String assignedTo;
	
	private String addedBy;
	
	public String getAddedBy() {
		return addedBy;
	}

	public void setAddedBy(String addedBy) {
		this.addedBy = addedBy;
	}

	
	private String status;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	
	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getCheckListType() {
		return checkListType;
	}

	public void setCheckListType(String checkListType) {
		this.checkListType = checkListType;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}
	
	
	public JSONObject toInsertJSON(CheckList checkList){
		
		try{
			JSONObject object = new JSONObject();
	        object.put("transaction_type", "MODIFICATION_PUSH");
	        object.put("action", "INSERT");
	        object.put("tablename", "checklist");
	        
	        String telephone = SharedUtil.getActiveTelephone();
	        long activeTripId = SharedUtil.getActiveTripId();
	        object.put("trip_id", activeTripId);
	        object.put("telephone", telephone);
	        
	        JSONArray array = new JSONArray();
	        array.put(0, new JSONObject().put("column", DatabaseHelper.KEY_TRIP_ID)
	                .put("value", checkList.getTripId()));
	        array.put(1, new JSONObject().put("column", DatabaseHelper.KEY_ITEM_ID)
	                .put("value", checkList.getItemId()));
	        array.put(2, new JSONObject().put("column", DatabaseHelper.KEY_ITEM_NAME)
	                .put("value", checkList.getItemName()));
	        array.put(3, new JSONObject().put("column", DatabaseHelper.KEY_QUANTITY)
	                .put("value",checkList.getQuantity()));
	        array.put(4, new JSONObject().put("column", DatabaseHelper.KEY_TYPE)
	                .put("value", checkList.getCheckListType()));
	        array.put(5, new JSONObject().put("column", DatabaseHelper.KEY_ASSIGNED_TO)
	                .put("value", checkList.getAssignedTo()));
	        array.put(6, new JSONObject().put("column", DatabaseHelper.KEY_ADDED_BY)
	                .put("value", checkList.getAddedBy()));
	        array.put(7, new JSONObject().put("column", DatabaseHelper.KEY_STATUS)
	                .put("value", checkList.getStatus()));
	       
	        object.put("data", array);
        	return object;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;	
	}

	public JSONObject toDeleteJSON(long tripId, String itemId) {

		try{
			JSONObject object = new JSONObject();
	        object.put("transaction_type", "MODIFICATION_PUSH");
	        object.put("action", "DELETE");
	        object.put("tablename", DatabaseHelper.TABLE_CHECKLIST);
	       
	        String telephone = SharedUtil.getActiveTelephone();
	        long activeTripId = SharedUtil.getActiveTripId();
	        object.put("trip_id", activeTripId);
	        object.put("telephone", telephone);
	        
	        JSONArray array = new JSONArray();
	        array.put(0, new JSONObject().put("column", DatabaseHelper.KEY_TRIP_ID)
	                .put("value", tripId));
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
	
	public JSONObject toUpdateJSON(String status, String itemId) {
		JSONObject object = new JSONObject();
		try {
			object.put("transaction_type", "MODIFICATION_PUSH");

			object.put("action", "UPDATE");
			object.put("tablename", DatabaseHelper.TABLE_CHECKLIST);
			String telephone = SharedUtil.getActiveTelephone();
			long activeTripId = SharedUtil.getActiveTripId();

			object.put("trip_id", activeTripId);
			object.put("telephone", telephone);

			JSONArray array = new JSONArray();
			array.put(0,
					new JSONObject().put("column", DatabaseHelper.KEY_STATUS)
							.put("value", status));

			JSONArray array2 = new JSONArray();
			array2.put(0,
					new JSONObject().put("column", DatabaseHelper.KEY_TRIP_ID)
							.put("value", activeTripId));
			array2.put(1,
					new JSONObject().put("column", DatabaseHelper.KEY_ITEM_ID)
							.put("value", itemId));

			object.put("data", array);
			object.put("where", array2);
			System.out.println(object.toString());
			return object;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

}
