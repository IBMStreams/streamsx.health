//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.model;

import java.io.Serializable;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;

import com.ibm.streamsx.health.ingest.types.model.Observation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

public class ObxParseResult implements Serializable{
	
	private static FhirContext ctx = FhirContext.forDstu3();
	private static IParser jsonParser = ctx.newJsonParser();

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<Observation> observations;
	
	private Exception exception;
	
	
	private BundleEntryComponent bundle;

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
		return jsonParser.encodeResourceToString(getBundle().getResource());
	}
	
	public ObxParseResult setBundle(BundleEntryComponent bundle) {
		this.bundle = bundle;
		return this;
	}
	
	public BundleEntryComponent getBundle() {
		return bundle;
	}
	
	

}
