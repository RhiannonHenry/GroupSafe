package com.kainos.groupsafe;

import java.util.List;
import java.util.logging.Logger;

import com.kainos.groupsafe.utilities.ConnectionDetector;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddContactActivity extends Activity {

	private final static Logger LOGGER = Logger
			.getLogger(AddContactActivity.class.getName());
	private static String contactName = null;
	private static String contactNumber = null;

	private static String newContactObjectID = null;
	static AddContactActivity _instance = null;

	private boolean internetPresent = false;
	ConnectionDetector connectionDetector;

	private EditText addContactName;
	private EditText addContactNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");

		connectionDetector = new ConnectionDetector(getApplicationContext());

		ParseAnalytics.trackAppOpened(getIntent());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_contact);

		_instance = this;

		enableAllButtons();
	}

	public void cancelNewContact(View view) {
		disableAllButtons();
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			Intent intent = new Intent(_instance, HomeActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
			enableAllButtons();
		}
	}

	public void saveNewContact(View view) {
		disableAllButtons();
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			addContactToDatabase();
		} else {
			showNoInternetConnectionDialog();
			enableAllButtons();
		}
	}

	private void addContactToDatabase() {
		addContactName = (EditText) findViewById(R.id.addContactNameInput);
		addContactNumber = (EditText) findViewById(R.id.addContactNumberInput);
		contactName = addContactName.getText().toString();
		contactNumber = addContactNumber.getText().toString();

		checkThatUserExistsInDatabase();
	}

	private void checkThatUserExistsInDatabase() {
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", contactNumber);
		query.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> userList, ParseException e) {
				if (e == null) {
					LOGGER.info("Successfully retrieved user: "
							+ userList.get(0).getUsername());
					addToContactsTable();
				} else {
					LOGGER.info("An error occurred retrieving the user!");
					Toast.makeText(getApplicationContext(),
							"User does not exist. Try Again!",
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	private void addToContactsTable() {
		checkIfContactIsPresentInTable();
	}

	private void checkIfContactIsPresentInTable() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Contact");
		query.whereEndsWith("name", contactName);
		query.whereEqualTo("number", contactNumber);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> foundContacts, ParseException e) {
				if (e == null) {
					if (foundContacts.size() != 0) {
						LOGGER.info("A CONTACT ALREADY EXISTS");
						foundContacts.get(0);
						retrieveObjectID();
					} else {
						LOGGER.info("THIS IS A NEW CONATCT");
						createNewContactInTable();
					}
				}
			}
		});
	}

	protected void createNewContactInTable() {
		ParseObject contact = new ParseObject("Contact");
		contact.put("name", contactName);
		contact.put("number", contactNumber);
		try {
			contact.save();
		} catch (ParseException e2) {
			LOGGER.info("UNABLE TO SAVE NEW CONTACT AT THIS TIME...");
			e2.printStackTrace();
		}
		retrieveObjectID();
	}

	private void retrieveObjectID() {
		ParseQuery<ParseObject> contactID = ParseQuery.getQuery("Contact");
		contactID.whereEqualTo("number", contactNumber);
		contactID.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> contactList, ParseException e) {
				if (e == null) {
					LOGGER.info("Retrieved: " + contactList.size()
							+ " contacts");
					for (int i = 0; i < contactList.size(); i++) {
						ParseObject current = contactList.get(i);
						LOGGER.info("Contact " + i + ": Name: "
								+ current.get("name") + " Number: "
								+ current.get("number") + " ObjectID: "
								+ current.getObjectId());
						if (current.get("name").equals(contactName)
								&& current.get("number").equals(contactNumber)) {
							newContactObjectID = current.getObjectId();
							addContactToUserContactList(newContactObjectID);
						} else {
							LOGGER.info("NO MATCH FOR CONTACT");
						}
					}
				} else {
					LOGGER.info("UNABLE TO RETRIEVE CONTACT FROM CONTACT TABLE");
				}
			}

			private void addContactToUserContactList(String newContactObjectID) {
				LOGGER.info("New Contact ObjectID: " + newContactObjectID);
				LOGGER.info("Starting query on user contact list...");
				ParseUser currentUser = ParseUser.getCurrentUser();
				currentUser.addUnique("contacts", newContactObjectID);
				currentUser.saveInBackground(new SaveCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null) {
							LOGGER.info("SAVED CONTACT SUCCESSFULLY");
							Toast.makeText(getApplicationContext(),
									"Saved Contact Successfully!",
									Toast.LENGTH_LONG).show();
							Intent intent = new Intent(_instance,
									HomeActivity.class);
							startActivity(intent);
							finish();
						} else {
							Toast.makeText(getApplicationContext(),
									"Unable to Save Contact...",
									Toast.LENGTH_LONG).show();
							LOGGER.info("ENCOUNTERED ERROR SAVING CONTACT");
							LOGGER.info(e.getMessage());
						}
					}
				});
			}
		});
	}

	/*
	 * MENU ...
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_contact, menu);
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
	
	/*
	 * UTILITY METHODS ...
	 */

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

	private void enableAllButtons() {
		Button saveButton = (Button) findViewById(R.id.addContactSaveButton);
		saveButton.setClickable(true);
		saveButton.setEnabled(true);

		Button cancelButton = (Button) findViewById(R.id.addContactCancelButton);
		cancelButton.setClickable(true);
		cancelButton.setEnabled(true);
	}

	public void disableAllButtons() {
		Button saveButton = (Button) findViewById(R.id.addContactSaveButton);
		saveButton.setClickable(false);
		saveButton.setEnabled(false);

		Button cancelButton = (Button) findViewById(R.id.addContactCancelButton);
		cancelButton.setClickable(false);
		cancelButton.setEnabled(false);
	}
}
