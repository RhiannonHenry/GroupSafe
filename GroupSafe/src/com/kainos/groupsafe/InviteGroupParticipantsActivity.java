package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.kainos.groupsafe.adapters.ParticipantContactRowAdapter;
import com.kainos.groupsafe.utilities.ParticipantContact;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class InviteGroupParticipantsActivity extends Activity {

	static InviteGroupParticipantsActivity _instance = null;
	private final static Logger LOGGER = Logger
			.getLogger(InviteGroupParticipantsActivity.class.getName());
	
	// TODO: create a new adapter with PENDING, DECLINED, ACTIVE indicator
	ParticipantContactRowAdapter adapter = null;
	ListView listView = null;
	
	// TODO: populate this with contact name and number [Parse call]
	private ArrayList<ParticipantContact> retrievedContacts = new ArrayList<ParticipantContact>();
	// Array of contacts passed through from before
	ArrayList<String> participants;
	int radius;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());
		
		// TODO: specify a default Activity to handle push notifications
		// PushService.setDefaultPushCallback(this, InviteGroupParticipantsActivity.class);
		// ParseInstallation.getCurrentInstallation().saveInBackground();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite_group_participants);
		_instance = this;

		Intent intent = getIntent();
		participants = intent.getStringArrayListExtra("chosenParticipants");
		radius = Integer.parseInt(intent.getStringExtra("geoFenceRadius"));
		LOGGER.info("Got Participants: ");
		for(int i = 0; i<participants.size(); i++){
			LOGGER.info(""+participants.get(i));
		}
		LOGGER.info("Got Radius size: "+radius);
		
		
		// create an Array Adapter from the String Array
		listView = (ListView) findViewById(R.id.participantsList);
		View footer = getLayoutInflater().inflate(R.layout.footer_select_participants, null);
		View header = getLayoutInflater().inflate(R.layout.header_invite_participants, null);
		listView.addFooterView(footer);
		listView.addHeaderView(header);
		adapter = new ParticipantContactRowAdapter(this,
		R.layout.select_group_participant_row, retrievedContacts);
		// assign adapter to ListView
		listView.setAdapter(adapter);
		
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.invite_group_participants, menu);
		return true;
	}

}
