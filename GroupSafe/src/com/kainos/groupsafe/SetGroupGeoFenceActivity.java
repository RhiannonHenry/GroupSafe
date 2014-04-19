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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * This Activity is the second step in the 'Create Group' process, where the
 * Group Details are specified. For Example: set group geo fence radius, set
 * group name and organization. From this screen the user will be able to go
 * back a step and choose participants, or go to the next step and send the
 * group invitation to selected participants.
 * 
 * @author Rhiannon Henry
 * 
 */
public class SetGroupGeoFenceActivity extends Activity {

	private static final String TAG = "Set_Group_Details_Activity";
	private static SetGroupGeoFenceActivity _instance = null;

	static final String SAVED_GROUP_ORGANIZATION = "groupOrganization";
	static final String SAVED_GROUP_NAME = "groupName";
	static final String SAVED_GEO_FENCE_RADIUS = "radius";

	private Spinner radiusSpinner, organizationSpinner;
	private Button nextButton, previousButton, cancelButton;
	private String organizationName;
	private List<String> list;

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
	 * This method is used if the user clicks on the 'Cancel' button on the
	 * {@link SetGroupGeoFenceActivity} screen. This will exit the 'Create
	 * Group' process and return the user to the Home screen
	 * {@link HomeActivity}
	 */
	private void addListenerOnCancelButton() {
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				Log.d(TAG, "Exiting the Create Group process...");
				Intent intent = new Intent(_instance, HomeActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}

	/**
	 * This method is used to fetch any organisation to which the user creating
	 * the group may be associated with. This means that the group would be
	 * visible to the organisation administrator. The user will be able to
	 * select an organisation name if one is available, otherwise they can
	 * select 'None'.
	 */
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
							Log.i(TAG, "FOUND ORGANIZATION: ");
							ParseObject current = organizations.get(0);
							organizationName = current.get("organizationName")
									.toString();
							Log.i(TAG, organizationName);
							list.add(organizationName);
						} else {
							Log.i(TAG, "NO ORGANIZATIONS FOUND");
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

	/**
	 * This method is used if the user clicks on the 'Next' button on the
	 * {@link SetGroupGeoFenceActivity} screen. This will place all information
	 * from the page into the intent to be passed through to the final stage of
	 * the 'Create Group' process {@link InviteGroupParticipantsActivity}
	 */
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

				Log.i(TAG, "GOING TO STEP 3 WITH:");
				Log.i(TAG, "Radius: " + selectedRadius + " meters");
				Log.i(TAG, "Organization: " + selectedOrgaization);
				Log.i(TAG, "Group Name: " + groupName);
				Log.i(TAG, "Participants: ");
				for (int i = 0; i < selectedParticipants.size(); i++) {
					Log.i(TAG, "" + selectedParticipants.get(i));
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

	/**
	 * This method will be used if the user clicks on the 'Prev' button on the
	 * {@link SetGroupGeoFenceActivity} screen. This will return the user to the
	 * {@link SelectGroupParticipantsActivity} where they can alter the
	 * participants they have chosen.
	 */
	private void addListenerOnPrevButton() {
		previousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "GOING BACK TO STEP 1...");
				disableAllButtons();
				Intent intent = new Intent(_instance,
						SelectGroupParticipantsActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}

	/**
	 * This method is used to fetch the selected participants that were passed
	 * through from Step 1: {@link SelectGroupParticipantsActivity} of the
	 * 'Create Group' process.
	 * 
	 * @return chosenGroupPaticipants An ArrayList<String> object containing all
	 *         the phone numbers of the chosen participants.
	 */
	public ArrayList<String> getSelectedParticipants() {
		Intent intent = getIntent();
		ArrayList<String> chosenGroupParticipants = intent
				.getStringArrayListExtra("chosenParticipants");
		return chosenGroupParticipants;
	}

	/**
	 * This method is called when the user changes the item selected on the
	 * GeoFence Radius Drop Down list.
	 */
	private void addListenerOnRadiusSpinnerItemSelect() {
		radiusSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView,
					View selectedItemView, int position, long id) {
				String tempRadius = parentView.getItemAtPosition(position)
						.toString();
				Log.i(TAG, "You have selected: " + tempRadius);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				Log.i(TAG,
						"You have not selected a Radius. Resorting to Default Setting [10]");
			}

		});
	}

	/**
	 * This method is used when the user changes the selected organisation in
	 * the Organisation Drop Down List.
	 */
	private void addListenerOnOrganizationSpinnerItemSelect() {
		organizationSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parentView,
							View selectedItemView, int position, long id) {
						String tempRadius = parentView.getItemAtPosition(
								position).toString();
						Log.i(TAG, "You have selected: " + tempRadius);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parentView) {
						Log.i(TAG,
								"You have not selected an Organization. Resorting to Default Setting [NONE]");
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
		getMenuInflater().inflate(R.menu.set_group_geo_fence, menu);
		return true;
	}

	/**
	 * Disables all buttons on the screen @see activity_set_group_geo_fence and @see
	 * activity_set_group_geo_fence.xml
	 */
	private void disableAllButtons() {
		previousButton.setClickable(false);
		previousButton.setEnabled(false);
		nextButton.setClickable(false);
		nextButton.setEnabled(false);
		cancelButton.setClickable(false);
		cancelButton.setEnabled(false);
	}

	/**
	 * Enables all buttons on the screen @see activity_set_group_geo_fence and @see
	 * activity_set_group_geo_fence.xml
	 */
	private void enableAllButtons() {
		previousButton.setClickable(true);
		previousButton.setEnabled(true);
		nextButton.setClickable(true);
		nextButton.setEnabled(true);
		cancelButton.setClickable(true);
		cancelButton.setEnabled(true);
	}
}
