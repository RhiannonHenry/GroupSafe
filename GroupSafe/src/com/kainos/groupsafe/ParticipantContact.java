package com.kainos.groupsafe;

/**
 * This class is used to represent a contact who is in the users address book
 * and can be chosen as a group participant. This contact is different from the
 * initial contact and invitee contact a checkbox is associated with this
 * contact. This contact is used on the @see
 * {@link SelectGroupParticipantsActivity} and is used to populate the @see
 * select_group_participants_row.xml using the @see
 * {@link ParticipantContactRowAdapter}
 * 
 * @author Rhiannon Henry
 * 
 */
public class ParticipantContact {

	String participantContactName = null;
	String participantContactNumber = null;
	String objectId = null;
	boolean selected = false;

	/**
	 * This is the structure used to initialise a new ParticipantContact. The
	 * parameters required are below.
	 * 
	 * @param participantContactName
	 *            a String value representing the name of the contact
	 * @param participantContactNumber
	 *            a String value representing the phone number of the contact
	 * @param objectId
	 *            a String value representing the unique contact identifier for
	 *            the contact
	 * @param selected
	 *            a boolean value representing wheter the contact has been
	 *            chosen as a group participant or not
	 */
	public ParticipantContact(String participantContactName,
			String participantContactNumber, String objectId, boolean selected) {
		super();
		this.participantContactName = participantContactName;
		this.participantContactNumber = participantContactNumber;
		this.objectId = objectId;
		this.selected = selected;
	}

	/**
	 * This method is used to fetch and return the String value of the contacts
	 * unique contact reference
	 * 
	 * @return objectId a String value representation of the unique contact
	 *         identifier for this contact.
	 */
	public String getObjectId() {
		return objectId;
	}

	/**
	 * This method is used to assign the String value of unique contact
	 * identifier to the current contact.
	 * 
	 * @param objectId
	 *            a String value representation of the unique contact identifier
	 *            for this contact.
	 */
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	/**
	 * This method is used to fetch and return the String value of the contacts
	 * name
	 * 
	 * @return participantContactName a String value representation of the name
	 *         for this contact.
	 */
	public String getParticipantContactName() {
		return participantContactName;
	}

	/**
	 * This method is used to assign the String value of participantContactName
	 * to the current contact.
	 * 
	 * @param participantContactName
	 *            a String value representation of the name to be assigned to
	 *            this contact.
	 */
	public void setParticipantContactName(String participantContactName) {
		this.participantContactName = participantContactName;
	}

	/**
	 * This method is used to fetch and return the String value of the contacts
	 * phone number.
	 * 
	 * @return participantContactNumber a String value representation of the
	 *         phone number for this contact.
	 */
	public String getParticipantContactNumber() {
		return participantContactNumber;
	}

	/**
	 * This method is used to assign the String value of
	 * participantContactNumber to the current contact.
	 * 
	 * @param participantContactNumber
	 *            a String value representation of the phone number to be
	 *            assigned to this contact.
	 */
	public void setParticipantContactNumber(String participantContactNumber) {
		this.participantContactNumber = participantContactNumber;
	}

	/**
	 * This method is used to fetch and return the boolean value of whether the
	 * contact has been chosen as a group participant or not.
	 * 
	 * @return selected a boolean value representing if a contact has been
	 *         selected to participate in a group or not
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * This method is used to set/update the boolean value of whether the
	 * contact has been chosen as a group participant or not.
	 * 
	 * @param selected
	 *            a boolean value representing if a contact has been selected to
	 *            participate in a group or not
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
