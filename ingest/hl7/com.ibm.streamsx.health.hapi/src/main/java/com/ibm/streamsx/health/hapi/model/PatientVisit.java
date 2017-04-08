//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************


package com.ibm.streamsx.health.hapi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/* 
 * Represents patient's visit event
 */
public class PatientVisit implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String patientClass = IHL7Constants.EMPTYSTR;
	private String location = IHL7Constants.EMPTYSTR;
	private String priorLocation = IHL7Constants.EMPTYSTR;
	private String visitNumber = IHL7Constants.EMPTYSTR;
	private List<Clinician> attendingDoctors = new ArrayList<Clinician>();
	private List<Clinician> consultingDoctors = new ArrayList<Clinician>();
	
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
	public List<Clinician> getAttendingDoctors() {
		return attendingDoctors;
	}
	public void setAttendingDoctors(List<Clinician> attendingDoctors) {
		this.attendingDoctors = attendingDoctors;
	}
	public List<Clinician> getConsultingDoctors() {
		return consultingDoctors;
	}
	
	public void setConsultingDoctors(List<Clinician> consultingDoctors) {
		this.consultingDoctors = consultingDoctors;
	}
	
	public void addAttendingDoctor(Clinician doctor) {
		attendingDoctors.add(doctor);
	}
	
	public void removeAttendingDoctor(Clinician doctor) {
		attendingDoctors.remove(doctor);		
	}
	
	public void addConsultingDoctor(Clinician doctor) {
		consultingDoctors.add(doctor);
	}
	
	public void removeConsultingDoctor(Clinician doctor) {
		consultingDoctors.remove(doctor);
	}
	
	
	

}
