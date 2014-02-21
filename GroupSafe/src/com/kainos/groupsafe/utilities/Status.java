package com.kainos.groupsafe.utilities;

public enum Status {

	PENDING("P"), ACCEPTED("A"), DECLINED("D");
	
	private String statusCode;

	private Status(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusCode() {
		return statusCode;
	}
	
	
}
