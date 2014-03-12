package com.kainos.groupsafe;

import java.util.Iterator;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationResponseReceiver extends BroadcastReceiver {
	private static final String TAG = "InvitationResponseReceiver";
	private static final Logger LOGGER = Logger
			.getLogger(NotificationResponseReceiver.class.getName());

	String action, channel, key, title, alert, groupLeaderId, groupId, participantId;
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
				while (itr.hasNext()) {
					key = itr.next().toString();
					LOGGER.info(" ... " + key + " => " + json.getString(key));
					if (key.equals("title")) {
						title = json.getString(key);
					} else if (key.equals("alert")) {
						alert = json.getString(key);
					} else if (key.equals("groupLeaderId")) {
						groupLeaderId = json.getString(key);
					} else if (key.equals("groupId")) {
						groupId = json.getString(key);
					} else if (key.equals("participantId")) {
						participantId = json.getString(key);
					}
				}
			}
			
			if(action.equalsIgnoreCase("com.kainos.groupsafe.GroupTerminationNotificationActivity")){
				Intent i = new Intent(context, GroupTerminationNotificationActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("title", title);
				i.putExtra("alert", alert);
				context.startActivity(i);
			} else if(action.equalsIgnoreCase("com.kainos.groupsafe.AcceptDeclineInvitationActivity")){
				Intent i = new Intent(context, AcceptDeclineInvitationActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("com.parse.Channel", channel);
				i.putExtra("title", title);
				i.putExtra("alert", alert);
				i.putExtra("groupLeaderId", groupLeaderId);
				i.putExtra("groupId", groupId);
				i.putExtra("participantId", participantId);
				
				context.startActivity(i);
			}

		} catch (JSONException e) {
			LOGGER.info("ERROR in JSON: "+e.getMessage());
			e.printStackTrace();
		}
	}

}
