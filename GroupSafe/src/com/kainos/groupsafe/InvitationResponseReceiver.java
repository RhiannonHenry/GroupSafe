package com.kainos.groupsafe;

import java.util.Iterator;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InvitationResponseReceiver extends BroadcastReceiver {
	private static final String TAG = "InvitationResponseReceiver";
	private static final Logger LOGGER = Logger
			.getLogger(InvitationResponseReceiver.class.getName());

	String action, channel, key;
	JSONObject json;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			action = intent.getAction();
			channel = intent.getExtras().getString("com.parse.Channel");
			json = new JSONObject(intent.getExtras().getString(
					"com.parse.Data"));

			LOGGER.info("" + TAG + " Got Action: " + action + "on channel " + channel + " with: ");
			
			@SuppressWarnings("rawtypes")
			Iterator itr = json.keys();
			while (itr.hasNext()) {
				key = itr.next().toString();
				LOGGER.info("" + TAG + " ... " + key + " => "
						+ json.getString(key));
			}

		} catch (JSONException e) {
			LOGGER.info("ERROR in JSON: "+e.getMessage());
			e.printStackTrace();
		}
	}

}
