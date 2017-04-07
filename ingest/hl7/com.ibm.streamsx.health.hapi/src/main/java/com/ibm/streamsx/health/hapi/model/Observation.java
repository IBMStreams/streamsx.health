//*******************************************************************************
//* Copyright (C) 2016 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.hapi.model;

import java.io.Serializable;

public class Observation implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private String ts = IHL7Constants.EMPTYSTR;
	private String location = IHL7Constants.EMPTYSTR ;
	private String observationId = IHL7Constants.EMPTYSTR;
	private String observationValue = IHL7Constants.EMPTYSTR;
	private String unit = IHL7Constants.EMPTYSTR;
	private String sendingApp = IHL7Constants.EMPTYSTR;
	private String sendingFacility = IHL7Constants.EMPTYSTR;

	// This is the SPL type defined by the toolkit.  This must match the Observation model class.
	public static final String OBSERVATION_SCHEMA_SPL = "tuple<rstring ts, rstring location, rstring observationId,rstring observationValue, rstring unit, rstring sendingApp, rstring sendingFacility>";
	
	public Observation() {
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		
		if (location != null)
			this.location = location;
	}

	public String getTs() {
		return ts;
	}

	public void setTs(String ts) {
		if (ts != null)
			this.ts = ts;
	}

	public String getObservationId() {
		return observationId;
	}

	public void setObservationId(String observationId) {
		if (observationId != null)
			this.observationId = observationId;
	}

	public String getObservationValue() {
		return observationValue;
	}

	public void setObservationValue(String observationValue) {
		if (observationValue != null)
			this.observationValue = observationValue;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		if (unit != null)
			this.unit = unit;
	}
	
	public void setSendingApp(String sendingApp) {
		this.sendingApp = sendingApp;
	}
	
	public String getSendingApp() {
		return sendingApp;
	}
	
	public void setSendingFacility(String sendingFacility) {
		this.sendingFacility = sendingFacility;
	}
	
	public String getSendingFacility() {
		return sendingFacility;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Location: " + location + ", ");
		buffer.append("Ts: " + ts + ", ");
		buffer.append("ObxId: " + observationId + ", ");
		buffer.append("ObxValue: " + observationValue + ", ");
		buffer.append("ObxUnit: " + unit + ", ");
		buffer.append("sendingApp: " + sendingApp + ",");
		buffer.append("sendingFacility: " + sendingFacility + ",");
		return buffer.toString();
	}

}
