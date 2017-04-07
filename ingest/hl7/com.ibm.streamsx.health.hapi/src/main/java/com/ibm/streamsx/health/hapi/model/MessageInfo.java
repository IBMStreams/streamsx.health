package com.ibm.streamsx.health.hapi.model;

import java.io.Serializable;

public class MessageInfo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	String sendingApp = IHL7Constants.EMPTYSTR;
	String sendingFacility = IHL7Constants.EMPTYSTR;
	String receivingApp = IHL7Constants.EMPTYSTR;
	String receivingFacility = IHL7Constants.EMPTYSTR;
	String messageTs = IHL7Constants.EMPTYSTR;
	String messageType = IHL7Constants.EMPTYSTR;
	
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
