package com.example.completetravelplanner.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.controllers.ManageExpensesActivity;
import com.example.completetravelplanner.helper.DatabaseHelper;
import com.example.completetravelplanner.helper.SharedUtil;
import com.example.completetravelplanner.models.Expenses;
import com.example.completetravelplanner.services.ServiceHandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class TripExpensesAdapter extends BaseAdapter 
{
	private static final String LOGTAG = "TripExpensesAdapter";

	/** The LayoutInflater. */
	private LayoutInflater mInflater;

	/** The context. */
	Context context;

	/** The holder. */
	ViewHolder holder;

	/** The data ArrayList. */
	ArrayList<Expenses> dataArrayList;
	View popupView;
	public PopupWindow popupWindow;
	ServiceHandler handle;

	/**
	 * Instantiates a new events list adapter.
	 * @param context the context
	 * @param articleArray the view.
	 */
	public TripExpensesAdapter(Context context, ArrayList<Expenses> expensesObj)
	{
		this.context = context;		
		mInflater = LayoutInflater.from(context);
		this.dataArrayList = expensesObj;

		LayoutInflater layoutInflater = (LayoutInflater) ((Activity) context).getBaseContext()
				.getSystemService(context.LAYOUT_INFLATER_SERVICE);
		
	}

	@Override
	public int getCount() 
	{
		if (dataArrayList!=null) 
		{
			return dataArrayList.size();

		}
		return 0;
	}

	@Override
	public Object getItem(int position) 
	{
		if(dataArrayList!=null)
		{
			try {
				return dataArrayList.get(position);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;

	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) 
	{
		convertView = mInflater.inflate(R.layout.custom_list_trip_expenses, null);	
		holder = new ViewHolder(); 	

		Expenses item = dataArrayList.get(position);
		String currentUser = SharedUtil.getCurrentUser(context);

		long tripId = SharedUtil.getActiveTripId();
		DatabaseHelper dbh = new DatabaseHelper(context);
		int numberOfMembers = dbh.getNumberOfMembers(tripId);
		final String itemName = item.getItemName();
		final float amount = item.getAmount();
		final String addedBy = item.getAddedBy();
		final Date strDate = item.getAddedOn();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
		String addedOn = sdf.format(strDate);
		final String expenseId = item.getItemId();

		holder.etItemName = (TextView)convertView.findViewById(R.id.list_expenses_itemName);
		holder.etAddedOn = (TextView)convertView.findViewById(R.id.list_expenses_time);
		holder.etPaidAmount = (TextView)convertView.findViewById(R.id.list_expenses_amountPaid);
		holder.etUserShare = (TextView)convertView.findViewById(R.id.list_expenses_userShare);
		holder.deleteBtn = (ImageView) convertView.findViewById(R.id.list_expenses_deleteBtnID);

		holder.etItemName.setText(itemName);
		holder.etAddedOn.setText(addedOn.toString());

		if(addedBy.equals(currentUser)){
			holder.etPaidAmount.setText("You Paid " + amount);
			holder.etUserShare.setText("You lent " + (amount - (amount/numberOfMembers)));
		}
		else{
			holder.etPaidAmount.setText(addedBy + " Paid " + amount);
			holder.etUserShare.setText("You owe " + (amount/numberOfMembers));
		}

		Log.i(LOGTAG, "Item Name: " + itemName);

		convertView.setTag(holder);	

		holder.deleteBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				handle = new ServiceHandler(context);
				JSONObject membersJSON = Expenses.toDeleteExpenseJSON(expenseId);
				handle.startServices(membersJSON.toString());
				dataArrayList.remove(position);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				showProgress();
				populateSummary(context);
				Log.i(LOGTAG, "Expense item deleted.");
			}
		});
		return convertView;
	}

	public void populateSummary(Context context){
		float totalExpenses = 0, userShare = 0, userPaid = 0, individualShare = 0;
		String userShareStr;
		ArrayList<Expenses> tripExpensesData = new ArrayList<Expenses>();
		long tripId = SharedUtil.getActiveTripId();
		DatabaseHelper bdh = new DatabaseHelper(context);
		int numberOfMembers = bdh.getNumberOfMembers(tripId);
		tripExpensesData = bdh.getExpensesList(tripId);
		String user = SharedUtil.getCurrentUser(context);
		Log.i(LOGTAG, "User Name is: " + user);
		for(int i = 0; i < tripExpensesData.size(); i++){
			Log.i(LOGTAG, "tripExpensesData size: " + tripExpensesData.size());
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
	public void showProgress()
	{
		final ProgressDialog dialog = new ProgressDialog(context);
		dialog.setMessage("Please wait.");
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
		dialog.show();

		long delayInMillis = 1000;
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {

				((Activity)context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						notifyDataSetChanged();
						dialog.dismiss();
					}
				});
			}
		}, delayInMillis);
	}


	/**
	 * The Class ViewHolder.
	 */
	static class ViewHolder
	{
		TextView etItemName, etAddedOn, etPaidAmount, etUserShare;
		ImageView deleteBtn;
	}

}