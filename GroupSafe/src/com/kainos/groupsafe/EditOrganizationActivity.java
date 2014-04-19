package com.kainos.groupsafe;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * This Activity will be displayed when the user clicks on 'Edit Organization'
 * on the Settings view @see header_settings.xml @see
 * activity_edit_organization.xml
 * 
 * @author Rhiannon Henry
 */
public class EditOrganizationActivity extends Activity {

	private static final String TAG = "EDIT_ORGNAIZATION";
	private static EditOrganizationActivity _instance = null;
	private static String organizationId = null;
	private EditText organizationIdInput;
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
		connectionDetector = new ConnectionDetector(getApplicationContext());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_organization);
		_instance = this;
		organizationIdInput = (EditText) findViewById(R.id.organizationIdInput);
		enableAllButtons();
		cancelButtonClicked();
		saveButtonClicked();
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
	 * This method is called when the user clicks on 'Save' button from the @see
	 * activity_edit_organization.xml screen
	 */
	private void saveButtonClicked() {
		Button saveOrganization = (Button) findViewById(R.id.editOrganizationSaveButton);
		saveOrganization.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				disableAllButtons();
				internetPresent = connectionDetector.isConnectedToInternet();
				if (internetPresent) {
					checkIfGroupExists();
				} else {
					Utilities.showNoInternetConnectionDialog(_instance);
					enableAllButtons();
				}

			}
		});
	}

	/**
	 * This method is used to check if the unique identifier entered by the user
	 * is a valid unique identifier for an organization saved in the
	 * Organization entity in the database.
	 */
	protected void checkIfGroupExists() {
		organizationId = organizationIdInput.getText().toString();

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Organization");
		query.whereEqualTo("objectId", organizationId);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> organizationsList,
					ParseException e) {
				if (e == null) {
					if (organizationsList.size() > 0) {
						Log.i(TAG, "FOUND ORGANIZATION");
						ParseObject currentOrganization = organizationsList
								.get(0);
						Log.i(TAG,
								""
										+ currentOrganization.get(
												"organizationName").toString());
						saveOrganizationForUser();
					} else {
						enableAllButtons();
						Toast.makeText(getApplicationContext(),
								"Organization does not exist. Try Again!",
								Toast.LENGTH_LONG).show();
					}
				} else {
					Log.e(TAG, "AN ERROR HAS OCCURRED: ");
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * This method is used to save the unique organization idenifier for the
	 * user to the relevant field in the User Entity in the database
	 */
	protected void saveOrganizationForUser() {
		Log.i(TAG, "Saving Organization ID: " + organizationId + " to User");
		ParseUser user = ParseUser.getCurrentUser();
		user.put("organizationId", organizationId);
		user.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					Toast.makeText(getApplicationContext(),
							"Saved User Organization Successfully!",
							Toast.LENGTH_LONG).show();
					Log.i(TAG, "UPDATING ORGANIZATION MEMBERS:");
					updateOrganizationMembers();
				} else {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * This method is used to add the users unique identifer to the list of
	 * members associated with the organization. The user will be returned to
	 * the Settings screen once their unique identifier has been added.
	 */
	private void updateOrganizationMembers() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Organization");
		query.whereEqualTo("objectId", organizationId);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> organizationsList,
					ParseException e) {
				if (e == null) {
					if (organizationsList.size() > 0) {
						Log.i(TAG, "FOUND ORGANIZATION");
						ParseObject currentOrganization = organizationsList
								.get(0);
						Log.i(TAG,
								""
										+ currentOrganization.get(
												"organizationName").toString());
						currentOrganization.addUnique("organizationMembers",
								ParseUser.getCurrentUser().getObjectId()
										.toString());
						currentOrganization
								.saveInBackground(new SaveCallback() {

									@Override
									public void done(ParseException e) {
										if (e == null) {
											Log.i(TAG,
													"Success: Successfully updated Organization Members!");
											Intent intent = new Intent(
													_instance,
													SettingsActivity.class);
											startActivity(intent);
											finish();
										} else {
											Log.e(TAG,
													"UNABLE TO SAVE ORGANIZATION WITH UPDATED MEMEBERS");
											e.printStackTrace();
										}
									}
								});
					} else {
						enableAllButtons();
						Toast.makeText(getApplicationContext(),
								"Organization does not exist. Try Again!",
								Toast.LENGTH_LONG).show();
					}
				} else {
					Log.e(TAG, "AN ERROR HAS OCCURRED: ");
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * This method will be called if the user clicks 'Cancel' on the Edit
	 * Organization screen @see activity_edit_organization.xml. The user will be
	 * returned to the Settings screen.
	 */
	private void cancelButtonClicked() {
		Button cancelEditOrganization = (Button) findViewById(R.id.editOrganizationCancelButton);
		cancelEditOrganization.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				internetPresent = connectionDetector.isConnectedToInternet();
				if (internetPresent) {
					Intent intent = new Intent(_instance,
							SettingsActivity.class);
					startActivity(intent);
					finish();
				} else {
					Utilities.showNoInternetConnectionDialog(_instance);
					enableAllButtons();
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
		getMenuInflater().inflate(R.menu.edit_organization, menu);
		return true;
	}

	/**
	 * Disables all buttons that are on the EditOrganizationActivity.java view.
	 * 
	 * @see activity_edit_organization.xml
	 */
	private void disableAllButtons() {
		Button saveOrganization = (Button) findViewById(R.id.editOrganizationSaveButton);
		Button cancelOrganization = (Button) findViewById(R.id.editOrganizationCancelButton);

		saveOrganization.setClickable(false);
		saveOrganization.setEnabled(false);
		cancelOrganization.setClickable(false);
		cancelOrganization.setEnabled(false);
	}

	/**
	 * Enables all buttons that are on the EditOrganizationActivity.java view.
	 * 
	 * @see activity_edit_organization.xml
	 */
	private void enableAllButtons() {
		Button saveOrganization = (Button) findViewById(R.id.editOrganizationSaveButton);
		Button cancelOrganization = (Button) findViewById(R.id.editOrganizationCancelButton);

		saveOrganization.setClickable(true);
		saveOrganization.setEnabled(true);
		cancelOrganization.setClickable(true);
		cancelOrganization.setEnabled(true);
	}

}
