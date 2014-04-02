package com.kainos.groupsafe;

import com.parse.Parse;
import com.parse.PushService;

import android.app.Application;

/**
 * This class is called when the application is first installed and is used to
 * Initialise Parse with the Application ID and Client Key. It also sets the
 * DefaultPushCallback for any incoming Push Notifications.
 * 
 * @author Rhiannon Henry
 * 
 */
public class GroupSafeApplication extends Application {

	private static boolean activityVisible;

	public GroupSafeApplication() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		PushService.setDefaultPushCallback(this, HomeActivity.class);
	}

	public static boolean isActivityVisible() {
		return activityVisible;
	}

	public static void activityResumed() {
		activityVisible = true;
	}

	public static void activityPaused() {
		activityVisible = false;
	}

}
