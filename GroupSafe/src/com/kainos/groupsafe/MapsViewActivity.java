package com.kainos.groupsafe;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Activity that will be displayed to the user if they select 'View Map' from
 * the menu. The user will be able to see their current location on the Map.
 * 
 * Layout: @see activity_maps_view.xml Menu: @see maps_view.xml
 * 
 * @author Rhiannon Henry
 * 
 */
public class MapsViewActivity extends Activity {

	private static final String TAG = "Individual_Map_Activity";

	private GoogleMap googleMap;
	private MarkerOptions currentLocationMarker;
	private boolean internetPresent = false;
	private ConnectionDetector connectionDetector;
	private GPSTracker locationServices;
	private String userObjectId;
	private String userLocationObjectId;
	private double lat;
	private double lng;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		connectionDetector = new ConnectionDetector(getApplicationContext());
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

	/**
	 * This method is called and uses the GPSTracker class to find the users
	 * current latitude and longitude location.
	 * 
	 * @see {@link GPSTracker}
	 */
	private void getCurrentLocation() {
		Log.d(TAG, "Entering Get Current Location...");
		locationServices = new GPSTracker(MapsViewActivity.this);
		if (locationServices.canGetLocation()) {
			lat = getMyLat();
			lng = getMyLng();
			Log.i(TAG, "Location: Lat: " + lat + " Lng: " + lng);
		} else {
			locationServices.showSettingAlert();
		}
	}

	/**
	 * This method is used to check if the user has a current unique location
	 * identifier associated with their contact. If there is a currently
	 * associated location, we want to update that object. If there is not
	 * currently a location in the database associated with the user, we want to
	 * create one.
	 */
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
						Log.i(TAG, "A LOCATION ENTRY ALREADY EXISTS FOR USER");
						Log.i(TAG,
								"EXISTING LOCATION ObjectID: "
										+ currentData.getObjectId());
						Log.i(TAG, "UPDATING LAT and LNG FOR CURRENT USER");
						currentData.put("currentLat", String.valueOf(lat));
						currentData.put("currentLng", String.valueOf(lng));
						try {
							currentData.save();
							Log.i(TAG,
									"Success:: UPDATED USER LOCATION DATA SUCCESSFULLY");
						} catch (ParseException e1) {
							Log.e(TAG, "UNABLE TO UPDATE USER LOCATION DATA");
							e1.printStackTrace();
						}
					} else {
						Log.i(TAG, "THIS IS A NEW LOCATION ENTRY");
						createNewLocationInTable();
					}
				} else {
					Log.e(TAG, "ERROR::");
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * This method is used to create a new location object in the Location
	 * Entity in the database.
	 */
	protected void createNewLocationInTable() {
		ParseObject location = new ParseObject("Location");
		location.put("currentLat", String.valueOf(lat));
		location.put("currentLng", String.valueOf(lng));
		location.put("userId", ParseUser.getCurrentUser().getObjectId());
		try {
			Log.i(TAG, "Success:: saved new user location to Location Table");
			location.save();
		} catch (ParseException e2) {
			Log.e(TAG, "UNABLE TO SAVE NEW LOCATION AT THIS TIME...");
			e2.printStackTrace();
		}
		retrieveObjectID();
	}

	/**
	 * This method is used to retrieve the unique location identifier that is
	 * associated with a user.
	 */
	private void retrieveObjectID() {
		Log.d(TAG, "RETRIEVING LOCATION OBJECT ID FOR EXISTING USER");
		ParseQuery<ParseObject> locationID = ParseQuery.getQuery("Location");
		locationID.whereEqualTo("userId", userObjectId);
		locationID.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> locationList, ParseException e) {
				if (e == null) {
					for (int i = 0; i < locationList.size(); i++) {
						ParseObject current = locationList.get(i);
						Log.i(TAG, "Location: " + current.get("currentLat")
								+ "," + current.get("currentLng")
								+ " for user: " + userObjectId);
						if (current.get("userId").equals(userObjectId)) {
							userLocationObjectId = current.getObjectId();
							addLocationToUserCurrentLocation(userLocationObjectId);
						} else {
							Log.e(TAG, "NO MATCH FOR LOCATION");
						}
					}
				} else {
					Log.e(TAG,
							"UNABLE TO RETRIEVE LOCATION FROM LOCATION TABLE");
				}
			}

			/**
			 * This method is used to update the attribute 'currentLocation' in
			 * the User object. This attribute holds a String value that is a
			 * reference to the unique location identifier that is associated
			 * with the user.
			 * 
			 * @param userLocationObjectId
			 *            a String value that represents the unique location
			 *            identifier for a Location object that is assoication
			 *            with a User.
			 */
			private void addLocationToUserCurrentLocation(
					String userLocationObjectId) {
				Log.d(TAG,
						"UPDATING USER TABLE WITH CURRENT LOCATION OBJECT ID");
				Log.i(TAG, "New Location ObjectID: " + userLocationObjectId);
				ParseUser currentUser = ParseUser.getCurrentUser();
				currentUser.put("currentLocation", userLocationObjectId);
				currentUser.saveInBackground(new SaveCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null) {
							Log.i(TAG, "UPDATED USER SUCCESSFULLY");
						} else {
							Log.e(TAG, "ERROR SAVING LOCATION FOR USER");
						}
					}
				});
			}
		});
	}

	/**
	 * This method is used to initialise the Google Map Fragment if it has not
	 * already initialised.
	 */
	private void initialiseMap() {
		Log.d(TAG, "Entering Initialise Map...");
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Unable to create map..", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * This method is used to initialise a Marker that will be used to point to
	 * the users current location. If a marker has already been initialised, it
	 * will be used.
	 */
	private void createCurrentLocationMarker() {
		Log.d(TAG, "Entering Create Location Marker...");
		if (currentLocationMarker == null) {
			currentLocationMarker = new MarkerOptions();
		}
	}

	/**
	 * This method is used to position the marker on the google map at the users
	 * current position as defined by the parameters. The camera will then be
	 * positioned at the marker.
	 * 
	 * @param lat
	 *            a double value representing the users current latitude
	 *            location.
	 * @param lng
	 *            a double value representing the users current longitude
	 *            location.
	 */
	private void setMarker(double lat, double lng) {
		Log.d(TAG, "Entering Set Marker...");

		LatLng currentPosition = new LatLng(lat, lng);
		googleMap.addMarker(currentLocationMarker.position(currentPosition)
				.title("Current Location").draggable(false));

		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(currentPosition).zoom(12).build();
		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));

	}

	/**
	 * This method uses the locally created location services {@link GPSTracker}
	 * to get the users latitude.
	 * 
	 * @return latitude a double value representing the users latitude location.
	 */
	private double getMyLat() {
		double latitude = locationServices.getLat();
		return latitude;
	}

	/**
	 * This method uses the locally created location services {@link GPSTracker}
	 * to get the users longitude.
	 * 
	 * @return longitude a double value representing the users longitude
	 *         location.
	 */
	private double getMyLng() {
		double longitude = locationServices.getLng();
		return longitude;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		GroupSafeApplication.activityResumed();
		initialiseMap();
		createCurrentLocationMarker();
		setMarker(lat, lng);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.maps_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		internetPresent = connectionDetector.isConnectedToInternet();
		return MenuUtilities.menuOptions(id, this, internetPresent, TAG);
	}
}
