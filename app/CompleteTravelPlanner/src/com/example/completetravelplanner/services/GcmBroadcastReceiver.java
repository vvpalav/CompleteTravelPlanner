package com.example.completetravelplanner.services;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Log.i("GCM Broadcast Recieved", "Test");
		ComponentName comp = new ComponentName(arg0.getPackageName(), GcmIntentService.class.getName());
		startWakefulService(arg0, arg1.setComponent(comp));
		setResultCode(Activity.RESULT_OK);
	}
}