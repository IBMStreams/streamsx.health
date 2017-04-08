//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************


package com.ibm.streamsx.health.ingest.types.model;

import java.io.Serializable;

/*
 * Represents event details from an ADT message
 */
public class EventDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	String eventType = IInjestServicesConstants.EMPTYSTR;
	String recordTs = IInjestServicesConstants.EMPTYSTR;
	String eventTs = IInjestServicesConstants.EMPTYSTR;
	
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
