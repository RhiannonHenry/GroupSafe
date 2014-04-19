package com.kainos.groupsafe;

import java.util.ArrayList;
import com.kainos.groupsafe.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * This class is used to add rows to the participant contact List View on the
 * {@link SelectGroupParticipantsActivity} @see
 * activity_select_group_participants.xml. The information from each possible
 * group participant {@link ParticipantContact} will be extracted and placed in
 * the appropriate place within the row @see select_group_participant_row.xml.
 * 
 * @author Rhiannon Henry
 */
public class ParticipantContactRowAdapter extends
		ArrayAdapter<ParticipantContact> {

	private static final String TAG = "Participant_Contact_Row_Adapter";

	public ArrayList<ParticipantContact> participantContactList;

	/**
	 * This is the constructor for the adapter.
	 * 
	 * @param context
	 *            the current state of the application and device
	 * @param layoutResourceId
	 *            an integer value referencing the xml layout for the row (@see
	 *            select_group_participant_row.xml)
	 * @param participantContactList
	 *            this is an ArrayList containing information about all the
	 *            contacts who could be selected to participate in the group
	 */
	public ParticipantContactRowAdapter(Context context, int layoutResourceId,
			ArrayList<ParticipantContact> participantContactList) {
		super(context, layoutResourceId, participantContactList);
		this.participantContactList = new ArrayList<ParticipantContact>();
		this.participantContactList.addAll(participantContactList);
	}

	/**
	 * This class holds the reference the items that are within the layout of
	 * the possible participants row @see select_group_participant_row.xml
	 * 
	 * @author Rhiannon Henry
	 * 
	 */
	private class ViewHolder {
		TextView contactNameGroup;
		TextView contactNumberGroup;
		CheckBox contactGroupCheckBox;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		Log.i(TAG, "ConvertView " + String.valueOf(position));
		Log.i(TAG,
				"Length of retrieved contacts: "
						+ participantContactList.size());

		LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.select_group_participant_row, null);
		holder = new ViewHolder();

		// Initialise all the UI elements from your GroupParticipantHolder
		// here...
		holder.contactNameGroup = (TextView) convertView
				.findViewById(R.id.contactNameGroup);
		holder.contactNumberGroup = (TextView) convertView
				.findViewById(R.id.contactNumberGroup);
		holder.contactGroupCheckBox = (CheckBox) convertView
				.findViewById(R.id.contactGroupCheckBox);

		convertView.setTag(holder);

		holder.contactGroupCheckBox
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						CheckBox cb = (CheckBox) v;
						ParticipantContact _participant = (ParticipantContact) cb
								.getTag();
						Log.i(TAG, "Clicked on Checkbox: " + cb.getText()
								+ " is " + cb.isChecked());
						_participant.setSelected(cb.isChecked());
					}
				});

		ParticipantContact participant = participantContactList.get(position);
		holder.contactNameGroup
				.setText(participant.getParticipantContactName());
		holder.contactNumberGroup.setText(participant
				.getParticipantContactNumber());
		holder.contactGroupCheckBox.setChecked(participant.isSelected());
		holder.contactGroupCheckBox.setTag(participant);

		return convertView;
	}
}
