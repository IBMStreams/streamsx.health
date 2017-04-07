package com.ibm.streamsx.health.hapi.model;

import java.io.Serializable;

public class ADTEvent implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private MessageInfo msg;
	private Event evt;
	private Patient patient;
	private PatientVisit pv;
	
	public MessageInfo getMsg() {
		return msg;
	}
	public void setMsg(MessageInfo msg) {
		this.msg = msg;
	}
	public Event getEvt() {
		return evt;
	}
	public void setEvt(Event evt) {
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
