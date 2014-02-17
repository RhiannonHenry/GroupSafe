package com.kainos.groupsafe;

import java.util.List;
import java.util.logging.Logger;

import com.kainos.groupsafe.utilities.ConnectionDetector;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddEmergencyContactActivity extends Activity {

	private final static Logger LOGGER = Logger
			.getLogger(AddEmergencyContactActivity.class.getName());
	static AddEmergencyContactActivity _instance = null;

	private static String emergencyContactName = null;
	private static String emergencyContactNumber = null;
	private static String emergencyContactRelationship = null;
	private static String newEmergencyContactObjectID = null;

	private EditText addEmergencyContactName;
	private EditText addEmergencyContactNumber;
	private EditText addEmergencyContactRelationship;

	private boolean internetPresent = false;
	ConnectionDetector connectionDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");

		connectionDetector = new ConnectionDetector(getApplicationContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_emergency_contact);
		_instance = this;

		enableAllButtons();

		cancelButtonClicked();
		saveButtonClicked();
	}

	private void saveButtonClicked() {
		Button saveEmergencyContact = (Button) findViewById(R.id.addEmergencyContactSaveButton);
		saveEmergencyContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				disableAllButtons();
				internetPresent = connectionDetector.isConnectedToInternet();
				if (internetPresent) {
					addEmergencyContactToDatabase();
				} else {
					showNoInternetConnectionDialog();
					enableAllButtons();
				}

			}
		});
	}

	protected void addEmergencyContactToDatabase() {
		addEmergencyContactName = (EditText) findViewById(R.id.addEmergencyContactNameInput);
		addEmergencyContactNumber = (EditText) findViewById(R.id.addEmergencyContactNumberInput);
		addEmergencyContactRelationship = (EditText) findViewById(R.id.addEmergencyContactRelationshipInput);
		emergencyContactName = addEmergencyContactName.getText().toString();
		emergencyContactNumber = addEmergencyContactNumber.getText().toString();
		emergencyContactRelationship = addEmergencyContactRelationship
				.getText().toString();

		ParseQuery<ParseObject> query = ParseQuery.getQuery("EmergencyContact");
		query.whereEqualTo("emergencyContactName", emergencyContactName);
		query.whereEqualTo("emergencyContactNumber", emergencyContactNumber);
		query.whereEqualTo("emergencyContactRelationship",
				emergencyContactRelationship);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> foundEmergencyContacts,
					ParseException e) {
				if (e == null) {
					if (foundEmergencyContacts.size() != 0) {
						LOGGER.info("A MATCHING EMERGENCY CONTACT ALREADY EXISTS");
						ParseObject contact = foundEmergencyContacts.get(0);
						LOGGER.info("FOUND: "+contact.getObjectId());
						retrieveObjectID();
					} else {
						LOGGER.info("THIS IS A NEW EMERGENCY CONATCT");
						createNewContactInTable();
					}
				}
			}
		});
	}

	protected void createNewContactInTable() {
		ParseObject contact = new ParseObject("EmergencyContact");
		contact.put("emergencyContactName", emergencyContactName);
		contact.put("emergencyContactNumber", emergencyContactNumber);
		contact.put("emergencyContactRelationship", emergencyContactRelationship);
		try {
			contact.save();
		} catch (ParseException e2) {
			LOGGER.info("UNABLE TO SAVE NEW EMERGENCY CONTACT AT THIS TIME...");
			e2.printStackTrace();
		}
		retrieveObjectID();

	}

	protected void retrieveObjectID() {
		ParseQuery<ParseObject> emergencyContactID = ParseQuery
				.getQuery("EmergencyContact");
		emergencyContactID.whereEqualTo("emergencyContactNumber",
				emergencyContactNumber);
		emergencyContactID.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> emergencyContactList,
					ParseException e) {
				if (e == null) {
					LOGGER.info("Retrieved: " + emergencyContactList.size()
							+ " emergency contacts");
					for (int i = 0; i < emergencyContactList.size(); i++) {
						ParseObject current = emergencyContactList.get(i);
						LOGGER.info("Emergency Contact " + i + ": Name: "
								+ current.get("emergencyContactName")
								+ " Number: "
								+ current.get("emergencyContactNumber")
								+ " Relationship: "
								+ current.get("emergencyContactRelationship")
								+ " ObjectID: " + current.getObjectId());
						if (current.get("emergencyContactName").equals(
								emergencyContactName)
								&& current.get("emergencyContactNumber")
										.equals(emergencyContactNumber)
								&& current.get("emergencyContactRelationship")
										.equals(emergencyContactRelationship)) {
							newEmergencyContactObjectID = current.getObjectId();
							addContactToUserEmergencyContactList(newEmergencyContactObjectID);
						} else {
							LOGGER.info("NO MATCH FOR EMERGENCY CONTACT");
						}
					}
				} else {
					LOGGER.info("UNABLE TO RETRIEVE CONTACT FROM CONTACT TABLE");
				}
			}

			private void addContactToUserEmergencyContactList(
					String newEmergencyContactObjectID) {
				LOGGER.info("New Emergency Contact ObjectID: "
						+ newEmergencyContactObjectID);
				LOGGER.info("Starting query on user emergency contact list...");
				ParseUser currentUser = ParseUser.getCurrentUser();
				currentUser.addUnique("emergencyContacts",
						newEmergencyContactObjectID);
				currentUser.saveInBackground(new SaveCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null) {
							LOGGER.info("SAVED EMERGENCY CONTACT SUCCESSFULLY");
							Toast.makeText(getApplicationContext(),
									"Saved Emergency Contact Successfully!",
									Toast.LENGTH_LONG).show();
							Intent intent = new Intent(_instance,
									SettingsActivity.class);
							startActivity(intent);
							finish();
						} else {
							Toast.makeText(getApplicationContext(),
									"Unable to Save Emergency Contact...",
									Toast.LENGTH_LONG).show();
							LOGGER.info("ENCOUNTERED ERROR SAVING EMERGENCY CONTACT");
							LOGGER.info(e.getMessage());
						}
					}
				});
			}
		});
	}

	private void cancelButtonClicked() {
		Button cancelEmergencyContact = (Button) findViewById(R.id.addEmergencyContactCancelButton);
		cancelEmergencyContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				disableAllButtons();
				internetPresent = connectionDetector.isConnectedToInternet();
				if (internetPresent) {
					Intent intent = new Intent(_instance,
							SettingsActivity.class);
					startActivity(intent);
					finish();
				} else {
					showNoInternetConnectionDialog();
					enableAllButtons();
				}
			}
		});
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

	private void disableAllButtons() {
		Button saveEmergencyContact = (Button) findViewById(R.id.addEmergencyContactSaveButton);
		Button cancelEmergencyContact = (Button) findViewById(R.id.addEmergencyContactCancelButton);

		saveEmergencyContact.setClickable(false);
		saveEmergencyContact.setEnabled(false);
		cancelEmergencyContact.setClickable(false);
		cancelEmergencyContact.setEnabled(false);
	}

	private void enableAllButtons() {
		Button saveEmergencyContact = (Button) findViewById(R.id.addEmergencyContactSaveButton);
		Button cancelEmergencyContact = (Button) findViewById(R.id.addEmergencyContactCancelButton);

		saveEmergencyContact.setClickable(true);
		saveEmergencyContact.setEnabled(true);
		cancelEmergencyContact.setClickable(true);
		cancelEmergencyContact.setEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_emergency_contact, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_logout) {
			logCurrentUserOut();
		} else if (id == R.id.action_addContact) {
			addNewContact();
		} else if (id == R.id.action_viewMap) {
			viewMap();
		} else if (id == R.id.action_createGroup) {
			createGroup();
		} else if (id == R.id.action_home) {
			home();
		} else if (id == R.id.action_settings){
			settings();
		}
		return true;
	}

	private void logCurrentUserOut() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			LOGGER.info("Logging out user: " + ParseUser.getCurrentUser()
					+ "...");
			ParseUser.logOut();
			if (ParseUser.getCurrentUser() == null) {
				LOGGER.info("User successfully logged out!");
				Toast.makeText(getApplicationContext(),
						"Successfully Logged Out!", Toast.LENGTH_LONG).show();
				Intent intent = new Intent(getApplicationContext(),
						SplashActivity.class);
				startActivity(intent);
				finish();
			} else {
				Toast.makeText(getApplicationContext(), "Please Try Again!",
						Toast.LENGTH_LONG).show();
			}
		} else {
			showNoInternetConnectionDialog();
		}
	}

	private void addNewContact() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			LOGGER.info("Starting Add Contact Activity...");
			Intent intent = new Intent(getApplicationContext(),
					AddContactActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
		}
	}

	private void viewMap() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			LOGGER.info("Starting Map View Activity...");
			Intent intent = new Intent(getApplicationContext(),
					MapsViewActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
		}
	}

	private void createGroup() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			LOGGER.info("Starting Create Group Activity...");
			Intent intent = new Intent(getApplicationContext(),
					SelectGroupParticipantsActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
		}
	}

	private void home() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			LOGGER.info("Starting Home Activity...");
			Intent intent = new Intent(getApplicationContext(),
					HomeActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
		}
	}

	private void settings() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			LOGGER.info("Going to Settings page... ");
			Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
		}
	}
}
