package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.kainos.groupsafe.adapters.ContactRowAdapter;
import com.kainos.groupsafe.utilities.ConnectionDetector;
import com.kainos.groupsafe.utilities.Contact;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Home Activity
 */
public class HomeActivity extends Activity {

	private final static Logger LOGGER = Logger.getLogger(HomeActivity.class
			.getName());
	public static final String SELECTED_CONTACT = "com.kainos.groupsafe.activities.HomeActivity.SELECTEDCONTACT";
	private static ArrayList<Contact> retrievedContacts = new ArrayList<Contact>();
	static HomeActivity _instance = null;
	static ContactRowAdapter contactRowAdapter = null;
	static ListView contactListView = null;
	View activityRoot;

	private boolean internetPresent = false;
	ConnectionDetector connectionDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");

		connectionDetector = new ConnectionDetector(getApplicationContext());
		ParseAnalytics.trackAppOpened(getIntent());

		super.onCreate(savedInstanceState);
		_instance = this;

		refreshContacts();

		setContentView(R.layout.activity_home);
		contactListView = (ListView) findViewById(R.id.contactList);
		contactRowAdapter = new ContactRowAdapter(this, retrievedContacts);
		contactListView.setAdapter(contactRowAdapter);
		contactListView.setFocusable(true);
		contactListView
				.setOnItemClickListener(new ListView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Contact selectedRecord = retrievedContacts
								.get(position);
						Intent intent = new Intent(_instance,
								ContactDetailsActivity.class);
						intent.putExtra(SELECTED_CONTACT, selectedRecord);
						startActivity(intent);
					}
				});
	}

	private void refreshContacts() {
		retrievedContacts.clear();
		ParseUser currentUser = ParseUser.getCurrentUser();
		@SuppressWarnings("unchecked")
		ArrayList<Object> userContacts = (ArrayList<Object>) currentUser
				.get("contacts");
		LOGGER.info("Got Contacts:");
		for (int i = 0; i < userContacts.size(); i++) {
			Object currentContact = userContacts.get(i);
			createRetrievedContact(currentContact);
			LOGGER.info(i + ". " + currentContact);
		}
	}

	private void createRetrievedContact(Object currentContact) {
		String contactObjectId = currentContact.toString();
		ParseQuery<ParseObject> contact = ParseQuery.getQuery("Contact");
		contact.whereEqualTo("objectId", contactObjectId);
		contact.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> contactList, ParseException e) {
				if (e == null) {
					for (int i = 0; i < contactList.size(); i++) {
						Contact contact = new Contact();
						ParseObject current = contactList.get(i);
						LOGGER.info("Contact Name: "
								+ current.get("name").toString());
						contact.setContactName(current.get("name").toString());
						LOGGER.info("Contact Number: "
								+ current.get("number").toString());
						contact.setContactNumber(current.get("number")
								.toString());
						retrievedContacts.add(contact);
						contactListView.refreshDrawableState();
						contactRowAdapter.notifyDataSetChanged();
					}
				} else {
					LOGGER.info("SOMETHING WENT WRONG RETRIEVING CONTACT");
				}
			}
		});
	}

	/*
	 * MENU ...
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
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
