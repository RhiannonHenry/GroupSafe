package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.List;

import com.parse.FindCallback;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

/**
 * This Activity is the first step in the 'Create Group' process, where a user
 * can select contacts from their contact list that they wish to participate in
 * the group. From this screen, the user will be able select/de-select
 * participants and proceed to the next step in the 'Create Group' process
 * (GroupDetails @see {@link SetGroupGeoFenceActivity})
 * 
 * @author Rhiannon Henry
 * 
 */
public class SelectGroupParticipantsActivity extends Activity {

	private static final String TAG = "SELECT_PARTICIPANTS_ACTIVITY";
	private static SelectGroupParticipantsActivity _instance = null;
	private ParticipantContactRowAdapter adapter = null;
	private ArrayList<ParticipantContact> retrievedContacts = new ArrayList<ParticipantContact>();
	private ArrayList<String> chosenParticipants = new ArrayList<String>();
	private Button next, cancel;
	private ListView listView = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_group_participants);
		_instance = this;
		refreshContacts();
		// create an Array Adapter from the String Array
		listView = (ListView) findViewById(R.id.contactGroupList);
		View footer = getLayoutInflater().inflate(
				R.layout.footer_select_participants, null);
		listView.addFooterView(footer);
		adapter = new ParticipantContactRowAdapter(this,
				R.layout.select_group_participant_row, retrievedContacts);
		// assign adapter to ListView
		listView.setAdapter(adapter);

		next = (Button) footer
				.findViewById(R.id.selectGroupParticipantsNextButton);
		cancel = (Button) footer
				.findViewById(R.id.selectGroupParticipantsCancelButton);

		enableAllButtons();

		nextButtonOnClick();
		cancelButtonOnClick();
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
	 * This method is used if the user clicks 'Cancel' button on the
	 * {@link SetGroupGeoFenceActivity} screen (@see
	 * footer_select_participants.xml). This method returns the user to the Home
	 * screen {@link HomeActivity}
	 */
	private void cancelButtonOnClick() {
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				Log.i(TAG, "Exiting the Create Group process...");
				Intent intent = new Intent(_instance, HomeActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * This method is called if the user clicks the 'Next' button on the
	 * {@link SetGroupGeoFenceActivity} screen (@see
	 * footer_select_participants.xml). This method will gather all the selected
	 * participants and place them in an array. This array will be passed
	 * through to the next activity {@link SetGroupGeoFenceActivity} via the
	 * intent.
	 */
	private void nextButtonOnClick() {
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Disable all Buttons
				disableAllButtons();
				ArrayList<ParticipantContact> possibleParticipants = adapter.participantContactList;
				for (int i = 0; i < possibleParticipants.size(); i++) {
					ParticipantContact possibleParticipant = possibleParticipants
							.get(i);
					if (possibleParticipant.isSelected()) {
						chosenParticipants.add(possibleParticipant
								.getObjectId());
					}
				}
				Log.i(TAG, "Chosen Participants: ");
				for (int j = 0; j < chosenParticipants.size(); j++) {
					Log.i(TAG, j + ". " + chosenParticipants.get(j));
				}
				Intent intent = new Intent(_instance,
						SetGroupGeoFenceActivity.class);
				intent.putStringArrayListExtra("chosenParticipants",
						chosenParticipants);
				startActivity(intent);
			}
		});
	}

	/**
	 * Disables all buttons on the screen @see footer_select_participants.xml
	 * and @see footer_select_participants.xml
	 */
	private void disableAllButtons() {
		next.setClickable(false);
		next.setEnabled(false);
		cancel.setClickable(false);
		cancel.setEnabled(false);
	}

	/**
	 * Enables all buttons on the screen @see footer_select_participants.xml and @see
	 * footer_select_participants.xml
	 */
	private void enableAllButtons() {
		next.setClickable(true);
		next.setEnabled(true);
		cancel.setClickable(true);
		cancel.setEnabled(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_group_participants, menu);
		return true;
	}

	/**
	 * This method is used to retrieve a list of contacts for the currentUser
	 * from Parse.
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
	 * Uses the objectID retrieved from Parse to fetch the Contact from Parse.
	 * Once retrieved from Parse, Create ArrayList of <ParticipantContact>
	 * called retrievedContacts.
	 * 
	 * @param Object
	 *            This is the objectID of the Contact returned from Parse.
	 */
	private void createRetrievedContact(Object currentContact) {
		String contactObjectId = currentContact.toString();
		ParseQuery<ParseObject> contact = ParseQuery.getQuery("Contact");
		contact.whereEqualTo("objectId", contactObjectId);
		contact.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> contactList, ParseException e) {
				if (e == null) {
					for (int i = 0; i < contactList.size(); i++) {
						ParseObject current = contactList.get(i);
						String contactName = current.get("name").toString();
						String contactNumber = current.get("number").toString();
						String objectId = current.getObjectId();
						Log.i(TAG, "Contact Name: " + contactName);
						Log.i(TAG, "Contact Number: " + contactNumber);
						ParseQuery<ParseUser> getUserForContact = ParseUser
								.getQuery();
						getUserForContact.whereEqualTo("username",
								contactNumber);
						try {
							List<ParseUser> contactUserList = getUserForContact
									.find();
							if (contactUserList.size() > 0) {
								ParseUser contactUser = contactUserList.get(0);
								if (contactUser.getBoolean("groupLeader")) {
									Log.i(TAG,
											"User is currently a GROUP LEADER");
								} else if (contactUser
										.getBoolean("groupMember")) {
									Log.i(TAG,
											"User is currently a GROUP MEMBER");
								} else {
									ParticipantContact contact = new ParticipantContact(
											contactName, contactNumber,
											objectId, false);
									retrievedContacts.add(contact);
									adapter.participantContactList.add(contact);
									adapter.notifyDataSetChanged();
									listView.refreshDrawableState();
								}
							} else {
								Log.e(TAG,
										"ERROR:: Unable to find user for contact");
							}
						} catch (ParseException e1) {
							Log.e(TAG, "ERROR::");
							e1.printStackTrace();
						}

					}
				} else {
					Log.e(TAG, "SOMETHING WENT WRONG RETRIEVING CONTACT");

				}
			}
		});
	}

}
