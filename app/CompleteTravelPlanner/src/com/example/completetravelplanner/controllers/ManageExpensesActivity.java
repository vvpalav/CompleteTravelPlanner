package com.example.completetravelplanner.controllers;

import java.util.ArrayList;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.adapter.TripExpensesAdapter;
import com.example.completetravelplanner.helper.DatabaseHelper;
import com.example.completetravelplanner.helper.SharedUtil;
import com.example.completetravelplanner.models.Expenses;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

public class ManageExpensesActivity extends Activity{
	private static final String LOGTAG = "ManageExpensesActivity";
	private ListView expensesList;
	private ArrayList<Expenses> tripExpensesData = new ArrayList<Expenses>();
	private TripExpensesAdapter adapter;
	public static EditText userPaid, userShare, totalExpenses;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_expenses);
		setTitle("Manage Expenses");
		initUI();
		fetchdata();
	}
	private void initUI() {
		expensesList = (ListView) findViewById(R.id.expenses_history_listID);
		userPaid = (EditText) findViewById(R.id.et_user_total_paid);
		userShare = (EditText) findViewById(R.id.et_user_total_share);
		totalExpenses = (EditText) findViewById(R.id.et_total_expenses);
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
		builder.show();
	}
	
	public void populateSummary(){
		float totalExpenses = 0, userShare = 0, userPaid = 0, individualShare = 0;
		long tripId = SharedUtil.getActiveTripId();
		DatabaseHelper dbh = new DatabaseHelper(this);
		int numberOfMembers = dbh.getNumberOfMembers(tripId);
		String userShareStr;
		String user = SharedUtil.getCurrentUser(this);
		for(int i = 0; i < tripExpensesData.size(); i++){
			totalExpenses += tripExpensesData.get(i).getAmount();
			if(tripExpensesData.get(i).getAddedBy().equals(user)){
				userPaid += tripExpensesData.get(i).getAmount();
			}
		}
		individualShare = totalExpenses/numberOfMembers;
		if((individualShare - userPaid) >= 0){
			userShare = individualShare - userPaid;
			userShareStr = "You Owe\n";
		}
		else
		{
			userShare = userPaid - individualShare;
			userShareStr = "You Lent\n";
		}
		
		ManageExpensesActivity.userPaid.setText("You Paid\n" + userPaid);
		ManageExpensesActivity.userShare.setText(userShareStr + userShare);
		ManageExpensesActivity.totalExpenses.setText("Total\n" + totalExpenses);
	}
	
	/**
	 * This method invoked the TripHistoryAdaptor which calls the layoutInflator to populate the list of trips.
	 */
	void fetchdata() {
		DatabaseHelper dbh = new DatabaseHelper(this);
		long tripId = SharedUtil.getActiveTripId();
		tripExpensesData = dbh.getExpensesList(tripId);
		
		if (tripExpensesData != null) {
			populateSummary();
			adapter = new TripExpensesAdapter(this, tripExpensesData);
			expensesList.setAdapter(adapter);
		} else {
			showMessage("No data!!", "No expenses added yet.");
		}
	}

	public void addItem(View v){
		Intent addItemIntent = new Intent(this, AddItemActivity.class);
		startActivityForResult(addItemIntent, 1);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK && requestCode == 1){
			initUI();
			fetchdata();
		}
	}
	@Override
	public void onBackPressed() {
		if (adapter!=null) {
			if (adapter.popupWindow!=null) {
				adapter.popupWindow.dismiss();
			}
		}else{
			ManageExpensesActivity.this.finish();
		}
		super.onBackPressed();
		ManageExpensesActivity.this.finish();
	}	
}
