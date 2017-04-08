//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************


package com.ibm.streamsx.health.hapi.model;

import java.io.Serializable;

/*
 * Represents event details from an ADT message
 */
public class EventDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	String eventType = IHL7Constants.EMPTYSTR;
	String recordTs = IHL7Constants.EMPTYSTR;
	String eventTs = IHL7Constants.EMPTYSTR;
	
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public String getRecordTs() {
		return recordTs;
	}
	public void setRecordTs(String recordTs) {
		this.recordTs = recordTs;
	}
	public String getEventTs() {
		return eventTs;
	}
	public void setEventTs(String eventTs) {
		this.eventTs = eventTs;
	}
	
	
	
}
