package com.kainos.groupsafe;

/**
 * This class is used to represent a contact who has been invited to a group by
 * another user. This contact is different from the initial contact and
 * participant contact as a status is associated with this contact. This contact
 * is used on the @see InviteGroupParticipantsActivity.java and is used to
 * populate the @see invitee_row using the @see InviteesAdapter.java
 * 
 * @author Rhiannon Henry
 * 
 */
public class InviteeContact {

	String inviteeContactName = null;
	String inviteeContactNumber = null;
	String objectId = null;
	Status status;

	/**
	 * This is the structure used to initialise a new InviteeContact. The
	 * parameters required are below.
	 * 
	 * @param inviteeContactName
	 *            a String value representing the name of the invited contact
	 * @param inviteeContactNumber
	 *            a String value representing the phone number of the invited
	 *            contact
	 * @param objectId
	 *            a String value that is a unique reference to the participant
	 *            object associated with this invited contact
	 * @param status
	 *            a Status Enum value that is used to represent that current
	 *            status of the invited contact. One of PENDING, ACCEPTED or
	 *            DECLINED
	 */
	public InviteeContact(String inviteeContactName,
			String inviteeContactNumber, String objectId, Status status) {
		super();
		this.inviteeContactName = inviteeContactName;
		this.inviteeContactNumber = inviteeContactNumber;
		this.objectId = objectId;
		this.status = status;
	}

	/**
	 * This method is used to fetch and return the String value of the contacts
	 * unique participant reference
	 * 
	 * @return objectId a String value representation of the unique participant
	 *         identifier for this contact.
	 */
	public String getObjectId() {
		return objectId;
	}

	/**
	 * This method is used to assign the String value of unique participant
	 * identifier to the current contact.
	 * 
	 * @param objectId
	 *            a String value representation of the unique participant
	 *            identifier for this contact.
	 */
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	/**
	 * This method is used to fetch and return the String value of the contacts
	 * name
	 * 
	 * @return inviteeContactName a String value representation of the name for
	 *         this contact.
	 */
	public String getInviteeContactName() {
		return inviteeContactName;
	}

	/**
	 * This method is used to assign the String value of inviteeContactName to
	 * the current contact.
	 * 
	 * @param inviteeContactName
	 *            a String value representation of the name to be assigned to
	 *            this contact.
	 */
	public void setInviteeContactName(String inviteeContactName) {
		this.inviteeContactName = inviteeContactName;
	}

	/**
	 * This method is used to fetch and return the String value of the contacts
	 * phone number.
	 * 
	 * @return inviteeContactNumber a String value representation of the phone
	 *         number for this contact.
	 */
	public String getInviteeContactNumber() {
		return inviteeContactNumber;
	}

	/**
	 * This method is used to assign the String value of inviteeContactNumber to
	 * the current contact.
	 * 
	 * @param inviteeContactNumber
	 *            a String value representation of the phone number to be
	 *            assigned to this contact.
	 */
	public void setInviteeContactNumber(String inviteeContactNumber) {
		this.inviteeContactNumber = inviteeContactNumber;
	}

	/**
	 * This is used to fetch and return the current status of the contact (i.e.
	 * PENDING, ACCEPTED, DECLINED)
	 * 
	 * @return status an enum value from the Enum class Status @see Status.java.
	 *         This will represent the current status of the contact.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * This is used to assign/update the current status of the contact (i.e.
	 * PENDING, ACCEPTED, DECLINED)
	 * 
	 * @param status
	 *            an enum value from the Enum class Status @see Status.java.
	 *            This will represent the current status of the contact.
	 */
	public void setStatus(Status status) {
		this.status = status;
	}
}
