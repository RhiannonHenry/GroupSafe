package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class SelectGroupParticipantsActivity extends Activity {

	private final static Logger LOGGER = Logger
			.getLogger(SelectGroupParticipantsActivity.class.getName());
	static SelectGroupParticipantsActivity _instance = null;

	ParticipantContactRowAdapter adapter = null;
	private ArrayList<ParticipantContact> retrievedContacts = new ArrayList<ParticipantContact>();
	private ArrayList<String> chosenParticipants = new ArrayList<String>();
	private Button next, cancel;
	ListView listView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());

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

	private void cancelButtonOnClick() {
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				disableAllButtons();

				LOGGER.info("Exiting the Create Group process...");
				Intent intent = new Intent(_instance, HomeActivity.class);
				startActivity(intent);
			}
		});
	}

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

				LOGGER.info("Chosen Participants: ");
				for (int j = 0; j < chosenParticipants.size(); j++) {
					LOGGER.info(j + ". " + chosenParticipants.get(j));
				}

				Intent intent = new Intent(_instance,
						SetGroupGeoFenceActivity.class);
				intent.putStringArrayListExtra("chosenParticipants",
						chosenParticipants);
				startActivity(intent);
			}
		});
	}

	private void disableAllButtons() {
		next.setClickable(false);
		next.setEnabled(false);
		cancel.setClickable(false);
		cancel.setEnabled(false);
	}

	private void enableAllButtons() {
		next.setClickable(true);
		next.setEnabled(true);
		cancel.setClickable(true);
		cancel.setEnabled(true);
	}

	/**
	 * This is a Javadoc example. It explains the working of Javadoc comments.
	 * 
	 * @param String
	 *            text The above line is used to document what the parameters
	 *            that are passed to the method do. Each parameter gets its own @param
	 *            block.
	 * 
	 * @return void This explains what the output / result of the method is. In
	 *         this case, it's void.
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
	 * 
	 * @return void This method doesn't return anything.
	 */
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

	/**
	 * Uses the objectID retrieved from Parse to fetch the Contact from Parse.
	 * Once retrieved from Parse, Create ArrayList of <ParticipantContact>
	 * called retrievedContacts.
	 * 
	 * @param Object
	 *            This is the objectID of the Contact returned from Parse.
	 * 
	 * @return void This explains what the output / result of the method is. In
	 *         this case, it's void.
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
						LOGGER.info("Contact Name: " + contactName);
						LOGGER.info("Contact Number: " + contactNumber);
						ParticipantContact contact = new ParticipantContact(
								contactName, contactNumber, objectId, false);
						retrievedContacts.add(contact);
						adapter.participantContactList.add(contact);
						adapter.notifyDataSetChanged();
						listView.refreshDrawableState();
					}
				} else {
					LOGGER.info("SOMETHING WENT WRONG RETRIEVING CONTACT");

				}
			}
		});
	}

}
