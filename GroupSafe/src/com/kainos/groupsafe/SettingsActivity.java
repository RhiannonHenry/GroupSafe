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
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

	private final static Logger LOGGER = Logger
			.getLogger(SettingsActivity.class.getName());

	static SettingsActivity _instance = null;
	EmergencyContactRowAdapter adapter = null;
	private ArrayList<EmergencyContact> emergencyContacts = new ArrayList<EmergencyContact>();
	ListView listView = null;

	TextView userUsernameView, userDisplayNameView, userEmailView,
			userOrganization;

	private boolean internetPresent = false;
	ConnectionDetector connectionDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());
		connectionDetector = new ConnectionDetector(getApplicationContext());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		_instance = this;

		refreshEmergencyContacts();

		// create an Array Adapter from the String Array
		listView = (ListView) findViewById(R.id.emergencyContactList);
		View header = getLayoutInflater().inflate(R.layout.header_settings,
				null);
		listView.addHeaderView(header);
		adapter = new EmergencyContactRowAdapter(this,
				R.layout.emergency_contact_row, emergencyContacts);
		// assign adapter to ListView
		listView.setAdapter(adapter);

		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			LOGGER.info("Populating page...");
			populatePage();
		} else {
			showNoInternetConnectionDialog();
		}

		userClicksOnEmergencyContact();
		userClicksOnAddEmergencyContact();
		userClicksOnResetPassword();
		userClicksOnEditOrganizationId();
		userClicksOnDeleteOrganization();

	}

	private void userClicksOnDeleteOrganization() {

		Button deleteOrganization = (Button) findViewById(R.id.editOrganizationDeleteButton);
		deleteOrganization.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ParseUser currentUser = ParseUser.getCurrentUser();
				final String currentOrganizationId = currentUser.get(
						"organizationId").toString();

				currentUser.put("organizationId", "");
				currentUser.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {
						if (e == null) {
							LOGGER.info("Success: Removed Organization from User");
							removeUserFromMembersArray(currentOrganizationId);
						} else {
							LOGGER.info("UNABLE TO REMOVE ORGANIZATION FROM USER");
							e.printStackTrace();
						}
					}

					private void removeUserFromMembersArray(
							String currentOrganizationId) {
						ParseQuery<ParseObject> query = ParseQuery
								.getQuery("Organization");
						query.whereEqualTo("objectId", currentOrganizationId);
						query.findInBackground(new FindCallback<ParseObject>() {

							@Override
							public void done(List<ParseObject> organizations,
									ParseException e) {
								if (e == null) {
									if (organizations.size() > 0) {
										ParseObject current = organizations
												.get(0);
										@SuppressWarnings("unchecked")
										ArrayList<String> membersArray = (ArrayList<String>) current
												.get("organizationMembers");
										LOGGER.info("GOT MEMBERS:");
										for (int i = 0; i < membersArray.size(); i++) {
											LOGGER.info(""
													+ membersArray.get(i));
										}
										membersArray.remove(ParseUser
												.getCurrentUser().getObjectId()
												.toString());
										LOGGER.info("MEMBERS AFTER REMOVAL:");
										for (int i = 0; i < membersArray.size(); i++) {
											LOGGER.info(""
													+ membersArray.get(i));
										}
										current.put("organizationMembers",
												membersArray);
										current.saveInBackground(new SaveCallback() {

											@Override
											public void done(ParseException e) {
												if (e == null) {
													LOGGER.info("Success: Removed User from Organization Members");
													Intent intent = new Intent(
															getApplicationContext(),
															SettingsActivity.class);
													startActivity(intent);
													finish();
												} else {
													LOGGER.info("UNABLE TO REMOVE USER FROM ORGANIZATION");
												}
											}
										});
									}
								} else {
									LOGGER.info("ERROR:");
									e.printStackTrace();
								}
							}
						});
					}
				});

			}
		});

	}

	private void userClicksOnEditOrganizationId() {
		Button editOrganisationButton = (Button) findViewById(R.id.editOrganizationButton);
		editOrganisationButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				internetPresent = connectionDetector.isConnectedToInternet();
				if (internetPresent) {
					LOGGER.info("Going to Edit Organization Activity...");
					Intent intent = new Intent(getApplicationContext(),
							EditOrganizationActivity.class);
					startActivity(intent);
					finish();
				} else {
					showNoInternetConnectionDialog();
				}
			}
		});
	}

	private void userClicksOnResetPassword() {
		Button passwordResetButton = (Button) findViewById(R.id.resetPasswordButton);
		passwordResetButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				internetPresent = connectionDetector.isConnectedToInternet();
				if (internetPresent) {
					LOGGER.info("Starting Reset Password Functionality...");
					sendResetEmail();
				} else {
					showNoInternetConnectionDialog();
				}
			}
		});
	}

	protected void sendResetEmail() {
		ParseUser currentUser = ParseUser.getCurrentUser();
		String email = currentUser.get("email").toString();
		ParseUser.requestPasswordResetInBackground(email,
				new RequestPasswordResetCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null) {
							Toast.makeText(getApplicationContext(),
									"Email has been sent!", Toast.LENGTH_SHORT)
									.show();
						} else {
							Toast.makeText(getApplicationContext(),
									"Error Sending Password Reset Email!",
									Toast.LENGTH_SHORT).show();
						}
					}
				});
	}

	private void populatePage() {
		ParseUser currentUser = ParseUser.getCurrentUser();
		String username = currentUser.get("username").toString();
		String displayName = currentUser.get("displayName").toString();
		String email = currentUser.get("email").toString();
		String organization = "";
		try {
			organization = currentUser.get("organizationId").toString();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		LOGGER.info("Trying to get Username and Display name Text view: ");
		userUsernameView = (TextView) findViewById(R.id.yourCurrentUserName);
		userDisplayNameView = (TextView) findViewById(R.id.yourCurrentDisplayName);
		userEmailView = (TextView) findViewById(R.id.yourCurrentEmail);
		userOrganization = (TextView) findViewById(R.id.yourCurrentOrganization);
		LOGGER.info("Username View: " + userUsernameView.toString());
		LOGGER.info("Display Name View: " + userDisplayNameView.toString());

		LOGGER.info("Current Username: " + username);
		LOGGER.info("Current Display Name: " + displayName);
		LOGGER.info("Current Email: " + email);
		LOGGER.info("Current Organization: " + organization);

		userUsernameView.setText(username);
		userDisplayNameView.setText(displayName);
		userEmailView.setText(email);

		if (!organization.equals("")) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Organization");
			query.whereEqualTo("objectId", organization);
			query.findInBackground(new FindCallback<ParseObject>() {
				@Override
				public void done(List<ParseObject> foundContacts,
						ParseException e) {
					if (e == null) {
						if (foundContacts.size() != 0) {
							LOGGER.info("AN ORGANIZATION WAS FOUND");
							ParseObject found = foundContacts.get(0);
							userOrganization.setText(found.get(
									"organizationName").toString());
						} else {
							LOGGER.info("NO ORGANIZATION WITH THAT ID FOUND");
						}
					}
				}
			});
		} else {
			userOrganization.setText("n/a");
		}
	}

	private void userClicksOnAddEmergencyContact() {
		Button addEmergencyContactButton = (Button) findViewById(R.id.addEmergencyContactButton);
		addEmergencyContactButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				internetPresent = connectionDetector.isConnectedToInternet();
				if (internetPresent) {
					LOGGER.info("Starting Add Emergency Contact Activity...");
					Intent intent = new Intent(getApplicationContext(),
							AddEmergencyContactActivity.class);
					startActivity(intent);
					finish();
				} else {
					showNoInternetConnectionDialog();
				}
			}
		});
	}

	private void userClicksOnEmergencyContact() {
		listView.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				EmergencyContact selectedRecord = emergencyContacts
						.get(position - 1);
				String emergencyContactName = selectedRecord
						.getEmergencyContactName();
				String emergencyContactNumber = selectedRecord
						.getEmergencyContactNumber();
				String emergencyContactRelationship = selectedRecord
						.getEmergencyContactRelationship();
				Intent intent = new Intent(_instance,
						EmergencyContactDetailsActivity.class);
				intent.putExtra("name", emergencyContactName);
				intent.putExtra("number", emergencyContactNumber);
				intent.putExtra("relationship", emergencyContactRelationship);
				startActivity(intent);
			}
		});
	}

	private void refreshEmergencyContacts() {
		emergencyContacts.clear();
		ParseUser currentUser = ParseUser.getCurrentUser();
		@SuppressWarnings("unchecked")
		ArrayList<Object> userEmergencyContacts = (ArrayList<Object>) currentUser
				.get("emergencyContacts");
		LOGGER.info("Got Emergency Contacts:");
		for (int i = 0; i < userEmergencyContacts.size(); i++) {
			Object currentEmergencyContact = userEmergencyContacts.get(i);
			createRetrievedEmergencyContact(currentEmergencyContact);
			LOGGER.info(i + ". " + currentEmergencyContact);
		}
	}

	private void createRetrievedEmergencyContact(Object currentEmergencyContact) {
		String eContactObjectId = currentEmergencyContact.toString();
		ParseQuery<ParseObject> contact = ParseQuery
				.getQuery("EmergencyContact");
		contact.whereEqualTo("objectId", eContactObjectId);
		contact.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> eContactList, ParseException e) {
				if (e == null) {
					for (int i = 0; i < eContactList.size(); i++) {
						ParseObject current = eContactList.get(i);
						String name = current.get("emergencyContactName")
								.toString();
						String number = current.get("emergencyContactNumber")
								.toString();
						String relationship = current.get(
								"emergencyContactRelationship").toString();

						EmergencyContact emergencyContact = new EmergencyContact(
								name, number, relationship);
						LOGGER.info("Emergency Contact Name: " + name);
						emergencyContact.setEmergencyContactName(name);
						LOGGER.info("Contact Number: " + number);
						emergencyContact.setEmergencyContactNumber(number);
						LOGGER.info("Emergency Contact Relationship: "
								+ relationship);
						emergencyContact
								.setEmergencyContactRelationship(relationship);

						emergencyContacts.add(emergencyContact);
						adapter.emergencyContactList.add(emergencyContact);
						adapter.notifyDataSetChanged();
						listView.refreshDrawableState();

					}
				} else {
					LOGGER.info("SOMETHING WENT WRONG RETRIEVING EMERGENCY CONTACT");
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_logout) {
			logCurrentUserOut();
		} else if (id == R.id.action_addContact) {
			addNewContact();
		} else if (id == R.id.action_viewMap) {
			viewMap();
		} else if (id == R.id.action_createGroup) {
			createGroup();
		} else if (id == R.id.action_home) {
			home();
		} else if (id == R.id.action_settings) {
			settings();
		}
		return true;
	}

	private void logCurrentUserOut() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			LOGGER.info("Logging out user: " + ParseUser.getCurrentUser()
					+ "...");
			ParseUser.logOut();
			if (ParseUser.getCurrentUser() == null) {
				LOGGER.info("User successfully logged out!");
				Toast.makeText(getApplicationContext(),
						"Successfully Logged Out!", Toast.LENGTH_LONG).show();
				Intent intent = new Intent(getApplicationContext(),
						SplashActivity.class);
				startActivity(intent);
				finish();
			} else {
				Toast.makeText(getApplicationContext(), "Please Try Again!",
						Toast.LENGTH_LONG).show();
			}
		} else {
			showNoInternetConnectionDialog();
		}
	}

	private void addNewContact() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			LOGGER.info("Starting Add Contact Activity...");
			Intent intent = new Intent(getApplicationContext(),
					AddContactActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
		}
	}

	private void viewMap() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			LOGGER.info("Starting Map View Activity...");
			Intent intent = new Intent(getApplicationContext(),
					MapsViewActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
		}
	}

	private void createGroup() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			LOGGER.info("Starting Create Group Activity...");
			Intent intent = new Intent(getApplicationContext(),
					SelectGroupParticipantsActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
		}
	}

	private void home() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			LOGGER.info("Starting Home Activity...");
			Intent intent = new Intent(getApplicationContext(),
					HomeActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
		}
	}

	private void settings() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			LOGGER.info("Going to Settings page... ");
			Intent intent = new Intent(getApplicationContext(),
					SettingsActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
		}
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

}
