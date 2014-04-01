package com.kainos.groupsafe;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This class is used to check if the users device is currently connected to the
 * Internet via either WiFi or Mobile Data.
 * 
 * @author Rhiannon Henry
 * 
 */
public class ConnectionDetector {

	private Context _context;

	/**
	 * Connection Detector constructor. The context of the application/device is
	 * passed in and used to check connectivity status.
	 * 
	 * @param context
	 *            this variable is passed in and holds all information about the
	 *            various states of the device at a moment in time
	 */
	public ConnectionDetector(Context context) {
		this._context = context;
	}

	/**
	 * This method checks if the device is connected to the Internet and returns
	 * TRUE if it is connected and FALSE if it is not connected.
	 * 
	 * @return boolean the boolean value of true means that a connection has
	 *         been found and false means that the device is not currently
	 *         connected to the Internet
	 */
	public boolean isConnectedToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) _context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
		}
		return false;
	}
}
