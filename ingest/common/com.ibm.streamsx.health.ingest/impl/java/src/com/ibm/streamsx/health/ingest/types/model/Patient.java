//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************


package com.ibm.streamsx.health.ingest.types.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
 * Represents a patient 
 */
public class Patient implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private String id = IInjestServicesConstants.EMPTYSTR;
	private List<String> alternateIds = new ArrayList<String>();
	private String name = IInjestServicesConstants.EMPTYSTR;
	private List<String> alternateNames = new ArrayList<String>();
	
	private String gender = IInjestServicesConstants.EMPTYSTR;
	private String dateOfBirth = IInjestServicesConstants.EMPTYSTR;
	
	/*
	 * Bed status from PV1 if available
	 */
	private String status = IInjestServicesConstants.EMPTYSTR;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<String> getAlternateIds() {
		return alternateIds;
	}
	
	public void setAlternateIds(List<String> alternateIds) {
		this.alternateIds = alternateIds;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getDateOfBirth() {
		return dateOfBirth;
	}
	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public void addAlternativeId(String id)
	{
		alternateIds.add(id);
	}
	public List<String> getAlternateNames() {
		return alternateNames;
	}
	public void setAlternateNames(List<String> alternateNames) {
		this.alternateNames = alternateNames;
	}
	
	public void addAlternateName(String name)
	{
		this.alternateNames.add(name);
	}

}
