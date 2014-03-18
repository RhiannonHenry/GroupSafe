package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class InviteGroupParticipantsActivity extends Activity {

	static InviteGroupParticipantsActivity _instance = null;
	private final static Logger LOGGER = Logger
			.getLogger(InviteGroupParticipantsActivity.class.getName());

	InviteesAdapter adapter = null;
	ListView listView = null;

	// populate this with contact name and number [Parse call]
	// getParticipantInformation method
	private ArrayList<InviteeContact> invitedContacts = new ArrayList<InviteeContact>();
	private ArrayList<String> groupParticipants_participantId = new ArrayList<String>();
	// Array of contacts passed through from before
	private ArrayList<String> participants;
	private int radius;
	private boolean participantsInvited = false;
	private String groupName, groupOrganization, participantUserObjectID,
			groupId, groupLeaderId, groupLeaderDisplayName, participantId;
	private Button invite, prev, next, cancel;
	private View header, footer;
	private Timer autoUpdate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "TOLfW1Hct4MUsKvpcUgB8rbMgHEryr4MW95A0bAZ",
				"C5QjK9SQaHuVqSXqkBfFBw3WuAVynntpdn3xiQvN");
		ParseAnalytics.trackAppOpened(getIntent());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite_group_participants);
		_instance = this;

		initializeVariables();

		// create an Array Adapter from the String Array
		listView = (ListView) findViewById(R.id.inviteeList);
		footer = getLayoutInflater().inflate(
				R.layout.footer_invite_participants, null);
		header = getLayoutInflater().inflate(
				R.layout.header_invite_participants, null);
		listView.addFooterView(footer);
		listView.addHeaderView(header);
		adapter = new InviteesAdapter(this, R.layout.invitee_row,
				invitedContacts);
		// assign adapter to ListView
		listView.setAdapter(adapter);

		LOGGER.info("Got Participants: ");
		for (int i = 0; i < participants.size(); i++) {
			LOGGER.info("" + participants.get(i));
			getParticipantInformation(participants.get(i));
		}
		LOGGER.info("Got Radius size: " + radius);
		LOGGER.info("Got Group Name: " + groupName);
		LOGGER.info("Got Group Organization: " + groupOrganization);

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
	
	@Override
	public void onResume() {
		LOGGER.info("UPDATING...");
		super.onResume();
		autoUpdate = new Timer();
		autoUpdate.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						if (participantsInvited) {
							for (int i = 0; i < invitedContacts.size(); i++) {
								LOGGER.info("Updating Participant: "
										+ invitedContacts.get(i)
												.getInviteeContactName());
								updateParticipantStatus(invitedContacts.get(i),
										i);
							}
						} else {
							LOGGER.info("NOTHING TO UPDATE");
						}
					}
				});
			}
		}, 0, 30000); // updates every 30 seconds
	}

	@Override
	public void onPause() {
		autoUpdate.cancel();
		super.onPause();
	}

	private void cancelButtonClicked() {
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				setGroupLeaderStatus(false);
				notifyGroupMembers();
			}

			private void notifyGroupMembers() {
				ParseQuery<ParseObject> getGroupQuery = ParseQuery
						.getQuery("Group");
				getGroupQuery.whereEqualTo("objectId", groupId);
				try {
					List<ParseObject> foundGroups = getGroupQuery.find();
					LOGGER.info("SUCCESS:: Found Group");
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
						LOGGER.info("FAILURE:: Cannot access group");
					}
				} catch (ParseException e) {
					LOGGER.info("ERROR:: Unable to Find Group...");
					e.printStackTrace();
				}

				deleteGroup();
			}

			private void deleteGroup() {
				ParseQuery<ParseObject> query = ParseQuery.getQuery("Group");
				query.whereEqualTo("objectId", groupId);
				query.findInBackground(new FindCallback<ParseObject>() {
					@Override
					public void done(List<ParseObject> foundGroups,
							ParseException e) {
						if (e == null) {
							if (foundGroups.size() > 0) {
								LOGGER.info("SUCCESS:: Found Group!");
								ParseObject group = foundGroups.get(0);
								group.deleteEventually(new DeleteCallback() {
									@Override
									public void done(ParseException e) {
										if (e == null) {
											LOGGER.info("SUCCESS:: Deleted Group From DB");
										} else {
											LOGGER.info("ERROR:: Failed to Delete Group");
											e.printStackTrace();
										}
									}
								});
							} else {
								LOGGER.info("FAILURE:: Failed to Find Group");
							}
						} else {
							LOGGER.info("ERROR::");
							e.printStackTrace();
						}
					}
				});
			}

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
										LOGGER.info("SUCCESS:: Found Participant");
										ParseObject current = foundParticipants
												.get(0);
										String number = current.get(
												"participantNumber").toString();
										LOGGER.info("Got username: " + number
												+ " for participant");
										findUserInDb(number);
									} else {
										LOGGER.info("FAILURE:: Failed to get Participant");
									}
								} else {
									LOGGER.info("ERROR::");
									e.printStackTrace();
								}
							}

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
														LOGGER.info("SUCCESS:: Found User "
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
															LOGGER.info("ERROR: Error Creating JSON for Temination Notification.");
															e1.printStackTrace();
														}
													} else {
														LOGGER.info("FAILURE:: Unable to find User");
													}
												} else {
													LOGGER.info("ERROR::");
													e.printStackTrace();
												}
											}

											private void sendNotification(
													String userObjectId,
													JSONObject terminationData) {
												String notificationChannel = "user_"
														+ userObjectId;
												LOGGER.info("#004: Channel = "
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

	private void updateParticipantStatus(InviteeContact currentParticipant,
			int position) {
		String participantId = currentParticipant.getObjectId();
		String displayedStatus = currentParticipant.getStatus().toString();
		String participantName = currentParticipant.getInviteeContactName();
		String participantNumber = currentParticipant.getInviteeContactNumber();

		LOGGER.info("Trying to Find Participant in DB...");
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Participant");
		query.whereEqualTo("participantId", participantId);
		query.whereEqualTo("groupId", groupId);
		query.whereEqualTo("groupLeaderId", groupLeaderId);
		try {
			List<ParseObject> foundParticipant = query.find();
			if (foundParticipant.size() > 0) {
				ParseObject current = foundParticipant.get(0);
				LOGGER.info("SUCCESS:: Found Participant "
						+ current.getObjectId().toString());
				String currentStatus = current.get("status").toString();
				if (!displayedStatus.equals(currentStatus)) {
					LOGGER.info("STATUS DIFFERENCE!");
					InviteeContact updatedContact = new InviteeContact(
							participantName, participantNumber, participantId,
							Status.valueOf(currentStatus));
					invitedContacts.set(position, updatedContact);
					adapter.inviteeList.set(position, updatedContact);
					adapter.notifyDataSetChanged();
					listView.refreshDrawableState();
				} else {
					LOGGER.info("NO CHANGES TO PARTICIPANT STATE!");
				}
			} else {
				LOGGER.info("FAILURE:: Could Not Find Participant.");
			}
		} catch (ParseException e) {
			LOGGER.info("ERROR:: Unable to find Participant");
			e.printStackTrace();
		}
	}

	private void previousButtonClicked() {
		prev.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				// Go back to setRadiusView
				Intent intent = new Intent(_instance,
						SetGroupGeoFenceActivity.class);
				intent.putStringArrayListExtra("chosenParticipants",
						participants);
				startActivity(intent);
			}
		});
	}

	private void nextButtonClicked() {
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				sendStartPushNotificationToParticipants();
			}
		});
	}

	protected void sendStartPushNotificationToParticipants() {
		ParseQuery<ParseObject> getGroupQuery = ParseQuery
				.getQuery("Group");
		getGroupQuery.whereEqualTo("objectId", groupId);
		try {
			List<ParseObject> foundGroups = getGroupQuery.find();
			LOGGER.info("SUCCESS:: Found Group");
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
				LOGGER.info("FAILURE:: Cannot access group");
			}
		} catch (ParseException e) {
			LOGGER.info("ERROR:: Unable to Find Group...");
			e.printStackTrace();
		}		
	}

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
								LOGGER.info("SUCCESS:: Found Participant");
								ParseObject current = foundParticipants
										.get(0);
								String number = current.get(
										"participantNumber").toString();
								LOGGER.info("Got username: " + number
										+ " for participant");
								findUserInDb(number);
							} else {
								LOGGER.info("FAILURE:: Failed to get Participant");
							}
						} else {
							LOGGER.info("ERROR::");
							e.printStackTrace();
						}
					}

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
												LOGGER.info("SUCCESS:: Found User "
														+ user.get("displayName"));
												String groupLeaderDisplayName = ParseUser
														.getCurrentUser()
														.get("displayName")
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
													sendNotification(
															user.getObjectId()
																	.toString(),
															startData);
												} catch (JSONException e1) {
													LOGGER.info("ERROR: Error Creating JSON for Temination Notification.");
													e1.printStackTrace();
												}
											} else {
												LOGGER.info("FAILURE:: Unable to find User");
											}
										} else {
											LOGGER.info("ERROR::");
											e.printStackTrace();
										}
									}

									private void sendNotification(
											String userObjectId,
											JSONObject terminationData) {
										String notificationChannel = "user_"
												+ userObjectId;
										LOGGER.info("#004: Channel = "
												+ notificationChannel);
										ParsePush push = new ParsePush();
										push.setData(terminationData);
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

	private void inviteButtonClicked() {
		invite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disableAllButtons();
				// create group in DB
				createGroupInDB();
				setGroupLeaderStatus(true);
			}

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
							LOGGER.info("SUCCESS:: Saved Group");
							getGroupID();
						} else {
							LOGGER.info("ERROR:: ");
							e.printStackTrace();
						}
					}

					private void getGroupID() {
						LOGGER.info("GroupLeader: " + groupLeaderId);
						LOGGER.info("GroupName: " + groupName);
						ParseQuery<ParseObject> query = ParseQuery
								.getQuery("Group");
						query.whereEqualTo("groupLeaderId", groupLeaderId);
						query.whereEqualTo("groupName", groupName);
						try {
							List<ParseObject> foundGroups = query.find();
							if (foundGroups.size() > 0) {
								ParseObject current = foundGroups.get(0);
								LOGGER.info("SUCCESS:: Group Found!"
										+ current.get("groupName"));
								groupId = current.getObjectId().toString();
								LOGGER.info("GroupID: " + groupId);
								createParticipantsInDB();
							} else {
								LOGGER.info("FAILURE:: Could Not Find Any Groups...");
							}
						} catch (ParseException e) {
							LOGGER.info("ERROR Finding Group: ");
							e.printStackTrace();
						}
					}
				});
			}

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

					LOGGER.info("Preparing to add participant: ");
					LOGGER.info("Name: " + participantName);
					LOGGER.info("Number: " + participantNumber);
					LOGGER.info("ID: " + participantId);
					LOGGER.info("Status: " + participantStatus);
					LOGGER.info("Group Leader ID: " + groupLeaderId);
					LOGGER.info("Group ID: " + groupId);
					LOGGER.info("... To Participant Table in DB.");

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
								LOGGER.info("SUCCESS:: Created Participant Successfully");
							} else {
								LOGGER.info("ERROR:: ");
								e.printStackTrace();
							}
						}
					});
					if (i == invitedContacts.size() - 1) {
						LOGGER.info("Finished Creating the last Participant...");
						LOGGER.info("Fetching all the Participant 'objectId' fields...");
						populateGroupParticipantsArray();
					}
				}
				// send push notifications to participants
				sendPushNotifications();
				participantsInvited = true;
			}

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
								LOGGER.info("SUCCESS:: Found Participants for Group!");
								for (int i = 0; i < participantsInGroup.size(); i++) {
									String currentObjectId = participantsInGroup
											.get(i).getObjectId().toString();
									LOGGER.info(i + ": " + currentObjectId);
									groupParticipants_participantId
											.add(currentObjectId);
									if (i == participantsInGroup.size() - 1) {
										LOGGER.info("That's the last Participant in the group!");
										addParticipantsArrayToGroupTableDB();
									}
								}
							} else {
								LOGGER.info("FAILURE:: Unable to find Participants for Group!");
							}
						} else {
							LOGGER.info("ERROR: ");
							e.printStackTrace();
						}
					}

					private void addParticipantsArrayToGroupTableDB() {
						ParseQuery<ParseObject> query = ParseQuery
								.getQuery("Group");
						query.whereEqualTo("groupLeaderId", groupLeaderId);
						query.whereEqualTo("groupName", groupName);
						try {
							List<ParseObject> foundGroups = query.find();
							if (foundGroups.size() > 0) {
								LOGGER.info("SUCCESS:: Group Found!");
								ParseObject current = foundGroups.get(0);
								current.put("groupParticipants",
										groupParticipants_participantId);
								current.saveInBackground(new SaveCallback() {
									@Override
									public void done(ParseException e) {
										if (e == null) {
											LOGGER.info("SUCCESS:: Updated Group with Participants!");
										} else {
											LOGGER.info("ERROR:: Updating Group with Participants FAILED");
											e.printStackTrace();
										}
									}
								});
							} else {
								LOGGER.info("FAILURE:: Could Not Find Any Groups...");
							}
						} catch (ParseException e) {
							LOGGER.info("ERROR Finding Group: ");
							e.printStackTrace();
						}
					}
				});
			}

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
											LOGGER.info("Installation ID: "
													+ installationId);
										} else {
											LOGGER.info("ID: 001 Search Returned No Results.");
										}
									} else {
										LOGGER.info("Error Getting Participant Data.");
										e.printStackTrace();
									}
								}
							});

					LOGGER.info("Sending Push Notification to User: "
							+ invitedContacts.get(i).getInviteeContactName());
					getParticipantUserObjectID(invitedContacts.get(i)
							.getInviteeContactNumber(), data, i);
				}
			}

			protected void getParticipantUserObjectID(String number,
					final JSONObject data, final int place) {
				ParseQuery<ParseUser> query = ParseUser.getQuery();
				query.whereEqualTo("username", number);
				query.findInBackground(new FindCallback<ParseUser>() {
					@Override
					public void done(List<ParseUser> userList, ParseException e) {
						if (e == null) {
							if (userList.size() > 0) {
								ParseUser current = userList.get(0);
								LOGGER.info("Successfully retrieved participant user account: "
										+ current.getUsername()
										+ " ObjectID: "
										+ current.getObjectId().toString());
								participantUserObjectID = current.getObjectId()
										.toString();
								sendPushInvitation(data);

							} else {
								LOGGER.info("An error occurred retrieving the participant user account!");
							}
						} else {
							LOGGER.info("ERROR: ");
							e.printStackTrace();
						}
					}
				});
			}

			private void sendPushInvitation(JSONObject data) {
				String channel = "user_" + participantUserObjectID;
				LOGGER.info("#003: Channel = " + channel);
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

	protected void setGroupLeaderStatus(boolean groupLeaderStatus) {
		ParseUser currentUser = ParseUser.getCurrentUser();
		currentUser.put("groupLeader", groupLeaderStatus);
		try {
			currentUser.save();
			LOGGER.info("SUCCESS:: Updated 'groupLeader' value for user!");
		} catch (ParseException e) {
			LOGGER.info("ERROR:: Updating 'groupLeader' value for user!");
			e.printStackTrace();
		}
	}

	private void initializeVariables() {
		Intent intent = getIntent();
		participants = intent.getStringArrayListExtra("chosenParticipants");
		radius = Integer.parseInt(intent.getStringExtra("geoFenceRadius"));
		groupName = intent.getStringExtra("groupName");
		groupOrganization = intent.getStringExtra("groupOrganization");
		groupLeaderId = ParseUser.getCurrentUser().getObjectId().toString();
	}

	protected JSONObject generateJSON(int position) {
		groupLeaderDisplayName = ParseUser.getCurrentUser()
				.get("displayName").toString();
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
			LOGGER.info("INVALID JSON!!!");
			e.printStackTrace();
		}
		return data;
	}

	private void getParticipantInformation(String participantObjectId) {
		LOGGER.info("Getting Name for Participant with id: "
				+ participantObjectId);

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Contact");
		query.whereEqualTo("objectId", participantObjectId);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> foundContactInformation,
					ParseException e) {
				if (e == null) {
					if (foundContactInformation.size() != 0) {
						LOGGER.info("FOUND CONTACT");
						ParseObject currentData = foundContactInformation
								.get(0);
						String contactName = currentData.get("name").toString();
						String contactNumber = currentData.get("number")
								.toString();
						String objectId = currentData.getObjectId();
						LOGGER.info("CONTACT NAME: " + contactName);
						LOGGER.info("CONTACT NUMBER: " + contactNumber);
						InviteeContact contact = new InviteeContact(
								contactName, contactNumber, objectId,
								Status.PENDING);
						invitedContacts.add(contact);
						adapter.inviteeList.add(contact);
						adapter.notifyDataSetChanged();
						listView.refreshDrawableState();
					} else {
						LOGGER.info("UNABLE TO FIND CONTACT");
					}
				}
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.invite_group_participants, menu);
		return true;
	}

	/**
	 * This is a Javadoc example. It explains the working of Javadoc comments.
	 * 
	 * @param String
	 *            text The above line is used to document what the parameters
	 *            that are passed to the method do. Each parameter gets its own @param
	 *            block.
	 * 
	 * @return void This explains what the output / result of the method is. In
	 *         this case, it's void.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_logout) {
			logCurrentUserOut();
		} else if (id == R.id.action_addContact) {
			LOGGER.info("Starting Add Contact Activity...");
			Intent intent = new Intent(getApplicationContext(),
					AddContactActivity.class);
			startActivity(intent);
			finish();
		} else if (id == R.id.action_viewMap) {
			LOGGER.info("Starting Map View Activity...");
			Intent intent = new Intent(getApplicationContext(),
					MapsViewActivity.class);
			startActivity(intent);
			finish();
		} else if (id == R.id.action_createGroup) {
			LOGGER.info("Starting Create Group Activity...");
			Intent intent = new Intent(getApplicationContext(),
					SelectGroupParticipantsActivity.class);
			startActivity(intent);
			finish();
		} else if (id == R.id.action_home) {
			LOGGER.info("Starting Home Activity...");
			Intent intent = new Intent(getApplicationContext(),
					HomeActivity.class);
			startActivity(intent);
			finish();
		} else if (id == R.id.action_settings) {
			LOGGER.info("Going to Settings page... ");
			Intent intent = new Intent(getApplicationContext(),
					SettingsActivity.class);
			startActivity(intent);
			finish();
		}
		return true;
	}

	/**
	 * This is a Javadoc example. It explains the working of Javadoc comments.
	 * 
	 * @param String
	 *            text The above line is used to document what the parameters
	 *            that are passed to the method do. Each parameter gets its own @param
	 *            block.
	 * 
	 * @return void This explains what the output / result of the method is. In
	 *         this case, it's void.
	 */
	private void logCurrentUserOut() {
		LOGGER.info("Logging out user: " + ParseUser.getCurrentUser() + "...");
		ParseUser.logOut();
		if (ParseUser.getCurrentUser() == null) {
			LOGGER.info("User successfully logged out!");
			Toast.makeText(getApplicationContext(), "Successfully Logged Out!",
					Toast.LENGTH_LONG).show();
			Intent intent = new Intent(getApplicationContext(),
					SplashActivity.class);
			startActivity(intent);
			finish();
		} else {
			Toast.makeText(getApplicationContext(), "Please Try Again!",
					Toast.LENGTH_LONG).show();
		}

	}

	private void enableAllButtons() {
		invite.setClickable(true);
		invite.setEnabled(true);
		next.setClickable(true);
		next.setEnabled(true);
		prev.setClickable(true);
		prev.setEnabled(true);
	}

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
