package com.example.completetravelplanner.controllers;

import java.util.ArrayList;
import java.util.List;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.adapter.CheckListAdapterPrivate;
import com.example.completetravelplanner.adapter.CheckListAdapterShared;
import com.example.completetravelplanner.helper.DatabaseHelper;
import com.example.completetravelplanner.helper.SharedUtil;
import com.example.completetravelplanner.models.CheckList;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;


public class ChecklistActivity extends Activity implements OnClickListener,OnItemClickListener {
	
	LinearLayout checklistBtnLinear, privateBtnLinear, sharedBtnLinear;
	Button addBtn;
	ListView checkList;
	ArrayList<CheckList> checkListData; 
	CheckListAdapterPrivate adapterPrivate;
	CheckListAdapterShared adapterShared;
	private static String CHECKLIST_PRIVATE= "Private";
	private static String CHECKLIST_SHARED= "Shared";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checklist_screen);
		
		initUI();
		fetchdata(CHECKLIST_PRIVATE);
	}

	void fetchdata(String checkListType) {

		DatabaseHelper dbHelper = new DatabaseHelper(ChecklistActivity.this);
		long tripId = SharedUtil.getActiveTripId();
		checkListData = dbHelper.getCheckList(checkListType,tripId);
		if (checkListData != null) {

			if(checkListType!=null && checkListType.equalsIgnoreCase(CHECKLIST_PRIVATE))
			{
				adapterPrivate = new CheckListAdapterPrivate(this,checkListData);
				checkList.setAdapter(adapterPrivate);
			}else
			{
				adapterShared = new CheckListAdapterShared(this,checkListData);
				checkList.setAdapter(adapterShared);
			}

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
		checklistBtnLinear = (LinearLayout) findViewById(R.id.checklist_Button_linearID);
		privateBtnLinear = (LinearLayout) findViewById(R.id.checklist_privateBtn_LinearID);
		sharedBtnLinear = (LinearLayout) findViewById(R.id.checklist_sharedBtn_LinearID);
		addBtn = (Button) findViewById(R.id.checklist_addBtnID);
		checkList = (ListView) findViewById(R.id.checklist_listID);
		checkList.setOnItemClickListener(this);
		addBtn.setOnClickListener(ChecklistActivity.this);
		privateBtnLinear.setOnClickListener(this);
		sharedBtnLinear.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		if (v == addBtn) {
			
			Intent manageMembersIntent = new Intent(this, AddChecklistActivity.class);
			startActivity(manageMembersIntent);
			this.finish();
		}
		if(v == privateBtnLinear)
		{
			//Toast.makeText(ChecklistActivity.this, "Comming soon.", Toast.LENGTH_SHORT).show();
			checklistBtnLinear.setBackgroundResource(R.drawable.private_btn);
			fetchdata(CHECKLIST_PRIVATE);
			
		}
		if(v == sharedBtnLinear)
		{
			//Toast.makeText(ChecklistActivity.this, "Comming soon.", Toast.LENGTH_SHORT).show();
			checklistBtnLinear.setBackgroundResource(R.drawable.shared_btn);
			fetchdata(CHECKLIST_SHARED);
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
	}

}
