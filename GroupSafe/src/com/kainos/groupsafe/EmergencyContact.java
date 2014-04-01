package com.kainos.groupsafe;

/**
 * This is the class that is used to represent an Emergency Contact for the @see
 * EmergencyContactRowAdapter.java and @see SettingsActivity.java
 * 
 * @author Rhiannon Henry
 * 
 */
public class EmergencyContact {

	String emergencyContactName = null;
	String emergencyContactNumber = null;
	String emergencyContactRelationship = null;

	/**
	 * This is the constructor for an Emergency Contact. A new Emergency Contact
	 * must have a name, number and relationship (see parameters below)
	 * 
	 * @param emergencyContactName
	 *            the String value representing the name of the emergency
	 *            contact
	 * @param emergencyContactNumber
	 *            the String value representing the phone number of the
	 *            emergency contacts
	 * @param emergencyContactRelationship
	 *            the String value representing the relationship between the
	 *            user and the emergency contact
	 */
	public EmergencyContact(String emergencyContactName,
			String emergencyContactNumber, String emergencyContactRelationship) {
		super();
		this.emergencyContactName = emergencyContactName;
		this.emergencyContactNumber = emergencyContactNumber;
		this.emergencyContactRelationship = emergencyContactRelationship;
	}

	/**
	 * Used to return the name of an Emergency Contact
	 * 
	 * @return name a String representation of the requested Emergency Contacts
	 *         name
	 */
	public String getEmergencyContactName() {
		return emergencyContactName;
	}

	/**
	 * Used to set/update the name for an emergency contact
	 * 
	 * @param emergencyContactName
	 *            a String value of the name of the emergency contact
	 */
	public void setEmergencyContactName(String emergencyContactName) {
		this.emergencyContactName = emergencyContactName;
	}

	/**
	 * Used to return the number of an Emergency Contact
	 * 
	 * @return number a String representation of the requested Emergency
	 *         Contacts number
	 */
	public String getEmergencyContactNumber() {
		return emergencyContactNumber;
	}

	/**
	 * Used to set/update the phone number for an emergency contact
	 * 
	 * @param emergencyContactNumber
	 *            a String value of the phone number of the emergency contact
	 */
	public void setEmergencyContactNumber(String emergencyContactNumber) {
		this.emergencyContactNumber = emergencyContactNumber;
	}

	/**
	 * Used to return the relationship of an Emergency Contact to a user
	 * 
	 * @return relationship a String representation of the requested Emergency
	 *         Contacts relationship to the user
	 */
	public String getEmergencyContactRelationship() {
		return emergencyContactRelationship;
	}

	/**
	 * Used to set/update the relationship value of an emergency contact and a
	 * user
	 * 
	 * @param emergencyContactRelationship
	 *            a String value of the relationship between an emergency
	 *            contact and a user
	 */
	public void setEmergencyContactRelationship(
			String emergencyContactRelationship) {
		this.emergencyContactRelationship = emergencyContactRelationship;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EmergencyContact [emergencyContactName=" + emergencyContactName
				+ ", emergencyContactNumber=" + emergencyContactNumber
				+ ", emergencyContactRelationship="
				+ emergencyContactRelationship + "]";
	}

}
