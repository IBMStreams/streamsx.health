//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************


package com.ibm.streamsx.health.hapi.model;

import java.io.Serializable;


/**
 * Represents a clinican 
 *
 */
public class Clinician implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/*
	 * Clinician's ID in system
	 */
	private String id = "";
	
	/*
	 * Clinician's name
	 */
	private String name = "";
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	
	
}
