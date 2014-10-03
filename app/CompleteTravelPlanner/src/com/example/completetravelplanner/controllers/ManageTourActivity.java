package com.example.completetravelplanner.controllers;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.cloudsharing.CloudSharingActivity;
import com.example.completetravelplanner.helper.SharedUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class ManageTourActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_tour_screen);
	}
	
	public void manageMembers(View v){
		Intent manageMembersIntent = new Intent(this, ManageMembersActivity.class);
		startActivity(manageMembersIntent);
	}
	
	public void manageExpenses(View v){
		Intent manageExpensesIntent = new Intent(this, ManageExpensesActivity.class);
		startActivity(manageExpensesIntent);
	}

	public void manageTour(View v){
		Intent manageTourIntent = new Intent(this, AddLocationActivity.class);
		startActivity(manageTourIntent);
	}
	public void manageChecklist(View v){
		Intent manageChecklistIntent = new Intent(this, ChecklistActivity.class);
		startActivity(manageChecklistIntent);
	}
	public void managePhotoSharing(View v){
		Intent managePhotoSharing = new Intent(this, CloudSharingActivity.class);
		startActivity(managePhotoSharing);
	}
	public void manageChat(View v){
		Toast.makeText(ManageTourActivity.this, "Coming soon...", Toast.LENGTH_SHORT).show();
	}
	@Override
	public void onBackPressed()
	{
		 SharedPreferences sharedPreferences_put = getSharedPreferences(SharedUtil.TRIP_ID, Activity.MODE_PRIVATE);
		 SharedPreferences.Editor editor = sharedPreferences_put.edit();
		 editor.putLong(SharedUtil.TRIP_ID, 0);
		 this.finish();
	} 
}
