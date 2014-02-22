package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.logging.Logger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kainos.groupsafe.R;

public class EmergencyContactRowAdapter extends ArrayAdapter<EmergencyContact> {

	private final static Logger LOGGER = Logger
			.getLogger(EmergencyContactRowAdapter.class.getName());

	public ArrayList<EmergencyContact> emergencyContactList;

	public EmergencyContactRowAdapter(Context context, int layoutResourceId,
			ArrayList<EmergencyContact> emergencyContactList) {
		super(context, layoutResourceId, emergencyContactList);
		this.emergencyContactList = new ArrayList<EmergencyContact>();
		this.emergencyContactList.addAll(emergencyContactList);
	}
	
	private class ViewHolder {
		TextView emergencyContactName;
		TextView emergencyContactNumber;
		TextView emergencyContactRelationship;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		LOGGER.info("ConvertView " + String.valueOf(position));
		LOGGER.info("Number of emergency contacts: "+emergencyContactList.size());

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
		LOGGER.info("Creating View with Emergency Contact:");
		LOGGER.info("Name: "+eContact.getEmergencyContactName());
		LOGGER.info("Number: "+eContact.getEmergencyContactNumber());
		LOGGER.info("Relationship: "+eContact.getEmergencyContactRelationship());
		
		holder.emergencyContactName
				.setText(eContact.getEmergencyContactName());
		holder.emergencyContactNumber.setText(eContact
				.getEmergencyContactNumber());
		holder.emergencyContactRelationship.setText(eContact
				.getEmergencyContactRelationship());

		return convertView;
	}
	

}
