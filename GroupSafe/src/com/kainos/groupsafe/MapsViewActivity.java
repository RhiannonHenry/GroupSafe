package com.kainos.groupsafe;

import java.util.List;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
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
	private String userObjectId;
	private String userLocationObjectId;
	private double lat;
	private double lng;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps_view);

		try {
			// Get User Location : Lat, Lng...
			getCurrentLocation();
			// Check if User has a pre-existing location in the database...
			checkForExistingEntry();
			// Loading Map
			initialiseMap();
			createCurrentLocationMarker();
			setMarker(lat, lng);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void checkForExistingEntry() {
		userObjectId = ParseUser.getCurrentUser().getObjectId();

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
		query.whereEqualTo("userId", userObjectId);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> foundExistingLocation,
					ParseException e) {
				if (e == null) {
					if (foundExistingLocation.size() != 0) {
						ParseObject currentData = foundExistingLocation.get(0);
						LOGGER.info("A LOCATION ENTRY ALREADY EXISTS FOR USER");
						LOGGER.info("EXISTING LOCATION ObjectID: "
								+ currentData.getObjectId());
						LOGGER.info("UPDATING LAT and LNG FOR CURRENT USER");
						currentData.put("currentLat", String.valueOf(lat));
						currentData.put("currentLng", String.valueOf(lng));
						try {
							currentData.save();
							LOGGER.info("Success:: UPDATED USER LOCATION DATA SUCCESSFULLY");
						} catch (ParseException e1) {
							LOGGER.info("UNABLE TO UPDATE USER LOCATION DATA");
							e1.printStackTrace();
						}
					} else {
						LOGGER.info("THIS IS A NEW LOCATION ENTRY");
						createNewLocationInTable();
					}
				}
			}
		});
	}

	protected void createNewLocationInTable() {
		ParseObject location = new ParseObject("Location");
		location.put("currentLat", String.valueOf(lat));
		location.put("currentLng", String.valueOf(lng));
		location.put("userId", ParseUser.getCurrentUser().getObjectId());
		try {
			LOGGER.info("Success:: saved new user location to Location Table");
			location.save();
		} catch (ParseException e2) {
			LOGGER.info("UNABLE TO SAVE NEW LOCATION AT THIS TIME...");
			e2.printStackTrace();
		}
		retrieveObjectID();
	}

	private void retrieveObjectID() {
		LOGGER.info("RETRIEVING LOCATION OBJECT ID FOR EXISTING USER");
		ParseQuery<ParseObject> locationID = ParseQuery.getQuery("Location");
		locationID.whereEqualTo("userId", userObjectId);
		locationID.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> locationList, ParseException e) {
				if (e == null) {
					for (int i = 0; i < locationList.size(); i++) {
						ParseObject current = locationList.get(i);
						LOGGER.info("Location: " + current.get("currentLat")
								+ "," + current.get("currentLng")
								+ " for user: " + userObjectId);
						if (current.get("userId").equals(userObjectId)) {
							userLocationObjectId = current.getObjectId();
							addLocationToUserCurrentLocation(userLocationObjectId);
						} else {
							LOGGER.info("NO MATCH FOR LOCATION");
						}
					}
				} else {
					LOGGER.info("UNABLE TO RETRIEVE LOCATION FROM LOCATION TABLE");
				}
			}

			private void addLocationToUserCurrentLocation(
					String userLocationObjectId) {
				LOGGER.info("UPDATING USER TABLE WITH CURRENT LOCATION OBJECT ID");
				LOGGER.info("New Location ObjectID: " + userLocationObjectId);
				ParseUser currentUser = ParseUser.getCurrentUser();
				currentUser.put("currentLocation", userLocationObjectId);
				currentUser.saveInBackground(new SaveCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null) {
							LOGGER.info("UPDATED USER SUCCESSFULLY");
						} else {
							LOGGER.info("ERROR SAVING LOCATION FOR USER");
						}
					}
				});
			}
		});
	}

	private void initialiseMap() {
		LOGGER.info("Entering Initialise Map...");
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Unable to create map..", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void createCurrentLocationMarker() {
		LOGGER.info("Entering Create Location Marker...");
		if (currentLocationMarker == null) {
			currentLocationMarker = new MarkerOptions();
		}
	}

	private void getCurrentLocation() {
		LOGGER.info("Entering Get Current Location...");
		locationServices = new GPSTracker(MapsViewActivity.this);
		if (locationServices.canGetLocation()) {
			lat = getMyLat();
			lng = getMyLng();
			LOGGER.info("Location: Lat: " + lat + " Lng: " + lng);
		} else {
			locationServices.showSettingAlert();
		}
	}

	private void setMarker(double lat, double lng) {
		LOGGER.info("Entering Set Marker...");

		LatLng currentPosition = new LatLng(lat, lng);
		googleMap.addMarker(currentLocationMarker.position(currentPosition)
				.title("Current Location").draggable(false));

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(currentPosition).zoom(12).build();
		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));

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
		createCurrentLocationMarker();
		setMarker(lat, lng);
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
