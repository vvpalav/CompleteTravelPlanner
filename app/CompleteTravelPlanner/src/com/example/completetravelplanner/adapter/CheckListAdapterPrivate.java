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
import com.example.completetravelplanner.helper.DatabaseHelper;
import com.example.completetravelplanner.helper.SharedUtil;
import com.example.completetravelplanner.models.CheckList;
import com.example.completetravelplanner.services.ServiceHandler;

public class CheckListAdapterPrivate extends BaseAdapter 
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
	public CheckListAdapterPrivate(Context context,ArrayList<CheckList> checkListObj)
	{
		this.context = context;		
		mInflater = LayoutInflater.from(context);
		this.dataArrayList=checkListObj;
		
		 LayoutInflater layoutInflater = (LayoutInflater) ((Activity) context).getBaseContext()
                 .getSystemService(context.LAYOUT_INFLATER_SERVICE);
         //popupView = layoutInflater.inflate(R.layout.popup_window_screen, null);
		 popupWindow = new PopupWindow(popupView,250, LayoutParams.WRAP_CONTENT,true);
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
		convertView = mInflater.inflate(R.layout.custome_checklist_private, null);	
		holder = new ViewHolder(); 	
		
		CheckList checkListObj = dataArrayList.get(position);
		
		//final long tripId = checkListObj.getTripId();
		final long tripId = SharedUtil.getActiveTripId();
		final String itemId = checkListObj.getItemId();
		final String itemName = checkListObj.getItemName();
		final String itemQuantity = checkListObj.getQuantity();
		final String itemAssignedTO = checkListObj.getAssignedTo();
		final String itemType = checkListObj.getCheckListType();
		
		 
		holder.itemName = (TextView)convertView.findViewById(R.id.list_checklist_itemName);
		holder.itemQuantity = (TextView)convertView.findViewById(R.id.list_checklist_quantity);
		holder.assignedTo = (TextView)convertView.findViewById(R.id.list_checklist_assignedto);
		//holder.itemType = (TextView)convertView.findViewById(R.id.list_checklist_type);
		holder.deleteBtn = (ImageView) convertView.findViewById(R.id.list_checklist_deleteBtnID);
		
		holder.itemName.setText(itemName);
		holder.itemQuantity.setText(itemQuantity);
		//holder.assignedTo.setText(itemAssignedTO);
		//holder.itemType.setText(itemType);
		
		
		
		convertView.setTag(holder);	
		
		
		holder.deleteBtn.setOnClickListener(new OnClickListener() {
			
			@Override
		public void onClick(View view) {
				
				
				if(itemType!=null && itemType.equalsIgnoreCase(SharedUtil.CHECKLIST_PRIVATE))
				{
					DatabaseHelper db = new DatabaseHelper(context);
					Boolean boolean1 = db.deleteCheckList(tripId,itemId);
					if (boolean1) {
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

	}

}
