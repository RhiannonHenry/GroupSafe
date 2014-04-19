package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.List;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Activity that will be displayed to the user if they select an emergency
 * contact from their emergency contact list on the Settings screen. This
 * activity allows a user to view details about a specific emergency contact and
 * also delete a specific emergency contact from their emergency contact address
 * book.
 * 
 * Layout: @see activity__emergency_contact_details.xml Menu: @see
 * emergency_contact_details.xml
 * 
 * @author Rhiannon Henry
 * 
 */
public class EmergencyContactDetailsActivity extends Activity {

	private static final String TAG = "Detailed_Emergency_Contact_Activity";
	private static EmergencyContactDetailsActivity _instance = null;
	private static String emergencyContactName = null;
	private static String emergencyContactNumber = null;
	private static String emergencyContactRelationship = null;
	private String objectId = null;
	private boolean internetPresent = false;
	private ConnectionDetector connectionDetector;

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
		Intent intent = getIntent();
		emergencyContactName = intent.getStringExtra("name");
		emergencyContactNumber = intent.getStringExtra("number");
		emergencyContactRelationship = intent.getStringExtra("relationship");

		Log.i(TAG, "Got Emergenct Contact: " + emergencyContactName + ", "
				+ emergencyContactNumber + ", " + emergencyContactRelationship
				+ " from SettingsActivity");
		this.setTitle(emergencyContactName);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emergency_contact_details);
		_instance = this;
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			Log.i(TAG, "Populating page...");
			updatePageFields();
		} else {
			Utilities.showNoInternetConnectionDialog(this);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		GroupSafeApplication.activityResumed();
	}

	@Override
	protected void onPause() {
		super.onPause();
		GroupSafeApplication.activityPaused();
	}


	/**
	 * This method is used to update the page with the relevant information
	 * retrieved from the intent (passed through from the settings page) such as
	 * name, number and relationship of the emergency contact
	 */
	protected void updatePageFields() {
		TextView eContactNameView = (TextView) findViewById(R.id.detailedEmergencyContactName);
		TextView eContactNumberView = (TextView) findViewById(R.id.detailedEmergencyContactNumber);
		TextView eContactRelationshipView = (TextView) findViewById(R.id.detailedEmergencyContactRelationship);
		Log.d(TAG, "Emergency Contact Name: " + emergencyContactName);
		Log.d(TAG, "Emergency Contact Number: " + emergencyContactNumber);
		Log.d(TAG, "Emergency Contact Relationship: "
				+ emergencyContactRelationship);
		eContactNameView.setText(emergencyContactName);
		eContactNumberView.setText(emergencyContactNumber);
		eContactRelationshipView.setText(emergencyContactRelationship);
	}

	/**
	 * This method will be called if the user clicks on the 'OK' button @see
	 * activity_emergency_contact_details.xml. The user will be returned to the
	 * Settings activity.
	 * 
	 * @param view
	 *            the base class for widgets, which are used to create
	 *            interactive UI components (buttons, text fields, etc.).
	 */
	public void OK(View view) {
		disableAllButtons();
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			Log.i(TAG, "User clicked OK. Going to Settings Page...");
			Intent intent = new Intent(_instance, SettingsActivity.class);
			startActivity(intent);
			finish();
		} else {
			Utilities.showNoInternetConnectionDialog(this);
			enableAllButtons();
		}
	}

	/**
	 * This method will be called if the user clicks on the 'Delete' button @see
	 * activity_emergency_contact_details.xml. This will remove the emergency
	 * contact from the users list of emergency contacts.
	 * 
	 * @param view
	 *            the base class for widgets, which are used to create
	 *            interactive UI components (buttons, text fields, etc.).
	 */
	public void delete(View view) {
		disableAllButtons();
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			ParseQuery<ParseObject> emergencyContactIDQuery = ParseQuery
					.getQuery("EmergencyContact");
			emergencyContactIDQuery.whereEqualTo("emergencyContactName",
					emergencyContactName);
			emergencyContactIDQuery.whereEqualTo("emergencyContactNumber",
					emergencyContactNumber);
			emergencyContactIDQuery.whereEqualTo(
					"emergencyContactRelationship",
					emergencyContactRelationship);
			emergencyContactIDQuery
					.findInBackground(new FindCallback<ParseObject>() {
						@Override
						public void done(
								List<ParseObject> emergencyContactList,
								ParseException e) {
							if (e == null) {
								Log.i(TAG,
										"Retrieved: "
												+ emergencyContactList.size()
												+ " contacts");
								for (int i = 0; i < emergencyContactList.size(); i++) {
									ParseObject current = emergencyContactList
											.get(i);
									if (current.get("emergencyContactName")
											.equals(emergencyContactName)
											&& current
													.get("emergencyContactNumber")
													.equals(emergencyContactNumber)
											&& current
													.get("emergencyContactRelationship")
													.equals(emergencyContactRelationship)) {
										objectId = current.getObjectId();
										removeContactFromUserList();
									}
								}
							} else {
								Log.e(TAG,
										"UNABLE TO FIND CONTACT ID TO REMOVE FROM USER CONTACT LIST");
								e.printStackTrace();
							}
						}

						/**
						 * This method is responsible for fetching the user list
						 * of emergency contacts and removing the selected
						 * emergency contact from that list. The user will then
						 * be returned to theSettings activity.
						 */
						private void removeContactFromUserList() {
							final ParseUser currentUser = ParseUser
									.getCurrentUser();
							currentUser.get("emergencyContacts");
							currentUser
									.fetchInBackground(new GetCallback<ParseObject>() {
										@Override
										public void done(
												ParseObject emergencyContacts,
												ParseException e) {
											if (e == null) {
												Log.i(TAG,
														"Emergency Contacts: "
																+ emergencyContacts
																		.get("emergencyContacts")
																		.toString());
												@SuppressWarnings("unchecked")
												ArrayList<String> emergencyContactArray = (ArrayList<String>) emergencyContacts
														.get("emergencyContacts");
												for (int i = 0; i < emergencyContactArray
														.size(); i++) {
													Log.i(TAG,
															"Current contact: "
																	+ emergencyContactArray
																			.get(i));
													if (emergencyContactArray
															.get(i).equals(
																	objectId)) {
														emergencyContactArray
																.remove(i);
													}
												}
												currentUser.put(
														"emergencyContacts",
														emergencyContactArray);
												try {
													emergencyContacts.save();
													Intent intent = new Intent(
															_instance,
															SettingsActivity.class);
													startActivity(intent);
													finish();
												} catch (ParseException e1) {
													Log.e(TAG,
															"UNABLE TO SAVE USER AFTER REMOVING EMERGENCY CONTACT");
													e1.printStackTrace();
												}
											}

										}
									});
						}
					});
		} else {
			Utilities.showNoInternetConnectionDialog(this);
			enableAllButtons();
		}
	}

	/**
	 * Enables all buttons that are on the EmergencyContactDetailsActivity.java
	 * view.
	 * 
	 * @see activity_emergency_contact_details.xml
	 */
	private void enableAllButtons() {
		Button detailedEmergencyContactOKButton = (Button) findViewById(R.id.detailedEmergencyContactOKButton);
		Button detailedEmergencyContactDeleteButton = (Button) findViewById(R.id.detailedEmergencyContactDeleteButton);

		detailedEmergencyContactOKButton.setClickable(true);
		detailedEmergencyContactOKButton.setEnabled(true);
		detailedEmergencyContactDeleteButton.setClickable(true);
		detailedEmergencyContactDeleteButton.setEnabled(true);
	}

	/**
	 * Disables all buttons that are on the EmergencyContactDetailsActivity.java
	 * view.
	 * 
	 * @see activity_emergency_contact_details.xml
	 */
	private void disableAllButtons() {
		Button detailedEmergencyContactOKButton = (Button) findViewById(R.id.detailedEmergencyContactOKButton);
		Button detailedEmergencyContactDeleteButton = (Button) findViewById(R.id.detailedEmergencyContactDeleteButton);

		detailedEmergencyContactOKButton.setClickable(false);
		detailedEmergencyContactOKButton.setEnabled(false);
		detailedEmergencyContactDeleteButton.setClickable(false);
		detailedEmergencyContactDeleteButton.setEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.emergency_contact_details, menu);
		return true;
	}
}
