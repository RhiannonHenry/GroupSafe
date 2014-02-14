package com.kainos.groupsafe.utilities;

import java.util.logging.Logger;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

public class GPSTracker extends Service implements LocationListener {

	private final static Logger LOGGER = Logger.getLogger(GPSTracker.class
			.getName());
	// 10 meter(s)
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 10;
	// 1 minute(s)
	private static final long MIN_TIME_BETWEEN_UPDATES = 1000 * 60 * 1;

	private final Context mContext;
	boolean isGPSEnabled = false;
	boolean isNetworkEnabled = false;
	boolean canGetLocation = false;
	Location location;
	double lat;
	double lng;

	protected LocationManager locationManager;

	// Constructor
	public GPSTracker(Context context) {
		this.mContext = context;
		getLocation();
	}

	private Location getLocation() {
		try {
			locationManager = (LocationManager) mContext
					.getSystemService(LOCATION_SERVICE);
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// No network provider is enabled
			} else {
				this.canGetLocation = true;
				// Get location from Network Provider
				if (isNetworkEnabled) {
					LOGGER.info("Getting Location: Network Provider...");
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BETWEEN_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
					LOGGER.info("Getting Location: .....");
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if(location!=null){
							lat = location.getLatitude();
							lng = location.getLongitude();
						}
					}
				}
				// Get Location using GPS
				if (isGPSEnabled) {
					LOGGER.info("Getting Location: GPS...");
					locationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER,
							MIN_TIME_BETWEEN_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
					LOGGER.info("Getting Location: .....");
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if(location!=null){
							lat = location.getLatitude();
							lng = location.getLongitude();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return location;
	}

	public double getLat(){
		if(location !=null){
			lat = location.getLatitude();
		}
		// Will return 0.00 if failed
		return lat;
	}
	
	public double getLng(){
		if(location != null){
			lng = location.getLongitude();
		}
		// Will return 0.00 if failed
		return lng;
	}
	
	public boolean canGetLocation(){
		return this.canGetLocation;
	}
	
	public void showSettingAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		alertDialog.setTitle("GPS Settings");
		alertDialog.setMessage("Cannot get location. Enable Location Services in Settings.");
		
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
			public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
		
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
			public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
		alertDialog.show();
	}
	
	public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }       
    }
	
	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
