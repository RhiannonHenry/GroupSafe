package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class EmergencyContactDetailsActivity extends Activity {

	private final static Logger LOGGER = Logger
			.getLogger(EmergencyContactDetailsActivity.class.getName());
	static EmergencyContactDetailsActivity _instance = null;
	private static String emergencyContactName = null;
	private static String emergencyContactNumber = null;
	private static String emergencyContactRelationship = null;

	private String objectId = null;
	private boolean internetPresent = false;
	ConnectionDetector connectionDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");

		connectionDetector = new ConnectionDetector(getApplicationContext());

		Intent intent = getIntent();
		emergencyContactName = intent.getStringExtra("name");
		emergencyContactNumber = intent.getStringExtra("number");
		emergencyContactRelationship = intent.getStringExtra("relationship");

		LOGGER.info("Got Emergenct Contact: " + emergencyContactName + ", "
				+ emergencyContactNumber + ", " + emergencyContactRelationship
				+ " from SettingsActivity");

		this.setTitle(emergencyContactName);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emergency_contact_details);
		_instance = this;

		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			LOGGER.info("Populating page...");
			updatePageFields();
		} else {
			showNoInternetConnectionDialog();
		}
	}

	protected void updatePageFields() {
		TextView eContactNameView = (TextView) findViewById(R.id.detailedEmergencyContactName);
		TextView eContactNumberView = (TextView) findViewById(R.id.detailedEmergencyContactNumber);
		TextView eContactRelationshipView = (TextView) findViewById(R.id.detailedEmergencyContactRelationship);
		LOGGER.info("Emergency Contact Name: " + emergencyContactName);
		LOGGER.info("Emergency Contact Number: " + emergencyContactNumber);
		LOGGER.info("Emergency Contact Relationship: "
				+ emergencyContactRelationship);
		eContactNameView.setText(emergencyContactName);
		eContactNumberView.setText(emergencyContactNumber);
		eContactRelationshipView.setText(emergencyContactRelationship);
	}

	public void OK(View view) {
		disableAllButtons();
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			LOGGER.info("User clicked OK. Going to Settings Page...");
			Intent intent = new Intent(getApplicationContext(),
					SettingsActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
			enableAllButtons();
		}
	}

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
								LOGGER.info("Retrieved: "
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
								LOGGER.info("UNABLE TO FIND CONTACT ID TO REMOVE FROM USER CONTACT LIST");
							}
						}

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
												// Get the Array of Emergency
												// Contacts for the user
												LOGGER.info("Emergency Contacts: "
														+ emergencyContacts
																.get("emergencyContacts")
																.toString());
												@SuppressWarnings("unchecked")
												ArrayList<String> emergencyContactArray = (ArrayList<String>) emergencyContacts
														.get("emergencyContacts");
												for (int i = 0; i < emergencyContactArray
														.size(); i++) {
													LOGGER.info("Current contact: "
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
															getApplicationContext(),
															SettingsActivity.class);
													startActivity(intent);
													finish();
												} catch (ParseException e1) {
													LOGGER.info("UNABLE TO SAVE USER AFTER REMOVING EMERGENCY CONTACT");
													e1.printStackTrace();
												}
											}

										}
									});
						}
					});
		} else {
			showNoInternetConnectionDialog();
			enableAllButtons();
		}
	}

	private void enableAllButtons() {
		Button detailedEmergencyContactOKButton = (Button) findViewById(R.id.detailedEmergencyContactOKButton);
		Button detailedEmergencyContactDeleteButton = (Button) findViewById(R.id.detailedEmergencyContactDeleteButton);

		detailedEmergencyContactOKButton.setClickable(true);
		detailedEmergencyContactOKButton.setEnabled(true);
		detailedEmergencyContactDeleteButton.setClickable(true);
		detailedEmergencyContactDeleteButton.setEnabled(true);
	}

	private void disableAllButtons() {
		Button detailedEmergencyContactOKButton = (Button) findViewById(R.id.detailedEmergencyContactOKButton);
		Button detailedEmergencyContactDeleteButton = (Button) findViewById(R.id.detailedEmergencyContactDeleteButton);

		detailedEmergencyContactOKButton.setClickable(false);
		detailedEmergencyContactOKButton.setEnabled(false);
		detailedEmergencyContactDeleteButton.setClickable(false);
		detailedEmergencyContactDeleteButton.setEnabled(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.emergency_contact_details, menu);
		return true;
	}

	private void showNoInternetConnectionDialog() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("Internet Settings");
		alertDialog
				.setMessage("Cannot connect to internet. Enable Internet Services in Settings.");

		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Settings.ACTION_SETTINGS);
						startActivity(intent);
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

}
