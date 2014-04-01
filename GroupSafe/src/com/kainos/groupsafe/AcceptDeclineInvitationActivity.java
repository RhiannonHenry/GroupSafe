package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.List;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity is displayed to the user if they receive a group invitation
 * notification (i.e. one of their contacts has invited them to join a group.).
 * Form this activity the user will be able to either accept or decline the
 * group notification.
 * 
 * @see activity_accept_decline_invitation.xml
 * 
 * @author Rhiannon Henry
 * 
 */
public class AcceptDeclineInvitationActivity extends Activity {

	private static final String TAG = "ACCEPT/DECLINE_INVITATION";
	private static final String STATUS_ACCEPT = "ACCEPTED";
	private static final String STATUS_DECLINE = "DECLINED";
	private String channel, userId, groupLeaderId, groupId, participantId,
			removeParticipant;
	private TextView pageTitle, pageMessage;
	private Button accept, decline;
	private ParseUser myself;
	private boolean internetPresent = false;
	private ConnectionDetector connectionDetector;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());
		connectionDetector = new ConnectionDetector(getApplicationContext());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_accept_decline_invitation);

		accept = (Button) findViewById(R.id.acceptButton);
		decline = (Button) findViewById(R.id.declineButton);
		myself = ParseUser.getCurrentUser();
		internetPresent = connectionDetector.isConnectedToInternet();

		if (internetPresent) {
			Log.d(TAG, "Populating Page...");
			populatePage();
		} else {
			Utilities.showNoInternetConnectionDialog(this);
		}
		enableAllButtons();
		acceptButtonClicked();
		declineButtonClicked();
	}

	@Override
	public void onResume() {
		internetPresent = connectionDetector.isConnectedToInternet();
		enableAllButtons();
		if (internetPresent) {
			populatePage();
		}
	}

	/**
	 * This method is used to update the text on the Accept/Decline Invitation
	 * screen from information passed through via the Intent. The information
	 * pass through includes: channel, groupLeaderId, groupId and participantId
	 */
	private void populatePage() {
		Intent intent = getIntent();
		channel = intent.getStringExtra("com.parse.Channel");
		userId = channel.substring(4);
		groupLeaderId = intent.getStringExtra("groupLeaderId");
		groupId = intent.getStringExtra("groupId");
		participantId = intent.getStringExtra("participantId");
		pageTitle = (TextView) findViewById(R.id.notificationTitle);
		pageTitle.setText(intent.getStringExtra("title"));
		pageMessage = (TextView) findViewById(R.id.notificationMessage);
		pageMessage.setText(intent.getStringExtra("alert"));

		Log.i(TAG, "Starting with: channel = " + channel + " userId = "
				+ userId + " groupLeaderId = " + groupLeaderId + " groupId = "
				+ groupId + " participantId = " + participantId);
	}

	/**
	 * This method will be called if the user clicks 'Decline' on the
	 * Accept/Decline Invitation screen @see
	 * activity_accept_decline_invitation.xml
	 */
	private void declineButtonClicked() {
		if (internetPresent) {
			Log.i(TAG, "Internet Detected. Clicking 'Decline'");
			decline.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					disableAllButtons();
					getParticipantAndUpdateStatusDecline();
				}

				/**
				 * This method fetches the participant associated with this user
				 * and updates the status of their response to 'DECLINED'
				 */
				private void getParticipantAndUpdateStatusDecline() {
					ParseQuery<ParseObject> query = ParseQuery
							.getQuery("Participant");
					query.whereEqualTo("groupLeaderId", groupLeaderId);
					query.whereEqualTo("groupId", groupId);
					query.whereEqualTo("participantNumber",
							myself.get("username"));
					query.findInBackground(new FindCallback<ParseObject>() {
						@Override
						public void done(List<ParseObject> foundParticipants,
								ParseException e) {
							if (e == null) {
								if (foundParticipants.size() > 0) {
									final ParseObject current = foundParticipants
											.get(0);
									Log.i(TAG, "SUCCESS:: Found Participant: "
											+ current.getObjectId());
									removeParticipant = current.getObjectId()
											.toString();
									current.put("status", STATUS_DECLINE);
									current.saveInBackground(new SaveCallback() {
										@Override
										public void done(ParseException e) {
											if (e == null) {
												Log.i(TAG,
														"SUCCESS:: Successfully Updated Participant Status -> DECLINED");
												Toast.makeText(
														getApplicationContext(),
														"You Have Declined!",
														Toast.LENGTH_SHORT)
														.show();
												removeFromParticipantsArrayInDb();
											} else {
												Log.e(TAG,
														"ERROR:: Unable to Save Updated Participant (Decline)");
												e.printStackTrace();
											}
										}

										/**
										 * This method is used to remove the
										 * user from the list of participants
										 * that are in the group. This will
										 * un-subscribe them from receiving
										 * future notifications from the group.
										 */
										private void removeFromParticipantsArrayInDb() {
											Log.d(TAG,
													"Removing from Participant Array: "
															+ removeParticipant);
											ParseQuery<ParseObject> getGroupQuery = ParseQuery
													.getQuery("Group");
											getGroupQuery.whereEqualTo(
													"objectId", groupId);
											getGroupQuery
													.findInBackground(new FindCallback<ParseObject>() {
														@Override
														public void done(
																List<ParseObject> foundGroups,
																ParseException e) {
															if (e == null) {
																if (foundGroups
																		.size() > 0) {
																	Log.i(TAG,
																			"SUCCESS:: Found Group with id: "
																					+ groupId);
																	ParseObject currentGroup = foundGroups
																			.get(0);
																	@SuppressWarnings("unchecked")
																	ArrayList<String> currentParticipants = (ArrayList<String>) currentGroup
																			.get("groupParticipants");
																	Log.i(TAG,
																			"Got Participants: ");
																	for (String participant : currentParticipants) {
																		Log.i(TAG,
																				""
																						+ participant
																						+ ",");
																	}
																	Log.i(TAG,
																			"from DB.");

																	currentParticipants
																			.remove(removeParticipant);

																	Log.i(TAG,
																			"Sending Participants: ");
																	for (String participant : currentParticipants) {
																		Log.i(TAG,
																				""
																						+ participant
																						+ ",");
																	}
																	Log.i(TAG,
																			"to DB.");

																	currentGroup
																			.put("groupParticipants",
																					currentParticipants);
																	try {
																		currentGroup
																				.save();
																		Log.i(TAG,
																				"SUCCESS: Successfully removed participant from array");
																		finish();
																	} catch (ParseException e1) {
																		Log.e(TAG,
																				"ERROR::");
																		e1.printStackTrace();
																	}
																} else {
																	Log.e(TAG,
																			"FAILURE:: Unable to Find Group with id: "
																					+ groupId);
																}
															} else {
																Log.e(TAG,
																		"ERROR:: ");
																e.printStackTrace();
															}
														}
													});
										}
									});
								}
							} else {
								Log.e(TAG,
										"ERROR:: Unable to Find Participants...");
								e.printStackTrace();
							}
						}
					});
				}
			});
		} else {
			Log.e(TAG,
					"Unable to click 'Decline' as there is no internet connection detected.");
			enableAllButtons();
			Utilities.showNoInternetConnectionDialog(this);
		}
	}

	/**
	 * This method will be used if the user clicks 'Accept' on the
	 * Accept/Decline Invitation screen @see
	 * activity_accept_decline_invitation.xml
	 */
	private void acceptButtonClicked() {
		if (internetPresent) {
			Log.i(TAG, "Internet found: Clicking 'Accept'");
			accept.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					disableAllButtons();
					getParticipantAndUpdateStatusAccept();
					updateUserGroupMemberStatus(true);
				}

				/**
				 * This method fetches the participant associated with this user
				 * and updates the status of their response from 'PENDING' to
				 * 'ACCEPTED'
				 */
				private void getParticipantAndUpdateStatusAccept() {
					ParseQuery<ParseObject> query = ParseQuery
							.getQuery("Participant");
					query.whereEqualTo("groupLeaderId", groupLeaderId);
					query.whereEqualTo("groupId", groupId);
					query.whereEqualTo("participantNumber",
							myself.get("username"));
					query.findInBackground(new FindCallback<ParseObject>() {
						@Override
						public void done(List<ParseObject> foundParticipants,
								ParseException e) {
							if (e == null) {
								if (foundParticipants.size() > 0) {
									ParseObject current = foundParticipants
											.get(0);
									Log.i(TAG, "SUCCESS:: Found Participant: "
											+ current.getObjectId());
									current.put("status", STATUS_ACCEPT);
									current.saveInBackground(new SaveCallback() {
										@Override
										public void done(ParseException e) {
											if (e == null) {
												Log.i(TAG,
														"SUCCESS:: Successfully Updated Participant Status -> ACCEPTED");
												Toast.makeText(
														getApplicationContext(),
														"You Have Accepted!",
														Toast.LENGTH_SHORT)
														.show();
												finish();
											} else {
												Log.e(TAG,
														"ERROR:: Unable to Save Updated Participant (Accept)");
												e.printStackTrace();
											}
										}
									});
								}
							} else {
								Log.e(TAG,
										"ERROR:: Unable to Find Participants...");
								e.printStackTrace();
							}
						}
					});
				}
			});

		} else {
			Log.e(TAG,
					"Unable to click 'Accept' as no internet connection has been found");
			enableAllButtons();
			Utilities.showNoInternetConnectionDialog(this);
		}
	}

	/**
	 * This method is used to update the 'groupMember' attribute of the User
	 * from FALSE to TRUE, to indicate that they are currently participating in
	 * a group.
	 * 
	 * @param groupMemberStatus
	 *            Boolean value of TRUE or FALSE used to indicate whether or not
	 *            a user is currently a participating member of a group.
	 */
	protected void updateUserGroupMemberStatus(boolean groupMemberStatus) {
		if (internetPresent) {
			ParseUser currentUser = ParseUser.getCurrentUser();
			currentUser.put("groupMember", groupMemberStatus);
			currentUser.saveInBackground(new SaveCallback() {
				@Override
				public void done(ParseException e) {
					if (e == null) {
						Log.i(TAG, "SUCCESS:: Updated User groupMember status.");
					} else {
						Log.e(TAG, "FAILURE:: Encountered Error: ");
						e.printStackTrace();
					}
				}
			});
		} else {
			Log.e(TAG,
					"Unable to update value of user as there is no internet connection detected.");
		}
	}

	/**
	 * Enables all buttons that are on the AcceptDeclineInvitationActivity.java
	 * view.
	 * 
	 * @see activity_accept_decline_invitation.xml
	 */
	private void enableAllButtons() {
		accept.setClickable(true);
		accept.setEnabled(true);
		decline.setClickable(true);
		decline.setEnabled(true);
	}

	/**
	 * Disables all buttons that are on the AcceptDeclineInvitationActivity.java
	 * view.
	 * 
	 * @see activity_accept_decline_invitation.xml
	 */
	private void disableAllButtons() {
		accept.setClickable(false);
		accept.setEnabled(false);
		decline.setClickable(false);
		decline.setEnabled(false);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.accept_decline_invitation, menu);
		return true;
	}
}
