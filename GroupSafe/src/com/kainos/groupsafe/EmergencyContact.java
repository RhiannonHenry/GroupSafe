package com.kainos.groupsafe;

public class EmergencyContact {

	String emergencyContactName = null;
	String emergencyContactNumber = null;
	String emergencyContactRelationship = null;

	public EmergencyContact(String emergencyContactName,
			String emergencyContactNumber, String emergencyContactRelationship) {
		super();
		this.emergencyContactName = emergencyContactName;
		this.emergencyContactNumber = emergencyContactNumber;
		this.emergencyContactRelationship = emergencyContactRelationship;
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

	@Override
	public String toString() {
		return "EmergencyContact [emergencyContactName=" + emergencyContactName
				+ ", emergencyContactNumber=" + emergencyContactNumber
				+ ", emergencyContactRelationship="
				+ emergencyContactRelationship + "]";
	}

	
}
