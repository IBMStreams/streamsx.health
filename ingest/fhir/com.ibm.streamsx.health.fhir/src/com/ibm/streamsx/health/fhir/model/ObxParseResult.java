//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.model;

import java.io.Serializable;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;

import com.ibm.streamsx.health.ingest.types.model.Observation;

public class ObxParseResult implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<Observation> observations;
	
	private Exception exception;
	
	private String rawMessage;

	public List<Observation> getObservations() {
		return observations;
	}

	public ObxParseResult setObservations(List<Observation> observations) {
		this.observations = observations;
		return this;
	}

	public Exception getException() {
		return exception;
	}

	public ObxParseResult setExceptions(Exception exceptions) {
		this.exception = exceptions;
		return this;
	}

	public String getRawMessage() {
		return rawMessage;
	}

	public ObxParseResult setRawMessage(String rawMessage) {
		this.rawMessage = rawMessage;
		return this;
	}
	
	

}
