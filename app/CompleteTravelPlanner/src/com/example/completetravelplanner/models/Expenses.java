package com.example.completetravelplanner.models;


import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.completetravelplanner.helper.DatabaseHelper;
import com.example.completetravelplanner.helper.SharedUtil;

public class Expenses {

	private String itemId;
	private long tripId;
	private String itemName;
	private float amount;
	private String addedBy;
	private Date addedOn;
	
	public String getItemId() {
		return itemId;
	}
	public long getTripId(){
		return tripId;
	}
	public String getItemName() {
		return itemName;
	}
	public float getAmount() {
		return amount;
	}
	public String getAddedBy() {
		return addedBy;
	}
	public Date getAddedOn() {
		return addedOn;
	}
	
	public JSONObject toInsertJSON(Expenses expenseList){
		
		try{
		JSONObject object = new JSONObject();
        object.put("transaction_type", "MODIFICATION_PUSH");
        object.put("action", "INSERT");
        object.put("tablename", "expenses");
        String telephone = SharedUtil.getActiveTelephone();
        long activeTripId = SharedUtil.getActiveTripId();
        object.put("trip_id", activeTripId);
        object.put("telephone", telephone);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Date now = expenseList.getAddedOn();
	    String strDate = sdfDate.format(now);
        
        JSONArray array = new JSONArray();
        array.put(0, new JSONObject().put("column", DatabaseHelper.KEY_TRIP_ID)
                .put("value", expenseList.getTripId()));
        array.put(1, new JSONObject().put("column", DatabaseHelper.KEY_ITEM_NAME)
                .put("value", expenseList.getItemName()));
        array.put(2, new JSONObject().put("column", DatabaseHelper.KEY_AMOUNT)
                .put("value",expenseList.getAmount()));
        array.put(3, new JSONObject().put("column", DatabaseHelper.KEY_ADDED_ON)
                .put("value", strDate));
        array.put(4, new JSONObject().put("column", DatabaseHelper.KEY_ADDED_BY)
                .put("value", expenseList.getAddedBy()));
        array.put(5, new JSONObject().put("column", DatabaseHelper.KEY_ITEM_ID)
                .put("value", expenseList.getItemId()));

        object.put("data", array);
    	return object;
	}catch(Exception e)
	{
		
	}
	return null;	
}
	
	public static JSONObject toDeleteExpenseJSON(String expenseId){

		try{
			JSONObject object = new JSONObject();
			object.put("transaction_type", "MODIFICATION_PUSH");
			object.put("action", "DELETE");
			object.put("tablename", DatabaseHelper.TABLE_EXPENSES);
			String telephone = SharedUtil.getActiveTelephone();
			long activeTripId = SharedUtil.getActiveTripId();
			object.put("trip_id", activeTripId);
			object.put("telephone", telephone);

			JSONArray array = new JSONArray();
			array.put(0, new JSONObject().put("column", DatabaseHelper.KEY_ITEM_ID)
					.put("value", expenseId));
			object.put("data", array);
			return object;
		}catch(Exception e)
		{

		}
		return null;	
	}

	public Expenses(String itemId, long tripId, String itemName, float amount, String addedBy, Date addedOn){
		this.itemId = itemId;
		this.tripId = tripId;
		this.itemName = itemName;
		this.amount = amount;
		this.addedBy = addedBy;
		this.addedOn = addedOn;
	}
}
