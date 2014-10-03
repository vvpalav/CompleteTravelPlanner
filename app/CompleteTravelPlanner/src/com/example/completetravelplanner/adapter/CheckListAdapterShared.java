package com.example.completetravelplanner.adapter;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.controllers.MainActivity;
import com.example.completetravelplanner.controllers.ManageTourActivity;
import com.example.completetravelplanner.helper.DatabaseHelper;
import com.example.completetravelplanner.helper.SharedUtil;
import com.example.completetravelplanner.models.CheckList;
import com.example.completetravelplanner.services.ServiceHandler;

public class CheckListAdapterShared extends BaseAdapter 
{
	/** The LayoutInflater. */
	private LayoutInflater mInflater;
	
	/** The context. */
	Context context;
	
	/** The holder. */
	ViewHolder holder;

	/** The data ArrayList. */
	ArrayList<CheckList> dataArrayList;
	View popupView;
	public PopupWindow popupWindow;

	/**
	 * Instantiates a new events list adapter.
	 * @param context the context
	 * @param articleArray the view.
	 */
	public CheckListAdapterShared(Context context,ArrayList<CheckList> checkListObj)
	{
		this.context = context;		
		mInflater = LayoutInflater.from(context);
		this.dataArrayList=checkListObj;
		
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
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) 
	{
		convertView = mInflater.inflate(R.layout.custome_checklist_sharedlist, null);	
		holder = new ViewHolder(); 	
		
		CheckList checkListObj = dataArrayList.get(position);

		//final long tripId = checkListObj.getTripId();
		final long tripId = SharedUtil.getActiveTripId();
		final String itemId = checkListObj.getItemId();
		
		final String itemName = checkListObj.getItemName();
		final String itemQuantity = checkListObj.getQuantity();
		final String itemAssignedTO = checkListObj.getAssignedTo();
		final String itemType = checkListObj.getCheckListType();
		final String status = checkListObj.getStatus();
		
		
		holder.itemName = (TextView)convertView.findViewById(R.id.list_checklist_itemName);
		holder.itemQuantity = (TextView)convertView.findViewById(R.id.list_checklist_quantity);
		holder.assignedTo = (TextView)convertView.findViewById(R.id.list_checklist_assignedto);
		//holder.itemType = (TextView)convertView.findViewById(R.id.list_checklist_type);
		holder.checkListStatus = (CheckBox) convertView.findViewById(R.id.chkAndroid);
		if(status!=null && status.equalsIgnoreCase("closed"))
		{
			holder.checkListStatus.setChecked(true);
			holder.checkListStatus.setEnabled(false);
		}
		else
			holder.checkListStatus.setEnabled(true);
		
		holder.deleteBtn = (ImageView) convertView.findViewById(R.id.list_checklist_deleteBtnID);
		
		holder.itemName.setText(itemName);
		holder.itemQuantity.setText(itemQuantity);
		holder.assignedTo.setText(itemAssignedTO);
		//holder.itemType.setText(itemType);
		
		
		
		convertView.setTag(holder);	
		
		holder.checkListStatus.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 CheckList checkList = new CheckList();
				Context applicationContext = MainActivity.getContextOfApplication();
				String username = SharedUtil.getCurrentUser(applicationContext);
				if(username!=null && itemAssignedTO!=null && username.equalsIgnoreCase(itemAssignedTO))
				{
					if(itemType!=null && itemType.equalsIgnoreCase(SharedUtil.CHECKLIST_SHARED))
					{
						ServiceHandler handle = new ServiceHandler(applicationContext);
						JSONObject checklistJSON = checkList.toUpdateJSON("closed", itemId);
						if(checklistJSON!=null)
						{
							handle.startServices(checklistJSON.toString());
						}	
					}		
					v.setEnabled(false);
				}
				else
				{
					
					Toast.makeText(applicationContext, "Item is not assigned to you...", Toast.LENGTH_SHORT).show();
					((CheckBox) v).setChecked(false);
				}
				
			}
		});
		
		
		holder.deleteBtn.setOnClickListener(new OnClickListener() {
			
			@Override
		public void onClick(View view) {
				 CheckList checkList = new CheckList();
				Context applicationContext = MainActivity.getContextOfApplication();
				if(itemType!=null && itemType.equalsIgnoreCase(SharedUtil.CHECKLIST_SHARED))
				{
					ServiceHandler handle = new ServiceHandler(applicationContext);
					JSONObject checklistJSON = checkList.toDeleteJSON(tripId,itemId);
					if(checklistJSON!=null)
					{
						handle.startServices(checklistJSON.toString());
						dataArrayList.remove(position);
						showProgress();
					}	
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

		TextView itemName,itemQuantity, assignedTo;//,itemType;	
		ImageView deleteBtn; 
		CheckBox checkListStatus;

	}

}
