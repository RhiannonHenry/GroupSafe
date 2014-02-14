package com.kainos.groupsafe.utilities;

public class ParticipantContact {

	String participantContactName = null;
	String participantContactNumber = null;
	boolean selected = false;

	public ParticipantContact(String participantContactName,
			String participantContactNumber, boolean selected) {
		super();
		this.participantContactName = participantContactName;
		this.participantContactNumber = participantContactNumber;
		this.selected = selected;
	}

	public String getParticipantContactName() {
		return participantContactName;
	}

	public void setParticipantContactName(String participantContactName) {
		this.participantContactName = participantContactName;
	}

	public String getParticipantContactNumber() {
		return participantContactNumber;
	}

	public void setParticipantContactNumber(String participantContactNumber) {
		this.participantContactNumber = participantContactNumber;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	
}
