package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.kainos.groupsafe.utilities.ConnectionDetector;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	private final static Logger LOGGER = Logger
			.getLogger(RegisterActivity.class.getName());
	static RegisterActivity _instance = null;

	private EditText registerUsernameInput;
	private EditText registerPasswordInput;
	private EditText registerRetypePasswordInput;
	private EditText registerDisplayNameInput;
	private EditText registerEmailInput;

	private static String username = null;
	private static String password = null;
	private static String retypedPassword = null;
	private static String displayName = null;
	private static String emailAddress = null;

	private boolean errorsPresent = false;

	private boolean internetPresent = false;
	ConnectionDetector connectionDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		connectionDetector = new ConnectionDetector(getApplicationContext());

		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");

		// Used to track statistics around application opens.
		ParseAnalytics.trackAppOpened(getIntent());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		_instance = this;

		enableAllButtons();
	}
	
	public void registerCancel(View view) {
		disableAllButtons();

		Intent intent = new Intent(getApplicationContext(),
				SplashActivity.class);
		startActivity(intent);
		finish();
	}

	public void register(View view) {
		disableAllButtons();
		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			proceedToRegistration();
		} else {
			showNoInternetConnectionDialog();
			enableAllButtons();
		}
	}

	private void proceedToRegistration() {
		if (errorsPresent) {
			clearExistingErrors();
		}

		// Get the objects input by the user.
		registerUsernameInput = (EditText) findViewById(R.id.registerUsernameInput);
		registerPasswordInput = (EditText) findViewById(R.id.registerPasswordInput);
		registerRetypePasswordInput = (EditText) findViewById(R.id.registerRetypePasswordInput);
		registerDisplayNameInput = (EditText) findViewById(R.id.registerDisplayNameInput);
		registerEmailInput = (EditText) findViewById(R.id.registerEmailInput);

		// Convert the input objects to Strings
		username = registerUsernameInput.getText().toString();
		password = registerPasswordInput.getText().toString();
		retypedPassword = registerRetypePasswordInput.getText().toString();
		displayName = registerDisplayNameInput.getText().toString();
		emailAddress = registerEmailInput.getText().toString();

		validateForm();
	}

	private void validateForm() {
		View focusView = null;
		String usernameRegex = "^[0-9]{11}$";
		String passwordRegex = "^([a-zA-Z0-9@*#]{4,8})$";

		// Check that username has been input
		if (TextUtils.isEmpty(username)) {
			registerUsernameInput
					.setError(getString(R.string.error_requiredField));
			focusView = registerUsernameInput;
			errorsPresent = true;
		}
		// Check that entered Username is valid
		else if (!username.matches(usernameRegex)) {
			registerUsernameInput
					.setError(getString(R.string.error_invalidUsername));
			focusView = registerUsernameInput;
			errorsPresent = true;
		}

		// Check that retyped password has been entered
		if (TextUtils.isEmpty(retypedPassword)) {
			registerRetypePasswordInput
					.setError(getString(R.string.error_requiredField));
			focusView = registerRetypePasswordInput;
			errorsPresent = true;
		}
		// Checks that both passwords match
		else if (password != null && !retypedPassword.equals(password)) {
			registerRetypePasswordInput
					.setError(getString(R.string.error_mismatchedPasswords));
			focusView = registerRetypePasswordInput;
			errorsPresent = true;
		}

		// Checks that a password has been entered
		if (TextUtils.isEmpty(password)) {
			registerPasswordInput
					.setError(getString(R.string.error_requiredField));
			focusView = registerPasswordInput;
			errorsPresent = true;
		}
		// Check that the password entered is valid
		else if (!password.matches(passwordRegex)) {
			registerPasswordInput
					.setError(getString(R.string.error_invalidPassword));
			focusView = registerPasswordInput;
			errorsPresent = true;
		}

		// Check that an email address has been entered
		if (TextUtils.isEmpty(emailAddress)) {
			registerEmailInput
					.setError(getString(R.string.error_requiredField));
			focusView = registerEmailInput;
			errorsPresent = true;
		}
		// Check that the email address is valid
		else if (!emailAddress.contains("@")) {
			registerEmailInput
					.setError(getString(R.string.error_invalidEmailAddress));
			focusView = registerEmailInput;
			errorsPresent = true;
		}

		if (errorsPresent) {
			enableAllButtons();
			focusView.requestFocus();
			errorsPresent = false;
		} else {
			signUp();
		}
	}

	private void signUp() {
		ArrayList<String> contacts = new ArrayList<String>();
		ArrayList<String> emergencyContacts = new ArrayList<String>();
		ArrayList<String> currentLocation = new ArrayList<String>();
		ParseUser user = new ParseUser();
		user.setUsername(username);
		user.setPassword(password);
		user.setEmail(emailAddress);
		user.put("displayName", displayName);
		user.put("groupLeader", false);
		user.put("groupMember", false);
		user.put("contacts", contacts);
		user.put("emergencyContacts", emergencyContacts);
		user.put("currentLocation", currentLocation);

		user.signUpInBackground(new SignUpCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					LOGGER.info("Account Created Successfully");
					// Show a simple Toast message upon successful registration
					Toast.makeText(getApplicationContext(),
							"Successfully Registered!", Toast.LENGTH_LONG)
							.show();
					Intent intent = new Intent(getApplicationContext(),
							HomeActivity.class);
					startActivity(intent);
					finish();
				} else {
					LOGGER.info("A user with username: {" + username
							+ "} already exists");
					Toast.makeText(getApplicationContext(),
							"Username already exists!", Toast.LENGTH_LONG)
							.show();
				}
			}
		});
	}

	private void clearExistingErrors() {
		registerUsernameInput.setError(null);
		registerPasswordInput.setError(null);
		registerRetypePasswordInput.setError(null);
		registerEmailInput.setError(null);
	}

	/*
	 * MENU...
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			goToSettings();
		}
		return true;
	}

	private void goToSettings() {
		Intent intent = new Intent(Settings.ACTION_SETTINGS);
		startActivity(intent);
	}
	
	/*
	 * UTILITIES...
	 */
	private void enableAllButtons() {
		Button registerCancelButton = (Button) findViewById(R.id.registerCancelButton);
		registerCancelButton.setClickable(true);
		registerCancelButton.setEnabled(true);

		Button registerButton = (Button) findViewById(R.id.registerButton);
		registerButton.setClickable(true);
		registerButton.setEnabled(true);
	}

	private void disableAllButtons() {
		Button registerCancelButton = (Button) findViewById(R.id.registerCancelButton);
		registerCancelButton.setClickable(false);
		registerCancelButton.setEnabled(false);

		Button registerButton = (Button) findViewById(R.id.registerButton);
		registerButton.setClickable(false);
		registerButton.setEnabled(false);
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
