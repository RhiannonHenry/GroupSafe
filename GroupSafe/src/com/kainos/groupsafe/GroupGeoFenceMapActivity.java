package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * This activity is used to display the group map screen to both group leaders
 * and group members. This screen displays the map, the current participants and
 * the geo-fence.
 * 
 * 
 * @author Rhiannon Henry
 * 
 */
public class GroupGeoFenceMapActivity extends FragmentActivity implements
		OnMapLongClickListener {

	private static GroupGeoFenceMapActivity _instance = null;
	final Context context = this;
	private static final String TAG = "Group_Map_Activity";

	private GoogleMap googleMap;
	private Circle circle;
	private CircleOptions geoFence;
	private MarkerOptions groupLeaderMarker;
	private MarkerOptions ownMarker;
	private Map<String, MarkerOptions> participantLocationMarkers;
	private ArrayList<String> groupParticipants;
	private ArrayList<String> participantNumbers;
	private GPSTracker locationServices;
	private ParseUser currentUser;
	private String userId, userLocationObjectId, groupId, groupLeaderId;
	private double lat;
	private double lng;
	private int radius;

	private MenuItem terminateMenuItem;
	private Spinner customRadiusSpinner;
	private Button updateButton, okButton;
	private Timer autoUpdate;
	private boolean mapInitialized = false;
	private boolean initialGeoFenceDrawn = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_geo_fence_map);
		_instance = this;

		currentUser = ParseUser.getCurrentUser();
		userId = currentUser.getObjectId().toString();
		locationServices = new GPSTracker(GroupGeoFenceMapActivity.this);
		participantLocationMarkers = new HashMap<String, MarkerOptions>();
		groupParticipants = new ArrayList<String>();
		participantNumbers = new ArrayList<String>();

		Intent intent = getIntent();
		groupId = intent.getStringExtra("groupId");
		groupLeaderId = intent.getStringExtra("groupLeaderId");
		radius = intent.getIntExtra("radius", 2);

		Log.i(TAG, "GOT GROUP_ID: " + groupId);
		Log.i(TAG, "GOT GROUP_LEADER_ID: " + groupLeaderId);
		Log.i(TAG, "GOT RADIUS: " + radius);
		Log.d(TAG, "Entering START ACTIVITY LOGIC...");
		createGroupLeaderCurrentLocationMarker();
		startActivityLogic();
	}

	@Override
	protected void onPause() {
		super.onPause();
		GroupSafeApplication.activityPaused();
		autoUpdate.cancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	public void onResume() {
		Log.i(TAG, "UPDATING...");
		super.onResume();
		GroupSafeApplication.activityResumed();
		autoUpdate = new Timer();
		autoUpdate.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						if (mapInitialized) {
							if (currentUser.get("groupLeader").equals(true)) {
								Log.i(TAG, "Refreshing View for GROUP LEADER");
								createGroupLeaderCurrentLocationMarker();
								setMarker(lat, lng, "My Location");
								createGeoFence(lat, lng);
								for (int i = 0; i < participantNumbers.size(); i++) {
									getParticipantLocation(participantNumbers
											.get(i));
									ParseQuery<ParseUser> getParticipantUserId = ParseUser
											.getQuery();
									getParticipantUserId.whereEqualTo(
											"username",
											participantNumbers.get(i));
									try {
										List<ParseUser> participantUser = getParticipantUserId
												.find();
										String userObjectId = participantUser
												.get(0).getObjectId()
												.toString();
										String userDisplayName = participantUser
												.get(0).get("displayName")
												.toString();
										if (userObjectId != null) {
											checkIfParticipantInGeoFence(
													userObjectId,
													userDisplayName);
										}
									} catch (ParseException e) {
										Log.e(TAG, "ERROR:: ");
										e.printStackTrace();
									}
								}
							} else {
								Log.i(TAG, "Refreshing View for GROUP MEMBER");

								getLeaderLocationAndSetMarker();
								createOwnCurrentLocationMarker();
								setOwnMarker(lat, lng);
								for (int i = 0; i < participantNumbers.size(); i++) {
									getParticipantLocation(participantNumbers
											.get(i));
								}
							}
						} else {
							Log.e(TAG, "Map has not been initialized!");
						}
					}

					/**
					 * This method is used to check if the user is currently
					 * inside the group geo-fence, or if they are outside it.
					 * This is calculated using the Haversine Formula.
					 * 
					 * @param userObjectId
					 *            the unique identifier for the user we are
					 *            checking.
					 * @param userDisplayName
					 */
					private void checkIfParticipantInGeoFence(
							String userObjectId, String userDisplayName) {
						MarkerOptions participant = participantLocationMarkers
								.get(userObjectId);
						Log.i(TAG, "Getting location for user: " + userObjectId);
						try {
							LatLng participantLocation = participant
									.getPosition();
							Log.i(TAG, "User is at location: "
									+ participantLocation.latitude + ","
									+ participantLocation.longitude);
							LatLng geoFenceCenter = circle.getCenter();
							Log.i(TAG, "Center of Geo-Fence is at: "
									+ geoFenceCenter.latitude + ","
									+ geoFenceCenter.longitude);

							int meters = Haversine.getDistanceDifference(
									participantLocation, geoFenceCenter);
							if (meters > radius) {
								JSONObject participantNotificationData = null;
								try {
									participantNotificationData = new JSONObject(
											"{"
													+ "\"alert\":\""
													+ "You have left the Geo-Fence\", "
													+ "\"action\":\"com.kainos.groupsafe.ExitGeoFenceNotificationActivity\", "
													+ "\"title\": \"Exit!\"}");
									sendAlertNotification(userObjectId,
											participantNotificationData);
								} catch (JSONException e1) {
									Log.e(TAG,
											"ERROR: Error Creating JSON for Temination Notification.");
									e1.printStackTrace();
								}
								notifyGroupLeader(userObjectId, userDisplayName);
							} else {
								Log.i(TAG, "PARTICIPANT IS WITHIN THE GEO-FENCE");
							}
						} catch (NullPointerException e) {
							Log.e(TAG, "Participnt has not been positioned yet...");
						}
						
					}

					/**
					 * This method is used to send a notification to the user
					 * who is outside the geo-fence.
					 * 
					 * @param userObjectId
					 *            the user to whom the notification should be
					 *            sent.
					 * @param participantNotificationData
					 *            the JSON data to be sent in the notification
					 *            body.
					 */
					private void sendAlertNotification(String userObjectId,
							JSONObject participantNotificationData) {
						String notificationChannel = "user_" + userObjectId;
						Log.i(TAG, "#004: Channel = " + notificationChannel);
						ParsePush push = new ParsePush();
						push.setData(participantNotificationData);
						push.setChannel(notificationChannel);
						push.sendInBackground();
					}

					/**
					 * This method is used to notify the group leader that a
					 * user is outside the geo-fence.
					 * 
					 * @param userObjectId
					 *            the user who is outside the geo-fence
					 * @param userDisplayName
					 */
					private void notifyGroupLeader(String userObjectId,
							String userDisplayName) {
						final Dialog dialog = new Dialog(context);
						dialog.setContentView(R.layout.custom_notification_dialog);
						dialog.setTitle("Exit Geo-Fence");
						okButton = (Button) dialog
								.findViewById(R.id.dialogNotificationOK);
						TextView message = (TextView) dialog
								.findViewById(R.id.dialogNotificationMessage);
						message.setText("Participant " + userDisplayName
								+ " has left the geo-fence!");
						okButton.setEnabled(true);
						okButton.setClickable(true);
						dialog.show();

						okButton.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								okButton.setEnabled(false);
								okButton.setClickable(false);
								dialog.dismiss();
							}
						});
					}
				});
			}
		}, 0, 30000); // updates every 30 seconds
	}

	/**
	 * This method is used to initialise the Group Geo Fence Map Activity for
	 * group leader / group member depending on the user who is viewing the map.
	 */
	private void startActivityLogic() {
		Log.d(TAG, "ENTERED START ACTIVITY LOGIC!");
		try {
			Log.i(TAG, "Initialising Map...");
			initialiseMap();
			getCurrentLocation();
			checkForExisitingLocationEntryForUser();

			if (currentUser.get("groupLeader").equals(true)) {
				Log.i(TAG, "GROUP LEADER FLOW");
				createGroupLeaderCurrentLocationMarker();
				setMarker(lat, lng, "My Location");
				createGeoFence(lat, lng);
				findParticipants();
			} else {
				Log.i(TAG, "GROUP MEMBER FLOW");
				createGroupLeaderCurrentLocationMarker();
				getLeaderLocationAndSetMarker();
				createOwnCurrentLocationMarker();
				setOwnMarker(lat, lng);
				findParticipants();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is used by a GROUP MEMBER. This method is used to set the
	 * marker for the user who is a group member. The marker for the user (who
	 * is a member) will be YELLOW.
	 * 
	 * @param latitude
	 *            a double value representing the latitude location of the user
	 * @param longitudea
	 *            double value representing the longitude location of the user
	 */
	private void setOwnMarker(double latitude, double longitude) {
		Log.i(TAG, "Entering Set Own Marker...");
		Log.i(TAG, "Placing marker at: " + lat + "," + lng);

		// TODO: Remove hard coding...
		// 54.5822043,-5.9380233 --> lat, lng
		LatLng currentPosition = new LatLng(latitude, longitude);
		googleMap.addMarker(ownMarker
				.position(currentPosition)
				.title("My Location")
				.draggable(false)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

		if (!mapInitialized) {
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(currentPosition).zoom(14.5F).bearing(300F) // orientation
					.tilt(50F) // viewing angle
					.build();
			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
		}
	}

	/**
	 * This method is used to find the unique reference of the Location of the
	 * group leader. This will reference an object in the Location entity in the
	 * database.
	 */
	private void getLeaderLocationAndSetMarker() {
		// Get Group Leader current Location
		ParseQuery<ParseUser> getLeaderLocationId = ParseUser.getQuery();
		getLeaderLocationId.whereEqualTo("objectId", groupLeaderId);
		getLeaderLocationId.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> foundUserList, ParseException e) {
				if (e == null) {
					if (foundUserList.size() > 0) {
						Log.i(TAG, "SUCCESS:: Found the Group Leader User!");
						ParseUser groupLeaderUser = foundUserList.get(0);
						String groupLeaderLocationId = null;
						try {
							groupLeaderLocationId = groupLeaderUser.get(
									"currentLocation").toString();
						} catch (NullPointerException e1) {
							Log.e(TAG, "ERROR:: ");
							e1.printStackTrace();
						}
						if (groupLeaderLocationId == null) {
							Log.i(TAG,
									"Group Leader doesn't have a location ID");
						} else {
							getLocationOfGroupLeader(groupLeaderUser.get(
									"currentLocation").toString());
						}
					} else {
						Log.e(TAG,
								"FAILURE:: Failed to retrieve the group leader user ("
										+ groupLeaderId + ")");
					}
				} else {
					Log.e(TAG, "Error:: ");
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * This method is used to find the latitude and longitude of the group
	 * leaders current location. This infomation is kept in the Location entity
	 * in the database.
	 * 
	 * @param locationId
	 *            a String value that is the unique reference to an object
	 *            within the Location entity in the database that is associated
	 *            with the group leaders user.
	 */
	protected void getLocationOfGroupLeader(String locationId) {
		ParseQuery<ParseObject> getGroupLeaderLocation = ParseQuery
				.getQuery("Location");
		getGroupLeaderLocation.whereEqualTo("objectId", locationId);
		getGroupLeaderLocation.whereEqualTo("userId", groupLeaderId);
		getGroupLeaderLocation
				.findInBackground(new FindCallback<ParseObject>() {
					@Override
					public void done(List<ParseObject> foundLocationList,
							ParseException e) {
						if (e == null) {
							if (foundLocationList.size() > 0) {
								Log.i(TAG,
										"SUCCESS:: Found the Location for Group Leader!");
								ParseObject groupLeaderLocation = foundLocationList
										.get(0);
								double groupLeaderLat = Double
										.parseDouble(groupLeaderLocation.get(
												"currentLat").toString());
								double groupLeaderLng = Double
										.parseDouble(groupLeaderLocation.get(
												"currentLng").toString());
								Log.i(TAG, "Got Location: " + groupLeaderLat
										+ "," + groupLeaderLng);
								// Set Marker For Group Leader
								setMarker(groupLeaderLat, groupLeaderLng,
										"Group Leader");
							} else {
								Log.e(TAG,
										"FAILURE:: Failed to retrieve the location for user ("
												+ groupLeaderId + ")");
							}
						} else {
							Log.e(TAG, "Error:: ");
							e.printStackTrace();
						}
					}
				});
	}

	/**
	 * This method is used by GROUP LEADER and GROUP MEMBERS. This method is
	 * used to position any group participants on the map. This method loops
	 * through all of the participants.
	 */
	private void positionParticipants() {
		Log.d(TAG, "Entered GET PARTICIPANTS NUMBER: ");
		for (int i = 0; i < groupParticipants.size(); i++) {
			Log.i(TAG,
					"Getting number for participant: "
							+ groupParticipants.get(i));
			ParseQuery<ParseObject> getParticipantNumber = ParseQuery
					.getQuery("Participant");
			getParticipantNumber.whereEqualTo("objectId",
					groupParticipants.get(i));
			getParticipantNumber.whereEqualTo("groupId", groupId);
			getParticipantNumber
					.findInBackground(new FindCallback<ParseObject>() {
						@Override
						public void done(
								List<ParseObject> foundParticipantList,
								ParseException e) {
							if (e == null) {
								if (foundParticipantList.size() > 0) {
									Log.i(TAG, "SUCCESS:: Found Participant");
									ParseObject current = foundParticipantList
											.get(0);
									String currentParticipantNumber = current
											.get("participantNumber")
											.toString();
									Log.i(TAG, "Got Participant Number: "
											+ currentParticipantNumber);
									if (currentParticipantNumber
											.equals(currentUser.getUsername())) {
										Log.i(TAG,
												"This participant is the same as the logged in user, they have already retreived and diaplayed their location. Continuing...");
									} else {
										participantNumbers
												.add(currentParticipantNumber);
										getParticipantLocation(currentParticipantNumber);
									}

								} else {
									Log.e(TAG,
											"FAILURE:: Unable to find Participant");
								}
							} else {
								Log.e(TAG, "ERROR:: ");
								e.printStackTrace();
							}
						}
					});
		}
		Log.i(TAG, "MAP HAS BEEN INITIALIZED!!");
		mapInitialized = true;
	}

	/**
	 * This method is used to get the unique location reference associate with
	 * each participants user. This will reference the current location of each
	 * participant in the Location entity.
	 * 
	 * @param participantNumber
	 *            a String value of the participants phone number (aka. their
	 *            username)
	 */
	protected void getParticipantLocation(String participantNumber) {
		Log.d(TAG,
				"Entered GET PARTICIPANT LOCATION...for participant with number: "
						+ participantNumber);
		ParseQuery<ParseUser> userLocationQuery = ParseUser.getQuery();
		userLocationQuery.whereEqualTo("username", participantNumber);
		userLocationQuery.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> foundUserList, ParseException e) {
				if (e == null) {
					if (foundUserList.size() > 0) {
						Log.i(TAG, "SUCCESS:: Found User for Participant");
						ParseUser current = foundUserList.get(0);
						String userLocation = null;
						try {
							userLocation = current.get("currentLocation")
									.toString();
						} catch (NullPointerException e1) {
							Log.e(TAG, "Participant doen't have a location");
							e1.printStackTrace();
						}

						if (userLocation == null) {
							Log.i(TAG, "User Location is NULL");
						} else {
							Log.i(TAG, "Got location ID: " + userLocation);
							Log.i(TAG,
									"Adding Location ID to participantLocations array...");
							getParticipantLatLng(userLocation,
									current.get("displayName").toString());
						}
					} else {
						Log.e(TAG,
								"FAILURE:: Unable to find User for Participant");
					}
				} else {
					Log.e(TAG, "ERROR::");
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * This method is used to get the latitude and longitude value for each
	 * participant. This information is fetched form the Location entity in the
	 * database by using the unique location reference that is passed through in
	 * the parameters. The Marker for each participant who is not the current
	 * user/group leader is ROSE (PINK)
	 * 
	 * @param userLocationID
	 *            a String value representing a unique reference to the location
	 *            associated with the user/participant.
	 */
	protected void getParticipantLatLng(String userLocationID,
			final String displayName) {
		ParseQuery<ParseObject> getLocationQuery = ParseQuery
				.getQuery("Location");
		getLocationQuery.whereEqualTo("objectId", userLocationID);
		getLocationQuery.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> foundLocationList,
					ParseException e) {
				if (e == null) {
					if (foundLocationList.size() > 0) {
						Log.i(TAG, "SUCCESS:: Found Location");
						ParseObject currentLocation = foundLocationList.get(0);
						// Create Marker for Participant using lat and lng
						// values...
						double latitude = Double.parseDouble(currentLocation
								.get("currentLat").toString());
						double longitude = Double.parseDouble(currentLocation
								.get("currentLng").toString());
						String userId = currentLocation.get("userId")
								.toString();

						// TODO:: remove hard coding. Delete these 4 lines.
						// if (latitude == 0 && longitude == 0) {
						// latitude = 54.5871171;
						// longitude = -5.9338856;
						// }
						Log.i(TAG, "Creating Marker Options with Location: "
								+ latitude + "," + longitude);
						Log.i(TAG, "For Participant with userID: " + userId);
						LatLng currentPosition = new LatLng(latitude, longitude);
						if (participantLocationMarkers.containsKey(userId)) {
							// Fetch old participant marker and update position
							MarkerOptions currentMarker = participantLocationMarkers
									.get(userId);
							if (currentMarker == null) {
								currentMarker = new MarkerOptions();
								googleMap.addMarker(currentMarker
										.position(currentPosition)
										.title("Participant " + displayName)
										.draggable(false)
										.icon(BitmapDescriptorFactory
												.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
							} else {
								currentMarker.position(currentPosition);
							}
						} else {
							MarkerOptions participantMarker = new MarkerOptions();
							googleMap.addMarker(participantMarker
									.position(currentPosition)
									.title("Participant " + displayName)
									.draggable(false)
									.icon(BitmapDescriptorFactory
											.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
							participantLocationMarkers.put(userId,
									participantMarker);
						}
					} else {
						Log.e(TAG, "FAILURE:: Unable to find a location");
					}
				} else {
					Log.e(TAG, "ERROR::");
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * This method is used to fetch each participant associated with the group.
	 * This method will then start to position the group participants by calling
	 * the @see positionParticipants() method.
	 */
	private void findParticipants() {
		Log.d(TAG, "ENTERED FIND PARTICIPANTS");
		ParseQuery<ParseObject> queryGroup = ParseQuery.getQuery("Group");
		queryGroup.whereEqualTo("objectId", groupId);
		queryGroup.whereEqualTo("groupLeaderId", groupLeaderId);
		queryGroup.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> foundGroupList, ParseException e) {
				if (e == null) {
					if (foundGroupList.size() > 0) {
						Log.i(TAG, "SUCCESS:: Found Group with ID: " + groupId);
						ParseObject group = foundGroupList.get(0);
						@SuppressWarnings("unchecked")
						ArrayList<String> participants = (ArrayList<String>) group
								.get("groupParticipants");
						groupParticipants = participants;
						// Check that participants is correct
						for (int i = 0; i < groupParticipants.size(); i++) {
							Log.i(TAG, "" + i + ". Got Participant: "
									+ groupParticipants.get(i));
						}
						positionParticipants();
					} else {
						Log.e(TAG, "FAILURE:: No Group Found with ID: "
								+ groupId);
					}
				} else {
					Log.e(TAG, "ERROR:: ");
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * This method is used to position the geo-fence on the map. This is
	 * achieved by passing though the latitude and longitude values for the
	 * centre of the geo-fence (this will be the group leaders current
	 * location.)
	 * 
	 * @param latitude
	 *            a double value representing the latitude location of the
	 *            centre of the geo-fence
	 * @param longitude
	 *            a double value representing the longitude location of the
	 *            centre of the geo-fence
	 */
	private void createGeoFence(double latitude, double longitude) {
		ParseQuery<ParseObject> getRadius = ParseQuery.getQuery("Group");
		getRadius.whereEqualTo("objectId", groupId);
		try {
			List<ParseObject> currentGroupList = getRadius.find();
			ParseObject currentGroup = currentGroupList.get(0);
			int currentRadius = Integer.parseInt(currentGroup.get(
					"groupGeoFenceRadius").toString());
			Log.i(TAG, "Creating Geo-Fence with Radius: " + currentRadius
					+ " around Location: [" + latitude + "," + longitude + "]");
			if (geoFence == null) {
				geoFence = new CircleOptions();
			} else {
				// TODO: Remove hard coding...
				// 54.5821639,-5.9368431 --> latitude, longitude
				if (!initialGeoFenceDrawn) {
					geoFence.center(new LatLng(latitude, longitude))
							.radius(currentRadius)
							.fillColor(Color.parseColor("#f6a9f3"));
					circle = googleMap.addCircle(geoFence);
					initialGeoFenceDrawn = true;
				} else {
					// TODO: Remove hard coding...
					// 54.5821639,-5.9368431 --> latitude, longitude
					circle.setCenter(new LatLng(latitude, longitude));
					circle.setRadius(currentRadius);
				}
			}
		} catch (ParseException e) {
			Log.e(TAG, "ERROR:: Unable to fetch Group Radius");
			e.printStackTrace();
		}
	}

	/**
	 * This method is used to set the marker for the group leader. The group
	 * leader marker will be GREEN for both the group leader view and the group
	 * memeber view.
	 * 
	 * @param latitude
	 *            a double value representing the latitude location of the group
	 *            leader.
	 * @param longitude
	 *            a double value representing the longitude location of the
	 *            group leader.
	 * @param title
	 *            a String value representing the title that should be given to
	 *            the marker (i.e. Group Leader/My Location.)
	 */
	private void setMarker(double latitude, double longitude, String title) {
		Log.d(TAG, "Entering Set Marker...");
		Log.i(TAG, "Placing Group Leader at: " + latitude + "," + longitude);
		// TODO: Remove hard coding...
		// 54.5821639,-5.9368431 --> lat, lng
		Log.e(TAG, "PLACING GROUP LEADER MARKER NOW");
		LatLng currentPosition = new LatLng(latitude, longitude);
		googleMap.addMarker(groupLeaderMarker
				.position(currentPosition)
				.title(title)
				.draggable(false)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
		groupLeaderMarker.position(currentPosition);

		if (currentUser.get("groupLeader").equals(true)) {
			if (!mapInitialized) {
				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(currentPosition).zoom(10.5F).bearing(300F) // orientation
						.tilt(50F) // viewing angle
						.build();
				googleMap.animateCamera(CameraUpdateFactory
						.newCameraPosition(cameraPosition));
			}
		} else {
			Log.i(TAG, "You are not the Group Leader...");
			createGeoFence(latitude, longitude);
		}
	}

	/**
	 * This method is used to get the current location of the user.
	 */
	private void getCurrentLocation() {
		if (locationServices.canGetLocation) {
			Log.i(TAG, "SUCCESS:: Able to get location");
			lat = getCurrentLatitude();
			lng = getCurrentLongitude();
			Log.i(TAG, "LAT: " + lat + " LNG: " + lng);
		} else {
			Log.e(TAG, "ERROR:: Unable to get location");
			locationServices.showSettingAlert();
		}
	}

	/**
	 * This method is used to get the latitude value of the users location.
	 * 
	 * @return latitude a double value representing the users current latitude
	 *         location.
	 */
	private double getCurrentLatitude() {
		double latitude = locationServices.getLat();
		return latitude;
	}

	/**
	 * This method is used to get the longitude value of the users location.
	 * 
	 * @return longitude a double value representing the users current longitude
	 *         location.
	 */
	private double getCurrentLongitude() {
		double longitude = locationServices.getLng();
		return longitude;
	}

	/**
	 * This method is used to check if the user currently had an exiting unique
	 * location reference associated with them in the database. If an existing
	 * unique location reference cannot be found, a new one will be created.
	 */
	private void checkForExisitingLocationEntryForUser() {
		ParseQuery<ParseObject> existingLocationQuery = ParseQuery
				.getQuery("Location");
		existingLocationQuery.whereEqualTo("userId", userId);
		existingLocationQuery.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> existingLocationList,
					ParseException e) {
				if (e == null) {
					if (existingLocationList.size() > 0) {
						Log.i(TAG,
								"SUCCESS:: Found an existing location entry for user"
										+ userId);
						ParseObject currentLocationData = existingLocationList
								.get(0);
						userLocationObjectId = currentLocationData
								.getObjectId().toString();
						// Update existing Data with new Location Information
						currentLocationData.put("currentLat",
								String.valueOf(lat));
						currentLocationData.put("currentLng",
								String.valueOf(lng));
						currentLocationData
								.saveInBackground(new SaveCallback() {
									@Override
									public void done(ParseException e) {
										if (e == null) {
											Log.i(TAG,
													"SUCCESS:: Updated Location Data for User: "
															+ userId);
											initialiseMap();
										} else {
											Log.e(TAG,
													"ERROR:: Unable to Update Location Data for User: "
															+ userId);
											e.printStackTrace();
										}
									}
								});
					} else {
						Log.e(TAG,
								"INFO:: Unable to find an existing location entry for user: "
										+ userId);
						Log.i(TAG, "Create New Location Entry in DB");
						createNewLocationDataInDB();
					}
				} else {
					Log.e(TAG, "ERROR:: ");
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * This method is used to create a new location in the Location entity,
	 * which will then be associated with a user via the unique location
	 * identifier.
	 */
	protected void createNewLocationDataInDB() {
		ParseObject location = new ParseObject("Location");
		location.put("currentLat", String.valueOf(lat));
		location.put("currentLng", String.valueOf(lng));
		location.put("userId", userId);
		location.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					Log.i(TAG, "SUCCESS:: Saved Location Data for User: "
							+ userId);
					retrieveLocationObjectId();
				} else {
					Log.e(TAG,
							"ERROR:: Unable to Save NEW Location Data for User: "
									+ userId);
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * This method is used to retrieve the unique location reference for a
	 * specific location object within the Location entity in the database.
	 */
	protected void retrieveLocationObjectId() {
		ParseQuery<ParseObject> queryLocationId = ParseQuery
				.getQuery("Location");
		queryLocationId.whereEqualTo("userId", userId);
		queryLocationId.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> existingLocationList,
					ParseException e) {
				if (e == null) {
					if (existingLocationList.size() > 0) {
						Log.i(TAG,
								"SUCCESS:: Found Exisiting Location Data for User: "
										+ userId);
						ParseObject current = existingLocationList.get(0);
						userLocationObjectId = current.getObjectId().toString();
						addLocationToUserInformationInDB();
					} else {
						Log.e(TAG,
								"INFO:: Unable to find Location Data for user: "
										+ userId);
					}
				} else {
					Log.e(TAG, "ERROR:: ");
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * This method is used to update the unique location reference associated to
	 * the user by updation the value of 'currentLocation' in the User entity in
	 * the database.
	 */
	protected void addLocationToUserInformationInDB() {
		Log.i(TAG, "Updating User Entity with Location ID: "
				+ userLocationObjectId);
		currentUser.put("currentLocation", userLocationObjectId);
		currentUser.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					Log.i(TAG,
							"SUCCESS:: Successfully updated User with Location ID "
									+ userLocationObjectId);
					initialiseMap();
				} else {
					Log.e(TAG, "ERROR:: ");
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * This method is used to initialise the Google Map fragment for the screen.
	 */
	protected void initialiseMap() {
		Log.i(TAG, "Initializing Map... ");
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.groupMap)).getMap();
			googleMap.setOnMapLongClickListener(this);
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Unable to create map..", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * This is used by a GROUP MEMBER user to create their own marker.
	 */
	protected void createOwnCurrentLocationMarker() {
		Log.d(TAG, "Entering Create Location Marker...");
		if (ownMarker == null) {
			ownMarker = new MarkerOptions();
		}
	}

	/**
	 * This is used by a GROUP LEADER and GROUP MEMBER to create the marker
	 * associated with the group leader.
	 */
	protected void createGroupLeaderCurrentLocationMarker() {
		Log.d(TAG, "Entering Create Location Marker...");
		if (groupLeaderMarker == null) {
			groupLeaderMarker = new MarkerOptions();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.group_geo_fence_map, menu);
		terminateMenuItem = menu.findItem(R.id.action_terminateGroup);
		Log.i(TAG, "TERMINATE MENU ITEM = " + terminateMenuItem);
		terminateMenuItem.setVisible(currentUser.get("groupLeader")
				.equals(true));
		return super.onPrepareOptionsMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_terminateGroup) {
			terminateGroup();
		}
		return true;
	}

	/**
	 * This method is used by the GROUP LEADER when they select 'Terminate
	 * Group' from their menu. This method will set the 'groupLeader' user
	 * attribute to FALSE and then notify participants that the group has been
	 * terminated.
	 */
	private void terminateGroup() {
		setGroupLeaderStatus(false);
		notifyGroupMembers();
	}

	/**
	 * This method sets the value of the 'groupLeader' User attribute to the
	 * value of the boolean passed in.
	 * 
	 * @param groupLeaderStatus
	 *            a boolean value to represent if the user is a group leader or
	 *            not.
	 */
	protected void setGroupLeaderStatus(boolean groupLeaderStatus) {
		ParseUser currentUser = ParseUser.getCurrentUser();
		currentUser.put("groupLeader", groupLeaderStatus);
		try {
			currentUser.save();
			Log.i(TAG, "SUCCESS:: Updated 'groupLeader' value for user!");
		} catch (ParseException e) {
			Log.e(TAG, "ERROR:: Updating 'groupLeader' value for user!");
			e.printStackTrace();
		}
	}

	/**
	 * This method is used to create the notification that will be sent to all
	 * participants to notify them that the group leader has terminated the
	 * group.
	 */
	private void notifyGroupMembers() {
		for (int i = 0; i < participantNumbers.size(); i++) {
			ParseQuery<ParseUser> getParticipantUserId = ParseUser.getQuery();
			getParticipantUserId.whereEqualTo("username",
					participantNumbers.get(i));
			getParticipantUserId
					.findInBackground(new FindCallback<ParseUser>() {
						@Override
						public void done(List<ParseUser> foundUserList,
								ParseException e) {
							if (e == null) {
								if (foundUserList.size() > 0) {
									Log.i(TAG,
											"SUCCESS:: Found user for number");
									ParseUser user = foundUserList.get(0);
									String groupLeaderDisplayName = currentUser
											.get("displayName").toString();
									JSONObject terminationData = null;
									try {
										terminationData = new JSONObject(
												"{"
														+ "\"alert\":\""
														+ groupLeaderDisplayName
														+ " has Terminated the group.\", "
														+ "\"action\":\"com.kainos.groupsafe.GroupTerminationNotificationActivity\", "
														+ "\"title\": \"Group Termination!\"}");
										sendNotification(user.getObjectId()
												.toString(), terminationData);
									} catch (JSONException e1) {
										Log.e(TAG,
												"ERROR: Error Creating JSON for Temination Notification.");
										e1.printStackTrace();
									}
								} else {
									Log.e(TAG,
											"FAILURE:: Failed to find a user for number");
								}
							} else {
								Log.e(TAG, "ERROR:: ");
								e.printStackTrace();
							}
						}
					});
		}
		deleteGroup();
	}

	/**
	 * This method is used to send the terminated group notification. Once the
	 * notification has been sent the group leader will be returned to the Home
	 * screen for the application.
	 * 
	 * @param userId
	 *            the unique user identifer for the participant to whom the
	 *            notification is to be sent.
	 * @param terminationData
	 *            this is the JSON object containing the notification message to
	 *            be sent to the participants.
	 */
	protected void sendNotification(String userId, JSONObject terminationData) {
		String notificationChannel = "user_" + userId;
		Log.i(TAG, "#005: Channel = " + notificationChannel);
		ParsePush push = new ParsePush();
		push.setData(terminationData);
		push.setChannel(notificationChannel);
		push.sendInBackground();

		Intent intent = new Intent(_instance, HomeActivity.class);
		startActivity(intent);
		finish();
	}

	/**
	 * This method is used to delete the group from the Group entity in the
	 * database.
	 */
	private void deleteGroup() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
		query.whereEqualTo("objectId", groupId);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> foundGroups, ParseException e) {
				if (e == null) {
					if (foundGroups.size() > 0) {
						Log.i(TAG, "SUCCESS:: Found Group!");
						ParseObject group = foundGroups.get(0);
						group.deleteEventually(new DeleteCallback() {
							@Override
							public void done(ParseException e) {
								if (e == null) {
									Log.i(TAG,
											"SUCCESS:: Deleted Group From DB");
								} else {
									Log.e(TAG, "ERROR:: Failed to Delete Group");
									e.printStackTrace();
								}
							}
						});
					} else {
						Log.e(TAG, "FAILURE:: Failed to Find Group");
					}
				} else {
					Log.e(TAG, "ERROR::");
					e.printStackTrace();
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.gms.maps.GoogleMap.OnMapLongClickListener#onMapLongClick
	 * (com.google.android.gms.maps.model.LatLng)
	 */
	@Override
	public void onMapLongClick(LatLng point) {
		if (currentUser.get("groupLeader").equals(true)) {
			final Dialog dialog = new Dialog(context);
			dialog.setContentView(R.layout.custom_geofence_dialog);
			dialog.setTitle("Edit Geo-Fence Radius");
			updateButton = (Button) dialog
					.findViewById(R.id.dialogButtonUpdate);
			updateButton.setEnabled(true);
			updateButton.setClickable(true);
			customRadiusSpinner = (Spinner) dialog
					.findViewById(R.id.customRadiusSpinner);
			dialog.show();
			addListenerToSpinner();

			updateButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					updateButton.setEnabled(false);
					updateButton.setClickable(false);
					radius = Integer.parseInt(String
							.valueOf(customRadiusSpinner.getSelectedItem()));
					ParseQuery<ParseObject> getGroup = ParseQuery
							.getQuery("Group");
					getGroup.whereEqualTo("objectId", groupId);
					getGroup.findInBackground(new FindCallback<ParseObject>() {
						@Override
						public void done(List<ParseObject> foundGroupList,
								ParseException e) {
							if (e == null) {
								if (foundGroupList.size() > 0) {
									Log.i(TAG, "SUCCESS:: Found group!");
									ParseObject currentGroup = foundGroupList
											.get(0);
									currentGroup.put("groupGeoFenceRadius",
											radius);
									currentGroup
											.saveInBackground(new SaveCallback() {

												@Override
												public void done(
														ParseException e) {
													if (e == null) {
														Log.i(TAG,
																"Successfully Updated GeoFence for Group.");
														dialog.dismiss();
													} else {
														Log.e(TAG, "ERROR::");
														e.printStackTrace();
													}
												}
											});
								} else {
									Log.e(TAG, "FAILURE: unable to find group");
								}
							} else {
								Log.e(TAG, "ERROR:: ");
								e.printStackTrace();
							}
						}
					});
				}
			});
		}
	}

	/**
	 * This method is used to listen to the spinner(drop down) list on the
	 * Custom Radius Dialog which the group leader can access by long clicking
	 * on the map.
	 */
	private void addListenerToSpinner() {
		customRadiusSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parentView,
							View selectedItemView, int position, long id) {
						String tempRadius = parentView.getItemAtPosition(
								position).toString();
						Log.i(TAG, "You have selected: " + tempRadius);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parentView) {
						Log.i(TAG,
								"You have not selected a Radius. Resorting to Default Setting [10]");
					}
				});
	}
}
