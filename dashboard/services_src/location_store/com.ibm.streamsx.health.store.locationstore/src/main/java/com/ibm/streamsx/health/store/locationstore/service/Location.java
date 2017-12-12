package com.ibm.streamsx.health.store.locationstore.service;

import java.io.Serializable;

public class Location implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String id;			// patient id
	private long ts;			// timestamp in milliseconds, epoch time
	private double latitude;	// in degrees
	private double longitude;	// in degrees
	private double speed;		// speed in km/hr
	private double heading;		// direction of travel, degrees east from north
	private String driverId;	// driver Id
		
	
	public String getId() {
		return id;
	}
	public Location setId(String id) {
		this.id = id;
		return this;
	}
	public long getTs() {
		return ts;
	}
	public Location setTs(long ts) {
		this.ts = ts;
		return this;
	}
	public double getLatitude() {
		return latitude;
	}
	public Location setLatitude(double latitude) {
		this.latitude = latitude;
		return this;
	}
	public double getLongitude() {
		return longitude;
	}
	public Location setLongitude(double longitude) {
		this.longitude = longitude;
		return this;
	}
	public double getSpeed() {
		return speed;
	}
	public Location setSpeed(double speed) {
		this.speed = speed;
		return this;
	}
	public double getHeading() {
		return heading;
	}
	public Location setHeading(double heading) {
		this.heading = heading;
		return this;
	}

	public void setDriverId(String driverId) {
		this.driverId = driverId;
	}
	
	public String getDriverId() {
		return driverId;
	}

}
