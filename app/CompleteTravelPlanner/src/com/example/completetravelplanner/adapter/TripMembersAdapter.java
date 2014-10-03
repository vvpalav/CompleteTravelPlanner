package com.example.completetravelplanner.adapter;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.helper.DatabaseHelper;
import com.example.completetravelplanner.models.Person;
import com.example.completetravelplanner.services.ServiceHandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class TripMembersAdapter extends BaseAdapter 
{
	private static final String LOGTAG = "TripMembersAdapter";

	/** The LayoutInflater. */
	private LayoutInflater mInflater;
	
	/** The context. */
	Context context;
	ServiceHandler handle;
	
	/** The holder. */
	ViewHolder holder;

	/** The data ArrayList. */
	ArrayList<Person> dataArrayList;
	View popupView;
	public PopupWindow popupWindow;

	/**
	 * Instantiates a new events list adapter.
	 * @param context the context
	 * @param articleArray the view.
	 */
	public TripMembersAdapter(Context context, ArrayList<Person> personObj)
	{
		this.context = context;		
		mInflater = LayoutInflater.from(context);
		this.dataArrayList = personObj;
		
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
		convertView = mInflater.inflate(R.layout.custom_list_trip_members, null);	
		holder = new ViewHolder(); 	
		
		Person member = dataArrayList.get(position);
		
		final String personName = member.getName();
		final long personPhoneNumber = member.getPhoneNumber();
		final Uri personImageUri = member.getPersonImageURI();
		
		holder.personName = (TextView)convertView.findViewById(R.id.list_membersList_personName);
		holder.personPhoneNumber = (TextView)convertView.findViewById(R.id.list_membersList_phoneNumber);
		holder.deleteBtn = (ImageView) convertView.findViewById(R.id.list_member_deleteBtnID);
		holder.personName.setText(personName);
		holder.personPhoneNumber.setText(personPhoneNumber+"");
		//holder.tripTime.setText(tripTime);
		//final String[] tokens = tripFriends.split(",");
		
		convertView.setTag(holder);	
		holder.deleteBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				//DatabaseHelper db = new DatabaseHelper(context);
				//Boolean boolean1 = db.deleteMemberItem(personPhoneNumber);
				//if (boolean1) {
					
					handle = new ServiceHandler(context);
					JSONObject membersJSON = Person.toDeleteMemberJSON(personPhoneNumber);
					handle.startServices(membersJSON.toString());
					dataArrayList.remove(position);
					showProgress();
				//}
				Log.i(LOGTAG, "Trip member deleted.");
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

		TextView personName, personPhoneNumber;	
		ImageView deleteBtn;
		//ImageView URI;

	}

}
