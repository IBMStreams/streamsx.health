//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************


package com.ibm.streamsx.health.ingest.types.model;

import java.io.Serializable;

/*
 * Top level object encapsulating an ADT event
 */
public class ADTEvent implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private MessageInfo msg;
	private EventDetails evt;
	private Patient patient;
	private PatientVisit pv;
	
	public MessageInfo getMsg() {
		return msg;
	}
	public void setMsg(MessageInfo msg) {
		this.msg = msg;
	}
	public EventDetails getEvt() {
		return evt;
	}
	public void setEvt(EventDetails evt) {
		this.evt = evt;
	}
	public Patient getPatient() {
		return patient;
	}
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	public PatientVisit getPv() {
		return pv;
	}
	public void setPv(PatientVisit pv) {
		this.pv = pv;
	}

	
	
}
