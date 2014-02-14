package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseUser;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class SetGroupGeoFenceActivity extends Activity {

	private final static Logger LOGGER = Logger
			.getLogger(SetGroupGeoFenceActivity.class.getName());
	static SetGroupGeoFenceActivity _instance = null;

	private Spinner radiusSpinner;
	private Button nextButton;
	private Button previousButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_group_geo_fence);
		_instance = this;

		addListenerOnNextButton();
		addListenerOnPrevButton();
		addListenerOnSpinnerItemSelect();
	}

	private void addListenerOnNextButton() {
		// TODO pass through to next activity

		radiusSpinner = (Spinner) findViewById(R.id.radiusSpinner);
		nextButton = (Button) findViewById(R.id.setGeoFenceRadiusNextButton);

		nextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ArrayList<String> selectedParticipants = getSelectedParticipants();
				String selectedRadius = String.valueOf(radiusSpinner
						.getSelectedItem());
				LOGGER.info("GOING TO STEP 3 WITH:");
				LOGGER.info("Radius: " + selectedRadius + " meters");
				LOGGER.info("Participants: ");
				for (int i = 0; i < selectedParticipants.size(); i++) {
					LOGGER.info("" + selectedParticipants.get(i));
				}
				Intent intent = new Intent(_instance,
						InviteGroupParticipantsActivity.class);
				intent.putStringArrayListExtra("chosenParticipants",
						selectedParticipants);
				intent.putExtra("geoFenceRadius", selectedRadius);
				startActivity(intent);
			}
		});
	}

	private void addListenerOnPrevButton() {
		// Go to previous screen
		previousButton = (Button) findViewById(R.id.setGeoFenceRadiusPrevButton);
		previousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LOGGER.info("GOING BACK TO STEP 1...");
				Intent intent = new Intent(_instance,
						SelectGroupParticipantsActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}

	public ArrayList<String> getSelectedParticipants() {
		Intent intent = getIntent();
		// This is the string of the phoneNumbers (aka. usernames) of the chosen
		// participants
		ArrayList<String> chosenGroupParticipants = intent
				.getStringArrayListExtra("chosenParticipants");
		return chosenGroupParticipants;
	}

	private void addListenerOnSpinnerItemSelect() {
		radiusSpinner = (Spinner) findViewById(R.id.radiusSpinner);
		radiusSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView,
					View selectedItemView, int position, long id) {
				String tempRadius = parentView.getItemAtPosition(position)
						.toString();
				LOGGER.info("You have selected: " + tempRadius);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				// TODO: Default Radius setting
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.set_group_geo_fence, menu);
		return true;
	}

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
		}
		return true;
	}

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
