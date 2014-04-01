package com.kainos.groupsafe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

public class Utilities {

	/**
	 * Method that displays an alert dialog to the user prompting them to alter
	 * their Internet settings. The user can cancel the dialog or they can be
	 * directed to the 'Settings' screen for their phone.
	 */
	public static void showNoInternetConnectionDialog(final Activity activity) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
		alertDialog.setTitle("Internet Settings");
		alertDialog
				.setMessage("Cannot connect to internet. Enable Internet Services in Settings.");

		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Settings.ACTION_SETTINGS);
						activity.startActivity(intent);
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
