package com.groupsafe.application;

import com.kainos.groupsafe.NotificationActivity;
import com.parse.Parse;
import com.parse.PushService;

public class Application extends android.app.Application {

	public Application() {
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Initialize the Parse SDK.
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");

		// Specify an Activity to handle all pushes by default.
		PushService.setDefaultPushCallback(this, NotificationActivity.class);
	}
}
