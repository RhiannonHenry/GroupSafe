package com.kainos.groupsafe;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.kainos.groupsafe.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class ParticipantContactRowAdapter extends
		ArrayAdapter<ParticipantContact> {

	private final static Logger LOGGER = Logger
			.getLogger(ParticipantContactRowAdapter.class.getName());

	public ArrayList<ParticipantContact> participantContactList;

	public ParticipantContactRowAdapter(Context context, int layoutResourceId,
			ArrayList<ParticipantContact> participantContactList) {
		super(context, layoutResourceId, participantContactList);
		this.participantContactList = new ArrayList<ParticipantContact>();
		this.participantContactList.addAll(participantContactList);
	}

	private class ViewHolder {
		TextView contactNameGroup;
		TextView contactNumberGroup;
		CheckBox contactGroupCheckBox;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		LOGGER.info("ConvertView " + String.valueOf(position));
		LOGGER.info("Length of retrieved contacts: "+participantContactList.size());

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
						LOGGER.info("Clicked on Checkbox: " + cb.getText()
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
