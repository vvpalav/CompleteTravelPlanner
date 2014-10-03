package com.example.completetravelplanner.adapter;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.controllers.MainActivity;
import com.example.completetravelplanner.helper.DatabaseHelper;
import com.example.completetravelplanner.helper.SharedUtil;
import com.example.completetravelplanner.models.CheckList;
import com.example.completetravelplanner.models.Trip;
import com.example.completetravelplanner.services.ServiceHandler;

public class TripHistoryAdapter extends BaseAdapter 
{
	/** The LayoutInflater. */
	private LayoutInflater mInflater;
	
	/** The context. */
	Context context;
	
	/** The holder. */
	ViewHolder holder;

	/** The data ArrayList. */
	ArrayList<Trip> dataArrayList;
	View popupView;
	public PopupWindow popupWindow;

	/**
	 * Instantiates a new events list adapter.
	 * @param context the context
	 * @param articleArray the view.
	 */
	public TripHistoryAdapter(Context context,ArrayList<Trip> tripListObj)
	{
		this.context = context;		
		mInflater = LayoutInflater.from(context);
		this.dataArrayList=tripListObj;
		
		 LayoutInflater layoutInflater = (LayoutInflater) ((Activity) context).getBaseContext()
                 .getSystemService(context.LAYOUT_INFLATER_SERVICE);
         //popupView = layoutInflater.inflate(R.layout.popup_window_screen, null);
		// popupWindow = new PopupWindow(popupView,250, LayoutParams.WRAP_CONTENT,true);
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
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) 
	{
		convertView = mInflater.inflate(R.layout.custome_trip_history, null);	
		holder = new ViewHolder(); 	
		
		final Trip tripObj = dataArrayList.get(position);
		
		final String tripName = tripObj.getTripName();
		final String startDate = tripObj.getStartDate();
		final String endDate = tripObj.getEndDate();
		
		holder.tripName = (TextView)convertView.findViewById(R.id.list_trip_name);
		holder.startDate = (TextView)convertView.findViewById(R.id.list_trip_startdate);
		holder.endDate = (TextView)convertView.findViewById(R.id.list_trip_enddate);
		holder.deleteBtn = (ImageView) convertView.findViewById(R.id.list_trip_deleteBtnID);
		
		holder.tripName.setText(tripName);
		holder.startDate.setText(startDate);
		holder.endDate.setText(endDate);
		convertView.setTag(holder);	
		
		holder.deleteBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				try {
					JSONObject object = new JSONObject();
					object.put("transaction_type", "TRIP_DELETION");
					object.put("trip_id", tripObj.getTripId());
					object.put("telephone", SharedUtil.getActiveTelephone());
					new ServiceHandler(MainActivity.getContextOfApplication())
						.startServices(object.toString());
					dataArrayList.remove(position);
					showProgress();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		
		return convertView;
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

		TextView tripName,startDate, endDate;//,itemType;	
		ImageView deleteBtn;

	}

}