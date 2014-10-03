package com.example.completetravelplanner.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.example.completetravelplanner.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * http://www.androidhive.info/2013/08/android-working-with-google-maps-v2/
 */
	
public class MapViewActivity extends Activity implements OnMapClickListener {
	
	 private GoogleMap googleMap;
	 LatLng Poprad;
	 
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapview_screen);
		
		initilizeMap();
        
	}
	
	/**
     * function to load map. If map is not created it will create it for you
     * */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	private void initilizeMap() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            googleMap.setOnMapClickListener(MapViewActivity.this);
            
         // latitude and longitude
        	double latitude = 43.7229266605499;
        	double longitude = -79.3884344372254;
        	Poprad = new LatLng(latitude, longitude);
            placeMarker();
            
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
    
    private void placeMarker()
    {
    	
    	 
    	// create marker
//    	MarkerOptions marker = new MarkerOptions().position(Poprad);
    	// GREEN color icon
//        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
    	// adding marker
//    	googleMap.addMarker(marker);
    	
    	googleMap.setMyLocationEnabled(true); 
    	googleMap.getUiSettings().setZoomControlsEnabled(true);
    	googleMap.getUiSettings().setMyLocationButtonEnabled(true);
    	googleMap.getUiSettings().setRotateGesturesEnabled(true);
    	
    	
    	googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Poprad, 15));
    	googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

    	
    	googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    	/**
    	 * we can show options of the map view as given bellow
    	 */
//    	googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//    	googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//    	googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
//    	googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
    	
    }

	@Override
	public void onMapClick(LatLng point) {
		Poprad = point;
//		placeMarker();
		
		double lat = Poprad.latitude;
		double lng = Poprad.longitude;
		String add = "";
		Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
		try {
			//Thread.sleep(2000);
		   List<Address> addresses = geoCoder.getFromLocation(lat, lng, 1);

		   //Thread.sleep(2000);
		   if (addresses.size() > 0) 
		   {
		      for (int i=0; i<addresses.get(0).getMaxAddressLineIndex();i++)
		     add += addresses.get(0).getAddressLine(i) + "\n";
		   }

		}
		catch (IOException e1) {                
		   e1.printStackTrace();
		} 
//		catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
		
		 Intent data = new Intent();
		  data.putExtra("city", add);
		  // Activity finished ok, return the data
		  setResult(RESULT_OK, data);
		  MapViewActivity.this.finish();
		
	}
    

}
