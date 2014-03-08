package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
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
import android.widget.Toast;

public class AcceptDeclineInvitationActivity extends Activity {

	static AcceptDeclineInvitationActivity _instance = null;
	private static final Logger LOGGER = Logger
			.getLogger(AcceptDeclineInvitationActivity.class.getName());
	private static final String STATUS_ACCEPT = "ACCEPTED";
	private static final String STATUS_DECLINE = "DECLINED";
	private String action, channel, key, userId, groupLeaderId, groupId,
			participantId, removeParticipant;
	private JSONObject json;
	private TextView pageTitle, pageMessage;
	private Button accept, decline;
	private ParseUser myself;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_accept_decline_invitation);
		_instance = this;

		accept = (Button) findViewById(R.id.acceptButton);
		decline = (Button) findViewById(R.id.declineButton);
		myself = ParseUser.getCurrentUser();

		populatePage();
		enableAllButtons();
		acceptButtonClicked();
		declineButtonClicked();
	}

	private void populatePage() {
		Intent intent = getIntent();
		try {
			action = intent.getAction();
			channel = intent.getExtras().getString("com.parse.Channel");
			userId = channel.substring(4);
			LOGGER.info("User ID: " + userId);

			json = new JSONObject(intent.getExtras()
					.getString("com.parse.Data"));

			LOGGER.info(" Got Action: " + action + "on channel " + channel
					+ " with: ");

			@SuppressWarnings("rawtypes")
			Iterator itr = json.keys();
			while (itr.hasNext()) {
				key = itr.next().toString();
				LOGGER.info(" ... " + key + " => " + json.getString(key));
				if (key.equals("title")) {
					pageTitle = (TextView) findViewById(R.id.notificationTitle);
					pageTitle.setText(json.getString(key));
					LOGGER.info("Setting Page Title to: " + json.getString(key));
				} else if (key.equals("alert")) {
					pageMessage = (TextView) findViewById(R.id.notificationMessage);
					pageMessage.setText(json.getString(key));
					LOGGER.info("Setting Page Message to: "
							+ json.getString(key));
				} else if (key.equals("groupLeaderId")) {
					groupLeaderId = json.getString(key);
					LOGGER.info("GroupLeaderID: " + groupLeaderId);
				} else if (key.equals("groupId")) {
					groupId = json.getString(key);
					LOGGER.info("GroupID: " + groupId);
				} else if (key.equals("participantId")) {
					participantId = json.getString(key);
					LOGGER.info("ParticipantID: " + participantId);
				}
			}
		} catch (JSONException e) {
			LOGGER.info("ERROR in JSON: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void declineButtonClicked() {
		decline.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				disableAllButtons();
				getParticipantAndUpdateStatusDecline();
			}

			private void getParticipantAndUpdateStatusDecline() {
				ParseQuery<ParseObject> query = ParseQuery
						.getQuery("Participant");
				query.whereEqualTo("groupLeaderId", groupLeaderId);
				query.whereEqualTo("groupId", groupId);
				query.whereEqualTo("participantNumber", myself.get("username"));
				query.findInBackground(new FindCallback<ParseObject>() {
					@Override
					public void done(List<ParseObject> foundParticipants,
							ParseException e) {
						if (e == null) {
							if (foundParticipants.size() > 0) {
								final ParseObject current = foundParticipants.get(0);
								LOGGER.info("SUCCESS:: Found Participant: "
										+ current.getObjectId());
								removeParticipant = current.getObjectId().toString();
								current.put("status", STATUS_DECLINE);
								current.saveInBackground(new SaveCallback() {
									@Override
									public void done(ParseException e) {
										if (e == null) {
											LOGGER.info("SUCCESS:: Successfully Updated Participant Status -> DECLINED");
											Toast.makeText(
													getApplicationContext(),
													"You Have Declined!",
													Toast.LENGTH_SHORT).show();
											removeFromParticipantsArrayInDb();
										} else {
											LOGGER.info("ERROR:: Unable to Save Updated Participant (Decline)");
											e.printStackTrace();
										}
									}

									private void removeFromParticipantsArrayInDb() {
										LOGGER.info("Removing from Participant Array: "
												+ removeParticipant);
										ParseQuery<ParseObject> getGroupQuery = ParseQuery
												.getQuery("Group");
										getGroupQuery.whereEqualTo("objectId",
												groupId);
										getGroupQuery
												.findInBackground(new FindCallback<ParseObject>() {
													@Override
													public void done(
															List<ParseObject> foundGroups,
															ParseException e) {
														if (e == null) {
															if (foundGroups
																	.size() > 0) {
																LOGGER.info("SUCCESS:: Found Group with id: "
																		+ groupId);
																ParseObject currentGroup = foundGroups
																		.get(0);
																@SuppressWarnings("unchecked")
																ArrayList<String> currentParticipants = (ArrayList<String>) currentGroup
																		.get("groupParticipants");
																LOGGER.info("Got Participants: ");
																for(String participant: currentParticipants){
																	LOGGER.info(""+participant+",");
																}
																LOGGER.info("from DB.");
																
																currentParticipants
																		.remove(removeParticipant);
																
																LOGGER.info("Sending Participants: ");
																for(String participant: currentParticipants){
																	LOGGER.info(""+participant+",");
																}
																LOGGER.info("to DB.");
																
																currentGroup
																		.put("groupParticipants",
																				currentParticipants);
																try {
																	currentGroup
																			.save();
																	LOGGER.info("SUCCESS: Successfully removed participant from array");
																	finish();
																} catch (ParseException e1) {
																	LOGGER.info("ERROR::");
																	e1.printStackTrace();
																}
															} else {
																LOGGER.info("FAILURE:: Unable to Find Group with id: "
																		+ groupId);
															}
														} else {
															LOGGER.info("ERROR:: ");
															e.printStackTrace();
														}
													}
												});
									}
								});
							}
						} else {
							LOGGER.info("ERROR:: Unable to Find Participants...");
							e.printStackTrace();
						}
					}
				});
			}
		});
	}

	private void acceptButtonClicked() {
		accept.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				getParticipantAndUpdateStatusAccept();
				updateUserGroupMemberStatus(true);
			}

			private void getParticipantAndUpdateStatusAccept() {
				ParseQuery<ParseObject> query = ParseQuery
						.getQuery("Participant");
				query.whereEqualTo("groupLeaderId", groupLeaderId);
				query.whereEqualTo("groupId", groupId);
				query.whereEqualTo("participantNumber", myself.get("username"));
				query.findInBackground(new FindCallback<ParseObject>() {
					@Override
					public void done(List<ParseObject> foundParticipants,
							ParseException e) {
						if (e == null) {
							if (foundParticipants.size() > 0) {
								ParseObject current = foundParticipants.get(0);
								LOGGER.info("SUCCESS:: Found Participant: "
										+ current.getObjectId());
								current.put("status", STATUS_ACCEPT);
								current.saveInBackground(new SaveCallback() {
									@Override
									public void done(ParseException e) {
										if (e == null) {
											LOGGER.info("SUCCESS:: Successfully Updated Participant Status -> ACCEPTED");
											Toast.makeText(
													getApplicationContext(),
													"You Have Accepted!",
													Toast.LENGTH_SHORT).show();
											finish();
										} else {
											LOGGER.info("ERROR:: Unable to Save Updated Participant (Accept)");
											e.printStackTrace();
										}
									}
								});
							}
						} else {
							LOGGER.info("ERROR:: Unable to Find Participants...");
							e.printStackTrace();
						}
					}
				});
			}
		});

	}

	protected void updateUserGroupMemberStatus(boolean groupMemberStatus) {
		ParseUser currentUser = ParseUser.getCurrentUser();
		currentUser.put("groupMember", groupMemberStatus);
		currentUser.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					LOGGER.info("SUCCESS:: Updated User groupMember status.");
				} else {
					LOGGER.info("FAILURE:: Encountered Error: ");
					e.printStackTrace();
				}
			}
		});
	}

	private void enableAllButtons() {
		accept.setClickable(true);
		accept.setEnabled(true);
		decline.setClickable(true);
		decline.setEnabled(true);
	}

	private void disableAllButtons() {
		accept.setClickable(false);
		accept.setEnabled(false);
		decline.setClickable(false);
		decline.setEnabled(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.accept_decline_invitation, menu);
		return true;
	}

}
