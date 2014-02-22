package com.kainos.groupsafe;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.PushService;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class NotificationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		PushService.setDefaultPushCallback(this, NotificationActivity.class);
		ParseAnalytics.trackAppOpened(getIntent());
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.notification, menu);
		return true;
	}

}
