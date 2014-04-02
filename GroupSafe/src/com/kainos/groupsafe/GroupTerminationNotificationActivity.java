package com.kainos.groupsafe;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * This Activity will be displayed if the user receives a group termination
 * notification from the group leader. This screen will be displayed to notify
 * the user of the termination of a group to which they are participating.
 * 
 * Layout: @see activity_group_termination_notification.xml Menu: @see
 * group_termination_notification.xml
 * 
 * @author Rhiannon Henry
 */
public class GroupTerminationNotificationActivity extends Activity {

	private static GroupTerminationNotificationActivity _instance = null;
	private static final String TAG = "GROUP_TERMINATION_NOTIFICATION";
	private TextView pageTitle, pageMessage;
	private Button ok;
	private ParseUser myself;

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
		setContentView(R.layout.activity_group_termination_notification);
		_instance = this;
		ok = (Button) findViewById(R.id.OKButton);
		myself = ParseUser.getCurrentUser();
		populatePage();
		enableAllButtons();
		OKButtonClicked();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		GroupSafeApplication.activityResumed();
	}

	@Override
	protected void onPause() {
		super.onPause();
		GroupSafeApplication.activityPaused();
	}

	/**
	 * This method is called if the user clicks the 'OK' button on the group
	 * termination notification screen @see
	 * activity_group_termination_notification.xml. This method will set the
	 * 'groupMember' status of the user to FALSE - indicating that they are no
	 * longer a group member.
	 */
	private void OKButtonClicked() {
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				Log.i(TAG, "Setting User 'groupMember' to FALSE");
				myself.put("groupMember", false);
				myself.saveInBackground(new SaveCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null) {
							Log.i(TAG,
									"SUCCESS: 'groupMember' has been set --> FALSE");
							enableAllButtons();
							Intent intent = new Intent(_instance,
									HomeActivity.class);
							startActivity(intent);
							finish();
						} else {
							Log.e(TAG,
									"ERROR:: Unable to change 'groupMember' --> FALSE");
						}
					}
				});
			}
		});
	}

	/**
	 * This method is called on the activity start so that the fields in the
	 * screen are populated with the correct information retrieved from the
	 * group leader via the intent.
	 */
	private void populatePage() {
		Intent intent = getIntent();
		pageTitle = (TextView) findViewById(R.id.terminationNotificationTitle);
		pageTitle.setText(intent.getStringExtra("title"));
		pageMessage = (TextView) findViewById(R.id.terminationNotificationMessage);
		pageMessage.setText(intent.getStringExtra("alert"));
	}

	/**
	 * This method is used to enable all buttons on the screen, so that the user
	 * can click them.
	 * 
	 * @see activity_group_termination_notification.xml
	 */
	private void enableAllButtons() {
		ok.setClickable(true);
		ok.setEnabled(true);
	}

	/**
	 * This method is used to disable all buttons on the screen, so that the
	 * user cannot click them.
	 * 
	 * @see activity_group_termination_notification.xml
	 */
	private void disableAllButtons() {
		ok.setClickable(false);
		ok.setEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.group_termination_notification, menu);
		return true;
	}

}
