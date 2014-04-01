package com.kainos.groupsafe;

import com.parse.Parse;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * This Activity is used to notify a user that is a group member that they have
 * left the groups geo-fence. This screen will be displayed if the user receives
 * a ExitGeoFenceNotification from the group leader.
 * 
 * @see activity_exit_geo_fence_notification.xml
 * 
 * @author Rhiannon Henry
 * 
 */
public class ExitGeoFenceNotificationActivity extends Activity {

	private static final String TAG = "EXIT_GEO_FENCE_NOTFICATION";
	private Button ok;

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
		setContentView(R.layout.activity_exit_geo_fence_notification);
		ok = (Button) findViewById(R.id.exitGeoFenctNotificationOKButton);
		enableAllButtons();
		OKButtonClicked();
	}

	/**
	 * This method is called when the user clicks 'OK' on the
	 * @see activity_exit_geo_fence_notification.xml screen. This will return the
	 * user to the group map.
	 */
	private void OKButtonClicked() {
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				Log.i(TAG,
						"Participant has been notified that they left the geo-fence.");
				finish();
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
		getMenuInflater().inflate(R.menu.exit_geo_fence_notification, menu);
		return true;
	}

	/**
	 * Enables all buttons that are on the ExitGeoFenceNotificationActivity.java
	 * view.
	 * 
	 * @see activity_exit_geo_fence_notification.xml
	 */
	private void enableAllButtons() {
		ok.setClickable(true);
		ok.setEnabled(true);
	}

	/**
	 * Disables all buttons that are on the
	 * ExitGeoFenceNotificationActivity.java view.
	 * 
	 * @see activity_exit_geo_fence_notification.xml
	 */
	private void disableAllButtons() {
		ok.setClickable(false);
		ok.setEnabled(false);
	}
}
