package com.kainos.groupsafe;

import java.util.logging.Logger;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ExitGeoFenceNotificationActivity extends Activity {

	static ExitGeoFenceNotificationActivity _instance = null;
	private static final Logger LOGGER = Logger
			.getLogger(ExitGeoFenceNotificationActivity.class.getName());
	private Button ok;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_exit_geo_fence_notification);
		_instance = this;

		ok = (Button) findViewById(R.id.exitGeoFenctNotificationOKButton);
		enableAllButtons();
		OKButtonClicked();
	}

	private void OKButtonClicked() {
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				disableAllButtons();
				LOGGER.info("Participant has been notified that they left the geo-fence.");
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.exit_geo_fence_notification, menu);
		return true;
	}

	private void enableAllButtons() {
		ok.setClickable(true);
		ok.setEnabled(true);
	}

	private void disableAllButtons() {
		ok.setClickable(false);
		ok.setEnabled(false);
	}
}
