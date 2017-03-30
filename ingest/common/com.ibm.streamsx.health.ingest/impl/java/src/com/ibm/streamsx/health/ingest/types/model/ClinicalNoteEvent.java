package com.ibm.streamsx.health.ingest.types.model;

import java.io.Serializable;

public class ClinicalNoteEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private String patientId;
	private String eventType;
	private String eventName;
	private long ts;
	private double value;
	private String uom;
	private String source;

	/**
	 * Default constructor
	 */
	public ClinicalNoteEvent() {
		
	}
	
	/**
	 * Copy constructor
	 * @param event Original ClinicalNoteEvent to copy from
	 */
	public ClinicalNoteEvent(ClinicalNoteEvent event) {
		this.patientId = event.patientId;
		this.eventType = event.eventType;
		this.eventName = event.eventName;
		this.ts =  event.ts;
		this.value = event.value;
		this.uom = event.uom;
		this.source = event.source;
	}
	
	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public long getTs() {
		return ts;
	}

	public void setTs(long ts) {
		this.ts = ts;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getUom() {
		return uom;
	}

	public void setUom(String uom) {
		this.uom = uom;
	}

	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
}
