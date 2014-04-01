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

/**
 * This class is used to populate each row of the ContactList for the user with
 * the correct contact information. @see activity_home.xml (ListView) and @see
 * contact_row.xml (Layout for each row in the list view)
 * 
 * @author Rhiannon Henry
 * 
 */
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

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return data.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return position;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
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
