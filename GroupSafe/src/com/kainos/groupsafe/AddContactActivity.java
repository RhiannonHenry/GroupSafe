package com.kainos.groupsafe;

import java.util.List;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity that will be displayed to the user if they select 'Add Contact' from
 * the menu at any point. This activity allows a user to add a new contact to
 * their address book.
 * 
 * Layout: @see activity_add_contact.xml Menu: @see add_contact.xml
 * 
 * @author Rhiannon Henry
 * 
 */
public class AddContactActivity extends Activity {

	private static final String TAG = "ADD_CONTACT_ACTIVITY";
	private static String contactName = null;
	private static String contactNumber = null;
	private static String newContactObjectID = null;
	private static AddContactActivity _instance = null;
	private boolean internetPresent = false;
	private ConnectionDetector connectionDetector;
	private EditText addContactName;
	private EditText addContactNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		connectionDetector = new ConnectionDetector(getApplicationContext());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_contact);
		_instance = this;
		enableAllButtons();
	}

	/**
	 * This method is called when the user clicks 'Cancel' from the AddContact
	 * view @see activity_add_contact.xml. The user will be returned to the Home
	 * screen.
	 * 
	 * @param view
	 *            the base class for widgets, which are used to create
	 *            interactive UI components (buttons, text fields, etc.).
	 */
	public void cancelNewContact(View view) {
		disableAllButtons();
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			Intent intent = new Intent(_instance, HomeActivity.class);
			startActivity(intent);
			finish();
		} else {
			Utilities.showNoInternetConnectionDialog(this);
			enableAllButtons();
		}
	}

	/**
	 * This method is called when the user clicks 'Save' from the AddContact
	 * view @see activity_add_contact.xml. The contact that the user specified
	 * will be validated and saved.
	 * 
	 * @param view
	 *            the base class for widgets, which are used to create
	 *            interactive UI components (buttons, text fields, etc.).
	 */
	public void saveNewContact(View view) {
		disableAllButtons();
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			addContactToDatabase();
		} else {
			Utilities.showNoInternetConnectionDialog(this);
			enableAllButtons();
		}
	}

	/**
	 * This method is used to save the information that the user input into the
	 * form to local variables: Contact name and Contact number
	 */
	private void addContactToDatabase() {
		addContactName = (EditText) findViewById(R.id.addContactNameInput);
		addContactNumber = (EditText) findViewById(R.id.addContactNumberInput);
		contactName = addContactName.getText().toString();
		contactNumber = addContactNumber.getText().toString();

		checkThatUserExistsInDatabase();
	}

	/**
	 * This method checks if a user with that number specified by the user is
	 * currently in the database. If there is, then this is a valid contact,
	 * otherwise it isn't.
	 */
	private void checkThatUserExistsInDatabase() {
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("username", contactNumber);
		query.findInBackground(new FindCallback<ParseUser>() {
			@Override
			public void done(List<ParseUser> userList, ParseException e) {
				if (e == null) {
					if (userList.size() > 0) {
						Log.i(TAG, "Successfully retrieved user: "
								+ userList.get(0).getUsername());
						checkIfContactIsPresentInTable();
					} else {
						enableAllButtons();
						Log.e(TAG, "User doesn't exist in database");
						Toast.makeText(getApplicationContext(),
								"User does not exist. Try Again!",
								Toast.LENGTH_LONG).show();
					}
				} else {
					enableAllButtons();
					Log.e(TAG, "An error occurred retrieving the user!");
					Toast.makeText(getApplicationContext(),
							"User does not exist. Try Again!",
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	/**
	 * This method checks if there is a contact with an identical name and
	 * number currently in the database. If there is, we will use that contact
	 * and reference it, otherwise, we will create a new contact
	 */
	private void checkIfContactIsPresentInTable() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Contact");
		query.whereEndsWith("name", contactName);
		query.whereEqualTo("number", contactNumber);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> foundContacts, ParseException e) {
				if (e == null) {
					if (foundContacts.size() != 0) {
						Log.i(TAG, "A CONTACT ALREADY EXISTS");
						retrieveObjectID();
					} else {
						Log.i(TAG, "THIS IS A NEW CONATCT");
						createNewContactInTable();
					}
				}
			}
		});
	}

	/**
	 * This method creates a new contact with the user specified name and number
	 * and saves it to the database in the Contact table.
	 */
	protected void createNewContactInTable() {
		ParseObject contact = new ParseObject("Contact");
		contact.put("name", contactName);
		contact.put("number", contactNumber);
		try {
			contact.save();
		} catch (ParseException e2) {
			Log.e(TAG, "UNABLE TO SAVE NEW CONTACT AT THIS TIME...");
			e2.printStackTrace();
		}
		retrieveObjectID();
	}

	/**
	 * This method is used to get the unique identifier of the contact that the
	 * user wishes to be added to their contact list. This unique identifier
	 * will be used to associate the contact with the user.
	 */
	private void retrieveObjectID() {
		ParseQuery<ParseObject> contactID = ParseQuery.getQuery("Contact");
		contactID.whereEqualTo("number", contactNumber);
		contactID.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> contactList, ParseException e) {
				if (e == null) {
					Log.i(TAG, "Retrieved: " + contactList.size() + " contacts");
					for (int i = 0; i < contactList.size(); i++) {
						ParseObject current = contactList.get(i);
						Log.i(TAG,
								"Contact " + i + ": Name: "
										+ current.get("name") + " Number: "
										+ current.get("number") + " ObjectID: "
										+ current.getObjectId());
						if (current.get("name").equals(contactName)
								&& current.get("number").equals(contactNumber)) {
							newContactObjectID = current.getObjectId();
							addContactToUserContactList(newContactObjectID);
						} else {
							Log.e(TAG, "NO MATCH FOR CONTACT");
						}
					}
				} else {
					Log.e(TAG, "UNABLE TO RETRIEVE CONTACT FROM CONTACT TABLE");
					e.printStackTrace();
				}
			}

			/**
			 * This method is used to place the unique identifier for the
			 * contact into the list of contacts for the user. Once this has
			 * been updated, the user will be returned to the Home screen.
			 * 
			 * @param newContactObjectID
			 *            the string value of the unique identifier for the new
			 *            contact
			 */
			private void addContactToUserContactList(String newContactObjectID) {
				Log.i(TAG, "New Contact ObjectID: " + newContactObjectID);
				ParseUser currentUser = ParseUser.getCurrentUser();
				currentUser.addUnique("contacts", newContactObjectID);
				currentUser.saveInBackground(new SaveCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null) {
							Log.i(TAG, "SAVED CONTACT SUCCESSFULLY");
							Toast.makeText(getApplicationContext(),
									"Saved Contact Successfully!",
									Toast.LENGTH_LONG).show();
							Intent intent = new Intent(_instance,
									HomeActivity.class);
							startActivity(intent);
							finish();
						} else {
							Toast.makeText(getApplicationContext(),
									"Unable to Save Contact...",
									Toast.LENGTH_LONG).show();
							Log.e(TAG, "ENCOUNTERED ERROR SAVING CONTACT");
							Log.e(TAG, e.getMessage());
						}
					}
				});
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
		getMenuInflater().inflate(R.menu.add_contact, menu);
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
		return MenuUtils.menuOptions(id, this, internetPresent, TAG);
	}

	

	/**
	 * Enables all buttons that are on the AddContactActivity.java view.
	 * 
	 * @see activity_add_contact.xml
	 */
	private void enableAllButtons() {
		Button saveButton = (Button) findViewById(R.id.addContactSaveButton);
		saveButton.setClickable(true);
		saveButton.setEnabled(true);
		Button cancelButton = (Button) findViewById(R.id.addContactCancelButton);
		cancelButton.setClickable(true);
		cancelButton.setEnabled(true);
	}

	/**
	 * Disables all buttons that are on the AddContactActivity.java view.
	 * 
	 * @see activity_add_contact.xml
	 */
	public void disableAllButtons() {
		Button saveButton = (Button) findViewById(R.id.addContactSaveButton);
		saveButton.setClickable(false);
		saveButton.setEnabled(false);
		Button cancelButton = (Button) findViewById(R.id.addContactCancelButton);
		cancelButton.setClickable(false);
		cancelButton.setEnabled(false);
	}
}
