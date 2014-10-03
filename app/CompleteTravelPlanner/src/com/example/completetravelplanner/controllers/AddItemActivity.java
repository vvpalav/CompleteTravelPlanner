package com.example.completetravelplanner.controllers;

import java.util.Date;

import org.json.JSONObject;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.helper.DatabaseHelper;
import com.example.completetravelplanner.helper.SharedUtil;
import com.example.completetravelplanner.models.Expenses;
import com.example.completetravelplanner.services.ServiceHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class AddItemActivity extends Activity{
	private static final String LOGTAG = "AddItemActivity";
	//private static final String TELEPHONE_NUMBER = "telephone";
	EditText itemNameEdit, itemAmountEdit;
	Expenses newExpenseItem;
	Context context;
	final ServiceHandler hand = new ServiceHandler(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_item);
		setTitle("Add Expense");
		initView();		
	}

	public void initView(){
		itemNameEdit = (EditText) findViewById(R.id.item_name_et);
		itemAmountEdit = (EditText) findViewById(R.id.amount_paid_et);
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

	/**
	 * Data to store data to database.
	 */
	public void persistExpense(View v) {
		Expenses newItem = createExpenseItem();
		if (newItem != null) {
			ServiceHandler handle = new ServiceHandler(this);
			JSONObject expenseJSON = newItem.toInsertJSON(newItem);
			if(expenseJSON!=null)
			{
				handle.startServices(expenseJSON.toString());
				Log.i(LOGTAG, "Expense Name: " + newItem.getItemName());
				Log.i(LOGTAG, "Amount: " + newItem.getAmount());
				Log.i(LOGTAG, "Item added on: " + newItem.getAddedOn());
				setResult(RESULT_OK);
				this.finish();
			}
			else{
				showMessage("No data!!", "Please fill item details!!");
			}
		}
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

	private int getMaxExpensesId(){
		
		int expensesId = SharedUtil.getMaxExpenseID();
		expensesId = expensesId + 1;
		return expensesId;
	}
	
	public Expenses createExpenseItem(){

		String itemNameStr = itemNameEdit.getText().toString();
		String itemAmountStr = itemAmountEdit.getText().toString();
		String itemId;
		
		Date now = new Date();
	    
		long tripId = SharedUtil.getActiveTripId();
		String telString = SharedUtil.getActiveTelephone();
		int maxItemId = getMaxExpensesId();
		itemId = telString + "_" + maxItemId;
		SharedUtil.updateExpenseId(maxItemId);
		//DatabaseHelper dbh = new DatabaseHelper(this);
		String userName = SharedUtil.getCurrentUser(this);
		Log.i(LOGTAG, "fetching "+ telString);
		float amount;

		if(telString.isEmpty()){
			Log.i(LOGTAG, "Telephone number not found");
		}
		if (itemNameEdit.length() > 0 
				&& itemAmountEdit.length() > 0 
				) {
			amount = Float.parseFloat(itemAmountStr);
			
			newExpenseItem = new Expenses(itemId, tripId, itemNameStr, amount, userName, now);
			return newExpenseItem;
		}
		return null;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		View v = new View(AddItemActivity.this);
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			cancelItemAddition(v);
		}		
		return true;
	}

	public void cancelItemAddition(View v) {
		this.finish();
	}
}