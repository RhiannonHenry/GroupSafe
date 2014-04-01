package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.List;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity that will be displayed to the user if they select a contact from
 * their contact list on the Home screen. This activity allows a user to view
 * details about a specific contact and also delete a specific contact from
 * their address book.
 * 
 * Layout: @see activity_contact_details.xml Menu: @see contact_details.xml
 * 
 * @author Rhiannon Henry
 * 
 */
public class ContactDetailsActivity extends Activity {

	private static final String TAG = "DETAILED_CONTACT";
	private static ContactDetailsActivity _instance = null;
	private static String contactName = null;
	private static String contactNumber = null;
	private String groupLeader = null;
	private String groupMember = null;
	private String objectId = null;
	private boolean internetPresent = false;
	private ConnectionDetector connectionDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		connectionDetector = new ConnectionDetector(getApplicationContext());

		Intent intent = getIntent();
		Contact selectedContact = intent
				.getParcelableExtra(HomeActivity.SELECTED_CONTACT);

		contactName = selectedContact.getContactName();
		contactNumber = selectedContact.getContactNumber();
		this.setTitle(contactName);
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			Log.i(TAG, "Populating page...");
			populatePage();
		} else {
			showNoInternetConnectionDialog();
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_details);
		_instance = this;
		enableAllButtons();
	}

	/**
	 * This method is used to retrieve information about the chosen contact from
	 * the database
	 */
	private void populatePage() {
		Log.i(TAG, "GETTING CONTACT SPECIFIC INFORMATION FOR USER...");
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", contactNumber);
		query.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> userList, ParseException e) {
				if (e == null) {
					Log.i(TAG, "SUCCESS");
					for (int i = 0; i < userList.size(); i++) {
						ParseUser user = userList.get(i);
						if ((Boolean) user.get("groupLeader")) {
							groupLeader = "YES";
							Log.i(TAG, "GROUP LEADER = YES");
						} else if (user.get("groupLeader") == null) {
							groupLeader = "NO";
							Log.i(TAG, "GROUP LEADER(null) = NO");
						} else {
							groupLeader = "NO";
							Log.i(TAG, "GROUP LEADER = NO");
						}
						if ((Boolean) user.get("groupMember")) {
							groupMember = "YES";
							Log.i(TAG, "GROUP MEMBER = YES");
						} else if (user.get("groupMember") == null) {
							groupMember = "NO";
							Log.i(TAG, "GROUP MEMBER (null)= NO");
						} else {
							groupMember = "NO";
							Log.i(TAG, "GROUP MEMBER = NO");
						}
					}
					updatePageFields();
				} else {
					Toast.makeText(getApplicationContext(),
							"Unable to retrieve contact details at this time.",
							Toast.LENGTH_LONG).show();
					Log.e(TAG,
							"Unable to retrieve contact details to populate the contact details page...");
				}
			}
		});
	}

	/**
	 * This method is used to update the relevant spaces on the Contact Details
	 * screen with the appropriate information previously retrieved from the
	 * database
	 */
	protected void updatePageFields() {
		TextView contactNameView = (TextView) findViewById(R.id.detailedContactName);
		contactNameView.setText(contactName);
		TextView contactNumberView = (TextView) findViewById(R.id.detailedContactNumber);
		contactNumberView.setText(contactNumber);
		TextView contactGroupLeaderView = (TextView) findViewById(R.id.detailedContactGroupLeader);
		contactGroupLeaderView.setText(groupLeader);
		TextView contactGroupMemberView = (TextView) findViewById(R.id.detailedContactGroupMember);
		contactGroupMemberView.setText(groupMember);
	}

	/**
	 * This method is used if the user clicks on the 'OK' button on the Contact
	 * Details page @see activity_contact_details.xml
	 * 
	 * @param view
	 *            the base class for widgets, which are used to create
	 *            interactive UI components (buttons, text fields, etc.).
	 */
	public void OK(View view) {
		disableAllButtons();
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			Log.i(TAG, "User clicked OK. Going to Home Page...");
			Intent intent = new Intent(getApplicationContext(),
					HomeActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
			enableAllButtons();
		}
	}

	/**
	 * This method is used if the user clicks on the 'Delete' button on the
	 * contact details page.
	 * 
	 * @param view
	 *            the base class for widgets, which are used to create
	 *            interactive UI components (buttons, text fields, etc.).
	 */
	public void delete(View view) {
		disableAllButtons();
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			ParseQuery<ParseObject> contactID = ParseQuery.getQuery("Contact");
			contactID.whereEqualTo("number", contactNumber);
			contactID.whereEqualTo("name", contactName);
			contactID.findInBackground(new FindCallback<ParseObject>() {
				@Override
				public void done(List<ParseObject> contactList, ParseException e) {
					if (e == null) {
						Log.i(TAG, "Retrieved: " + contactList.size()
								+ " contacts");
						for (int i = 0; i < contactList.size(); i++) {
							ParseObject current = contactList.get(i);
							if (current.get("name").equals(contactName)
									&& current.get("number").equals(
											contactNumber)) {
								objectId = current.getObjectId();
								removeContactFromUserList();
							}
						}
					} else {
						Log.e(TAG,
								"UNABLE TO FIND CONTACT ID TO REMOVE FROM USER CONTACT LIST");
						e.printStackTrace();
					}
				}

				/**
				 * This method will retrieve the users contact list and remove
				 * the corresponding unique identifier of this contact
				 */
				private void removeContactFromUserList() {
					final ParseUser currentUser = ParseUser.getCurrentUser();
					currentUser.get("contacts");
					currentUser
							.fetchInBackground(new GetCallback<ParseObject>() {
								@Override
								public void done(ParseObject contacts,
										ParseException e) {
									if (e == null) {
										// Get the Array of Contacts for the
										// user
										Log.i(TAG,
												"Contacts: "
														+ contacts.get(
																"contacts")
																.toString());
										@SuppressWarnings("unchecked")
										ArrayList<String> contactArray = (ArrayList<String>) contacts
												.get("contacts");
										for (int i = 0; i < contactArray.size(); i++) {
											Log.i(TAG, "Current contact: "
													+ contactArray.get(i));
											if (contactArray.get(i).equals(
													objectId)) {
												contactArray.remove(i);
											}
										}
										currentUser.put("contacts",
												contactArray);
										try {
											contacts.save();
											Intent intent = new Intent(
													_instance,
													HomeActivity.class);
											startActivity(intent);
											finish();
										} catch (ParseException e1) {
											Log.e(TAG,
													"UNABLE TO SAVE USER AFTER REMOVING CONTACT");
											e1.printStackTrace();
										}
									}
								}
							});
				}
			});
		} else {
			showNoInternetConnectionDialog();
			enableAllButtons();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact_details, menu);
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

	/**
	 * This method is used to log the current user out of the application. The
	 * user can only log out if they have internet connection.
	 */
	private void logCurrentUserOut() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			Log.i(TAG, "Logging out user: " + ParseUser.getCurrentUser()
					+ "...");
			ParseUser.logOut();
			if (ParseUser.getCurrentUser() == null) {
				Log.i(TAG, "User successfully logged out!");
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

	/**
	 * This method is used for the 'Add Contact' menu option. This will display
	 * the activity that will allow a user to add a new contact to their contact
	 * list @see AddContactActivity.java and @see activity_add_contact.xml
	 */
	private void addNewContact() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			Log.i(TAG, "Starting Add Contact Activity...");
			Intent intent = new Intent(getApplicationContext(),
					AddContactActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
		}
	}

	/**
	 * This method is used for the 'View Map' menu option. This will display the
	 * activity that will allow a user to view their current location on a map @see
	 * MapsViewActivity.java and @see activity_maps_view.xml
	 */
	private void viewMap() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			Log.i(TAG, "Starting Map View Activity...");
			Intent intent = new Intent(getApplicationContext(),
					MapsViewActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
		}
	}

	/**
	 * This method is used for the 'Create Group' menu option. This will display
	 * the activity that will allow a user to select participants from their
	 * contact list who they wish to participate in the group they are creating @see
	 * SelectGroupParticipantsActivity.java and @see
	 * activity_select_group_participants.xml
	 */
	private void createGroup() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			Log.i(TAG, "Starting Create Group Activity...");
			Intent intent = new Intent(getApplicationContext(),
					SelectGroupParticipantsActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
		}
	}

	/**
	 * This method is used for the 'Home' menu option. This will display the
	 * home activity screen to the user. @see HomeActivity.java and @see
	 * activity_home.xml
	 */
	private void home() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			Log.i(TAG, "Starting Home Activity...");
			Intent intent = new Intent(getApplicationContext(),
					HomeActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
		}
	}

	/**
	 * This method is used for the 'Settings' menu option. This will display the
	 * activity that will allow a user to view and change their current settings
	 * for the application @see SettingsActivity.java and @see
	 * activity_settings.xml
	 */
	private void settings() {
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			Log.i(TAG, "Going to Settings page... ");
			Intent intent = new Intent(getApplicationContext(),
					SettingsActivity.class);
			startActivity(intent);
			finish();
		} else {
			showNoInternetConnectionDialog();
		}
	}

	/**
	 * Method that displays an alert dialog to the user prompting them to alter
	 * their Internet settings. The user can cancel the dialog or they can be
	 * directed to the 'Settings' screen for their phone.
	 */
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

	/**
	 * Disables all buttons that are on the ContactDetailsActivity.java view.
	 * 
	 * @see activity_contact_details.xml
	 */
	private void disableAllButtons() {
		Button okButton = (Button) findViewById(R.id.detailedContactOKButton);
		okButton.setClickable(false);
		okButton.setEnabled(false);
		Button deleteButton = (Button) findViewById(R.id.detailedContactDeleteButton);
		deleteButton.setClickable(false);
		deleteButton.setEnabled(false);
	}

	/**
	 * Enables all buttons that are on the ContactDetailsActivity.java view.
	 * 
	 * @see activity_contact_details.xml
	 */
	private void enableAllButtons() {
		Button okButton = (Button) findViewById(R.id.detailedContactOKButton);
		okButton.setClickable(true);
		okButton.setEnabled(true);
		Button deleteButton = (Button) findViewById(R.id.detailedContactDeleteButton);
		deleteButton.setClickable(true);
		deleteButton.setEnabled(true);
	}
}
