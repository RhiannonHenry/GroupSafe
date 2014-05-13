package com.kainos.groupsafe;

import java.util.ArrayList;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This is the activity screen that will be displayed to the user when they
 * first wish to register with the application. @see activity_register.xml and @see
 * register.xml
 * 
 * @author Rhiannon Henry
 */
public class RegisterActivity extends Activity {
	private static final String TAG = "Register_Activity";
	private static RegisterActivity _instance = null;
	private ParseInstallation installation = null;
	private Context context;

	/**
	 * Below are the Editable Text Areas on the register form
	 */
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
		setContentView(R.layout.activity_register);
		_instance = this;
		context = getApplicationContext();
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
	 * This method is called when the user opts to cancel registration and
	 * navigate back to the Splash screen
	 * 
	 * @param view
	 *            the base class for widgets, which are used to create
	 *            interactive UI components (buttons, text fields, etc.).
	 */
	public void registerCancel(View view) {
		disableAllButtons();

		Intent intent = new Intent(_instance, SplashActivity.class);
		startActivity(intent);
		finish();
	}

	/**
	 * This method is called when the user clicks on the 'Register' button. This
	 * method begins the registration process (validation, registration, login).
	 * 
	 * @param view
	 *            the base class for widgets, which are used to create
	 *            interactive UI components (buttons, text fields, etc.).
	 */
	public void register(View view) {
		disableAllButtons();
		if (NetworkUtilities.getConnectivityStatus(_instance) == 0) {
			Utilities.showNoInternetConnectionDialog(_instance);
			enableAllButtons();
		} else {
			proceedToRegistration();
		}
	}

	/**
	 * This method is used to populate local variables with the input that the
	 * user input to the fields on the registration form.
	 */
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

	/**
	 * This method is used to validate each field of the registration form with
	 * the users input. If the validation fails at any point, the errorsPresent
	 * boolean variable will be set to true indicating that an validation
	 * failed.
	 */
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

	/**
	 * This method creates a ParseUser with the details the user input and
	 * initialises some other attributes of user to their starting state.
	 */
	private void signUp() {
		ArrayList<String> contacts = new ArrayList<String>();
		ArrayList<String> emergencyContacts = new ArrayList<String>();
		String currentLocation = "";
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
			/*
			 * (non-Javadoc)
			 * 
			 * @see com.parse.SignUpCallback#done(com.parse.ParseException)
			 */
			@Override
			public void done(ParseException e) {
				if (e == null) {
					Log.i(TAG, "Account Created Successfully");
					// Show a simple Toast message upon successful registration
					registerParseDevice();
				} else {
					enableAllButtons();
					Log.e(TAG, "A user with username: {" + username
							+ "} already exists");
					Toast.makeText(getApplicationContext(),
							"Username already exists!", Toast.LENGTH_LONG)
							.show();
					e.printStackTrace();
				}
			}

			/**
			 * Creates an installation associated with this device
			 */
			private void registerParseDevice() {
				new ParseAsyncTask().execute(context);
			}
		});
	}

	/**
	 * This method clears any existing prior errors that may remain from a prior
	 * registration attempt.
	 */
	private void clearExistingErrors() {
		registerUsernameInput.setError(null);
		registerPasswordInput.setError(null);
		registerRetypePasswordInput.setError(null);
		registerEmailInput.setError(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
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
		if (id == R.id.action_settings) {
			goToSettings();
		}
		return true;
	}

	/**
	 * This method is used to direct the current user to the 'Settings' page for
	 * their phone
	 */
	private void goToSettings() {
		Intent intent = new Intent(Settings.ACTION_SETTINGS);
		startActivity(intent);
		finish();
	}

	/**
	 * This method is used to enable all buttons on the current screen
	 */
	private void enableAllButtons() {
		Button registerCancelButton = (Button) findViewById(R.id.registerCancelButton);
		registerCancelButton.setClickable(true);
		registerCancelButton.setEnabled(true);

		Button registerButton = (Button) findViewById(R.id.registerButton);
		registerButton.setClickable(true);
		registerButton.setEnabled(true);
	}

	/**
	 * This method is used to disable all buttons on the current screen
	 */
	private void disableAllButtons() {
		Button registerCancelButton = (Button) findViewById(R.id.registerCancelButton);
		registerCancelButton.setClickable(false);
		registerCancelButton.setEnabled(false);

		Button registerButton = (Button) findViewById(R.id.registerButton);
		registerButton.setClickable(false);
		registerButton.setEnabled(false);
	}
	
	private class ParseAsyncTask extends AsyncTask<Context, Void, Context> {
		@Override
		protected Context doInBackground(Context... context) {
			installation = ParseInstallation.getCurrentInstallation();
			return null;
		}

		@Override
		protected void onPostExecute(Context result) {
			Log.i(TAG, "Finished ASYNC TASK");
			installation.saveInBackground(
					new SaveCallback() {
						@Override
						public void done(ParseException e) {
							if (e == null) {
								Toast.makeText(getApplicationContext(),
										"Successfully Registered!",
										Toast.LENGTH_LONG).show();
								Intent intent = new Intent(_instance,
										HomeActivity.class);
								startActivity(intent);
								finish();
							} else {
								Log.e(TAG,
										"UNABLE TO REGISTER WITHOUT INSTALLATION");
							}
						}
					});
			
		}
	}

}
