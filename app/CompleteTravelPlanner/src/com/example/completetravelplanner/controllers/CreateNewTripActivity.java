package com.example.completetravelplanner.controllers;

import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.models.Trip;
import com.example.completetravelplanner.services.ServiceHandler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@SuppressLint("SimpleDateFormat")
public class CreateNewTripActivity extends Activity implements OnTouchListener,
		OnClickListener {
	private static final String LOGTAG = "CreateNewTripActivity";
	private Button btnCreateTrip, btnCancelTrip;
	EditText tripNameEdit, startDateEdit, endDateEdit;
	Trip newTrip;
	private int mYear, mMonth, mDay;
	final Calendar c = Calendar.getInstance();
	String TAG = "CompleteTravelPlanner";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Select display layout.
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_trip_screen);

		initView();
	}

	public void initView() {
		tripNameEdit = (EditText) findViewById(R.id.trip_name_et);
		startDateEdit = (EditText) findViewById(R.id.start_date_et);
		startDateEdit.setOnTouchListener(this);
		endDateEdit = (EditText) findViewById(R.id.end_date_et);
		endDateEdit.setOnTouchListener(this);
		btnCreateTrip = (Button) findViewById(R.id.createBtnID);
		btnCancelTrip = (Button) findViewById(R.id.cancelBtnID);
		btnCreateTrip.setOnClickListener(this);
		btnCancelTrip.setOnClickListener(this);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Log.d(LOGTAG, "User selected " + item.getTitle());
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	public void setDate(View v, MotionEvent event, final EditText dateEdit) {
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);

		// Launch Date Picker Dialog
		DatePickerDialog datePicker = new DatePickerDialog(this,
				new DatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						// Display Selected date in editBox
						dateEdit.setText((monthOfYear + 1) +"-"+ dayOfMonth +"-"+ year);
					}
				}, mYear, mMonth, mDay);
		datePicker.show();
	}

	/**
	 * Data to store data to database.
	 */
	public void persistTrip(View v) {
		Trip newTrip = createTrip();
		if (newTrip != null) {
			Log.i(LOGTAG, "Trip ID: " + newTrip.getTripId());
			Log.i(LOGTAG, "Trip Name: " + newTrip.getTripName());
			Log.i(LOGTAG, "Trip Start Date: " + newTrip.getStartDate());
			Log.i(LOGTAG, "Trip End Date: " + newTrip.getEndDate());
			showMessage("Success", "Trip has been created successfully.");
			this.finish();
		} else {
			showMessage("Opps!!",
					"There was a problem while creating the trip. \nPlease fill all the data.");
		}
	}

	/**
	 * This method displays the dialog box with the title and message
	 * 
	 * @param title
	 * @param message
	 */
	public void showMessage(String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton("OK", null);
		builder.show();
	}

	public Trip createTrip() {

		String tripTitleStr = tripNameEdit.getText().toString();
		String startDateStr = startDateEdit.getText().toString();
		String endDateStr = endDateEdit.getText().toString();

		if (tripTitleStr.length() > 0 && startDateStr.length() > 0
				&& endDateStr.length() > 0) {

			newTrip = new Trip(1, tripTitleStr, startDateStr, endDateStr);
			return newTrip;
		}
		return null;
	}

	public void cancelTripCreation(View v) {
		this.finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		View v = new View(this);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			cancelTripCreation(v);
		}
		return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (v == startDateEdit) {
				setDate(v, event, startDateEdit);
			} else if (v == endDateEdit) {
				setDate(v, event, endDateEdit);
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		if (v == btnCreateTrip) {
			Trip trip = createTrip();
			if (trip != null) {
				try {
					JSONObject object = new JSONObject();
					object.put("transaction_type", "TRIP_CREATION");
					object.put("start_date", trip.getStartDate());
					object.put("end_date", trip.getEndDate());
					object.put("trip_name", trip.getTripName());
					object.put("created_by", PreferenceManager.getDefaultSharedPreferences(
							getApplicationContext()).getString("telephone", null));
					new ServiceHandler(this).startServices(object.toString());
					this.finish();
				} catch (JSONException e) {
					e.printStackTrace();
				} 
			} else {
				showDialog("Opps!!",
						"There was a problem while creating the trip. \nPlease fill all the data.");
			}
		}
		if (v == btnCancelTrip) {
			cancelTripCreation();
		}
	}

	/**
	 * This method displays the dialog box with the title and message
	 * 
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

	public void cancelTripCreation() {

		this.finish();
	}
}