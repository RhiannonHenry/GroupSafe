package com.kainos.groupsafe;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseUser;

public class MenuUtils {

	/**
	 * This method is used to select the appropriate menu method using the
	 * parameters passed in.
	 * 
	 * @param id
	 *            an integer value that identified that menu option that as been
	 *            selected.
	 * @param activity
	 *            an Android Activity that made the menu request.
	 * @param internetPresent
	 *            a boolean value to indicate if the device has access to the
	 *            internet.
	 * @param TAG
	 *            a String value used for logging purposes.
	 * @return boolean
	 */
	public static boolean menuOptions(int id, Activity activity,
			boolean internetPresent, String TAG) {
		if (id == R.id.action_logout) {
			MenuUtils.logCurrentUserOut(activity, internetPresent, TAG);
		} else if (id == R.id.action_addContact) {
			MenuUtils.addNewContact(activity, internetPresent, TAG);
		} else if (id == R.id.action_viewMap) {
			MenuUtils.viewMap(activity, internetPresent, TAG);
		} else if (id == R.id.action_createGroup) {
			MenuUtils.createGroup(activity, internetPresent, TAG);
		} else if (id == R.id.action_settings) {
			MenuUtils.settings(activity, internetPresent, TAG);
		} else if (id == R.id.action_home) {
			MenuUtils.home(activity, internetPresent, TAG);
		}
		return true;
	}

	/**
	 * This method is used to log the current user out of the application. The
	 * user can only log out if they have internet connection.
	 */
	public static void logCurrentUserOut(Activity activity,
			boolean internetPresent, String TAG) {
		if (internetPresent) {
			Log.d(TAG, "Logging out user: " + ParseUser.getCurrentUser()
					+ "...");
			ParseUser.logOut();
			if (ParseUser.getCurrentUser() == null) {
				Log.i(TAG, "User successfully logged out!");
				Toast.makeText(activity.getApplicationContext(),
						"Successfully Logged Out!", Toast.LENGTH_LONG).show();
				Intent intent = new Intent(activity.getApplicationContext(),
						SplashActivity.class);
				activity.startActivity(intent);
				activity.finish();
			} else {
				Toast.makeText(activity.getApplicationContext(),
						"Please Try Again!", Toast.LENGTH_LONG).show();
			}
		} else {
			Utilities.showNoInternetConnectionDialog(activity);
		}
	}

	/**
	 * This method is used for the 'Add Contact' menu option. This will display
	 * the activity that will allow a user to add a new contact to their contact
	 * list @see AddContactActivity.java and @see activity_add_contact.xml
	 */
	public static void addNewContact(Activity activity,
			boolean internetPresent, String TAG) {
		if (internetPresent) {
			Log.d(TAG, "Starting Add Contact Activity...");
			Intent intent = new Intent(activity.getApplicationContext(),
					AddContactActivity.class);
			activity.startActivity(intent);
			activity.finish();
		} else {
			Utilities.showNoInternetConnectionDialog(activity);
		}
	}

	/**
	 * This method is used for the 'View Map' menu option. This will display the
	 * activity that will allow a user to view their current location on a map @see
	 * MapsViewActivity.java and @see activity_maps_view.xml
	 */
	public static void viewMap(Activity activity, boolean internetPresent,
			String TAG) {
		if (internetPresent) {
			Log.d(TAG, "Starting Map View Activity...");
			Intent intent = new Intent(activity.getApplicationContext(),
					MapsViewActivity.class);
			activity.startActivity(intent);
			activity.finish();
		} else {
			Utilities.showNoInternetConnectionDialog(activity);
		}
	}

	/**
	 * This method is used for the 'Create Group' menu option. This will display
	 * the activity that will allow a user to select participants from their
	 * contact list who they wish to participate in the group they are creating @see
	 * SelectGroupParticipantsActivity.java and @see
	 * activity_select_group_participants.xml
	 */
	public static void createGroup(Activity activity, boolean internetPresent,
			String TAG) {
		if (internetPresent) {
			Log.d(TAG, "Starting Create Group Activity...");
			Intent intent = new Intent(activity.getApplicationContext(),
					SelectGroupParticipantsActivity.class);
			activity.startActivity(intent);
			activity.finish();
		} else {
			Utilities.showNoInternetConnectionDialog(activity);
		}
	}

	/**
	 * This method is used for the 'Settings' menu option. This will display the
	 * activity that will allow a user to view and change their current settings
	 * for the application @see SettingsActivity.java and @see
	 * activity_settings.xml
	 */
	public static void settings(Activity activity, boolean internetPresent,
			String TAG) {
		if (internetPresent) {
			Log.d(TAG, "Going to Settings page... ");
			Intent intent = new Intent(activity.getApplicationContext(),
					SettingsActivity.class);
			activity.startActivity(intent);
			activity.finish();
		} else {
			Utilities.showNoInternetConnectionDialog(activity);
		}
	}

	/**
	 * This method is used for the 'Home' menu option. This will display the
	 * home activity screen to the user. @see HomeActivity.java and @see
	 * activity_home.xml
	 */
	public static void home(Activity activity, boolean internetPresent,
			String TAG) {
		if (internetPresent) {
			Log.i(TAG, "Starting Home Activity...");
			Intent intent = new Intent(activity.getApplicationContext(),
					HomeActivity.class);
			activity.startActivity(intent);
			activity.finish();
		} else {
			Utilities.showNoInternetConnectionDialog(activity);
		}
	}
}
