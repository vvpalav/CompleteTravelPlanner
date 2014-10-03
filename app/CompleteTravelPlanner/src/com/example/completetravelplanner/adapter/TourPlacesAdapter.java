package com.example.completetravelplanner.adapter;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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
import com.example.completetravelplanner.models.CheckList;
import com.example.completetravelplanner.models.TourPlaces;
import com.example.completetravelplanner.services.ServiceHandler;

public class TourPlacesAdapter extends BaseAdapter 
{
	/** The LayoutInflater. */
	private LayoutInflater mInflater;
	
	/** The context. */
	Context context;
	
	/** The holder. */
	ViewHolder holder;

	/** The data ArrayList. */
	ArrayList<TourPlaces> dataArrayList;
	View popupView;
	public PopupWindow popupWindow;

	/**
	 * Instantiates a new events list adapter.
	 * @param context the context
	 * @param articleArray the view.
	 */
	public TourPlacesAdapter(Context context,ArrayList<TourPlaces> tourPlacesObj)
	{
		this.context = context;		
		mInflater = LayoutInflater.from(context);
		this.dataArrayList=tourPlacesObj;
		
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
		convertView = mInflater.inflate(R.layout.custome_list_tour_places, null);	
		holder = new ViewHolder(); 	
		
		TourPlaces tourPlacesObj = dataArrayList.get(position);
		final long tripId = tourPlacesObj.getTripId();
		final String itemId = tourPlacesObj.getItemID();
		final String destination = tourPlacesObj.getLocation();
		
		 
		holder.destination = (TextView)convertView.findViewById(R.id.list_tour_placesID);
		holder.deleteBtn = (ImageView) convertView.findViewById(R.id.list_tourplaces_deleteBtnID);
		
		holder.destination.setText(destination);
		
		convertView.setTag(holder);	
		
		
		holder.deleteBtn.setOnClickListener(new OnClickListener() {
			
			@Override
		public void onClick(View view) {
				Context applicationContext = MainActivity.getContextOfApplication();
				TourPlaces tourPlaces = new TourPlaces();
				ServiceHandler handle = new ServiceHandler(applicationContext);
				JSONObject tourPlacesJSON = tourPlaces.toDeleteJSON(itemId);
				if(tourPlacesJSON!=null)
				{
					handle.startServices(tourPlacesJSON.toString());
					dataArrayList.remove(position);
					showProgress();

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

		TextView destination;	
		ImageView deleteBtn;

	}

}
