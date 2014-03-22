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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class SetGroupGeoFenceActivity extends Activity {

	private final static Logger LOGGER = Logger
			.getLogger(SetGroupGeoFenceActivity.class.getName());
	static SetGroupGeoFenceActivity _instance = null;

	static final String SAVED_GROUP_ORGANIZATION = "groupOrganization";
	static final String SAVED_GROUP_NAME = "groupName";
	static final String SAVED_GEO_FENCE_RADIUS = "radius";

	private Spinner radiusSpinner, organizationSpinner;
	private Button nextButton, previousButton, cancelButton;
	private String organizationName;
	private List<String> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_group_geo_fence);
		_instance = this;

		radiusSpinner = (Spinner) findViewById(R.id.radiusSpinner);
		organizationSpinner = (Spinner) findViewById(R.id.organizationSpinner);
		nextButton = (Button) findViewById(R.id.setGeoFenceRadiusNextButton);
		previousButton = (Button) findViewById(R.id.setGeoFenceRadiusPrevButton);
		cancelButton = (Button) findViewById(R.id.setGeoFenceRadiusCancelButton);

		enableAllButtons();

		addItemsToOrganizationSpinner();
		addListenerOnNextButton();
		addListenerOnPrevButton();
		addListenerOnCancelButton();
		addListenerOnRadiusSpinnerItemSelect();
		addListenerOnOrganizationSpinnerItemSelect();
	}

	private void disableAllButtons() {
		previousButton.setClickable(false);
		previousButton.setEnabled(false);
		nextButton.setClickable(false);
		nextButton.setEnabled(false);
		cancelButton.setClickable(false);
		cancelButton.setEnabled(false);
	}

	private void enableAllButtons() {
		previousButton.setClickable(true);
		previousButton.setEnabled(true);
		nextButton.setClickable(true);
		nextButton.setEnabled(true);
		cancelButton.setClickable(true);
		cancelButton.setEnabled(true);
	}

	private void addListenerOnCancelButton() {
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				LOGGER.info("Exiting the Create Group process...");
				Intent intent = new Intent(_instance, HomeActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}

	@SuppressWarnings("null")
	private void addItemsToOrganizationSpinner() {
		ParseUser currentUser = ParseUser.getCurrentUser();
		String userOrganization = "";
		try {
			userOrganization = currentUser.get("organizationId").toString();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		list = new ArrayList<String>();
		list.add("None");

		if (userOrganization != null || !userOrganization.equals("")) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Organization");
			query.whereEqualTo("objectId", userOrganization);
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> organizations,
						ParseException e) {
					if (e == null) {
						if (organizations.size() > 0) {
							LOGGER.info("FOUND ORGANIZATION: ");
							ParseObject current = organizations.get(0);
							organizationName = current.get("organizationName")
									.toString();
							LOGGER.info(organizationName);
							list.add(organizationName);
						} else {
							LOGGER.info("NO ORGANIZATIONS FOUND");
						}
					} else {
						e.printStackTrace();
					}
				}
			});
		}

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		organizationSpinner.setAdapter(dataAdapter);
	}

	private void addListenerOnNextButton() {
		nextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				disableAllButtons();
				ArrayList<String> selectedParticipants = getSelectedParticipants();
				String selectedRadius = String.valueOf(radiusSpinner
						.getSelectedItem());
				String selectedOrgaization = String.valueOf(organizationSpinner
						.getSelectedItem());
				EditText groupNameInput = (EditText) findViewById(R.id.groupNameInput);
				String groupName = groupNameInput.getText().toString();

				LOGGER.info("GOING TO STEP 3 WITH:");
				LOGGER.info("Radius: " + selectedRadius + " meters");
				LOGGER.info("Organization: " + selectedOrgaization);
				LOGGER.info("Group Name: " + groupName);
				LOGGER.info("Participants: ");
				for (int i = 0; i < selectedParticipants.size(); i++) {
					LOGGER.info("" + selectedParticipants.get(i));
				}
				Intent intent = new Intent(_instance,
						InviteGroupParticipantsActivity.class);
				intent.putStringArrayListExtra("chosenParticipants",
						selectedParticipants);
				intent.putExtra("geoFenceRadius", selectedRadius);
				intent.putExtra("groupOrganization", selectedOrgaization);
				intent.putExtra("groupName", groupName);
				startActivity(intent);
			}
		});
	}

	private void addListenerOnPrevButton() {
		// Go to previous screen
		previousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LOGGER.info("GOING BACK TO STEP 1...");
				disableAllButtons();
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

	private void addListenerOnRadiusSpinnerItemSelect() {
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
				LOGGER.info("You have not selected a Radius. Resorting to Default Setting [10]");
			}

		});
	}

	private void addListenerOnOrganizationSpinnerItemSelect() {
		organizationSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parentView,
							View selectedItemView, int position, long id) {
						String tempRadius = parentView.getItemAtPosition(
								position).toString();
						LOGGER.info("You have selected: " + tempRadius);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parentView) {
						LOGGER.info("You have not selected an Organization. Resorting to Default Setting [NONE]");
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.set_group_geo_fence, menu);
		return true;
	}
}
