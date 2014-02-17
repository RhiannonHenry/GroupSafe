package com.kainos.groupsafe;

import java.util.logging.Logger;

import com.kainos.groupsafe.utilities.ConnectionDetector;
import com.kainos.groupsafe.utilities.Contact;
import com.kainos.groupsafe.utilities.EmergencyContact;
import com.parse.Parse;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;

public class EmergencyContactDetailsActivity extends Activity {

	private final static Logger LOGGER = Logger
			.getLogger(EmergencyContactDetailsActivity.class.getName());
	static EmergencyContactDetailsActivity _instance = null;
	private static String emergencyContactName = null;
	private static String emergencyContactNumber = null;
	private static String emergencyContactRelationship = null;

	private String objectId = null;
	private boolean internetPresent = false;
	ConnectionDetector connectionDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");

		connectionDetector = new ConnectionDetector(getApplicationContext());

		Intent intent = getIntent();
		emergencyContactName = intent.getStringExtra("name");
		emergencyContactNumber = intent.getStringExtra("number");
		emergencyContactRelationship = intent.getStringExtra("relationship");

		LOGGER.info("Got Emergenct Contact: " + emergencyContactName + ", "
				+ emergencyContactNumber + ", " + emergencyContactRelationship
				+ " from SettingsActivity");

		this.setTitle(emergencyContactName);

		internetPresent = connectionDetector.isConnectedToInternet();
		if (internetPresent) {
			LOGGER.info("Populating page...");
			populatePage();
		} else {
			showNoInternetConnectionDialog();
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_emergency_contact_details);
		_instance = this;
	}

	private void populatePage() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.emergency_contact_details, menu);
		return true;
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
