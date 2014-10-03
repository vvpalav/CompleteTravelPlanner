package com.example.completetravelplanner.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import com.example.completetravelplanner.helper.ClientServerRequestHandler;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

public class ServiceHandler {

	private final String serverUrl = "http://completetravelplanner.appspot.com/completetravelplannerserver";
	private Context myContext;

	public ServiceHandler(Context myContext) {
		this.myContext = myContext;
	}

	public void startServices(String message) {
		new Webmsgs(message).execute();
	}

	public void getConnectionService(String message) {
		Log.i("to_server", message);
		ClientServerRequestHandler.handleRequest(message);
		if (isConnected()) {
			performServerRequest(message);
		}
	}

	public void performServerRequest(String message) {
		try {
			URL url = new URL(serverUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestProperty("Content-Type", "application/json");
			urlConnection.setDoOutput(true);
			urlConnection.connect();
			OutputStreamWriter out = new  OutputStreamWriter(urlConnection.getOutputStream());
		    out.write(message.toString());
		    out.close();
		    
		    int result = urlConnection.getResponseCode();
			if(result == HttpURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				String data = null;
				StringBuilder sbr = new StringBuilder();
				while((data = br.readLine())!=null) {
					sbr.append(data); 
				}
				br.close();
				ClientServerRequestHandler.handleServerResponse(sbr.toString());
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isConnected() {
		ConnectivityManager connMgr = (ConnectivityManager) myContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netwrkInfo = connMgr.getActiveNetworkInfo();
		if (netwrkInfo != null && netwrkInfo.isConnected()) {
			return true;
		}
		Log.i("connection_test", "Failed .. No Internet Access");
		return false;
	}

	private class Webmsgs extends AsyncTask<Void, Void, Void> {
		String serviceparameters;

		public Webmsgs(String serviceparameters) {
			this.serviceparameters = serviceparameters;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			getConnectionService(serviceparameters);
			return null;
		}
	}
}