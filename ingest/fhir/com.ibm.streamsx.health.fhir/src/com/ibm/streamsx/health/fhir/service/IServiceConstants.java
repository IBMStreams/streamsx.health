//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.service;

public interface IServiceConstants {

	// Topic to publish observation
	String FHIR_OBX_TOPIC="ingest-fhir-obx";
	
	// Topic to subscribe or publish patient ids in the form of ObxQueryParams
	String FHIR_OBX_PATIENTIDS_TOPIC="ingest-fhir-obx-patientIds";
	
	// Topic to report component bundles that cannot be parsed
	String FHIR_OBX_ERROR_TOPIC = "ingest-fhir-obx-error";
	
	// Keys in properties file
	String KEY_PATIENTIDS = "patientids";
	String KEY_PERIOD = "period";
	String KEY_BASEURL = "baseurl";
	String KEY_DEBUG = "debug";
	String KEY_STREAMS_CONTEXT = "streamscontext";
	String KEY_VMARGS = "vmargs";

}
