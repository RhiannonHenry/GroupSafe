package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.kainos.groupsafe.adapters.EmergencyContactRowAdapter;
import com.kainos.groupsafe.utilities.ConnectionDetector;
import com.kainos.groupsafe.utilities.EmergencyContact;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

	private final static Logger LOGGER = Logger
			.getLogger(SettingsActivity.class.getName());

	static SettingsActivity _instance = null;
	EmergencyContactRowAdapter adapter = null;
	private ArrayList<EmergencyContact> emergencyContacts = new ArrayList<EmergencyContact>();
	ListView listView = null;

	private boolean internetPresent = false;
	ConnectionDetector connectionDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());
		connectionDetector = new ConnectionDetector(getApplicationContext());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		_instance = this;

		refreshEmergencyContacts();

		// create an Array Adapter from the String Array
		listView = (ListView) findViewById(R.id.emergencyContactList);
		View header = getLayoutInflater().inflate(R.layout.header_settings,
				null);
		listView.addHeaderView(header);
		adapter = new EmergencyContactRowAdapter(this,
				R.layout.emergency_contact_row, emergencyContacts);
		// assign adapter to ListView
		listView.setAdapter(adapter);

		userClicksOnEmergencyContact();
		userClicksOnAddEmergencyContact();

	}

	private void userClicksOnAddEmergencyContact() {
		Button addEmergencyContactButton = (Button) findViewById(R.id.addEmergencyContactButton);
		addEmergencyContactButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				internetPresent = connectionDetector.isConnectedToInternet();
				if (internetPresent) {
					LOGGER.info("Starting Add Emergency Contact Activity...");
					Intent intent = new Intent(getApplicationContext(),
							AddEmergencyContactActivity.class);
					startActivity(intent);
					finish();
				} else {
					showNoInternetConnectionDialog();
				}
			}
		});
	}

	private void userClicksOnEmergencyContact() {
		listView.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				EmergencyContact selectedRecord = emergencyContacts
						.get(position);
				String emergencyContactName = selectedRecord.getEmergencyContactName();
				String emergencyContactNumber = selectedRecord.getEmergencyContactNumber();
				String emergencyContactRelationship = selectedRecord.getEmergencyContactRelationship();
				 Intent intent = new Intent(_instance,
				 EmergencyContactDetailsActivity.class);
				 intent.putExtra("name", emergencyContactName);
				 intent.putExtra("number", emergencyContactNumber);
				 intent.putExtra("relationship", emergencyContactRelationship);
				 startActivity(intent);
			}
		});
	}

	private void refreshEmergencyContacts() {
		emergencyContacts.clear();
		ParseUser currentUser = ParseUser.getCurrentUser();
		@SuppressWarnings("unchecked")
		ArrayList<Object> userEmergencyContacts = (ArrayList<Object>) currentUser
				.get("emergencyContacts");
		LOGGER.info("Got Emergency Contacts:");
		for (int i = 0; i < userEmergencyContacts.size(); i++) {
			Object currentEmergencyContact = userEmergencyContacts.get(i);
			createRetrievedEmergencyContact(currentEmergencyContact);
			LOGGER.info(i + ". " + currentEmergencyContact);
		}
	}

	private void createRetrievedEmergencyContact(Object currentEmergencyContact) {
		String eContactObjectId = currentEmergencyContact.toString();
		ParseQuery<ParseObject> contact = ParseQuery
				.getQuery("EmergencyContact");
		contact.whereEqualTo("objectId", eContactObjectId);
		contact.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> eContactList, ParseException e) {
				if (e == null) {
					for (int i = 0; i < eContactList.size(); i++) {
						ParseObject current = eContactList.get(i);
						String name = current.get("emergencyContactName")
								.toString();
						String number = current.get("emergencyContactNumber")
								.toString();
						String relationship = current.get(
								"emergencyContactRelationship").toString();

						EmergencyContact emergencyContact = new EmergencyContact(
								name, number, relationship);
						LOGGER.info("Emergency Contact Name: " + name);
						emergencyContact.setEmergencyContactName(name);
						LOGGER.info("Contact Number: " + number);
						emergencyContact.setEmergencyContactNumber(number);
						LOGGER.info("Emergency Contact Relationship: "
								+ relationship);
						emergencyContact.setEmergencyContactName(relationship);
						emergencyContacts.add(emergencyContact);
						listView.refreshDrawableState();
						adapter.notifyDataSetChanged();
					}
				} else {
					LOGGER.info("SOMETHING WENT WRONG RETRIEVING EMERGENCY CONTACT");
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
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
		} else if (id == R.id.action_settings) {
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
