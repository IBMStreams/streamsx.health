package com.ibm.streamsx.health.fhir.service.model;

import java.io.Serializable;

public class ObxQueryParams implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String patientId;
	private int pageSize = 100;
	private long startTime = -1;
	private long endTime = -1;
	
	public String getPatientId() {
		return patientId;
	}
	public ObxQueryParams setPatientId(String patientId) {
		this.patientId = patientId;
		return this;
	}
	public int getPageSize() {
		return pageSize;
	}
	public ObxQueryParams setPageSize(int pageSize) {
		this.pageSize = pageSize;
		return this;
	}
	public long getStartTime() {
		return startTime;
	}
	public ObxQueryParams setStartTime(long startTime) {
		this.startTime = startTime;
		return this;
	}
	public long getEndTime() {
		return endTime;
	}
	public ObxQueryParams setEndTime(long endTime) {
		this.endTime = endTime;
		return this;
	}
	
	@Override
	public String toString() {
		return "PatientId: " + patientId + " pageSize: " + pageSize + " startTime" + startTime + " endTime" + endTime;
	}
	
	

}
