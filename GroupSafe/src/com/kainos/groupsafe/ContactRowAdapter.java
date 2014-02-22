package com.kainos.groupsafe;

import java.util.ArrayList;

import com.kainos.groupsafe.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ContactRowAdapter extends BaseAdapter {

	private static LayoutInflater inflater = null;

	private Activity activity;
	private ArrayList<Contact> data;

	public ContactRowAdapter(Activity a, ArrayList<Contact> retreivedContacts) {
		activity = a;
		data = retreivedContacts;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (convertView == null) {
			view = inflater.inflate(R.layout.contact_row, null);
		}

		TextView contactNameView = (TextView) view
				.findViewById(R.id.contactName);
		TextView contactNumberView = (TextView) view
				.findViewById(R.id.contactNumber);
		
		Contact contact = data.get(position);
		
		String contactName = contact.getContactName();
		String contactNumber = contact.getContactNumber();
		
		contactNameView.setText(contactName);
		contactNumberView.setText(contactNumber);
		
		return view;
	}

}
