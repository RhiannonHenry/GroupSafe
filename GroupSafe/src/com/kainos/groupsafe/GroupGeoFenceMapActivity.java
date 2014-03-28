package com.kainos.groupsafe;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

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
import com.parse.ParseAnalytics;
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

public class GroupGeoFenceMapActivity extends FragmentActivity implements
		OnMapLongClickListener {

	static GroupGeoFenceMapActivity _instance = null;
	final Context context = this;
	private final static Logger LOGGER = Logger
			.getLogger(GroupGeoFenceMapActivity.class.getName());
	private static final int EARTH_RADIUS_KM = 6371;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());

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

		LOGGER.info("GOT GROUP_ID: " + groupId);
		LOGGER.info("GOT GROUP_LEADER_ID: " + groupLeaderId);
		LOGGER.info("GOT RADIUS: " + radius);

		LOGGER.info("Entering START ACTIVITY LOGIC...");
		startActivityLogic();
	}

	@Override
	public void onResume() {
		LOGGER.info("UPDATING...");
		super.onResume();
		autoUpdate = new Timer();
		autoUpdate.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						if (mapInitialized) {
							if (currentUser.get("groupLeader").equals(true)) {
								LOGGER.info("Refreshing View for GROUP LEADER");
								// refresh view for group leader
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
										if (userObjectId != null) {
											checkIfParticipantInGeoFence(userObjectId);
										}
									} catch (ParseException e) {
										LOGGER.info("ERROR:: ");
										e.printStackTrace();
									}
								}
							} else {
								LOGGER.info("Refreshing View for GROUP MEMBER");
								// refresh view for group member
								createGroupLeaderCurrentLocationMarker();
								getLeaderLocationAndSetMarker();
								createOwnCurrentLocationMarker();
								setOwnMarker(lat, lng);
								for (int i = 0; i < participantNumbers.size(); i++) {
									getParticipantLocation(participantNumbers
											.get(i));
								}
							}
						} else {
							LOGGER.info("Map has not been initialized!");
						}
					}

					private void checkIfParticipantInGeoFence(
							String userObjectId) {
						MarkerOptions participant = participantLocationMarkers
								.get(userObjectId);
						LOGGER.info("Getting location for user: "+userObjectId);
						
						LatLng participantLocation = participant.getPosition();
						LOGGER.info("User is at location: "+participantLocation.latitude+","+participantLocation.longitude);
						LatLng geoFenceCenter = circle.getCenter();
						LOGGER.info("Center of Geo-Fence is at: "+geoFenceCenter.latitude+","+geoFenceCenter.longitude);

						// TODO: Get distance from Participant to Group Leader
						int meters = getDistanceDifference(participantLocation,
								geoFenceCenter);
						if (meters > radius) {
							// TODO: Send Notification
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
								LOGGER.info("ERROR: Error Creating JSON for Temination Notification.");
								e1.printStackTrace();
							}
							notifyGroupLeader(userObjectId);
						} else {
							LOGGER.info("PARTICIPANT IS WITHIN THE GEO-FENCE");
						}
					}

					private void sendAlertNotification(String userObjectId,
							JSONObject participantNotificationData) {
						String notificationChannel = "user_" + userObjectId;
						LOGGER.info("#004: Channel = " + notificationChannel);
						ParsePush push = new ParsePush();
						push.setData(participantNotificationData);
						push.setChannel(notificationChannel);
						push.sendInBackground();						
					}

					private void notifyGroupLeader(String userObjectId) {
						final Dialog dialog = new Dialog(context);
						dialog.setContentView(R.layout.custom_notification_dialog);
						dialog.setTitle("Exit Geo-Fence");
						okButton = (Button) dialog
								.findViewById(R.id.dialogNotificationOK);
						TextView message = (TextView) dialog
								.findViewById(R.id.dialogNotificationMessage);
						message.setText("Participant " + userObjectId
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

					private int getDistanceDifference(
							LatLng participantLocation, LatLng geoFenceCenter) {
						// Lat and Lng for PARTICIPANT
						double participantLatitude = participantLocation.latitude;
						double participantLongitude = participantLocation.longitude;

						// Lat and Lng for LEADER/CENTER OF GEOFENCE
						double geoFenceCenterLatitude = geoFenceCenter.latitude;
						double geoFenceCenterLongitude = geoFenceCenter.longitude;

						// Difference between Lat and Lng
						double deltaLatitude = Math
								.toRadians(participantLatitude
										- geoFenceCenterLatitude);
						double deltaLongitude = Math
								.toRadians(participantLongitude
										- geoFenceCenterLongitude);

						// Part 1:
						// sin^2(lat1-lat2/2)+cos(lat1)*cos(lat2)*sin^2(long1-long2/2)
						double part1 = Math.sin(deltaLatitude / 2)
								* Math.sin(deltaLatitude / 2)
								+ Math.cos(Math.toRadians(participantLatitude)
										* Math.toRadians(participantLongitude))
								* Math.sin(deltaLongitude / 2)
								* Math.sin(deltaLongitude / 2);
						// Part 2:
						// 2*sin^-1(sqrt(part1))
						double part2 = 2.0 * Math.asin(Math.sqrt(part1));

						// Part 3: Difference
						// radius*part2
						double difference = EARTH_RADIUS_KM * part2;
						LOGGER.info("Difference in KM: "+difference);
						DecimalFormat newFormat = new DecimalFormat("####");
						double meter = difference*1000;
						int meterInDec = Integer.valueOf(newFormat
								.format(meter));
						LOGGER.info("Distance from Center of Geo-fence = "
								+ meterInDec);
						return meterInDec;
					}
				});
			}
		}, 0, 30000); // updates every 30 seconds
	}

	@Override
	public void onPause() {
		autoUpdate.cancel();
		super.onPause();
	}

	private void startActivityLogic() {
		LOGGER.info("ENTERED START ACTIVITY LOGIC!");
		try {
			LOGGER.info("Initialising Map...");
			initialiseMap();
			getCurrentLocation();
			checkForExisitingLocationEntryForUser();

			if (currentUser.get("groupLeader").equals(true)) {
				LOGGER.info("GROUP LEADER FLOW");
				createGroupLeaderCurrentLocationMarker();
				setMarker(lat, lng, "My Location");
				createGeoFence(lat, lng);
				findParticipants();
			} else {
				LOGGER.info("GROUP MEMBER FLOW");
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

	private void setOwnMarker(double latitude, double longitude) {
		LOGGER.info("Entering Set Own Marker...");
		LOGGER.info("Placing marker at: " + lat + "," + lng);

		// TODO: Remove hard coding...
		// 54.5822043,-5.9380233 --> lat, lng
		LatLng currentPosition = new LatLng(54.5822043, -5.9380233);
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

	private void getLeaderLocationAndSetMarker() {
		// Get Group Leader current Location
		ParseQuery<ParseUser> getLeaderLocationId = ParseUser.getQuery();
		getLeaderLocationId.whereEqualTo("objectId", groupLeaderId);
		getLeaderLocationId.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> foundUserList, ParseException e) {
				if (e == null) {
					if (foundUserList.size() > 0) {
						LOGGER.info("SUCCESS:: Found the Group Leader User!");
						ParseUser groupLeaderUser = foundUserList.get(0);
						String groupLeaderLocationId = null;
						try {
							groupLeaderLocationId = groupLeaderUser.get(
									"currentLocation").toString();
						} catch (NullPointerException e1) {
							LOGGER.info("ERROR:: ");
							e1.printStackTrace();
						}
						if (groupLeaderLocationId == null) {
							LOGGER.info("Group Leader doesn't have a location ID");
						} else {
							getLocationOfGroupLeader(groupLeaderUser.get(
									"currentLocation").toString());
						}
					} else {
						LOGGER.info("FAILURE:: Failed to retrieve the group leader user ("
								+ groupLeaderId + ")");
					}
				} else {
					LOGGER.info("Error:: ");
					e.printStackTrace();
				}
			}
		});
	}

	protected void getLocationOfGroupLeader(String locationId) {
		// Get lat and lng of Group Leader Location
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
								LOGGER.info("SUCCESS:: Found the Location for Group Leader!");
								ParseObject groupLeaderLocation = foundLocationList
										.get(0);
								double groupLeaderLat = Double
										.parseDouble(groupLeaderLocation.get(
												"currentLat").toString());
								double groupLeaderLng = Double
										.parseDouble(groupLeaderLocation.get(
												"currentLng").toString());
								LOGGER.info("Got Location: " + groupLeaderLat
										+ "," + groupLeaderLng);
								// Set Marker For Group Leader
								setMarker(groupLeaderLat, groupLeaderLng,
										"Group Leader");
							} else {
								LOGGER.info("FAILURE:: Failed to retrieve the location for user ("
										+ groupLeaderId + ")");
							}
						} else {
							LOGGER.info("Error:: ");
							e.printStackTrace();
						}
					}
				});
	}

	private void positionParticipants() {
		LOGGER.info("Entered GET PARTICIPANTS NUMBER: ");
		for (int i = 0; i < groupParticipants.size(); i++) {
			LOGGER.info("Getting number for participant: "
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
									LOGGER.info("SUCCESS:: Found Participant");
									ParseObject current = foundParticipantList
											.get(0);
									String currentParticipantNumber = current
											.get("participantNumber")
											.toString();
									LOGGER.info("Got Participant Number: "
											+ currentParticipantNumber);
									if (currentParticipantNumber
											.equals(currentUser.getUsername())) {
										LOGGER.info("This participant is the same as the logged in user, they have already retreived and diaplayed their location. Continuing...");
									} else {
										participantNumbers
												.add(currentParticipantNumber);
										getParticipantLocation(currentParticipantNumber);
									}

								} else {
									LOGGER.info("FAILURE:: Unable to find Participant");
								}
							} else {
								LOGGER.info("ERROR:: ");
								e.printStackTrace();
							}
						}
					});
		}
		LOGGER.info("MAP HAS BEEN INITIALIZED!!");
		mapInitialized = true;
	}

	protected void getParticipantLocation(String participantNumber) {
		LOGGER.info("Entered GET PARTICIPANT LOCATION...for participant with number: "
				+ participantNumber);
		ParseQuery<ParseUser> userLocationQuery = ParseUser.getQuery();
		userLocationQuery.whereEqualTo("username", participantNumber);
		userLocationQuery.findInBackground(new FindCallback<ParseUser>() {

			@Override
			public void done(List<ParseUser> foundUserList, ParseException e) {
				if (e == null) {
					if (foundUserList.size() > 0) {
						LOGGER.info("SUCCESS:: Found User for Participant");
						ParseUser current = foundUserList.get(0);
						String userLocation = null;
						try {
							userLocation = current.get("currentLocation")
									.toString();
						} catch (NullPointerException e1) {
							LOGGER.info("Participant doen't have a location");
							e1.printStackTrace();
						}

						if (userLocation == null) {
							LOGGER.info("User Location is NULL");
						} else {
							LOGGER.info("Got location ID: " + userLocation);
							LOGGER.info("Adding Location ID to participantLocations array...");
							getParticipantLatLng(userLocation);
						}
					} else {
						LOGGER.info("FAILURE:: Unable to find User for Participant");
					}
				} else {
					LOGGER.info("ERROR::");
					e.printStackTrace();
				}
			}
		});
	}

	protected void getParticipantLatLng(String userLocationID) {
		ParseQuery<ParseObject> getLocationQuery = ParseQuery
				.getQuery("Location");
		getLocationQuery.whereEqualTo("objectId", userLocationID);
		getLocationQuery.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> foundLocationList,
					ParseException e) {
				if (e == null) {
					if (foundLocationList.size() > 0) {
						LOGGER.info("SUCCESS:: Found Location");
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
						if (latitude == 0 && longitude == 0) {
							latitude = 54.5871171;
							longitude = -5.9338856;
						}
						LOGGER.info("Creating Marker Options with Location: "
								+ latitude + "," + longitude);
						LOGGER.info("For Participant with userID: " + userId);
						LatLng currentPosition = new LatLng(latitude, longitude);
						if (participantLocationMarkers.containsKey(userId)) {
							// Fetch old participant marker and update position
							MarkerOptions currentMarker = participantLocationMarkers
									.get(userId);
							if (currentMarker == null) {
								currentMarker = new MarkerOptions();
								googleMap.addMarker(currentMarker
										.position(currentPosition)
										.title("Participant " + userId)
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
									.title("Participant " + userId)
									.draggable(false)
									.icon(BitmapDescriptorFactory
											.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));

							participantLocationMarkers.put(userId,
									participantMarker);
						}
					} else {
						LOGGER.info("FAILURE:: Unable to find a location");
					}
				} else {
					LOGGER.info("ERROR::");
					e.printStackTrace();
				}
			}
		});
	}

	private void findParticipants() {
		LOGGER.info("ENTERED FIND PARTICIPANTS");
		ParseQuery<ParseObject> queryGroup = ParseQuery.getQuery("Group");
		queryGroup.whereEqualTo("objectId", groupId);
		queryGroup.whereEqualTo("groupLeaderId", groupLeaderId);
		queryGroup.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> foundGroupList, ParseException e) {
				if (e == null) {
					if (foundGroupList.size() > 0) {
						LOGGER.info("SUCCESS:: Found Group with ID: " + groupId);
						ParseObject group = foundGroupList.get(0);
						@SuppressWarnings("unchecked")
						ArrayList<String> participants = (ArrayList<String>) group
								.get("groupParticipants");
						groupParticipants = participants;
						// Check that participants is correct
						for (int i = 0; i < groupParticipants.size(); i++) {
							LOGGER.info("" + i + ". Got Participant: "
									+ groupParticipants.get(i));
						}
						positionParticipants();

					} else {
						LOGGER.info("FAILURE:: No Group Found with ID: "
								+ groupId);
					}
				} else {
					LOGGER.info("ERROR:: ");
					e.printStackTrace();
				}
			}
		});
	}

	private void createGeoFence(double latitude, double longitude) {
		ParseQuery<ParseObject> getRadius = ParseQuery.getQuery("Group");
		getRadius.whereEqualTo("objectId", groupId);
		try {
			List<ParseObject> currentGroupList = getRadius.find();
			ParseObject currentGroup = currentGroupList.get(0);
			int currentRadius = Integer.parseInt(currentGroup.get(
					"groupGeoFenceRadius").toString());
			LOGGER.info("Creating Geo-Fence with Radius: " + currentRadius
					+ " around Location: [" + latitude + "," + longitude + "]");
			if (geoFence == null) {
				geoFence = new CircleOptions();
			} else {
				// TODO: Remove hard coding...
				// 54.5821639,-5.9368431 --> latitude, longitude
				if (!initialGeoFenceDrawn) {
					geoFence.center(new LatLng(54.5821639, -5.9368431))
							.radius(currentRadius)
							.fillColor(Color.parseColor("#f6a9f3"));
					circle = googleMap.addCircle(geoFence);
					initialGeoFenceDrawn = true;
				} else {
					// TODO: Remove hard coding...
					// 54.5821639,-5.9368431 --> latitude, longitude
					circle.setCenter(new LatLng(54.5821639, -5.9368431));
					circle.setRadius(currentRadius);
				}

			}
		} catch (ParseException e) {
			LOGGER.info("ERROR:: Unable to fetch Group Radius");
			e.printStackTrace();
		}
	}

	private void setMarker(double latitude, double longitude, String title) {
		LOGGER.info("Entering Set Marker...");
		LOGGER.info("Placing Group Leader at: " + latitude + "," + longitude);
		LOGGER.info("Google Map = " + googleMap.toString());
		LOGGER.info("Group Leader Marker = "
				+ groupLeaderMarker.describeContents());

		// TODO: Remove hard coding...
		// 54.5821639,-5.9368431 --> lat, lng
		LatLng currentPosition = new LatLng(54.5821639, -5.9368431);
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
			LOGGER.info("You are not the Group Leader...");
			createGeoFence(latitude, longitude);
		}
	}

	private void getCurrentLocation() {
		if (locationServices.canGetLocation) {
			LOGGER.info("SUCCESS:: Able to get location");
			lat = getCurrentLatitude();
			lng = getCurrentLongitude();
			LOGGER.info("LAT: " + lat + " LNG: " + lng);
		} else {
			LOGGER.info("ERROR:: Unable to get location");
			locationServices.showSettingAlert();
		}
	}

	private double getCurrentLatitude() {
		double latitude = locationServices.getLat();
		return latitude;
	}

	private double getCurrentLongitude() {
		double longitude = locationServices.getLng();
		return longitude;
	}

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
						LOGGER.info("SUCCESS:: Found an existing location entry for user"
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
											LOGGER.info("SUCCESS:: Updated Location Data for User: "
													+ userId);
											initialiseMap();
										} else {
											LOGGER.info("ERROR:: Unable to Update Location Data for User: "
													+ userId);
											e.printStackTrace();
										}
									}
								});
					} else {
						LOGGER.info("INFO:: Unable to find an existing location entry for user: "
								+ userId);
						LOGGER.info("Create New Location Entry in DB");
						createNewLocationDataInDB();
					}
				} else {
					LOGGER.info("ERROR:: ");
					e.printStackTrace();
				}
			}
		});
	}

	protected void createNewLocationDataInDB() {
		ParseObject location = new ParseObject("Location");
		location.put("currentLat", String.valueOf(lat));
		location.put("currentLng", String.valueOf(lng));
		location.put("userId", userId);
		location.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					LOGGER.info("SUCCESS:: Saved Location Data for User: "
							+ userId);
					retrieveLocationObjectId();
				} else {
					LOGGER.info("ERROR:: Unable to Save NEW Location Data for User: "
							+ userId);
					e.printStackTrace();
				}
			}
		});
	}

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
						LOGGER.info("SUCCESS:: Found Exisiting Location Data for User: "
								+ userId);
						ParseObject current = existingLocationList.get(0);
						userLocationObjectId = current.getObjectId().toString();
						addLocationToUserInformationInDB();
					} else {
						LOGGER.info("INFO:: Unable to find Location Data for user: "
								+ userId);
					}
				} else {
					LOGGER.info("ERROR:: ");
					e.printStackTrace();
				}
			}
		});

	}

	protected void addLocationToUserInformationInDB() {
		LOGGER.info("Updating User Entity with Location ID: "
				+ userLocationObjectId);
		currentUser.put("currentLocation", userLocationObjectId);
		currentUser.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					LOGGER.info("SUCCESS:: Successfully updated User with Location ID "
							+ userLocationObjectId);
					initialiseMap();
				} else {
					LOGGER.info("ERROR:: ");
					e.printStackTrace();
				}
			}
		});
	}

	protected void initialiseMap() {
		LOGGER.info("Initializing Map... ");
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

	protected void createOwnCurrentLocationMarker() {
		LOGGER.info("Entering Create Location Marker...");
		if (ownMarker == null) {
			ownMarker = new MarkerOptions();
		}
	}

	protected void createGroupLeaderCurrentLocationMarker() {
		LOGGER.info("Entering Create Location Marker...");
		if (groupLeaderMarker == null) {
			groupLeaderMarker = new MarkerOptions();
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.group_geo_fence_map, menu);
		terminateMenuItem = menu.findItem(R.id.action_terminateGroup);
		LOGGER.info("TERMINATE MENU ITEM = " + terminateMenuItem);
		terminateMenuItem.setVisible(currentUser.get("groupLeader")
				.equals(true));
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_terminateGroup) {
			terminateGroup();
		}
		return true;
	}

	private void terminateGroup() {
		setGroupLeaderStatus(false);
		notifyGroupMembers();
	}

	protected void setGroupLeaderStatus(boolean groupLeaderStatus) {
		ParseUser currentUser = ParseUser.getCurrentUser();
		currentUser.put("groupLeader", groupLeaderStatus);
		try {
			currentUser.save();
			LOGGER.info("SUCCESS:: Updated 'groupLeader' value for user!");
		} catch (ParseException e) {
			LOGGER.info("ERROR:: Updating 'groupLeader' value for user!");
			e.printStackTrace();
		}
	}

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
									LOGGER.info("SUCCESS:: Found user for number");
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
										LOGGER.info("ERROR: Error Creating JSON for Temination Notification.");
										e1.printStackTrace();
									}
								} else {
									LOGGER.info("FAILURE:: Failed to find a user for number");
								}
							} else {
								LOGGER.info("ERROR:: ");
								e.printStackTrace();
							}
						}
					});

		}
		deleteGroup();
	}

	protected void sendNotification(String userId, JSONObject terminationData) {
		String notificationChannel = "user_" + userId;
		LOGGER.info("#004: Channel = " + notificationChannel);
		ParsePush push = new ParsePush();
		push.setData(terminationData);
		push.setChannel(notificationChannel);
		push.sendInBackground();

		Intent intent = new Intent(_instance, HomeActivity.class);
		startActivity(intent);
		finish();
	}

	private void deleteGroup() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
		query.whereEqualTo("objectId", groupId);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> foundGroups, ParseException e) {
				if (e == null) {
					if (foundGroups.size() > 0) {
						LOGGER.info("SUCCESS:: Found Group!");
						ParseObject group = foundGroups.get(0);
						group.deleteEventually(new DeleteCallback() {
							@Override
							public void done(ParseException e) {
								if (e == null) {
									LOGGER.info("SUCCESS:: Deleted Group From DB");
								} else {
									LOGGER.info("ERROR:: Failed to Delete Group");
									e.printStackTrace();
								}
							}
						});
					} else {
						LOGGER.info("FAILURE:: Failed to Find Group");
					}
				} else {
					LOGGER.info("ERROR::");
					e.printStackTrace();
				}
			}
		});
	}

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
									LOGGER.info("SUCCESS:: Found group!");
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
														LOGGER.info("Successfully Updated GeoFence for Group.");
														dialog.dismiss();
													} else {
														LOGGER.info("ERROR::");
														e.printStackTrace();
													}
												}
											});
								} else {
									LOGGER.info("FAILURE: unable to find group");
								}
							} else {
								LOGGER.info("ERROR:: ");
								e.printStackTrace();
							}
						}
					});
				}
			});
		}
	}

	private void addListenerToSpinner() {
		customRadiusSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parentView,
							View selectedItemView, int position, long id) {
						String tempRadius = parentView.getItemAtPosition(
								position).toString();
						LOGGER.info("You have selected: " + tempRadius);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parentView) {
						LOGGER.info("You have not selected a Radius. Resorting to Default Setting [10]");
					}
				});
	}
}
