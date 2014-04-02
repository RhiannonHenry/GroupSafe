package com.kainos.groupsafe;

/**
 * This is an enum class that is used to represent the 3 possible states for a
 * group invitation response: PENDING, ACCEPTED or DECLINED.
 * 
 * @author Rhiannon Henry
 * 
 */
public enum Status {

	PENDING("P"), ACCEPTED("A"), DECLINED("D");

	private String statusCode;

	/**
	 * This method is used to set the status to one of the 3 possible enum
	 * values.
	 * 
	 * @param statusCode
	 *            a String value representing the state
	 */
	private Status(String statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * This method is used to retrieve the current status.
	 * 
	 * @return statusCode a String value representing the current status.
	 */
	public String getStatusCode() {
		return statusCode;
	}

}
