package com.example.completetravelplanner.controllers;

import java.util.ArrayList;

import org.json.JSONObject;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.adapter.TripMembersAdapter;
import com.example.completetravelplanner.helper.DatabaseHelper;
import com.example.completetravelplanner.helper.SharedUtil;
import com.example.completetravelplanner.models.Person;
import com.example.completetravelplanner.services.ServiceHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ManageMembersActivity extends Activity implements OnTouchListener{
	private static final String LOGTAG = "ManageMembersActivity";
	public static final int PICK_CONTACT = 1;
	private ListView membersList;
	private ArrayList<Person> tripMembersData = new ArrayList<Person>();
	private TripMembersAdapter adapter;
	private Button addMember;
	private Uri contactURI;
	private String contactID;
	private Person member;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_friends);
		initUI();
		fetchdata();
	}

	private void initUI() {

		membersList = (ListView) findViewById(R.id.trip_history_listID);
		addMember = (Button) findViewById(R.id.add_member_et);
		addMember.setOnTouchListener(this);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		 if(event.getAction() == MotionEvent.ACTION_DOWN) {
			 if (v == addMember) {
					
					Intent intent = new Intent(Intent.ACTION_PICK,Contacts.CONTENT_URI);
	                startActivityForResult(intent, PICK_CONTACT);
					fetchdata();
	                //this.finish();
				}
		 }
		return false;
	}
	
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
		
		switch (reqCode) {
        case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                		contactURI = data.getData();
                        
                        // get the contact ID
                        Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
                        cursor.moveToFirst();
	                        try {
	                        	 contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
							} catch (Exception e) {
								// TODO: handle exception
								contactID = null;
							}
	                       
	                        cursor.close();
	                        
	                        Log.d(LOGTAG, "Got contactID: " + contactID);
	                        if(contactID!=null)
	                        {
	                        	getContactInfo();
	                        	
	                        	//this.finish();
	                        }
	                        else
	                        	showMessage("Error", "Invalid Contact!!!");
                        
                }
                break;
		}
	}

	/**
	 * This method gets all the contact information from the contacts 
	 * list using the cursor and populating it in the PersonBeanClass.
	 * Also it stores the person record in Person Table.
	 */
	private void getContactInfo() {
		
		String contactName = null, contactNumber = null;
		
		Cursor cursorNumber = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactID,null, null);
        if (cursorNumber.moveToFirst()) {
        	contactNumber=cursorNumber.getString(cursorNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            Log.d(LOGTAG, "Got Number: " +contactNumber);
        }
        cursorNumber.close();
        Context context = getApplicationContext();
        DatabaseHelper dbh = new DatabaseHelper(context);
        contactNumber = SharedUtil.getPhoneNumber(contactNumber) + "";
        String userSelected = dbh.checkIfUserExists(contactNumber);
        if(contactNumber.equals(userSelected)){
        	showMessage("User Exists!!", "This member already exists in this trip.");
        }
        else{
			Cursor cursorName =  getContentResolver().query(contactURI, null, null, null, null);
	        if (cursorName.moveToFirst()) {
	          contactName = cursorName.getString(cursorName.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	          Log.d(LOGTAG, "Got contactName: " + contactName);
	        }
	        cursorName.close();
	        
	        String emailId = "nnm256@nyu.edu";
			Uri photoUri = getPhotoUri();
			updatedPeople(contactName.trim(), contactNumber, photoUri, emailId);
        }
		addMember.setText("");
	}
	
	/**
	 * This method fetched the image from the contact selected from the contacts list.
	 * @return
	 */
	public Uri getPhotoUri() {
        Uri person = ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactID));
        Uri photo = Uri.withAppendedPath(person,
                ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

        Cursor cur = ManageMembersActivity.this.getContentResolver().query(ContactsContract.Data.CONTENT_URI,null,
                        ContactsContract.Data.CONTACT_ID+ "="+ contactID+ " AND "+ ContactsContract.Data.MIMETYPE+ "='"
                                + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE+ "'", null, null);
        if (cur != null) {
            if (!cur.moveToFirst()) {
                return null; // no photo
            }
        } else {
            return null; // error in cursor process
        }
        return photo;
    }

	/**
	 * This method populated the PersonBeanClass
	 * @param contactName
	 * @param contactNumber
	 * @param contactEmail
	 * @param photoUri
	 */
	private void updatedPeople(String contactName,
			String contactNumber, Uri photoUri, String emailId) {
		long numb = SharedUtil.getPhoneNumber(contactNumber);
		
		if(contactName == null)
			contactName = "-";
			
		member = new Person(contactName, numb, photoUri, emailId);
		if (member != null) {
			ServiceHandler handle = new ServiceHandler(this);
			JSONObject membersJSON = member.toInsertJSON(member);
			
			if(membersJSON!=null)
			{
				handle.startServices(membersJSON.toString());				
			}
			else{
				//return null;
			}
		}
		this.finish();
	}

	/**
	 * This method displays the dialog box with the title and message 
	 * @param title
	 * @param message
	 */
	public void showMessage(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton("OK", null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	/**
	 * This method invoked the TripHistoryAdaptor which calls the layoutInflator to populate the list of trips.
	 */
	void fetchdata() {

		/*Person p1 = new Person("Ashish", "+1 347 232 3454", "ashish@as.com");
		Person p2 = new Person("Pooja", "+1 347 435 6765", "poojupatil@gmail.com");
		Person p3 = new Person("Ninad", "+1 347 345 5677", "nadscloud@gmail.com");
		tripMembersData.add(p1);
		tripMembersData.add(p2);
		tripMembersData.add(p3);*/
		DatabaseHelper dbh = new DatabaseHelper(this);
		long activeTripId = SharedUtil.getActiveTripId();
		/*ArrayList<String> phoneNumbers = dbh.getMembersList(activeTripId);
		for(String ph:phoneNumbers){
			Person p = dbh.getPerson(ph);
		*/	
		tripMembersData = dbh.getMembersList(activeTripId);
		
	//}
		
		
		if (tripMembersData != null) {

			adapter = new TripMembersAdapter(this, tripMembersData);
			membersList.setAdapter(adapter);

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

	@Override
	public void onBackPressed() {
		if (adapter!=null) {
			if (adapter.popupWindow!=null) {
				adapter.popupWindow.dismiss();
			}
		}
		else
		{
			ManageMembersActivity.this.finish();
		}
		super.onBackPressed();
	}	
}
