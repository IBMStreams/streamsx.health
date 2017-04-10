//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************


package com.ibm.streamsx.health.ingest.types.model;

import java.io.Serializable;

/*
 * Represents message header from an ADT message
 */
public class MessageInfo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	String sendingApp = IInjestServicesConstants.EMPTYSTR;
	String sendingFacility = IInjestServicesConstants.EMPTYSTR;
	String receivingApp = IInjestServicesConstants.EMPTYSTR;
	String receivingFacility = IInjestServicesConstants.EMPTYSTR;
	String messageTs = IInjestServicesConstants.EMPTYSTR;
	String messageType = IInjestServicesConstants.EMPTYSTR;
	
	public String getSendingApp() {
		return sendingApp;
	}
	public void setSendingApp(String sendingApp) {
		this.sendingApp = sendingApp;
	}
	public String getSendingFacility() {
		return sendingFacility;
	}
	public void setSendingFacility(String sendingFacility) {
		this.sendingFacility = sendingFacility;
	}
	public String getReceivingApp() {
		return receivingApp;
	}
	public void setReceivingApp(String receivingApp) {
		this.receivingApp = receivingApp;
	}
	public String getReceivingFacility() {
		return receivingFacility;
	}
	public void setReceivingFacility(String receivingFacility) {
		this.receivingFacility = receivingFacility;
	}
	public String getMessageTs() {
		return messageTs;
	}
	public void setMessageTs(String messageTs) {
		this.messageTs = messageTs;
	}
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

}
