package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.List;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * This is the activity screen that will be displayed to the user once they have
 * logged into the application @see activity_home.xml
 * 
 * From this screen the user can view their contact list, and also access other
 * features of the application from the drop-down menu
 * 
 * @author Rhiannon Henry
 */
public class HomeActivity extends Activity {

	private final static String TAG = "Home_Activity";
	public static final String SELECTED_CONTACT = "com.kainos.groupsafe.activities.HomeActivity.SELECTEDCONTACT";
	private static HomeActivity _instance = null;

	/**
	 * An Array of Contacts for the user @see Contact.java
	 */
	private static ArrayList<Contact> retrievedContacts = new ArrayList<Contact>();

	/**
	 * This ListView is within the Home activity and each row in the list is
	 * populated using an adapter
	 * 
	 * @see contact_row.xml
	 * @see home.xml
	 */
	private static ListView contactListView = null;
	/**
	 * This Adapter is used to place the contactName and contactNumber in the
	 * appropriate place for each contact. Each contact gets a new row in the
	 * list view
	 * 
	 * @see ContactRowAdapter.java
	 */
	private static ContactRowAdapter contactRowAdapter = null;

	private String currentUserId;
	private ParseUser currentUser;
	private ParseInstallation installation;

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
		PushService.setDefaultPushCallback(this, HomeActivity.class);
		currentUserId = ParseUser.getCurrentUser().getObjectId().toString();

		if (getApplicationContext() != null) {
			PushService.subscribe(getApplicationContext(), "user_"
					+ currentUserId, HomeActivity.class);
		}
		if (ParseInstallation.getCurrentInstallation() == null) {
			ParseInstallation.getCurrentInstallation().saveInBackground(
					new SaveCallback() {
						@Override
						public void done(ParseException e) {
							if (e == null) {
								installation = ParseInstallation
										.getCurrentInstallation();
								Log.d(TAG, "Got Installation: "
										+ installation.getObjectId().toString());
								installation.put("owner", currentUserId);
								installation.saveInBackground();
							} else {
								Log.e(TAG,
										"Unable to save the current installation");
								e.printStackTrace();
							}
						}
					});
		} else {
			installation = ParseInstallation.getCurrentInstallation();
			installation.put("owner", currentUserId);
			installation.saveInBackground();
		}

		currentUser = ParseUser.getCurrentUser();
		currentUser
				.put("installationId", installation.getObjectId().toString());
		currentUser.saveInBackground();

		connectionDetector = new ConnectionDetector(getApplicationContext());

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

					/*
					 * (non-Javadoc)
					 * 
					 * @see
					 * android.widget.AdapterView.OnItemClickListener#onItemClick
					 * (android.widget.AdapterView, android.view.View, int,
					 * long)
					 */
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
	 * This method is used to retrieve a list of contacts associated with the
	 * user who is currently logged-in. It makes a call to
	 * createRetrievedContact() which will add the contact to the ListView
	 */
	private void refreshContacts() {
		retrievedContacts.clear();
		ParseUser currentUser = ParseUser.getCurrentUser();
		@SuppressWarnings("unchecked")
		ArrayList<Object> userContacts = (ArrayList<Object>) currentUser
				.get("contacts");
		Log.i(TAG, "Got Contacts:");
		for (int i = 0; i < userContacts.size(); i++) {
			Object currentContact = userContacts.get(i);
			createRetrievedContact(currentContact);
			Log.i(TAG, i + ". " + currentContact);
		}
	}

	/**
	 * This method is used to create a local Contact @see Contact.java populated
	 * with the contacts name and number. This information will then be used to
	 * populate each row in the list of contacts.
	 * 
	 * @param currentContact
	 *            This is the unique objectId of a contact within the Contact
	 *            entity in the database.
	 */
	private void createRetrievedContact(Object currentContact) {
		String contactObjectId = currentContact.toString();
		ParseQuery<ParseObject> contact = ParseQuery.getQuery("Contact");
		contact.whereEqualTo("objectId", contactObjectId);
		contact.findInBackground(new FindCallback<ParseObject>() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see com.parse.FindCallback#done(java.util.List,
			 * com.parse.ParseException)
			 */
			@Override
			public void done(List<ParseObject> contactList, ParseException e) {
				if (e == null) {
					for (int i = 0; i < contactList.size(); i++) {
						Contact contact = new Contact();
						ParseObject current = contactList.get(i);
						Log.d(TAG, "Contact Name: "
								+ current.get("name").toString());
						contact.setContactName(current.get("name").toString());
						Log.d(TAG, "Contact Number: "
								+ current.get("number").toString());
						contact.setContactNumber(current.get("number")
								.toString());
						retrievedContacts.add(contact);
						contactListView.refreshDrawableState();
						contactRowAdapter.notifyDataSetChanged();
					}
				} else {
					Log.e(TAG,
							"ERROR:: Unable to retrieve contact from database");
					e.printStackTrace();
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
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
		return MenuUtils.menuOptions(id, this, internetPresent, TAG);
	}
}
