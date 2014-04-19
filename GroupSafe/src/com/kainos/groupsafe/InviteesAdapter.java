package com.kainos.groupsafe;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.kainos.groupsafe.R;

/**
 * This class is used to add rows to the invitee contact List View on the
 * InviteGroupParticipantsActivity.java. @see
 * activity_invite_group_participants.xml. The information from each invited
 * contact @see InviteeContact.java will be extracted and placed in the
 * appropriate place within the row @see invitee_row.xml.
 * 
 * @author Rhiannon Henry
 */
public class InviteesAdapter extends ArrayAdapter<InviteeContact> {

	private static final String TAG = "Invited_Contact_Row_Adapter";

	public ArrayList<InviteeContact> inviteeList;

	/**
	 * This is the constructor for the adapter.
	 * 
	 * @param context
	 *            the current state of the application and device
	 * @param layoutResourceId
	 *            an integer value referencing the xml layout for the row (@see
	 *            invitee_row.xml)
	 * @param inviteeList
	 *            this is an ArrayList containing information about all the
	 *            contacts who have been invited to participate in a group
	 */
	public InviteesAdapter(Context context, int layoutResourceId,
			ArrayList<InviteeContact> inviteeList) {
		super(context, layoutResourceId, inviteeList);
		this.inviteeList = new ArrayList<InviteeContact>();
		this.inviteeList.addAll(inviteeList);
	}

	/**
	 * This class holds the reference the items that are within the layout of
	 * the invited contact row @see invitee_row.xml
	 * 
	 * @author Rhiannon Henry
	 * 
	 */
	private class ViewHolder {
		TextView inviteeName;
		TextView inviteeNumber;
		TextView inviteeStatus;
	}

	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;
		Log.d(TAG, "ConvertView " + String.valueOf(position));
		Log.d(TAG, "Number of invitees: " + inviteeList.size());

		LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		convertView = vi.inflate(R.layout.invitee_row, null);
		holder = new ViewHolder();

		holder.inviteeName = (TextView) convertView
				.findViewById(R.id.inviteeName);
		holder.inviteeNumber = (TextView) convertView
				.findViewById(R.id.inviteeNumber);
		holder.inviteeStatus = (TextView) convertView
				.findViewById(R.id.inviteeStatus);

		convertView.setTag(holder);

		InviteeContact invitee = inviteeList.get(position);
		holder.inviteeName.setText(invitee.getInviteeContactName());
		holder.inviteeNumber.setText(invitee.getInviteeContactNumber());

		String status = invitee.getStatus().getStatusCode();
		if (status.equals("P")) {
			holder.inviteeStatus.setTextColor(Color.rgb(255, 165, 0));
			holder.inviteeStatus.setText("PENDING");
		} else if (status.equals("A")) {
			holder.inviteeStatus.setTextColor(Color.rgb(124, 252, 0));
			holder.inviteeStatus.setText("ACCEPTED");
		} else if (status.equals("D")) {
			holder.inviteeStatus.setTextColor(Color.rgb(255, 69, 0));
			holder.inviteeStatus.setText("DECLINED");
		} else {
			holder.inviteeStatus.setTextColor(Color.rgb(190, 190, 190));
			holder.inviteeStatus.setText("UNKNOWN");
		}

		return convertView;
	}

}
