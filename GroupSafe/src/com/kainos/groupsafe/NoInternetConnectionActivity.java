package com.kainos.groupsafe;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;

public class NoInternetConnectionActivity extends Activity {

	private static final String TAG = "NO_INTERNET_CONNECTION_ACTIVITY";
	private static NoInternetConnectionActivity _instance = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_instance = this;
		showNoInternetConnectionDialog();
	}

	private void showNoInternetConnectionDialog() {
		Log.i(TAG, "Displaying no internet connection dialog");
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(_instance);
		alertDialog.setTitle("Internet Settings");
		alertDialog
				.setMessage("Cannot connect to internet. Enable Internet Services in Settings.");

		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Settings.ACTION_SETTINGS);
						_instance.startActivity(intent);
					}
				});

		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						_instance.finish();
					}
				});
		alertDialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
