package com.kainos.groupsafe;

import java.util.ArrayList;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kainos.groupsafe.R;

/**
 * This class is used to add rows to the emergency contact List View on the
 * Settings activity. @see settings.xml. The information from each Emergency
 * Contact @see EmergencyContact.java will be extracted and placed in the
 * appropriate place within the row @see emergency_contact_row.xml.
 * 
 * @author Rhiannon Henry
 */
public class EmergencyContactRowAdapter extends ArrayAdapter<EmergencyContact> {

	private static final String TAG = "EMERGENCY_CONTACT_ROW_ADAPTER";
	public ArrayList<EmergencyContact> emergencyContactList;

	/**
	 * This is the constructor for the adapter.
	 * 
	 * @param context
	 *            the current state of the application and device
	 * @param layoutResourceId
	 *            an integer value referencing the xml layout for the row (@see
	 *            emergency_contact_row.xml)
	 * @param emergencyContactList
	 *            this is an ArrayList containing information about all the
	 *            emergency contacts for a user
	 */
	public EmergencyContactRowAdapter(Context context, int layoutResourceId,
			ArrayList<EmergencyContact> emergencyContactList) {
		super(context, layoutResourceId, emergencyContactList);
		this.emergencyContactList = new ArrayList<EmergencyContact>();
		this.emergencyContactList.addAll(emergencyContactList);
	}

	/**
	 * This class holds the reference the items that are within the layout of
	 * the emergency contact row @see emergency_contact_row.xml
	 * 
	 * @author Rhiannon Henry
	 * 
	 */
	private class ViewHolder {
		TextView emergencyContactName;
		TextView emergencyContactNumber;
		TextView emergencyContactRelationship;
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		Log.d(TAG,"ConvertView " + String.valueOf(position));
		Log.d(TAG,"Number of emergency contacts: "
				+ emergencyContactList.size());

		LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.emergency_contact_row, null);
		holder = new ViewHolder();

		// Initialise all the UI elements from your ViewHolder here...
		holder.emergencyContactName = (TextView) convertView
				.findViewById(R.id.emergencyContactName);
		holder.emergencyContactNumber = (TextView) convertView
				.findViewById(R.id.emergencyContactNumber);
		holder.emergencyContactRelationship = (TextView) convertView
				.findViewById(R.id.emergencyContactRelationship);

		convertView.setTag(holder);

		EmergencyContact eContact = emergencyContactList.get(position);
		Log.i(TAG,"Creating View with Emergency Contact:");
		Log.i(TAG,"Name: " + eContact.getEmergencyContactName());
		Log.i(TAG,"Number: " + eContact.getEmergencyContactNumber());
		Log.i(TAG,"Relationship: "
				+ eContact.getEmergencyContactRelationship());

		holder.emergencyContactName.setText(eContact.getEmergencyContactName());
		holder.emergencyContactNumber.setText(eContact
				.getEmergencyContactNumber());
		holder.emergencyContactRelationship.setText(eContact
				.getEmergencyContactRelationship());

		return convertView;
	}

}
