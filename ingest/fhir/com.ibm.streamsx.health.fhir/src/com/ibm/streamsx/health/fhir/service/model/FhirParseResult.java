//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.service.model;

import java.io.Serializable;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;

import com.ibm.streamsx.health.ingest.types.model.Observation;

public class FhirParseResult implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<Observation> observations;
	
	private Exception exception;
	
	private BundleEntryComponent rawMessage;

	public List<Observation> getObservations() {
		return observations;
	}

	public void setObservations(List<Observation> observations) {
		this.observations = observations;
	}

	public Exception getException() {
		return exception;
	}

	public void setExceptions(Exception exceptions) {
		this.exception = exceptions;
	}

	public BundleEntryComponent getRawMessage() {
		return rawMessage;
	}

	public void setRawMessage(BundleEntryComponent rawMessage) {
		this.rawMessage = rawMessage;
	}
	
	

}
