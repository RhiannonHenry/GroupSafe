package com.kainos.groupsafe.adapters;

import java.util.ArrayList;
import java.util.logging.Logger;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kainos.groupsafe.R;
import com.kainos.groupsafe.utilities.InviteeContact;

public class InviteesAdapter extends ArrayAdapter<InviteeContact> {

	private final static Logger LOGGER = Logger.getLogger(InviteesAdapter.class
			.getName());

	public ArrayList<InviteeContact> inviteeList;
	
	public InviteesAdapter(Context context, int layoutResourceId,
			ArrayList<InviteeContact> inviteeList) {
		super(context, layoutResourceId, inviteeList);
		this.inviteeList = new ArrayList<InviteeContact>();
		this.inviteeList.addAll(inviteeList);
	}
	
	private class ViewHolder {
		TextView inviteeName;
		TextView inviteeNumber;
		TextView inviteeStatus;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		LOGGER.info("ConvertView " + String.valueOf(position));
		LOGGER.info("Number of invitees: "+ inviteeList.size());

		LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.invitee_row, null);
		holder = new ViewHolder();

		// Initialise all the UI elements from your GroupParticipantHolder
		// here...
		holder.inviteeName = (TextView) convertView
				.findViewById(R.id.inviteeName);
		holder.inviteeNumber = (TextView) convertView
				.findViewById(R.id.inviteeNumber);
		holder.inviteeStatus = (TextView) convertView
				.findViewById(R.id.inviteeStatus);

		convertView.setTag(holder);

		InviteeContact invitee = inviteeList.get(position);
		holder.inviteeName
				.setText(invitee.getInviteeContactName());
		holder.inviteeNumber.setText(invitee
				.getInviteeContactNumber());
		
		String status = invitee.getStatus().getStatusCode();
		if(status.equals("P")){
			holder.inviteeStatus.setTextColor(Color.rgb(255, 165, 0));
			holder.inviteeStatus.setText("PENDING");
		}else if(status.equals("A")){
			holder.inviteeStatus.setTextColor(Color.rgb(124, 252, 0));
			holder.inviteeStatus.setText("ACCEPTED");
		}else if(status.equals("D")){
			holder.inviteeStatus.setTextColor(Color.rgb(255, 69, 0));
			holder.inviteeStatus.setText("DECLINED");
		}else{
			holder.inviteeStatus.setTextColor(Color.rgb(190, 190, 190));
			holder.inviteeStatus.setText("UNKNOWN");
		}

		return convertView;
	}
	
}
