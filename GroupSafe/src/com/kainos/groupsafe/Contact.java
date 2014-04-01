package com.kainos.groupsafe;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class can be used to store Contact information (i.e. Name and Number)
 * 
 * @author Rhiannon Henry
 * 
 */
public class Contact implements Parcelable {

	public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
		@Override
		public Contact createFromParcel(Parcel in) {
			return new Contact(in);
		}

		@Override
		public Contact[] newArray(int size) {
			return new Contact[size];
		}
	};

	String contactName = null;
	String contactNumber = null;

	/**
	 * This method is used to return the name of a given contact
	 * 
	 * @return contactName A string value holding the name of the contact
	 */
	public String getContactName() {
		return contactName;
	}

	/**
	 * This method is used to set the name of a given contact
	 * 
	 * @param contactName
	 *            The String value of the contacts name
	 */
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	/**
	 * This method can be used to return the phone number of a given contact
	 * 
	 * @return contactNumber A string value holding the phone number of the
	 *         contact
	 */
	public String getContactNumber() {
		return contactNumber;
	}

	/**
	 * This method can be used to set the phone number for a given contact
	 * 
	 * @param contactNumber
	 *            The String value of the contacts phone number
	 */
	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public Contact() {
	};

	public Contact(Parcel in) {
		contactName = in.readString();
		contactNumber = in.readString();
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(contactName);
		out.writeString(contactNumber);
	}

}
