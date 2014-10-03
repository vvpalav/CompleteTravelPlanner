package com.example.completetravelplanner.models;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.completetravelplanner.controllers.MainActivity;
import com.example.completetravelplanner.helper.DatabaseHelper;
import com.example.completetravelplanner.helper.SharedUtil;

import android.content.Context;
import android.net.Uri;

public class Person {
	private long personId;
	// Name of the user
	private String name;
	// To store users phone number
	private long phoneNumber;
	// To store users email id
	private String emailId;

	public long getPersonId() {
		return personId;
	}

	public void setPersonId(long personId) {
		this.personId = personId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPhoneNumber(long phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	// To store URI of person's profile picture.
	Uri personImageURI;

	public Uri getPersonImageURI() {
		return personImageURI;
	}

	public Person() {

	}

	public String getName() {
		return name;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public long getPhoneNumber() {
		return phoneNumber;
	}

	public JSONObject toInsertJSON(Person person) {

		try {
			Context context = MainActivity.getContextOfApplication();
			JSONObject object = new JSONObject();
			object.put("transaction_type", "TRIP_ADD_MEMBER");
			long activeTripId = SharedUtil.getActiveTripId();
			object.put("trip_id", activeTripId);
			object.put("telephone", person.getPhoneNumber());
			object.put("added_by", SharedUtil.getActiveTelephone());
			return object;
		} catch (Exception e) {

		}
		return null;
	}



	public static JSONObject toDeleteMemberJSON(long phoneNumber){

		try{
			JSONObject object = new JSONObject();
			object.put("transaction_type", "MODIFICATION_PUSH");
			object.put("action", "DELETE");
			object.put("tablename", DatabaseHelper.TABLE_TRIP_MEMBERS);
			String telephone = SharedUtil.getActiveTelephone();
			long activeTripId = SharedUtil.getActiveTripId();
			object.put("trip_id", activeTripId);
			object.put("telephone", telephone);

			JSONArray array = new JSONArray();
			array.put(0, new JSONObject().put("column", DatabaseHelper.KEY_TRIP_ID)
					.put("value", activeTripId));
			array.put(1, new JSONObject().put("column", DatabaseHelper.KEY_TELEPHONE)
					.put("value", phoneNumber));
			object.put("data", array);
			return object;
		}catch(Exception e)
		{

		}
		return null;	
	}


	@Override
	public String toString() {
		return name;
	}

	/**
	 * Create a Person model object from arguments
	 */
	public Person(String name, long phoneNumber, Uri personImageURI,
			String emailId) {
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.personImageURI = personImageURI;
		this.emailId = emailId;
	}

	public Person(String name, long phoneNumber, String emailId) {
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.emailId = emailId;
	}

}
