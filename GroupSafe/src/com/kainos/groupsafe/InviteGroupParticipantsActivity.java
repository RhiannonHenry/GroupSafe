package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

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

	// Array of contacts passed through from before
	private ArrayList<String> participants;
	private int radius;
	private String groupName, groupOrganization, participantUserObjectID,
			groupId, groupLeaderId, participantId, participantObjectId;
	private Button invite, prev, next;
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

		inviteButtonClicked();
		nextButtonClicked();
		previousButtonClicked();

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
						for (int i = 0; i < invitedContacts.size(); i++) {
							LOGGER.info("Updating Participant: "
									+ invitedContacts.get(i)
											.getInviteeContactName());
							updateParticipantStatus(invitedContacts.get(i), i);
						}
					}
				});
			}
		}, 0, 60000); // updates every 60 seconds
	}

	@Override
	public void onPause() {
		autoUpdate.cancel();
		super.onPause();
	}

	private void updateParticipantStatus(InviteeContact currentParticipant,
			int position) {
		// TODO: Update Participant Information
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
				Button nextButton = (Button) findViewById(R.id.inviteParticipantsNextButton);
				Button prevButton = (Button) findViewById(R.id.inviteParticipantsPrevButton);
				nextButton.setClickable(false);
				nextButton.setEnabled(false);
				prevButton.setClickable(false);
				prevButton.setEnabled(false);

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
				Button nextButton = (Button) findViewById(R.id.inviteParticipantsNextButton);
				Button prevButton = (Button) findViewById(R.id.inviteParticipantsPrevButton);
				nextButton.setClickable(false);
				nextButton.setEnabled(false);
				prevButton.setClickable(false);
				prevButton.setEnabled(false);

				// TODO: proceed to group map view
			}
		});
	}

	private void inviteButtonClicked() {
		invite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// create group in DB
				createGroupInDB();
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
								getNewParticipantInformation();
							} else {
								LOGGER.info("ERROR:: ");
								e.printStackTrace();
							}
						}

						private void getNewParticipantInformation() {
							ParseQuery<ParseObject> query = ParseQuery
									.getQuery("Participant");
							query.whereEqualTo("groupLeaderId", groupLeaderId);
							query.whereEqualTo("groupId", groupId);
							query.whereEqualTo("participantId", participantId);
							try {
								List<ParseObject> foundParticipants = query
										.find();
								LOGGER.info("SUCCESS:: Found Participant!");
								participantObjectId = foundParticipants.get(0)
										.getObjectId().toString();
								addParticipantObjectIdToGroup();
							} catch (ParseException e) {
								LOGGER.info("ERROR:: ");
								e.printStackTrace();
							}
						}

						private void addParticipantObjectIdToGroup() {
							ParseQuery<ParseObject> query = ParseQuery
									.getQuery("Group");
							query.whereEqualTo("groupLeaderId", groupLeaderId);
							query.whereEqualTo("groupName", groupName);
							try {
								List<ParseObject> foundGroups = query.find();
								if (foundGroups.size() > 0) {
									LOGGER.info("SUCCESS:: Group Found!");
									ParseObject current = foundGroups.get(0);
									current.addUnique("groupParticipants",
											participantObjectId);
									current.saveInBackground(new SaveCallback() {

										@Override
										public void done(ParseException e) {
											if (e == null) {
												LOGGER.info("SUCCESS:: Updated Group with NEW Participant!");
											} else {
												LOGGER.info("ERROR:: Updating Group with NEW Participant FAILED");
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
				// send push notifications to participants
				sendPushNotifications();
			}

			private void sendPushNotifications() {
				// Send push notification using CHANNEL
				// "user_[userObjectID]"
				JSONObject data = generateJSON();

				// Send Push Notification to each participant.
				for (int i = 0; i < invitedContacts.size(); i++) {
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
			}
		});
	}

	private void initializeVariables() {
		Intent intent = getIntent();
		participants = intent.getStringArrayListExtra("chosenParticipants");
		radius = Integer.parseInt(intent.getStringExtra("geoFenceRadius"));
		groupName = intent.getStringExtra("groupName");
		groupOrganization = intent.getStringExtra("groupOrganization");
		groupLeaderId = ParseUser.getCurrentUser().getObjectId().toString();
	}

	protected JSONObject generateJSON() {
		String groupLeaderDisplayName = ParseUser.getCurrentUser()
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

}
