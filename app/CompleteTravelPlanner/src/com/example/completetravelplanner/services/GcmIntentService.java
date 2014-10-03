package com.example.completetravelplanner.services;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.controllers.MainActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GcmIntentService extends IntentService {

	public static final int NOTIFICATION_ID = 1;
	private static final String TAG = "In GCM Intent Sevices";
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		Bundle extras = arg0.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(arg0);
		Log.i(TAG, "This is to verify that it reaches here");
		if(!extras.isEmpty()){
			if(GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)){
				sendNotification("Send error: - " + extras.toString());
			}else if(GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)){
				sendNotification("Deleted messages on server:  "+ extras.toString()); 
			}else if(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)){
				//to be handled after detailed discussion 
				Log.i(TAG, extras.toString());
				sendNotification("Received " +  extras.toString());
			}
		}
		GcmBroadcastReceiver.completeWakefulIntent(arg0);
	}

	private void sendNotification(String string){
		Log.i(TAG, "In Send Notification function");
		Log.i("In send notification", string);
		String msg = null;
		String[] str = string.split(",");
		for (String st : str) {
			Log.i("In send notification", st);
			if(st.contains("message=")){
				msg = st.substring(st.indexOf("=")+1);
			}
		}
		
		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, 
				new Intent(this, MainActivity.class), 0);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("GCM Notification")
				.setStyle(new NotificationCompat.BigTextStyle()
				.bigText(msg))
				.setContentText(msg)
				.setAutoCancel(true);
				mBuilder.setContentIntent(contentIntent);
				mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}
