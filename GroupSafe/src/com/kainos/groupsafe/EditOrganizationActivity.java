package com.kainos.groupsafe;

import java.util.List;
import java.util.logging.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
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

public class EditOrganizationActivity extends Activity {

	private final static Logger LOGGER = Logger
			.getLogger(EditOrganizationActivity.class.getName());
	static EditOrganizationActivity _instance = null;

	private static String organizationId = null;

	private EditText organizationIdInput;

	private boolean internetPresent = false;
	ConnectionDetector connectionDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");

		connectionDetector = new ConnectionDetector(getApplicationContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_organization);

		_instance = this;

		enableAllButtons();

		organizationIdInput = (EditText) findViewById(R.id.organizationIdInput);

		cancelButtonClicked();
		saveButtonClicked();
	}

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
					showNoInternetConnectionDialog();
					enableAllButtons();
				}

			}
		});
	}

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
						LOGGER.info("FOUND ORGANIZATION");
						ParseObject currentOrganization = organizationsList
								.get(0);
						LOGGER.info(""
								+ currentOrganization.get("organizationName")
										.toString());
						saveOrganizationForUser();
					} else {
						enableAllButtons();
						Toast.makeText(getApplicationContext(),
								"Organization does not exist. Try Again!",
								Toast.LENGTH_LONG).show();
					}
				} else {
					LOGGER.info("AN ERROR HAS OCCURRED: ");
					e.printStackTrace();
				}
			}
		});
	}

	protected void saveOrganizationForUser() {
		LOGGER.info("Saving Organization ID: "+organizationId+" to User");
		ParseUser user = ParseUser.getCurrentUser();
		user.put("organizationId", organizationId);
		user.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					Toast.makeText(getApplicationContext(),
							"Saved User Organization Successfully!",
							Toast.LENGTH_LONG).show();
					LOGGER.info("UPDATING ORGANIZATION MEMBERS:");
					updateOrganizationMembers();
				} else {
					e.printStackTrace();
				}
			}
		});
	}

	private void updateOrganizationMembers() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Organization");
		query.whereEqualTo("objectId", organizationId);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> organizationsList,
					ParseException e) {
				if (e == null) {
					if (organizationsList.size() > 0) {
						LOGGER.info("FOUND ORGANIZATION");
						ParseObject currentOrganization = organizationsList
								.get(0);
						LOGGER.info(""
								+ currentOrganization.get("organizationName")
										.toString());
						currentOrganization.addUnique("organizationMembers",
								ParseUser.getCurrentUser().getObjectId()
										.toString());
						currentOrganization
								.saveInBackground(new SaveCallback() {

									@Override
									public void done(ParseException e) {
										if (e == null) {
											LOGGER.info("Success: Successfully updated Organization Members!");
											Intent intent = new Intent(_instance,
													SettingsActivity.class);
											startActivity(intent);
											finish();
										} else {
											LOGGER.info("UNABLE TO SAVE ORGANIZATION WITH UPDATED MEMEBERS");
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
					LOGGER.info("AN ERROR HAS OCCURRED: ");
					e.printStackTrace();
				}
			}
		});
	}

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
					showNoInternetConnectionDialog();
					enableAllButtons();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_organization, menu);
		return true;
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

	private void disableAllButtons() {
		Button saveOrganization = (Button) findViewById(R.id.editOrganizationSaveButton);
		Button cancelOrganization = (Button) findViewById(R.id.editOrganizationCancelButton);

		saveOrganization.setClickable(false);
		saveOrganization.setEnabled(false);
		cancelOrganization.setClickable(false);
		cancelOrganization.setEnabled(false);
	}

	private void enableAllButtons() {
		Button saveOrganization = (Button) findViewById(R.id.editOrganizationSaveButton);
		Button cancelOrganization = (Button) findViewById(R.id.editOrganizationCancelButton);

		saveOrganization.setClickable(true);
		saveOrganization.setEnabled(true);
		cancelOrganization.setClickable(true);
		cancelOrganization.setEnabled(true);
	}

}
