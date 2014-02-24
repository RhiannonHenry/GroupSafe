package com.kainos.groupsafe;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class AcceptDeclineInvitationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_accept_decline_invitation);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.accept_decline_invitation, menu);
		return true;
	}

}
