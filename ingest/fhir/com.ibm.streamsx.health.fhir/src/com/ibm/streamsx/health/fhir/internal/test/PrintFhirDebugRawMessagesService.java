//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************
package com.ibm.streamsx.health.fhir.internal.test;

import com.ibm.streamsx.health.fhir.connector.FhirObxConnector;
import com.ibm.streamsx.health.fhir.service.AbstractFhirService;
import com.ibm.streamsx.health.fhir.service.IServiceConstants;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;

/* 
 * This is an internal service for testing the debug connector
 */
public class PrintFhirDebugRawMessagesService extends AbstractFhirService {

	/**
	 * 
	 */ 
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		PrintFhirDebugRawMessagesService service = new PrintFhirDebugRawMessagesService();
		service.run();
	}

	@Override
	protected Topology createTopology() {
		Topology topology = new Topology("FhirDebugRawMessages");

		TStream<String> rawMessages = FhirObxConnector.subscribeDebug(topology, IServiceConstants.FHIR_OBX_DEBUG_TOPIC);
		rawMessages.print();

		return topology;
	}

}
