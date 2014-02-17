package com.kainos.groupsafe;

import java.util.logging.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kainos.groupsafe.utilities.GPSTracker;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class MapsViewActivity extends FragmentActivity {

	private final static Logger LOGGER = Logger
			.getLogger(MapsViewActivity.class.getName());

	GPSTracker locationServices;
	double lat;
	double lng;
	View activityRoot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps_view);

		activityRoot = this.findViewById(android.R.id.content);
		activityRoot.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {

						GoogleMap mMap = ((SupportMapFragment) getSupportFragmentManager()
								.findFragmentById(R.id.personalMap)).getMap();
						enableMyLocationOnMap(mMap);

						locationServices = new GPSTracker(MapsViewActivity.this);
						if (locationServices.canGetLocation()) {
							lat = getMyLat();
							lng = getMyLng();
							ParseUser currentUser = ParseUser.getCurrentUser();
							String userObjectId = currentUser.getObjectId();
							LOGGER.info("The Current User is: " + userObjectId);
							// ParseObject location = new
							// ParseObject("Location");
							// location.put("currentLatitude", lat);
							// location.put("currentLongitude", lng);
							// location.put("user", userObjectId);
						} else {
							locationServices.showSettingAlert();
						}

						Marker marker = mMap.addMarker(new MarkerOptions()
								.position(new LatLng(lat, lng))
								.title("My Location").draggable(false));

						LatLngBounds.Builder builder = new LatLngBounds.Builder();
						builder.include(marker.getPosition());
						LatLngBounds bounds = builder.build();
						int padding = 100;

						CameraUpdate updatedView = CameraUpdateFactory
								.newLatLngBounds(bounds, padding);
						mMap.animateCamera(updatedView);

					}
				});

	}

	private double getMyLat() {
		double latitude = locationServices.getLat();
		return latitude;
	}

	private double getMyLng() {
		double longitude = locationServices.getLng();
		return longitude;
	}

	private void enableMyLocationOnMap(GoogleMap mMap) {
		mMap.setMyLocationEnabled(true);
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
