package com.kainos.groupsafe;

import com.parse.Parse;
import com.parse.PushService;

import android.app.Application;

public class GroupSafeApplication extends Application {

	public GroupSafeApplication() {

	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Initialize the Parse SDK.
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");

		// Specify an Activity to handle all pushes by default.
		PushService.setDefaultPushCallback(this, HomeActivity.class);
	}
}
