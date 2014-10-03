package com.example.completetravelplanner.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.helper.DatabaseHelper;
import com.example.completetravelplanner.helper.SharedUtil;
import com.example.completetravelplanner.models.CheckList;
import com.example.completetravelplanner.models.Person;
import com.example.completetravelplanner.services.ServiceHandler;
import com.google.android.gms.internal.db;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class AddChecklistActivity extends Activity implements OnClickListener{
	
	private EditText itemNameEditText, quantityEditText, typeEditText, assignedToEditText;
	private String itemId, itemName, itemQuantity, itemType, assignedTo, addedBy, status, reminder; 
	Spinner typeSpinner, assignedToSpinner;
	CheckList objCheckList;
	LinearLayout editTextLayout;
	private static String CHECKLIST_PRIVATE= "Private";
	private static String CHECKLIST_SHARED= "Shared";
	private Button addBtn, cancelBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_checklist_screen);
		
		initUI();
	}
	
	private void initUI() {

		itemNameEditText = (EditText) findViewById(R.id.itenNameEditTextID);
		quantityEditText = (EditText) findViewById(R.id.quantityEditTextID);
		//typeEditText = (EditText) findViewById(R.id.typeEditTextID);
		editTextLayout = (LinearLayout) findViewById(R.id.assignedToEditTextLayoutID);
		editTextLayout.setVisibility(View.GONE);
		assignedToSpinner = (Spinner) findViewById(R.id.assignedToEditTextID);
		addItemsOnAssignedToSpinner();
		assignedToSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				assignedTo = parent.getItemAtPosition(position).toString();
					
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
			
		});
		//assignedToEditText = (EditText) findViewById(R.id.assignedToEditTextID);
		typeSpinner = (Spinner) findViewById(R.id.spinnerType);
		typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				itemType = parent.getItemAtPosition(position).toString();
				if(itemType!=null && itemType.equalsIgnoreCase(CHECKLIST_PRIVATE))
				{
					editTextLayout.setVisibility(View.GONE);
				}else
				{
					editTextLayout.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		addBtn = (Button) findViewById(R.id.addNewChecklistBtnID);
		cancelBtn = (Button) findViewById(R.id.cancelBtnID);

		addBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
	}

	private void addItemsOnAssignedToSpinner() {
		Context applicationContext = MainActivity.getContextOfApplication();
		DatabaseHelper db = new DatabaseHelper(applicationContext);
		long tripId = SharedUtil.getActiveTripId();
		ArrayList<Person> list  = db.getMembersList(tripId);
		List<String> memberList = new ArrayList<String>();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Person person = (Person) iterator.next();
			memberList.add(person.getName());
		}
		String username = SharedUtil.getCurrentUser(applicationContext);
		memberList.add(username);
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, memberList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		assignedToSpinner.setAdapter(dataAdapter);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == addBtn) {
			Boolean isSuccess = createCheckList();
			if (!isSuccess) {
				// Show dialog
				showDialog("Opps!!",
						"There was a problem while creating the Checklist. \nPlease fill all the data.");
			} else{			
				Intent checkListIntent = new Intent(this, ChecklistActivity.class);
				startActivity(checkListIntent);
				this.finish();
			}
		}
		if (v == cancelBtn) {
			this.finish();
		}
	}
	
	private Boolean createCheckList() {
		CheckList checkList = getCheckList();

		if (checkList != null) {
			
			if(checkList.getCheckListType()!=null && checkList.getCheckListType().equalsIgnoreCase(CHECKLIST_SHARED))
			{
				ServiceHandler handle = new ServiceHandler(this);
				JSONObject checklistJSON = checkList.toInsertJSON(checkList);
				if(checklistJSON!=null)
				{
					handle.startServices(checklistJSON.toString());
					return true;
				}	
			}else
			{
				DatabaseHelper dbHelper = new DatabaseHelper(AddChecklistActivity.this);
				long checkListID = dbHelper.createCheckList(checkList);
				dbHelper.close();
				return true;
			}
			return false;
		}
		return false;
	}

	private int getMaxCheckListId(){
		
		int checkListid = SharedUtil.getMaxCheckListID();
		checkListid = checkListid+1;
		return checkListid;
	}
	
	private CheckList getCheckList() {

		long tripId = SharedUtil.getActiveTripId();
		String telephone = SharedUtil.getActiveTelephone();
		String userName = SharedUtil.getCurrentUser(this);

		int tempItemId = getMaxCheckListId();
		this.itemName = itemNameEditText.getText().toString();
		this.itemQuantity = quantityEditText.getText().toString();
		//this.assignedTo = assignedToEditText.getText().toString();
		this.itemId = telephone + "_" + tempItemId;
		SharedUtil.updateCheckListId(tempItemId);
		if (itemType != null && itemType.equalsIgnoreCase(CHECKLIST_SHARED)) {
			if (itemName.length() > 0 && itemQuantity.length() > 0
					&& itemType.length() > 0 && assignedTo.length() > 0) {

				objCheckList = new CheckList();
				objCheckList.setTripId(tripId);
				objCheckList.setItemId(this.itemId);
				objCheckList.setItemName(this.itemName);
				objCheckList.setAddedBy(userName);
				objCheckList.setStatus("open");
				objCheckList.setQuantity(this.itemQuantity);
				objCheckList.setCheckListType(this.itemType);
				objCheckList.setAssignedTo(this.assignedTo);
				
				return objCheckList;
			}
		} else if (itemType != null
				&& itemType.equalsIgnoreCase(CHECKLIST_PRIVATE)) {
			if (itemName.length() > 0 && itemQuantity.length() > 0
					&& itemType.length() > 0) {

				objCheckList = new CheckList();
				objCheckList.setTripId(tripId);
				objCheckList.setItemId(this.itemId);
				objCheckList.setItemName(this.itemName);
				objCheckList.setAddedBy(userName);
				objCheckList.setStatus("open");
				objCheckList.setQuantity(this.itemQuantity);
				objCheckList.setCheckListType(this.itemType);

				return objCheckList;
			}
		}
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This method displays the dialog box with the title and message 
	 * @param title
	 * @param message
	 */
	public void showDialog(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton("OK", null);
		builder.show();
	}
	

}
