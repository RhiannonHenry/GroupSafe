package com.kainos.groupsafe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ConnectionDetectorReceiver extends BroadcastReceiver {

	private static final String TAG = "CONNECTION_DETECTOR_RECEIVER";

	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.i(TAG, "Connection change detected...");
		if (GroupSafeApplication.isActivityVisible() == true) {
			Log.i(TAG, "Connection change detected...App is displayed");
			int status = NetworkUtilities.getConnectivityStatus(context);
			if (status == 0) {
				Log.i(TAG, "Displaying Dialog...");
				Intent i = new Intent(context,
						NoInternetConnectionActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);
			} else if (status == 1) {
				Toast.makeText(context, "Connected to WiFi", Toast.LENGTH_LONG)
						.show();
			} else if (status == 2) {
				Toast.makeText(context, "Connected to Mobile Data",
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
