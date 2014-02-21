package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.kainos.groupsafe.adapters.InviteesAdapter;
import com.kainos.groupsafe.utilities.InviteeContact;
import com.kainos.groupsafe.utilities.Status;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class InviteGroupParticipantsActivity extends Activity {

	static InviteGroupParticipantsActivity _instance = null;
	private final static Logger LOGGER = Logger
			.getLogger(InviteGroupParticipantsActivity.class.getName());
	
	InviteesAdapter adapter = null;
	ListView listView = null;
	
	// TODO: populate this with contact name and number [Parse call]
	private ArrayList<InviteeContact> invitedContacts = new ArrayList<InviteeContact>();
	
	// Array of contacts passed through from before
	ArrayList<String> participants;
	int radius;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());
				
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite_group_participants);
		_instance = this;

		Intent intent = getIntent();
		participants = intent.getStringArrayListExtra("chosenParticipants");
		radius = Integer.parseInt(intent.getStringExtra("geoFenceRadius"));
		
		
		
		// create an Array Adapter from the String Array
		listView = (ListView) findViewById(R.id.inviteeList);
		View footer = getLayoutInflater().inflate(R.layout.footer_invite_participants, null);
		View header = getLayoutInflater().inflate(R.layout.header_invite_participants, null);
		listView.addFooterView(footer);
		listView.addHeaderView(header);
		adapter = new InviteesAdapter(this,
		R.layout.invitee_row, invitedContacts);
		// assign adapter to ListView
		listView.setAdapter(adapter);
		
		LOGGER.info("Got Participants: ");
		for(int i = 0; i<participants.size(); i++){
			LOGGER.info(""+participants.get(i));
			getParticipantInformation(participants.get(i));	
		}
		LOGGER.info("Got Radius size: "+radius);
		
		Button invite = (Button) header.findViewById(R.id.inviteGroupParticipantsButton);
		invite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO: send push notifications to selected group participants
			}
		});
		
		
		Button next = (Button) footer.findViewById(R.id.inviteParticipantsNextButton);
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Button nextButton = (Button) findViewById(R.id.inviteParticipantsNextButton);
				Button prevButton = (Button) findViewById(R.id.inviteParticipantsPrevButton);
				nextButton.setClickable(false);
				nextButton.setEnabled(false);
				prevButton.setClickable(false);
				prevButton.setEnabled(false);
				
				//TODO: proceed to group map view
			}
		});
		
		Button prev = (Button) footer.findViewById(R.id.inviteParticipantsPrevButton);
		prev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Button nextButton = (Button) findViewById(R.id.inviteParticipantsNextButton);
				Button prevButton = (Button) findViewById(R.id.inviteParticipantsPrevButton);
				nextButton.setClickable(false);
				nextButton.setEnabled(false);
				prevButton.setClickable(false);
				prevButton.setEnabled(false);
				
				//Go back to setRadiusView
				Intent intent = new Intent(_instance,
						SetGroupGeoFenceActivity.class);
				intent.putStringArrayListExtra("chosenParticipants",
						participants);
				startActivity(intent);
			}
		});
	}

	private void getParticipantInformation(String participantObjectId) {
		// TODO: Get name and number for contact
		LOGGER.info("Getting Name for Participant with id: "+participantObjectId);
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Contact");
		query.whereEqualTo("objectId", participantObjectId);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> foundContactInformation,
					ParseException e) {
				if (e == null) {
					if (foundContactInformation.size() != 0) {
						LOGGER.info("FOUND CONTACT");
						ParseObject currentData = foundContactInformation.get(0);
						String contactName = currentData.get("name").toString();
						String contactNumber = currentData.get("number").toString();
						String objectId = currentData.getObjectId();
						LOGGER.info("CONTACT NAME: "+contactName);
						LOGGER.info("CONTACT NUMBER: "+contactNumber);
						InviteeContact contact = new InviteeContact(contactName, contactNumber, objectId, Status.PENDING);
						invitedContacts.add(contact);
						adapter.inviteeList.add(contact);
						adapter.notifyDataSetChanged();
						listView.refreshDrawableState();
					} else {
						LOGGER.info("UNABLE TO FIND CONTACT");
					}
				}
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.invite_group_participants, menu);
		return true;
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
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_logout) {
			logCurrentUserOut();
		} else if (id == R.id.action_addContact) {
			LOGGER.info("Starting Add Contact Activity...");
			Intent intent = new Intent(getApplicationContext(),
					AddContactActivity.class);
			startActivity(intent);
			finish();
		} else if (id == R.id.action_viewMap) {
			LOGGER.info("Starting Map View Activity...");
			Intent intent = new Intent(getApplicationContext(),
					MapsViewActivity.class);
			startActivity(intent);
			finish();
		} else if (id == R.id.action_createGroup) {
			LOGGER.info("Starting Create Group Activity...");
			Intent intent = new Intent(getApplicationContext(),
					SelectGroupParticipantsActivity.class);
			startActivity(intent);
			finish();
		} else if (id == R.id.action_home) {
			LOGGER.info("Starting Home Activity...");
			Intent intent = new Intent(getApplicationContext(),
					HomeActivity.class);
			startActivity(intent);
			finish();
		} else if (id == R.id.action_settings) {
			LOGGER.info("Going to Settings page... ");
			Intent intent = new Intent(getApplicationContext(),
					SettingsActivity.class);
			startActivity(intent);
			finish();
		}
		return true;
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
	private void logCurrentUserOut() {
		LOGGER.info("Logging out user: " + ParseUser.getCurrentUser() + "...");
		ParseUser.logOut();
		if (ParseUser.getCurrentUser() == null) {
			LOGGER.info("User successfully logged out!");
			Toast.makeText(getApplicationContext(), "Successfully Logged Out!",
					Toast.LENGTH_LONG).show();
			Intent intent = new Intent(getApplicationContext(),
					SplashActivity.class);
			startActivity(intent);
			finish();
		} else {
			Toast.makeText(getApplicationContext(), "Please Try Again!",
					Toast.LENGTH_LONG).show();
		}

	}


}
