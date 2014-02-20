package com.kainos.groupsafe;

import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.kainos.groupsafe.utilities.GPSTracker;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsViewActivity extends Activity {

	private final static Logger LOGGER = Logger
			.getLogger(MapsViewActivity.class.getName());

	private GoogleMap googleMap;
	MarkerOptions currentLocationMarker;
	GPSTracker locationServices;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps_view);

		try {
			// Loading Map
			initialiseMap();
			createCurrentLocationMarker();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createCurrentLocationMarker() {
		if (currentLocationMarker == null) {
			currentLocationMarker = new MarkerOptions();
			getCurrentLocation();
		} else {
			getCurrentLocation();
		}

	}

	private void getCurrentLocation() {
		locationServices = new GPSTracker(MapsViewActivity.this);
		if (locationServices.canGetLocation()) {
			double lat = getMyLat();
			double lng = getMyLng();
			ParseUser currentUser = ParseUser.getCurrentUser();
			String userObjectId = currentUser.getObjectId();
			LOGGER.info("The Current User is: " + userObjectId);
			LOGGER.info("" + userObjectId + ": Lat: " + lat + " Lng: " + lng);

			String latitude = String.valueOf(lat);
			String longitude = String.valueOf(lng);

			
			
			ParseObject currentLocation = new ParseObject("Location");
			currentLocation.put("currentLat", latitude);
			currentLocation.put("currentLng", longitude);
			currentLocation.put("userId", userObjectId);
			try {
				currentLocation.save();
			} catch (ParseException e2) {
				LOGGER.info("UNABLE TO SAVE NEW EMERGENCY CONTACT AT THIS TIME...");
				e2.printStackTrace();
			}
			setMarker(lat, lng);
		} else {
			locationServices.showSettingAlert();
		}
	}

	private void setMarker(double lat, double lng) {
		LatLng currentPosition = new LatLng(lat, lng);
		googleMap.addMarker(currentLocationMarker.position(currentPosition).title("Current Location").draggable(false));
		
		CameraPosition cameraPosition = new CameraPosition.Builder().target(currentPosition).zoom(12).build();
		googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		
	}

	private void initialiseMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Unable to create map..", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private double getMyLat() {
		double latitude = locationServices.getLat();
		return latitude;
	}

	private double getMyLng() {
		double longitude = locationServices.getLng();
		return longitude;
	}

	@Override
	protected void onResume() {
		super.onResume();
		initialiseMap();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.maps_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_logout) {
			logCurrentUserOut();
		} else if (id == R.id.action_addContact) {
			LOGGER.info("Starting Add Contact Activity...");
			Intent intent = new Intent(getApplicationContext(),
					AddContactActivity.class);
			startActivity(intent);
			finish();
		} else if (id == R.id.action_viewMap) {
			LOGGER.info("Starting Map View Activity...");
			Intent intent = new Intent(getApplicationContext(),
					MapsViewActivity.class);
			startActivity(intent);
			finish();
		} else if (id == R.id.action_createGroup) {
			LOGGER.info("Starting Create Group Activity...");
			Intent intent = new Intent(getApplicationContext(),
					SelectGroupParticipantsActivity.class);
			startActivity(intent);
			finish();
		} else if (id == R.id.action_home) {
			LOGGER.info("Starting Home Activity...");
			Intent intent = new Intent(getApplicationContext(),
					HomeActivity.class);
			startActivity(intent);
			finish();
		} else if (id == R.id.action_settings) {

			LOGGER.info("Going to Settings page... ");
			Intent intent = new Intent(getApplicationContext(),
					SettingsActivity.class);
			startActivity(intent);
			finish();
		}
		return true;
	}

	private void logCurrentUserOut() {
		LOGGER.info("Logging out user: " + ParseUser.getCurrentUser() + "...");
		ParseUser.logOut();
		if (ParseUser.getCurrentUser() == null) {
			LOGGER.info("User successfully logged out!");
			Toast.makeText(getApplicationContext(), "Successfully Logged Out!",
					Toast.LENGTH_LONG).show();
			Intent intent = new Intent(getApplicationContext(),
					SplashActivity.class);
			startActivity(intent);
			finish();
		} else {
			Toast.makeText(getApplicationContext(), "Please Try Again!",
					Toast.LENGTH_LONG).show();
		}
	}
}
