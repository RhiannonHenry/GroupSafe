package com.kainos.groupsafe;

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
import android.util.Log;

/**
 * This class is used to get the users current location via GPS or Network. I
 * followed a tutorial to help me implement this.
 * 
 * @author Rhiannon Henry and {@link http
 *         ://www.androidhive.info/2012/07/android-
 *         gps-location-manager-tutorial/}
 * 
 */
public class GPSTracker extends Service implements LocationListener {

	private static final String TAG = "GPS_TRACKER";
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

	/**
	 * This method is used to check for a connection to either GPS or Network
	 * and then attempts to get and return the users current latitude and
	 * longitude.
	 * 
	 * @return location a Location value with the users current latitude and
	 *         longitude
	 */
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
					Log.i(TAG, "Getting Location: Network Provider...");
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BETWEEN_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
					Log.i(TAG, "Getting Location: .....");
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							lat = location.getLatitude();
							lng = location.getLongitude();
							Log.i(TAG, "Got Latitude: " + lat);
							Log.i(TAG, "Got Longitude: " + lng);
						}
					}
				}
				// Get Location using GPS
				if (isGPSEnabled) {
					Log.i(TAG, "Getting Location: GPS...");
					locationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER,
							MIN_TIME_BETWEEN_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
					Log.i(TAG, "Getting Location: .....");
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if (location != null) {
							lat = location.getLatitude();
							lng = location.getLongitude();
							Log.i(TAG, "Got Latitude: " + lat);
							Log.i(TAG, "Got Longitude: " + lng);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return location;
	}

	/**
	 * Returns the users current latitude
	 * 
	 * @return latitude a double value of the users current latitude
	 */
	public double getLat() {
		if (location != null) {
			lat = location.getLatitude();
		}
		// Will return 0.00 if failed
		return lat;
	}

	/**
	 * Returns the users current longitude
	 * 
	 * @return longitude a double value of the users current longitude
	 */
	public double getLng() {
		if (location != null) {
			lng = location.getLongitude();
		}
		// Will return 0.00 if failed
		return lng;
	}

	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	/**
	 * Method that displays an alert dialog to the user prompting them to alter
	 * their Location settings. The user can cancel the dialog or they can be
	 * directed to the 'Settings' screen for their phone.
	 */
	public void showSettingAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		alertDialog.setTitle("GPS Settings");
		alertDialog
				.setMessage("Cannot get location. Enable Location Services in Settings.");

		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						mContext.startActivity(intent);
					}
				});

		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		alertDialog.show();
	}

	/**
	 * This method is used to stop the GPS in the application
	 */
	public void stopUsingGPS() {
		if (locationManager != null) {
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
