package com.kainos.groupsafe;

import java.util.ArrayList;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ParseException;

/**
 * Initial Activity that will be displayed to the user. The user can Sign-in or
 * Register from this Android Activity
 * 
 * Layout: @see activity_splash.xml Menu: @see splash.xml
 * 
 * @author Rhiannon Henry
 */
public class SplashActivity extends Activity {

	private final static String TAG = "Splash_Activity";
	private static SplashActivity _instance = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());
		ParseUser currentUser = ParseUser.getCurrentUser();
		Log.i(TAG, "User: " + currentUser);
		if (currentUser != null) {
			currentUser = null;
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		_instance = this;
		// createTestOrganization();
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
	 * When the user selects the 'Register/SignUp' button on the Splash
	 * Activity, this method will be triggered. The method checks for internet
	 * connection before proceeding opening the 'Registration Form' @see
	 * RegisterActivity.java
	 * 
	 * @param view
	 *            the base class for widgets, which are used to create
	 *            interactive UI components (buttons, text fields, etc.).
	 */
	public void registerNow(View view) {
		if (NetworkUtilities.getConnectivityStatus(_instance) == 0) {
			Utilities.showNoInternetConnectionDialog(_instance);
			enableAllButtons();
		} else {
			Log.i(TAG,
					"The user has clicked 'Register'. Now entering RegisterActivity.class");
			Intent intent = new Intent(_instance, RegisterActivity.class);
			startActivity(intent);
		}
	}

	/**
	 * When the user selected the 'Sign-in' button on the Splash Activity
	 * screen, this method will be triggered. The method checks for an internet
	 * connection before proceeding @see ConnectionDetector.
	 * 
	 * @param view
	 *            the base class for widgets, which are used to create
	 *            interactive UI components (buttons, text fields, etc.).
	 */
	public void signin(View view) {
		disableAllButtons();
		if (NetworkUtilities.getConnectivityStatus(_instance) == 0) {
			Utilities.showNoInternetConnectionDialog(_instance);
			enableAllButtons();
		} else {
			proceedToLogin();
		}
	}

	/**
	 * This method gets the users input from the Username and Password text
	 * fields. It will then attempt to log the user in using
	 * ParseUser.LogInInBackground()
	 * 
	 * The user will either be logged in successfully and the Home Android
	 * Activity will be displayed @see HomeActivity.java, or the will receive
	 * notification that the username/password they entered was invalid.
	 */
	private void proceedToLogin() {
		EditText usernameInput = (EditText) findViewById(R.id.usernameInput);
		EditText passwordInput = (EditText) findViewById(R.id.passwordInput);
		String username = usernameInput.getText().toString();
		String password = passwordInput.getText().toString();
		Log.i(TAG, "Username: " + username);
		Log.i(TAG, "Password: " + password);
		Log.i(TAG, "Attempting to send to parse");

		ParseUser.logInInBackground(username, password, new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException e) {
				if (e == null && user != null) {
					Toast.makeText(getApplicationContext(),
							"Successfully Logged In!", Toast.LENGTH_LONG)
							.show();
					Intent intent = new Intent(_instance, HomeActivity.class);
					startActivity(intent);
					finish();
				} else if (user == null) {
					usernameOrPasswordIsInvalid();
				} else {
					somethingWentWrong();
				}
			}

			/**
			 * Displays an error indicating that the supplied user credentials
			 * were not found in the database
			 */
			private void usernameOrPasswordIsInvalid() {
				@SuppressWarnings("unused")
				View focusView = null;
				View errorField = findViewById(R.id.usernameInput);
				((TextView) errorField)
						.setError(getString(R.string.error_invalid_credentials));
				focusView = errorField;
				enableAllButtons();
			}

			/**
			 * Displays a short message indicating that there was an error
			 * sending the LogIn request to Parse and prompting the user to 'Try
			 * Again'
			 */
			private void somethingWentWrong() {
				Toast.makeText(getApplicationContext(), "Try Again",
						Toast.LENGTH_LONG).show();
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
		getMenuInflater().inflate(R.menu.splash, menu);
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
	 * Displays the phones 'Settings' page to the user.
	 */
	private void goToSettings() {
		Intent intent = new Intent(Settings.ACTION_SETTINGS);
		startActivity(intent);
	}

	/**
	 * Enables all buttons that are on the SplashActivity.java view.
	 * 
	 * @see activity_splash.xml
	 */
	private void enableAllButtons() {
		Button signinButton = (Button) findViewById(R.id.signinButton);
		signinButton.setClickable(true);
		signinButton.setEnabled(true);

		Button rigisterNowButton = (Button) findViewById(R.id.registerNowButton);
		rigisterNowButton.setClickable(true);
		rigisterNowButton.setEnabled(true);
	}

	/**
	 * Disables all buttons that are on the SplashActivity.java view.
	 * 
	 * @see activity_splash.xml
	 */
	private void disableAllButtons() {
		Button signinButton = (Button) findViewById(R.id.signinButton);
		signinButton.setClickable(false);
		signinButton.setEnabled(false);
		Button registerNowButton = (Button) findViewById(R.id.registerNowButton);
		registerNowButton.setClickable(false);
		registerNowButton.setEnabled(false);
	}

	@SuppressWarnings("unused")
	private void createTestOrganization() {
		ArrayList<String> organizationMembers = new ArrayList<String>();

		ParseObject organization = new ParseObject("Organization");
		organization.put("organizationName", "Test Organization");
		organization.put("organizationMembers", organizationMembers);
		try {
			organization.save();
		} catch (ParseException e2) {
			Log.i(TAG, "UNABLE TO CREATE NEW ORGANIZATION...");
			e2.printStackTrace();
		}
	}

}
