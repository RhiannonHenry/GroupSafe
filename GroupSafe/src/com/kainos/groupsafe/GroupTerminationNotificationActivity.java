package com.kainos.groupsafe;

import java.util.logging.Logger;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class GroupTerminationNotificationActivity extends Activity {

	static GroupTerminationNotificationActivity _instance = null;
	private static final Logger LOGGER = Logger
			.getLogger(GroupTerminationNotificationActivity.class.getName());
	private TextView pageTitle, pageMessage;
	private Button ok;
	private ParseUser myself;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_termination_notification);
		_instance = this;

		ok = (Button) findViewById(R.id.OKButton);
		myself = ParseUser.getCurrentUser();

		populatePage();
		enableAllButtons();
		OKButtonClicked();
	}

	private void OKButtonClicked() {
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				LOGGER.info("Setting User 'groupMember' to FALSE");
				myself.put("groupMember", false);
				myself.saveInBackground(new SaveCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null) {
							LOGGER.info("SUCCESS: 'groupMember' has been set --> FALSE");
							enableAllButtons();
							finish();
						} else {
							LOGGER.info("ERROR:: Unable to change 'groupMember' --> FALSE");
						}
					}
				});
			}
		});
	}

	private void populatePage() {
		Intent intent = getIntent();
		pageTitle = (TextView) findViewById(R.id.terminationNotificationTitle);
		pageTitle.setText(intent.getStringExtra("title"));
		pageMessage = (TextView) findViewById(R.id.terminationNotificationMessage);
		pageMessage.setText(intent.getStringExtra("alert"));
		
//		try {
//			action = intent.getAction();
//			channel = intent.getExtras().getString("com.parse.Channel");
//			json = new JSONObject(intent.getExtras()
//					.getString("com.parse.Data"));
//
//			LOGGER.info(" Got Action: " + action + "on channel " + channel
//					+ " with: ");
//
//			@SuppressWarnings("rawtypes")
//			Iterator itr = json.keys();
//			while (itr.hasNext()) {
//				key = itr.next().toString();
//				LOGGER.info(" ... " + key + " => " + json.getString(key));
//				if (key.equals("title")) {
//					pageTitle = (TextView) findViewById(R.id.terminationNotificationTitle);
//					pageTitle.setText(json.getString(key));
//					LOGGER.info("Setting Page Title to: " + json.getString(key));
//				} else if (key.equals("alert")) {
//					pageMessage = (TextView) findViewById(R.id.terminationNotificationMessage);
//					pageMessage.setText(json.getString(key));
//					LOGGER.info("Setting Page Message to: "
//							+ json.getString(key));
//				}
//			}
//		} catch (JSONException e) {
//			LOGGER.info("ERROR in JSON: " + e.getMessage());
//			e.printStackTrace();
//		}
	}

	private void enableAllButtons() {
		ok.setClickable(true);
		ok.setEnabled(true);
	}

	private void disableAllButtons() {
		ok.setClickable(false);
		ok.setEnabled(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.group_termination_notification, menu);
		return true;
	}

}
