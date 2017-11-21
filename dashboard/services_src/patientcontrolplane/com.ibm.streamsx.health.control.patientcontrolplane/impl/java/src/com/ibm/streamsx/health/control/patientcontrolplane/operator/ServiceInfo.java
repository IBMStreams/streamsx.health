package com.ibm.streamsx.health.control.patientcontrolplane.operator;

import java.math.BigInteger;

import com.google.gson.Gson;

public class ServiceInfo{
	private static transient Gson gson = new Gson();
	
	public static enum Status {
		RUNNING,
		STOPPED;
	}
	
	String jobId;
	String jobName;
	String status;
	String serviceType;
	
	public ServiceInfo(BigInteger jobId, String jobName, Status status, ServiceType serviceType) {
		setJobId(jobId.toString());
		setJobName(jobName);
		setStatus(status);
		setServiceType(serviceType);
	}

	public void setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType.name().toLowerCase();
	}
	
	public void setStatus(Status status) {
		this.status = status.name().toLowerCase();
	}
	
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	@Override
	public String toString() {
		return gson.toJson(this);
	}
}
