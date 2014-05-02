package com.kainos.groupsafe.tests;

import com.google.android.gms.maps.model.LatLng;
import com.kainos.groupsafe.Haversine;

import junit.framework.TestCase;

public class HaversineTest extends TestCase {
	public void testHaversineZeroDifference() {
		// Test data inputs
		double latitude = 53.56;
		double longitude = -6.43;
		
		LatLng participantLocation = new LatLng(latitude, longitude);
		LatLng geoFenceCenter = new LatLng(latitude, longitude);
		
		int expected = 0;
		int actual = Haversine.getDistanceDifference(participantLocation, geoFenceCenter);
		
		assertEquals(expected, actual);
	}
	
	public void testHaversineNormalDifference() {
		// Test data inputs
		double pLatitude = 54.587076;
		double pLongitude = -5.933856;
		double gLatitude = 54.581707;
		double gLongitude = -5.937531;
		
		LatLng participantLocation = new LatLng(pLatitude, pLongitude);
		LatLng geoFenceCenter = new LatLng(gLatitude, gLongitude);
		
		int expected = 642;
		int actual = Haversine.getDistanceDifference(participantLocation, geoFenceCenter);
		
		assertEquals(expected, actual);
	}
	
	public void testHaversineSmallDifference() {
		// Test data inputs
		double pLatitude = 54.587396;
		double pLongitude = -5.936959;
		double gLatitude = 54.586563;
		double gLongitude = -5.937531;
		
		LatLng participantLocation = new LatLng(pLatitude, pLongitude);
		LatLng geoFenceCenter = new LatLng(gLatitude, gLongitude);
		
		int expected = 100;
		int actual = Haversine.getDistanceDifference(participantLocation, geoFenceCenter);
		
		assertEquals(expected, actual);
	}
	
	public void testHaversineLargeDifference() {
		// Test data inputs
		double pLatitude = 54.5844327;
		double pLongitude = -5.9358838;
		double gLatitude = 55.071615;
		double gLongitude = -6.518558;
		
		LatLng participantLocation = new LatLng(pLatitude, pLongitude);
		LatLng geoFenceCenter = new LatLng(gLatitude, gLongitude);
		
		int expected = 65804;
		int actual = Haversine.getDistanceDifference(participantLocation, geoFenceCenter);
		
		assertEquals(expected, actual);
	}
}

