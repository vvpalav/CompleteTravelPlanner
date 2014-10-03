package com.example.completetravelplanner.controllers;

import com.example.completetravelplanner.R;
import android.os.Bundle;
import android.R.id;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HomeScreenActivity extends Activity implements OnClickListener {
	private Button createTourBtn, manageExistingBtn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_screen);
		Log.i("somekey","somemsg");
		initUI();
	}

	private void initUI() {
		// TODO Auto-generated method stub
		
		createTourBtn = (Button) findViewById(R.id.create_tourBtnID);
		manageExistingBtn = (Button) findViewById(R.id.manage_existing_tripBtnID);
		manageExistingBtn.setOnClickListener(this);
		createTourBtn.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home_screen, menu);
		return true;
	}

	/**
	 * Receive result from CreateTripActivity here.
	 * Can be used to save instance of Trip object
	 * which can be viewed in the ViewTripActivity.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO - fill in here
		
		if (resultCode == RESULT_OK && requestCode == 1){
			
			showDialog("Congratulations!!", "Your Tour is scuessfully created.");
			// tour created
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == createTourBtn) {
			Intent intent = new Intent(HomeScreenActivity.this, CreateNewTripActivity.class);
			startActivityForResult(intent,1);
			
		}
		if (v == manageExistingBtn) {
			
			Intent intent = new Intent(HomeScreenActivity.this, TripHistoryActivity.class);
			startActivity(intent);
		}
		
	}
	
	public void showDialog(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton("OK", null);
		builder.show();
	}

}