package com.kainos.groupsafe.utilities;

public class EmergencyContact {

	String emergencyContactName = null;
	String emergencyContactNumber = null;
	String emergencyContactRelationship = null;

	public EmergencyContact(String emergencyContactName,
			String emergencyContactNumber, String emergencyConyactRelationship) {
		super();
		this.emergencyContactName = emergencyContactName;
		this.emergencyContactNumber = emergencyContactNumber;
		this.emergencyContactRelationship = emergencyConyactRelationship;
	}

	public String getEmergencyContactName() {
		return emergencyContactName;
	}

	public void setEmergencyContactName(String emergencyContactName) {
		this.emergencyContactName = emergencyContactName;
	}

	public String getEmergencyContactNumber() {
		return emergencyContactNumber;
	}

	public void setEmergencyContactNumber(String emergencyContactNumber) {
		this.emergencyContactNumber = emergencyContactNumber;
	}

	public String getEmergencyContactRelationship() {
		return emergencyContactRelationship;
	}

	public void setEmergencyContactRelationship(String emergencyContactRelationship) {
		this.emergencyContactRelationship = emergencyContactRelationship;
	}

	
}
