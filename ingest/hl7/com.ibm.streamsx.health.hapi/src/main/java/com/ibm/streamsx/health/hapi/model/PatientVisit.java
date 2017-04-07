package com.ibm.streamsx.health.hapi.model;

import java.io.Serializable;

public class PatientVisit implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String patientClass = IHL7Constants.EMPTYSTR;
	private String location = IHL7Constants.EMPTYSTR;
	private String priorLocation = IHL7Constants.EMPTYSTR;
	private String visitNumber = IHL7Constants.EMPTYSTR;
	
	public String getPatientClass() {
		return patientClass;
	}
	public void setPatientClass(String patientClass) {
		this.patientClass = patientClass;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getPriorLocation() {
		return priorLocation;
	}
	public void setPriorLocation(String priorLocation) {
		this.priorLocation = priorLocation;
	}
	public String getVisitNumber() {
		return visitNumber;
	}
	public void setVisitNumber(String visitNumber) {
		this.visitNumber = visitNumber;
	}

}
