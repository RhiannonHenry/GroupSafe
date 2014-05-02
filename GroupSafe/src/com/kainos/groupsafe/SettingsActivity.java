package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.List;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that will be displayed to the user if they select 'Settings' from
 * the menu at any point. This activity allows the user to view details about
 * their current status and also allows them to Add/View/Delete Emergency
 * Contacts and Add/View/Delete Organisations.
 * 
 * Layout: @see activity_settings.xml and @see header_settings.xml Menu: @see
 * settings.xml
 * 
 * @author Rhiannon Henry
 * 
 */
public class SettingsActivity extends Activity {

	private static final String TAG = "SETTINGS";
	private static SettingsActivity _instance = null;
	private EmergencyContactRowAdapter adapter = null;
	private ArrayList<EmergencyContact> emergencyContacts = new ArrayList<EmergencyContact>();
	private ListView listView = null;
	private TextView userUsernameView, userDisplayNameView, userEmailView,
			userOrganization;
	Button deleteOrganization, addEmergencyContactButton,
			editOrganisationButton, passwordResetButton;
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
		setContentView(R.layout.activity_settings);
		_instance = this;
		refreshEmergencyContacts();
		deleteOrganization = (Button) findViewById(R.id.editOrganizationDeleteButton);
		addEmergencyContactButton = (Button) findViewById(R.id.addEmergencyContactButton);
		editOrganisationButton = (Button) findViewById(R.id.editOrganizationButton);
		passwordResetButton = (Button) findViewById(R.id.resetPasswordButton);

		// create an Array Adapter from the String Array
		listView = (ListView) findViewById(R.id.emergencyContactList);
		View header = getLayoutInflater().inflate(R.layout.header_settings,
				null);
		listView.addHeaderView(header);
		adapter = new EmergencyContactRowAdapter(this,
				R.layout.emergency_contact_row, emergencyContacts);
		listView.setAdapter(adapter);

		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			Log.i(TAG, "Populating page...");
			populatePage();
		} else {
			Utilities.showNoInternetConnectionDialog(this);
		}
		userClicksOnEmergencyContact();
		userClicksOnAddEmergencyContact();
		userClicksOnResetPassword();
		userClicksOnEditOrganizationId();
		userClicksOnDeleteOrganization();
		enableAllButtons();
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
	 * This method is used if the user clicks 'Delete Organization' button on
	 * the {@link SettingsActivity} screen. This will remove the association of
	 * the user to the organisation.
	 */
	private void userClicksOnDeleteOrganization() {
		deleteOrganization.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				ParseUser currentUser = ParseUser.getCurrentUser();
				final String currentOrganizationId = currentUser.get(
						"organizationId").toString();
				currentUser.put("organizationId", "");
				currentUser.saveInBackground(new SaveCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null) {
							Log.i(TAG,
									"Success: Removed Organization from User");
							removeUserFromMembersArray(currentOrganizationId);
						} else {
							enableAllButtons();
							Log.e(TAG,
									"UNABLE TO REMOVE ORGANIZATION FROM USER");
							e.printStackTrace();
						}
					}

					/**
					 * This method is used to remove the user from the members
					 * array of the organization object in the Organization
					 * Entity in the Database.
					 * 
					 * @param currentOrganizationId
					 *            a String reference to a unique organization
					 *            identifier for an organization object in the
					 *            Organization Entity in the Database
					 */
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
										Log.i(TAG, "GOT MEMBERS:");
										for (int i = 0; i < membersArray.size(); i++) {
											Log.i(TAG, "" + membersArray.get(i));
										}
										membersArray.remove(ParseUser
												.getCurrentUser().getObjectId()
												.toString());
										Log.i(TAG, "MEMBERS AFTER REMOVAL:");
										for (int i = 0; i < membersArray.size(); i++) {
											Log.i(TAG, "" + membersArray.get(i));
										}
										current.put("organizationMembers",
												membersArray);
										current.saveInBackground(new SaveCallback() {
											@Override
											public void done(ParseException e) {
												if (e == null) {
													Log.i(TAG,
															"Success: Removed User from Organization Members");
													Intent intent = new Intent(
															getApplicationContext(),
															SettingsActivity.class);
													startActivity(intent);
													finish();
												} else {
													enableAllButtons();
													Log.e(TAG,
															"UNABLE TO REMOVE USER FROM ORGANIZATION");
												}
											}
										});
									}
								} else {
									enableAllButtons();
									Log.e(TAG, "ERROR:");
									e.printStackTrace();
								}
							}
						});
					}
				});
			}
		});
	}

	/**
	 * This method is called if the user clicks on 'Edit Organization' on the
	 * {@link SettingsActivity} screen. This will open up the Edit Organisation
	 * screen {@link EditOrganizationActivity}
	 */
	private void userClicksOnEditOrganizationId() {
		editOrganisationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				internetPresent = connectionDetector.isConnectedToInternet();
				if (internetPresent) {
					Log.i(TAG, "Going to Edit Organization Activity...");
					Intent intent = new Intent(getApplicationContext(),
							EditOrganizationActivity.class);
					startActivity(intent);
					finish();
				} else {
					enableAllButtons();
					Utilities.showNoInternetConnectionDialog(_instance);
				}
			}
		});
	}

	/**
	 * This method is used if the user clicks on the 'Reset Password' button.
	 * This will then call a method to send a reset password email to the email
	 * address with which the user registered.
	 */
	private void userClicksOnResetPassword() {
		passwordResetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				internetPresent = connectionDetector.isConnectedToInternet();
				if (internetPresent) {
					Log.i(TAG, "Starting Reset Password Functionality...");
					sendResetEmail();
				} else {
					enableAllButtons();
					Utilities.showNoInternetConnectionDialog(_instance);
				}
			}
		});
	}

	/**
	 * This method is used to send an email to the user that will allow them to
	 * reset their password.
	 */
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
							enableAllButtons();
						} else {
							Toast.makeText(getApplicationContext(),
									"Error Sending Password Reset Email!",
									Toast.LENGTH_SHORT).show();
							enableAllButtons();
						}
					}
				});
	}

	/**
	 * This method is used to make a call to Parse to fetch the information
	 * about the user that is needed to populate the fields on the page with
	 * relevant data.
	 */
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

		userUsernameView = (TextView) findViewById(R.id.yourCurrentUserName);
		userDisplayNameView = (TextView) findViewById(R.id.yourCurrentDisplayName);
		userEmailView = (TextView) findViewById(R.id.yourCurrentEmail);
		userOrganization = (TextView) findViewById(R.id.yourCurrentOrganization);
		Log.i(TAG, "Current Username: " + username);
		Log.i(TAG, "Current Display Name: " + displayName);
		Log.i(TAG, "Current Email: " + email);
		Log.i(TAG, "Current Organization: " + organization);

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
							Log.i(TAG, "AN ORGANIZATION WAS FOUND");
							ParseObject found = foundContacts.get(0);
							userOrganization.setText(found.get(
									"organizationName").toString());
						} else {
							Log.i(TAG, "NO ORGANIZATION WITH THAT ID FOUND");
						}
					}
				}
			});
		} else {
			userOrganization.setText("n/a");
		}
	}

	/**
	 * This method is used if the user clicks on 'Add Emergency Contact' button
	 * on the {@link SettingsActivity} screen. This will open the
	 * {@link AddEmergencyContactActivity} screen, where the user can add
	 * details about a new emergency contact.
	 */
	private void userClicksOnAddEmergencyContact() {
		addEmergencyContactButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				internetPresent = connectionDetector.isConnectedToInternet();
				if (internetPresent) {
					Log.i(TAG, "Starting Add Emergency Contact Activity...");
					Intent intent = new Intent(getApplicationContext(),
							AddEmergencyContactActivity.class);
					startActivity(intent);
					finish();
				} else {
					enableAllButtons();
					Utilities.showNoInternetConnectionDialog(_instance);
				}
			}
		});
	}

	/**
	 * This method is called if the user clicks on an emergency contact in the
	 * emergency contact list. Similarly to the {@link HomeActivity} this will
	 * open details about the Emergency Contact and the
	 * {@link EmergencyContactDetailsActivity} will be displayed.
	 */
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

	/**
	 * This method is called to update emergency contacts.
	 */
	private void refreshEmergencyContacts() {
		emergencyContacts.clear();
		ParseUser currentUser = ParseUser.getCurrentUser();
		@SuppressWarnings("unchecked")
		ArrayList<Object> userEmergencyContacts = (ArrayList<Object>) currentUser
				.get("emergencyContacts");
		Log.i(TAG, "Got Emergency Contacts:");
		for (int i = 0; i < userEmergencyContacts.size(); i++) {
			Object currentEmergencyContact = userEmergencyContacts.get(i);
			createRetrievedEmergencyContact(currentEmergencyContact);
			Log.i(TAG, i + ". " + currentEmergencyContact);
		}
	}

	/**
	 * When the emergency contact list is refreshed, any emergency contacts will
	 * be added to the list view of emergency contacts by creating a new
	 * {@link EmergencyContact} object and adding it to the list view via the
	 * {@link EmergencyContactRowAdapter}
	 * 
	 * @param currentEmergencyContact
	 *            a String representation of the unique emergency contact
	 *            Identifier for an emergency contact object in the
	 *            EmergencyContact Entity in the database
	 */
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
						Log.i(TAG, "Emergency Contact Name: " + name);
						emergencyContact.setEmergencyContactName(name);
						Log.i(TAG, "Contact Number: " + number);
						emergencyContact.setEmergencyContactNumber(number);
						Log.i(TAG, "Emergency Contact Relationship: "
								+ relationship);
						emergencyContact
								.setEmergencyContactRelationship(relationship);

						emergencyContacts.add(emergencyContact);
						adapter.emergencyContactList.add(emergencyContact);
						adapter.notifyDataSetChanged();
						listView.refreshDrawableState();

					}
				} else {
					Log.e(TAG,
							"SOMETHING WENT WRONG RETRIEVING EMERGENCY CONTACT");
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
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		internetPresent = connectionDetector.isConnectedToInternet();
		return MenuUtilities.menuOptions(id, this, internetPresent, TAG);
	}

	/**
	 * Enables all buttons that are on the Settings.java view.
	 * 
	 * @see activity_settings.xml
	 */
	private void enableAllButtons() {
		editOrganisationButton.setClickable(true);
		editOrganisationButton.setEnabled(true);
		deleteOrganization.setClickable(true);
		deleteOrganization.setEnabled(true);
		addEmergencyContactButton.setClickable(true);
		addEmergencyContactButton.setEnabled(true);
		passwordResetButton.setClickable(true);
		passwordResetButton.setEnabled(true);
	}

	/**
	 * Disables all buttons that are on the Settings.java view.
	 * 
	 * @see activity_settings.xml
	 */
	private void disableAllButtons() {
		editOrganisationButton.setClickable(false);
		editOrganisationButton.setEnabled(false);
		deleteOrganization.setClickable(false);
		deleteOrganization.setEnabled(false);
		addEmergencyContactButton.setClickable(false);
		addEmergencyContactButton.setEnabled(false);
		passwordResetButton.setClickable(false);
		passwordResetButton.setEnabled(false);
	}
}
