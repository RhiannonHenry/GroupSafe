package com.kainos.groupsafe;

public class InviteeContact {

	String inviteeContactName = null;
	String inviteeContactNumber = null;
	String objectId = null;
	Status status;

	public InviteeContact(String inviteeContactName,
			String inviteeContactNumber,String objectId, Status status) {
		super();
		this.inviteeContactName = inviteeContactName;
		this.inviteeContactNumber = inviteeContactNumber;
		this.objectId = objectId;
		this.status = status;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getInviteeContactName() {
		return inviteeContactName;
	}

	public void setInviteeContactName(String inviteeContactName) {
		this.inviteeContactName = inviteeContactName;
	}

	public String getInviteeContactNumber() {
		return inviteeContactNumber;
	}

	public void setInviteeContactNumber(String inviteeContactNumber) {
		this.inviteeContactNumber = inviteeContactNumber;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}
