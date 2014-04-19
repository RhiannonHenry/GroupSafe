package com.kainos.groupsafe;

import java.util.List;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity that will be displayed to the user if they select 'Add Emergency
 * Contact' from the Settings screen. This activity allows a user to add a new
 * emergency contact to their address book.
 * 
 * Layout: @see activity_add_emergency_contact.xml Menu: @see
 * add_emergency_contact.xml
 * 
 * @author Rhiannon Henry
 * 
 */
public class AddEmergencyContactActivity extends Activity {

	private static String TAG = "Add_Emergency_Contact_Activity";
	private static AddEmergencyContactActivity _instance = null;
	private static String emergencyContactName = null;
	private static String emergencyContactNumber = null;
	private static String emergencyContactRelationship = null;
	private static String newEmergencyContactObjectID = null;
	private EditText addEmergencyContactName;
	private EditText addEmergencyContactNumber;
	private EditText addEmergencyContactRelationship;
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
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_emergency_contact);
		_instance = this;
		enableAllButtons();
		cancelButtonClicked();
		saveButtonClicked();
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
	 * This method is called if the user clicks on Save. This will start a flow
	 * for adding a new Emergency contact to the database.
	 */
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
					Utilities.showNoInternetConnectionDialog(_instance);
					enableAllButtons();
				}

			}
		});
	}

	/**
	 * This method will retrieve the users input from the text fields on the
	 * screen @see activity_add_emergency_contact.xml and will check if there is
	 * an existing duplicate entry already in the database.
	 */
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
						Log.i(TAG,
								"A MATCHING EMERGENCY CONTACT ALREADY EXISTS");
						ParseObject contact = foundEmergencyContacts.get(0);
						Log.i(TAG, "FOUND: " + contact.getObjectId());
						retrieveObjectID();
					} else {
						Log.i(TAG, "THIS IS A NEW EMERGENCY CONATCT");
						createNewContactInTable();
					}
				} else {
					Log.e(TAG, "Error::");
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * This method will be called if the emergency contact being added is not
	 * presently in the Database. This method will add a new emergency contact
	 * with the supplied information to the database, in the EmergencyContact
	 * entity.
	 */
	protected void createNewContactInTable() {
		ParseObject contact = new ParseObject("EmergencyContact");
		contact.put("emergencyContactName", emergencyContactName);
		contact.put("emergencyContactNumber", emergencyContactNumber);
		contact.put("emergencyContactRelationship",
				emergencyContactRelationship);
		try {
			contact.save();
			Log.i(TAG, "New Emergency Contact Saved Successfully");
		} catch (ParseException e2) {
			Log.e(TAG, "UNABLE TO SAVE NEW EMERGENCY CONTACT AT THIS TIME...");
			e2.printStackTrace();
		}
		retrieveObjectID();
	}

	/**
	 * This method is used to find the unique identifier of the newly added
	 * emergency contact.
	 */
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
					Log.i(TAG, "Retrieved: " + emergencyContactList.size()
							+ " emergency contacts");
					for (int i = 0; i < emergencyContactList.size(); i++) {
						ParseObject current = emergencyContactList.get(i);
						Log.i(TAG,
								"Emergency Contact "
										+ i
										+ ": Name: "
										+ current.get("emergencyContactName")
										+ " Number: "
										+ current.get("emergencyContactNumber")
										+ " Relationship: "
										+ current
												.get("emergencyContactRelationship")
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
							Log.e(TAG, "NO MATCH FOR EMERGENCY CONTACT");
						}
					}
				} else {
					Log.e(TAG, "UNABLE TO RETRIEVE CONTACT FROM CONTACT TABLE");
				}
			}

			/**
			 * This method is used to add a reference to the emergency contact
			 * to the users list of emergency contacts for the user. Once
			 * complete, the user will be returned to the Settings screen for
			 * the application
			 * 
			 * @param newEmergencyContactObjectID
			 *            This is a String value giving the unique reference
			 *            that can be used to reference the newly created
			 *            Emergency Contact in the Emergency Contact entity.
			 */
			private void addContactToUserEmergencyContactList(
					String newEmergencyContactObjectID) {
				Log.i(TAG, "New Emergency Contact ObjectID: "
						+ newEmergencyContactObjectID);
				Log.i(TAG, "Starting query on user emergency contact list...");
				ParseUser currentUser = ParseUser.getCurrentUser();
				currentUser.addUnique("emergencyContacts",
						newEmergencyContactObjectID);
				currentUser.saveInBackground(new SaveCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null) {
							Log.i(TAG, "SAVED EMERGENCY CONTACT SUCCESSFULLY");
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
							Log.e(TAG,
									"ENCOUNTERED ERROR SAVING EMERGENCY CONTACT");
							Log.e(TAG, e.getMessage());
						}
					}
				});
			}
		});
	}

	/**
	 * This method will be called if the user clicks 'Cancel' on the Add
	 * Emergency Contact page @see activity_add_emergency_contact.xml. The user
	 * will be returned to the Settings page of the application.
	 */
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
					Utilities.showNoInternetConnectionDialog(_instance);
					enableAllButtons();
				}
			}
		});
	}

	/**
	 * Disables all buttons that are on the AddEmergencyContactActivity.java
	 * view.
	 * 
	 * @see activity_add_emergency_contact.xml
	 */
	private void disableAllButtons() {
		Button saveEmergencyContact = (Button) findViewById(R.id.addEmergencyContactSaveButton);
		Button cancelEmergencyContact = (Button) findViewById(R.id.addEmergencyContactCancelButton);

		saveEmergencyContact.setClickable(false);
		saveEmergencyContact.setEnabled(false);
		cancelEmergencyContact.setClickable(false);
		cancelEmergencyContact.setEnabled(false);
	}

	/**
	 * Enables all buttons that are on the AddEmergencyContactActivity.java
	 * view.
	 * 
	 * @see activity_add_emergency_contact.xml
	 */
	private void enableAllButtons() {
		Button saveEmergencyContact = (Button) findViewById(R.id.addEmergencyContactSaveButton);
		Button cancelEmergencyContact = (Button) findViewById(R.id.addEmergencyContactCancelButton);

		saveEmergencyContact.setClickable(true);
		saveEmergencyContact.setEnabled(true);
		cancelEmergencyContact.setClickable(true);
		cancelEmergencyContact.setEnabled(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_emergency_contact, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		internetPresent = connectionDetector.isConnectedToInternet();
		return MenuUtilities.menuOptions(id, this, internetPresent, TAG);
	}

}
