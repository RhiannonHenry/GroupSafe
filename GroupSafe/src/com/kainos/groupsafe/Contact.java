package com.kainos.groupsafe;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {
	
	public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
		@Override
		public Contact createFromParcel(Parcel in){
			return new Contact(in);
		}
		
		@Override
		public Contact[] newArray(int size){
			return new Contact[size];
		}
	};
	
	String contactName = null;
	String contactNumber = null;
	
	public String getContactName() {
		return contactName;
	}
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	public String getContactNumber() {
		return contactNumber;
	}
	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public Contact(){};
	
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
