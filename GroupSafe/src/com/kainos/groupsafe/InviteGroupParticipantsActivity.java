package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
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
import android.widget.ListView;

/**
 * This Activity is the last step in the 'Create Group' process, where a group
 * invitation is sent to the selected participants. From this screen, the user
 * will be able to Terminate a group, Invite participants, go back a step to
 * 'Group Details' or Start a group.
 * 
 * @author Rhiannon Henry
 * 
 */
public class InviteGroupParticipantsActivity extends Activity {

	private static InviteGroupParticipantsActivity _instance = null;
	private static final String TAG = "Invite_Group_Participants_Activity";
	private InviteesAdapter adapter = null;
	private ListView listView = null;
	private ArrayList<InviteeContact> invitedContacts = new ArrayList<InviteeContact>();
	private ArrayList<String> groupParticipants_participantId = new ArrayList<String>();
	private ArrayList<String> participants;
	private int radius;
	private boolean participantsInvited = false;
	private String groupName, groupOrganization, participantUserObjectID,
			groupId, groupLeaderId, groupLeaderDisplayName, participantId;
	private Button invite, prev, next, cancel;
	private View header, footer;
	private Timer autoUpdate;

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
		setContentView(R.layout.activity_invite_group_participants);
		_instance = this;
		initializeVariables();
		listView = (ListView) findViewById(R.id.inviteeList);
		footer = getLayoutInflater().inflate(
				R.layout.footer_invite_participants, null);
		header = getLayoutInflater().inflate(
				R.layout.header_invite_participants, null);
		listView.addFooterView(footer);
		listView.addHeaderView(header);
		adapter = new InviteesAdapter(this, R.layout.invitee_row,
				invitedContacts);
		listView.setAdapter(adapter);

		Log.i(TAG, "Got Participants: ");
		for (int i = 0; i < participants.size(); i++) {
			Log.i(TAG, "" + participants.get(i));
			getParticipantInformation(participants.get(i));
		}
		Log.i(TAG, "Got Radius size: " + radius);
		Log.i(TAG, "Got Group Name: " + groupName);
		Log.i(TAG, "Got Group Organization: " + groupOrganization);

		invite = (Button) header
				.findViewById(R.id.inviteGroupParticipantsButton);
		next = (Button) footer.findViewById(R.id.inviteParticipantsNextButton);
		prev = (Button) footer.findViewById(R.id.inviteParticipantsPrevButton);
		cancel = (Button) footer
				.findViewById(R.id.inviteParticipantsCancelButton);

		enableAllButtons();
		cancel.setClickable(false);
		cancel.setEnabled(false);
		next.setClickable(false);
		next.setEnabled(false);

		inviteButtonClicked();
		nextButtonClicked();
		previousButtonClicked();
		cancelButtonClicked();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		Log.d(TAG, "UPDATING...");
		super.onResume();
		GroupSafeApplication.activityPaused();
		autoUpdate = new Timer();
		autoUpdate.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						if (participantsInvited) {
							for (int i = 0; i < invitedContacts.size(); i++) {
								Log.i(TAG, "Updating Participant: "
										+ invitedContacts.get(i)
												.getInviteeContactName());
								updateParticipantStatus(invitedContacts.get(i),
										i);
							}
						} else {
							Log.i(TAG, "NOTHING TO UPDATE");
						}
					}
				});
			}
		}, 0, 30000); // updates every 30 seconds
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();
		autoUpdate.cancel();
		GroupSafeApplication.activityPaused();

	}

	/**
	 * This method is called if the user clicks on the 'Cancel' button on the
	 * screen @see activity_invite_group_participants.xml. Clicking this button
	 * will terminate the group, sending out the termination notification to all
	 * participants. This option is only enabled once the participants have been
	 * invited (i.e the user has clicked the 'Invite Participants' button)
	 */
	private void cancelButtonClicked() {
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				setGroupLeaderStatus(false);
				notifyGroupMembers();
			}

			/**
			 * This method is used to find the Group in the Group entity in the
			 * database to fetch the list of 'groupParticipants'. For each of
			 * these Participants in the list, we must then find the associated
			 * Participant object in the Participant table.
			 */
			private void notifyGroupMembers() {
				ParseQuery<ParseObject> getGroupQuery = ParseQuery
						.getQuery("Group");
				getGroupQuery.whereEqualTo("objectId", groupId);
				try {
					List<ParseObject> foundGroups = getGroupQuery.find();
					Log.i(TAG, "SUCCESS:: Found Group");
					if (foundGroups.size() > 0) {
						ParseObject group = foundGroups.get(0);
						@SuppressWarnings("unchecked")
						ArrayList<String> currentGroupParticipants = (ArrayList<String>) group
								.get("groupParticipants");
						for (int i = 0; i < currentGroupParticipants.size(); i++) {
							findParticipantInParticipantTable(currentGroupParticipants
									.get(i));
						}
					} else {
						Log.e(TAG, "FAILURE:: Cannot access group");
					}
				} catch (ParseException e) {
					Log.e(TAG, "ERROR:: Unable to Find Group...");
					e.printStackTrace();
				}

				deleteGroup();
			}

			/**
			 * This method is used to find and remove the current group from the
			 * Group Entity in the Database.
			 */
			private void deleteGroup() {
				ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
				query.whereEqualTo("objectId", groupId);
				query.findInBackground(new FindCallback<ParseObject>() {
					@Override
					public void done(List<ParseObject> foundGroups,
							ParseException e) {
						if (e == null) {
							if (foundGroups.size() > 0) {
								Log.i(TAG, "SUCCESS:: Found Group!");
								ParseObject group = foundGroups.get(0);
								group.deleteEventually(new DeleteCallback() {
									@Override
									public void done(ParseException e) {
										if (e == null) {
											Log.i(TAG,
													"SUCCESS:: Deleted Group From DB");
										} else {
											Log.e(TAG,
													"ERROR:: Failed to Delete Group");
											e.printStackTrace();
										}
									}
								});
							} else {
								Log.e(TAG, "FAILURE:: Failed to Find Group");
							}
						} else {
							Log.e(TAG, "ERROR::");
							e.printStackTrace();
						}
					}
				});
			}

			/**
			 * This method is used to find the Participant object in the
			 * Participant Entity in the Database using the passed in unique
			 * participant identifier.
			 * 
			 * @param participant
			 *            a String value that represents the unique participant
			 *            identifier for a specific participant in the
			 *            Participant Entity in the Database.
			 */
			private void findParticipantInParticipantTable(String participant) {
				ParseQuery<ParseObject> getParticipant = ParseQuery
						.getQuery("Participant");
				getParticipant.whereEqualTo("objectId", participant);
				getParticipant
						.findInBackground(new FindCallback<ParseObject>() {
							@Override
							public void done(
									List<ParseObject> foundParticipants,
									ParseException e) {
								if (e == null) {
									if (foundParticipants.size() > 0) {
										Log.i(TAG,
												"SUCCESS:: Found Participant");
										ParseObject current = foundParticipants
												.get(0);
										String number = current.get(
												"participantNumber").toString();
										Log.i(TAG, "Got username: " + number
												+ " for participant");
										findUserInDb(number);
									} else {
										Log.e(TAG,
												"FAILURE:: Failed to get Participant");
									}
								} else {
									Log.e(TAG, "ERROR::");
									e.printStackTrace();
								}
							}

							/**
							 * This method is used to find the User object in
							 * the User entity in the database using the passed
							 * in unique username (phone number) for the user.
							 * 
							 * @param number
							 *            a String value representing the users
							 *            phone number (unique username) for a
							 *            specific User in the database
							 */
							private void findUserInDb(String number) {
								ParseQuery<ParseUser> userQuery = ParseUser
										.getQuery();
								userQuery.whereEqualTo("username", number);
								userQuery
										.findInBackground(new FindCallback<ParseUser>() {
											@Override
											public void done(
													List<ParseUser> userList,
													ParseException e) {
												if (e == null) {
													if (userList.size() > 0) {
														ParseUser user = userList
																.get(0);
														Log.i(TAG,
																"SUCCESS:: Found User "
																		+ user.get("displayName"));
														String groupLeaderDisplayName = ParseUser
																.getCurrentUser()
																.get("displayName")
																.toString();
														JSONObject terminationData = null;
														try {
															terminationData = new JSONObject(
																	"{"
																			+ "\"alert\":\""
																			+ groupLeaderDisplayName
																			+ " has Terminated the group.\", "
																			+ "\"action\":\"com.kainos.groupsafe.GroupTerminationNotificationActivity\", "
																			+ "\"title\": \"Group Termination!\"}");
															sendNotification(
																	user.getObjectId()
																			.toString(),
																	terminationData);
														} catch (JSONException e1) {
															Log.e(TAG,
																	"ERROR: Error Creating JSON for Temination Notification.");
															e1.printStackTrace();
														}
													} else {
														Log.e(TAG,
																"FAILURE:: Unable to find User");
													}
												} else {
													Log.e(TAG, "ERROR::");
													e.printStackTrace();
												}
											}

											/**
											 * This method is used to send the
											 * termination notification to the
											 * participant (with associated user
											 * with unique user identifier).
											 * 
											 * @param userObjectId
											 *            a String value
											 *            representing the
											 *            unique user identifier
											 *            for the participant to
											 *            whom the notification
											 *            should be sent.
											 * @param terminationData
											 *            a JSONObject comprised
											 *            to form the body of
											 *            the notification to be
											 *            sent to the
											 *            participant
											 */
											private void sendNotification(
													String userObjectId,
													JSONObject terminationData) {
												String notificationChannel = "user_"
														+ userObjectId;
												Log.i(TAG, "#004: Channel = "
														+ notificationChannel);
												ParsePush push = new ParsePush();
												push.setData(terminationData);
												push.setChannel(notificationChannel);
												push.sendInBackground();

												Intent intent = new Intent(
														_instance,
														HomeActivity.class);
												startActivity(intent);
												finish();
											}
										});
							}

						});
			}
		});
	}

	/**
	 * This method is used to update the 'status' of an invited user.
	 * 
	 * @param currentParticipant
	 *            the @see {@link InviteeContact} object representing an invited
	 *            contact.
	 * @param position
	 *            the position in the list array for a specific invited contact.
	 */
	private void updateParticipantStatus(InviteeContact currentParticipant,
			int position) {
		String participantId = currentParticipant.getObjectId();
		String displayedStatus = currentParticipant.getStatus().toString();
		String participantName = currentParticipant.getInviteeContactName();
		String participantNumber = currentParticipant.getInviteeContactNumber();

		Log.i(TAG, "Trying to Find Participant in DB...");
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Participant");
		query.whereEqualTo("participantId", participantId);
		query.whereEqualTo("groupId", groupId);
		query.whereEqualTo("groupLeaderId", groupLeaderId);
		try {
			List<ParseObject> foundParticipant = query.find();
			if (foundParticipant.size() > 0) {
				ParseObject current = foundParticipant.get(0);
				Log.i(TAG, "SUCCESS:: Found Participant "
						+ current.getObjectId().toString());
				String currentStatus = current.get("status").toString();
				if (!displayedStatus.equals(currentStatus)) {
					Log.i(TAG, "STATUS DIFFERENCE!");
					InviteeContact updatedContact = new InviteeContact(
							participantName, participantNumber, participantId,
							Status.valueOf(currentStatus));
					invitedContacts.set(position, updatedContact);
					adapter.inviteeList.set(position, updatedContact);
					adapter.notifyDataSetChanged();
					listView.refreshDrawableState();
				} else {
					Log.i(TAG, "NO CHANGES TO PARTICIPANT STATE!");
				}
			} else {
				Log.e(TAG, "FAILURE:: Could Not Find Participant.");
			}
		} catch (ParseException e) {
			Log.e(TAG, "ERROR:: Unable to find Participant");
			e.printStackTrace();
		}
	}

	/**
	 * This method is used to take the user back to the 'Group Details' page @see
	 * {@link SetGroupGeoFenceActivity}. This button should be disabled if the
	 * user has clicked 'Invite Participants'
	 */
	private void previousButtonClicked() {
		prev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				Intent intent = new Intent(_instance,
						SetGroupGeoFenceActivity.class);
				intent.putStringArrayListExtra("chosenParticipants",
						participants);
				startActivity(intent);
			}
		});
	}

	/**
	 * This method is used to take the user to the Group Map View @see
	 * {@link GroupGeoFenceMapActivity}. Before this can be viewed, all the
	 * participants must first be notified that the group has been started.
	 */
	private void nextButtonClicked() {
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				sendStartPushNotificationToParticipants();
			}
		});
	}

	/**
	 * This method gets a new list of participants (as this list is changed as
	 * participants accept/decline the invitation)
	 */
	protected void sendStartPushNotificationToParticipants() {
		ParseQuery<ParseObject> getGroupQuery = ParseQuery.getQuery("Group");
		getGroupQuery.whereEqualTo("objectId", groupId);
		try {
			List<ParseObject> foundGroups = getGroupQuery.find();
			Log.i(TAG, "SUCCESS:: Found Group");
			if (foundGroups.size() > 0) {
				ParseObject group = foundGroups.get(0);
				@SuppressWarnings("unchecked")
				ArrayList<String> currentGroupParticipants = (ArrayList<String>) group
						.get("groupParticipants");
				for (int i = 0; i < currentGroupParticipants.size(); i++) {
					findParticipantInParticipantTable(currentGroupParticipants
							.get(i));
				}
			} else {
				Log.e(TAG, "FAILURE:: Cannot access group");
			}
		} catch (ParseException e) {
			Log.e(TAG, "ERROR:: Unable to Find Group...");
			e.printStackTrace();
		}
	}

	/**
	 * This method is used to find the associtated Participant object for each
	 * unique participant identifier stored in the 'groupParticipants' list in
	 * the Group Entity
	 * 
	 * @param participant
	 *            a String value representing the unique participant identifier
	 *            for a specific object (participant) in the Participant Entity
	 */
	private void findParticipantInParticipantTable(String participant) {
		ParseQuery<ParseObject> getParticipant = ParseQuery
				.getQuery("Participant");
		getParticipant.whereEqualTo("objectId", participant);
		getParticipant.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> foundParticipants,
					ParseException e) {
				if (e == null) {
					if (foundParticipants.size() > 0) {
						Log.i(TAG, "SUCCESS:: Found Participant");
						ParseObject current = foundParticipants.get(0);
						String number = current.get("participantNumber")
								.toString();
						Log.i(TAG, "Got username: " + number
								+ " for participant");
						findUserInDb(number);
					} else {
						Log.e(TAG, "FAILURE:: Failed to get Participant");
					}
				} else {
					Log.e(TAG, "ERROR::");
					e.printStackTrace();
				}
			}

			/**
			 * This method is used to find the User object in the User entity in
			 * the database using the passed in unique username (phone number)
			 * for the user.
			 * 
			 * @param number
			 *            a String value representing the users phone number
			 *            (unique username) for a specific User in the database
			 */
			private void findUserInDb(String number) {
				ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
				userQuery.whereEqualTo("username", number);
				userQuery.findInBackground(new FindCallback<ParseUser>() {
					@Override
					public void done(List<ParseUser> userList, ParseException e) {
						if (e == null) {
							if (userList.size() > 0) {
								ParseUser user = userList.get(0);
								Log.i(TAG,
										"SUCCESS:: Found User "
												+ user.get("displayName"));
								String groupLeaderDisplayName = ParseUser
										.getCurrentUser().get("displayName")
										.toString();
								JSONObject startData = null;
								try {
									startData = new JSONObject(
											"{"
													+ "\"alert\":\""
													+ groupLeaderDisplayName
													+ " has started the Group!\", "
													+ "\"title\": \"Group Started!\", "
													+ "\"groupLeaderId\": \""
													+ groupLeaderId
													+ "\", "
													+ "\"groupId\": \""
													+ groupId
													+ "\", "
													+ "\"radius\": \""
													+ Integer.toString(radius)
													+ "\", "
													+ "\"action\": \"com.kainos.groupsafe.GroupGeoFenceMapActivity\"}");
									sendNotification(user.getObjectId()
											.toString(), startData);
								} catch (JSONException e1) {
									Log.e(TAG,
											"ERROR: Error Creating JSON for Temination Notification.");
									e1.printStackTrace();
								}
							} else {
								Log.e(TAG, "FAILURE:: Unable to find User");
							}
						} else {
							Log.e(TAG, "ERROR::");
							e.printStackTrace();
						}
					}

					/**
					 * This method is used to send the start gorup notification
					 * to the participant (with associated user with unique user
					 * identifier).
					 * 
					 * @param userObjectId
					 *            a String value representing the unique user
					 *            identifier for the participant to whom the
					 *            notification should be sent.
					 * @param startData
					 *            a JSONObject comprised to form the body of the
					 *            notification to be sent to the participant
					 */
					private void sendNotification(String userObjectId,
							JSONObject startData) {
						String notificationChannel = "user_" + userObjectId;
						Log.i(TAG, "#004: Channel = " + notificationChannel);
						ParsePush push = new ParsePush();
						push.setData(startData);
						push.setChannel(notificationChannel);
						push.sendInBackground();

						Intent intent = new Intent(_instance,
								GroupGeoFenceMapActivity.class);
						intent.putExtra("groupId", groupId);
						intent.putExtra("groupLeaderId", groupLeaderId);
						intent.putExtra("radius", radius);
						startActivity(intent);
					}
				});
			}
		});
	}

	/**
	 * This method is used when the user clicks on the 'Invite Participants'
	 * button on the screen. This will send the group invitation notification to
	 * ALL prospective group participants.
	 */
	private void inviteButtonClicked() {
		invite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				createGroupInDB();
				setGroupLeaderStatus(true);
			}

			/**
			 * This method is used to create a group object in the Group Entity
			 * in the Database, with all the associated data for the current
			 * group (name, leader, radius, participants)
			 */
			private void createGroupInDB() {
				ArrayList<String> groupParticipants = new ArrayList<String>();
				ParseObject newGroup = new ParseObject("Group");
				newGroup.put("groupName", groupName);
				newGroup.put("groupLeaderId", groupLeaderId);
				newGroup.put("groupGeoFenceRadius", radius);
				newGroup.put("groupParticipants", groupParticipants);
				newGroup.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {
						if (e == null) {
							Log.i(TAG, "SUCCESS:: Saved Group");
							getGroupID();
						} else {
							Log.e(TAG, "ERROR:: ");
							e.printStackTrace();
						}
					}

					/**
					 * This method is used to get the unique group identifier
					 * for the newly created group in the Group Entity in the
					 * Database.
					 */
					private void getGroupID() {
						Log.i(TAG, "GroupLeader: " + groupLeaderId);
						Log.i(TAG, "GroupName: " + groupName);
						ParseQuery<ParseObject> query = ParseQuery
								.getQuery("Group");
						query.whereEqualTo("groupLeaderId", groupLeaderId);
						query.whereEqualTo("groupName", groupName);
						try {
							List<ParseObject> foundGroups = query.find();
							if (foundGroups.size() > 0) {
								ParseObject current = foundGroups.get(0);
								Log.i(TAG,
										"SUCCESS:: Group Found!"
												+ current.get("groupName"));
								groupId = current.getObjectId().toString();
								Log.i(TAG, "GroupID: " + groupId);
								createParticipantsInDB();
							} else {
								Log.e(TAG,
										"FAILURE:: Could Not Find Any Groups...");
							}
						} catch (ParseException e) {
							Log.e(TAG, "ERROR Finding Group: ");
							e.printStackTrace();
						}
					}
				});
			}

			/**
			 * This method is used to create a unique Participant object in the
			 * Participant Entity in the Database for each invited group
			 * participant.
			 */
			private void createParticipantsInDB() {
				for (int i = 0; i < invitedContacts.size(); i++) {
					InviteeContact currentParticipant = invitedContacts.get(i);
					String participantName = currentParticipant
							.getInviteeContactName();
					String participantNumber = currentParticipant
							.getInviteeContactNumber();
					String participantStatus = currentParticipant.getStatus()
							.toString();
					participantId = currentParticipant.getObjectId();

					Log.i(TAG, "Preparing to add participant: ");
					Log.i(TAG, "Name: " + participantName);
					Log.i(TAG, "Number: " + participantNumber);
					Log.i(TAG, "ID: " + participantId);
					Log.i(TAG, "Status: " + participantStatus);
					Log.i(TAG, "Group Leader ID: " + groupLeaderId);
					Log.i(TAG, "Group ID: " + groupId);
					Log.i(TAG, "... To Participant Table in DB.");

					ParseObject newParticipant = new ParseObject("Participant");
					newParticipant.put("groupId", groupId);
					newParticipant.put("groupLeaderId", groupLeaderId);
					newParticipant.put("participantId", participantId);
					newParticipant.put("participantName", participantName);
					newParticipant.put("participantNumber", participantNumber);
					newParticipant.put("status", participantStatus);
					newParticipant.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException e) {
							if (e == null) {
								Log.i(TAG,
										"SUCCESS:: Created Participant Successfully");
							} else {
								Log.e(TAG, "ERROR:: ");
								e.printStackTrace();
							}
						}
					});
					if (i == invitedContacts.size() - 1) {
						Log.i(TAG, "Finished Creating the last Participant...");
						Log.i(TAG,
								"Fetching all the Participant 'objectId' fields...");
						populateGroupParticipantsArray();
					}
				}
				sendPushNotifications();
				participantsInvited = true;
			}

			/**
			 * This method is used to fetch the unique participant identifier
			 * for each participant in the group and add it to an array list
			 */
			private void populateGroupParticipantsArray() {
				ParseQuery<ParseObject> query = ParseQuery
						.getQuery("Participant");
				query.whereEqualTo("groupLeaderId", groupLeaderId);
				query.whereEqualTo("groupId", groupId);
				query.findInBackground(new FindCallback<ParseObject>() {
					@Override
					public void done(List<ParseObject> participantsInGroup,
							ParseException e) {
						if (e == null) {
							if (participantsInGroup.size() > 0) {
								Log.i(TAG,
										"SUCCESS:: Found Participants for Group!");
								for (int i = 0; i < participantsInGroup.size(); i++) {
									String currentObjectId = participantsInGroup
											.get(i).getObjectId().toString();
									Log.i(TAG, i + ": " + currentObjectId);
									groupParticipants_participantId
											.add(currentObjectId);
									if (i == participantsInGroup.size() - 1) {
										Log.i(TAG,
												"That's the last Participant in the group!");
										addParticipantsArrayToGroupTableDB();
									}
								}
							} else {
								Log.e(TAG,
										"FAILURE:: Unable to find Participants for Group!");
							}
						} else {
							Log.e(TAG, "ERROR: ");
							e.printStackTrace();
						}
					}

					/**
					 * This method is used to add the array of unique
					 * participant identifiers to the 'groupParticipants'
					 * attribute for the group object in the Group Entity in the
					 * Database.
					 */
					private void addParticipantsArrayToGroupTableDB() {
						ParseQuery<ParseObject> query = ParseQuery
								.getQuery("Group");
						query.whereEqualTo("groupLeaderId", groupLeaderId);
						query.whereEqualTo("groupName", groupName);
						try {
							List<ParseObject> foundGroups = query.find();
							if (foundGroups.size() > 0) {
								Log.i(TAG, "SUCCESS:: Group Found!");
								ParseObject current = foundGroups.get(0);
								current.put("groupParticipants",
										groupParticipants_participantId);
								current.saveInBackground(new SaveCallback() {
									@Override
									public void done(ParseException e) {
										if (e == null) {
											Log.i(TAG,
													"SUCCESS:: Updated Group with Participants!");
										} else {
											Log.e(TAG,
													"ERROR:: Updating Group with Participants FAILED");
											e.printStackTrace();
										}
									}
								});
							} else {
								Log.e(TAG,
										"FAILURE:: Could Not Find Any Groups...");
							}
						} catch (ParseException e) {
							Log.e(TAG, "ERROR Finding Group: ");
							e.printStackTrace();
						}
					}
				});
			}

			/**
			 * This method is used to send the group invitation notification to
			 * each participant in the 'groupParticipants' array in the
			 * database.
			 */
			private void sendPushNotifications() {
				// Send push notification using CHANNEL
				// "user_[userObjectID]"

				// Send Push Notification to each participant.
				for (int i = 0; i < invitedContacts.size(); i++) {
					JSONObject data = generateJSON(i);
					String currentParticipant = invitedContacts.get(i)
							.getObjectId();
					ParseQuery<ParseUser> getParticpantInformation = ParseUser
							.getQuery();
					getParticpantInformation.whereEqualTo("objectId",
							currentParticipant);
					getParticpantInformation
							.findInBackground(new FindCallback<ParseUser>() {

								@Override
								public void done(List<ParseUser> foundUsers,
										ParseException e) {
									if (e == null) {
										if (foundUsers.size() > 0) {
											ParseUser user = foundUsers.get(0);
											String installationId = user.get(
													"installationId")
													.toString();
											Log.i(TAG, "Installation ID: "
													+ installationId);
										} else {
											Log.e(TAG,
													"ID: 001 Search Returned No Results.");
										}
									} else {
										Log.e(TAG,
												"Error Getting Participant Data.");
										e.printStackTrace();
									}
								}
							});

					Log.i(TAG, "Sending Push Notification to User: "
							+ invitedContacts.get(i).getInviteeContactName());
					getParticipantUserObjectID(invitedContacts.get(i)
							.getInviteeContactNumber(), data);
				}
			}

			/**
			 * This method ise just to sent the push notification to the
			 * appropriate participant.
			 * 
			 * @param number
			 *            a String value of the phone number of the participant
			 * @param data
			 *            a JSONObject object that contains the body of the push
			 *            notification
			 * @param place
			 */
			protected void getParticipantUserObjectID(String number,
					final JSONObject data) {
				ParseQuery<ParseUser> query = ParseUser.getQuery();
				query.whereEqualTo("username", number);
				query.findInBackground(new FindCallback<ParseUser>() {
					@Override
					public void done(List<ParseUser> userList, ParseException e) {
						if (e == null) {
							if (userList.size() > 0) {
								ParseUser current = userList.get(0);
								Log.i(TAG,
										"Successfully retrieved participant user account: "
												+ current.getUsername()
												+ " ObjectID: "
												+ current.getObjectId()
														.toString());
								participantUserObjectID = current.getObjectId()
										.toString();
								sendPushInvitation(data);

							} else {
								Log.e(TAG,
										"An error occurred retrieving the participant user account!");
							}
						} else {
							Log.e(TAG, "ERROR: ");
							e.printStackTrace();
						}
					}
				});
			}

			/**
			 * The actual sending of the group invitation push notification
			 * 
			 * @param data
			 *            a JSONObject value containing the group invitation
			 *            notification body.
			 */
			private void sendPushInvitation(JSONObject data) {
				String channel = "user_" + participantUserObjectID;
				Log.i(TAG, "#003: Channel = " + channel);
				ParsePush push = new ParsePush();
				push.setData(data);
				push.setChannel(channel);
				push.sendInBackground();

				next.setClickable(true);
				next.setEnabled(true);
				cancel.setClickable(true);
				cancel.setEnabled(true);
			}
		});
	}

	/**
	 * This method is used to set the boolean value of 'groupLeader' attribute
	 * in a User Object to TRUE or FALSE depending on the value of the parameter
	 * 
	 * @param groupLeaderStatus
	 *            a boolean value to represent if the user is a group leader
	 *            (TRUE) or not (FALSE)
	 */
	protected void setGroupLeaderStatus(boolean groupLeaderStatus) {
		ParseUser currentUser = ParseUser.getCurrentUser();
		currentUser.put("groupLeader", groupLeaderStatus);
		try {
			currentUser.save();
			Log.i(TAG, "SUCCESS:: Updated 'groupLeader' value for user!");
		} catch (ParseException e) {
			Log.e(TAG, "ERROR:: Updating 'groupLeader' value for user!");
			e.printStackTrace();
		}
	}

	/**
	 * This method is used to initialise the variables that are passed through
	 * from the 'Group Details' page @see {@link SetGroupGeoFenceActivity}.
	 * These variables include: chosenParticipants, radius, name and
	 * organization.
	 */
	private void initializeVariables() {
		Intent intent = getIntent();
		participants = intent.getStringArrayListExtra("chosenParticipants");
		radius = Integer.parseInt(intent.getStringExtra("geoFenceRadius"));
		groupName = intent.getStringExtra("groupName");
		groupOrganization = intent.getStringExtra("groupOrganization");
		groupLeaderId = ParseUser.getCurrentUser().getObjectId().toString();
	}

	/**
	 * This method is used to generate the JSONObject of the Group Invitation
	 * Notification
	 * 
	 * @param position
	 *            the integer value of the location of the participant in the
	 *            array of participants.
	 * @return JSONObject the generated JSONObject notification.
	 */
	protected JSONObject generateJSON(int position) {
		groupLeaderDisplayName = ParseUser.getCurrentUser().get("displayName")
				.toString();
		JSONObject data = null;
		try {
			data = new JSONObject(
					"{"
							+ "\"alert\":\""
							+ groupLeaderDisplayName
							+ " has invited you to join their group. Accept or Decline now.\", "
							+ "\"title\": \"Group Invitation!\", "
							+ "\"groupLeaderId\": \""
							+ groupLeaderId
							+ "\", "
							+ "\"groupId\": \""
							+ groupId
							+ "\", "
							+ "\"participantId\": \""
							+ participants.get(position)
							+ "\", "
							+ "\"action\": \"com.kainos.groupsafe.AcceptDeclineInvitationActivity\"}");
		} catch (JSONException e) {
			Log.e(TAG, "INVALID JSON!!!");
			e.printStackTrace();
		}
		return data;
	}

	/**
	 * This method is used to populate the information about an invited contact
	 * and is added to the adapter @see {@link InviteesAdapter} to be displayed
	 * on a row in the list view.
	 * 
	 * @param participantObjectId
	 *            a String value that represents the unique contact identifier
	 *            for a chosen participant. This will reference a specific
	 *            contact object in the Contact Entity in the database.
	 */
	private void getParticipantInformation(String participantObjectId) {
		Log.i(TAG, "Getting Name for Participant with id: "
				+ participantObjectId);

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Contact");
		query.whereEqualTo("objectId", participantObjectId);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> foundContactInformation,
					ParseException e) {
				if (e == null) {
					if (foundContactInformation.size() != 0) {
						Log.i(TAG, "FOUND CONTACT");
						ParseObject currentData = foundContactInformation
								.get(0);
						String contactName = currentData.get("name").toString();
						String contactNumber = currentData.get("number")
								.toString();
						String objectId = currentData.getObjectId();
						Log.i(TAG, "CONTACT NAME: " + contactName);
						Log.i(TAG, "CONTACT NUMBER: " + contactNumber);
						InviteeContact contact = new InviteeContact(
								contactName, contactNumber, objectId,
								Status.PENDING);
						invitedContacts.add(contact);
						adapter.inviteeList.add(contact);
						adapter.notifyDataSetChanged();
						listView.refreshDrawableState();
					} else {
						Log.e(TAG, "UNABLE TO FIND CONTACT");
					}
				}
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.invite_group_participants, menu);
		return true;
	}

	/**
	 * Enables all buttons on the screen @see header_invite_participants and @see
	 * footer_invite_participants
	 */
	private void enableAllButtons() {
		invite.setClickable(true);
		invite.setEnabled(true);
		next.setClickable(true);
		next.setEnabled(true);
		prev.setClickable(true);
		prev.setEnabled(true);
	}

	/**
	 * Disables all buttons on the screen @see header_invite_participants and @see
	 * footer_invite_participants
	 */
	private void disableAllButtons() {
		invite.setClickable(false);
		invite.setEnabled(false);
		next.setClickable(false);
		next.setEnabled(false);
		prev.setClickable(false);
		prev.setEnabled(false);
		cancel.setClickable(false);
		cancel.setEnabled(false);
	}

}
