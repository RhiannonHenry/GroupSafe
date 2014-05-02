package com.kainos.groupsafe;

import java.text.DecimalFormat;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Haversine {

	private static final int EARTH_RADIUS_KM = 6373;
	private static final String TAG = "HAVERSINE_CLASS";
	
	/**
	 * This method uses the Haversine Formula to calculate the
	 * distance the participant is from the centre of the
	 * geo-fence (i.e whether they are inside or outside the
	 * geo-fence)
	 * 
	 * @param participantLocation
	 *            the latitude and longitude of the participants
	 *            current location.
	 * @param geoFenceCenter
	 *            the latitude and longitude of the center of
	 *            the geo-fence
	 * @return difference and integer value of the distance
	 *         between the participant and the centre of the
	 *         geo-fence in meters.
	 */
	public static int getDistanceDifference(
			LatLng participantLocation, LatLng geoFenceCenter) {
		// Lat and Lng for PARTICIPANT
		double participantLatitude = participantLocation.latitude;
		double participantLongitude = participantLocation.longitude;
		// Lat and Lng for LEADER/CENTER OF GEOFENCE
		double geoFenceCenterLatitude = geoFenceCenter.latitude;
		double geoFenceCenterLongitude = geoFenceCenter.longitude;
		// Difference between Lat and Lng
		double deltaLatitude =
				Math.toRadians(participantLatitude)
				- Math.toRadians(geoFenceCenterLatitude);
		double deltaLongitude =
				Math.toRadians(participantLongitude)
				- Math.toRadians(geoFenceCenterLongitude);
		// Part 1:
		// sin^2(lat1-lat2/2)+cos(lat1)*cos(lat2)*sin^2(long1-long2/2)
		double part1 = Math.sin(deltaLatitude / 2)
				* Math.sin(deltaLatitude / 2)
				+ Math.cos(Math.toRadians(participantLatitude))
						* Math.cos(Math.toRadians(geoFenceCenterLatitude))
				* Math.sin(deltaLongitude / 2)
				* Math.sin(deltaLongitude / 2);
		// Part 2:
		// 2*sin^-1(sqrt(part1))
		double part2 = 2.0 * Math.asin(Math.sqrt(part1));
		// Part 3: Difference
		// radius*part2
		double difference = EARTH_RADIUS_KM * part2;
		Log.i(TAG, "Difference in KM: " + difference);
		DecimalFormat newFormat = new DecimalFormat("####");
		double meter = difference * 1000;
		int meterInDec = Integer.valueOf(newFormat
				.format(meter));
		Log.i(TAG, "Distance from Center of Geo-fence = "
				+ meterInDec);
		return meterInDec;
	}
}
