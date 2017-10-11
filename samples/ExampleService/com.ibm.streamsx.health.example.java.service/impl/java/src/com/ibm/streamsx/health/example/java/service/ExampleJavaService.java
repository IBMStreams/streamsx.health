//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.example.java.service;

import com.ibm.streamsx.health.ingest.types.connector.PublishConnector;
import com.ibm.streamsx.health.ingest.types.connector.SubscribeConnector;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;

public class ExampleJavaService extends AbstractService{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	
	public static void main(String[] args) {
		ExampleJavaService service = new ExampleJavaService();
		service.run();
	}

	@Override
	protected Topology createTopology() {

		Topology topo = new Topology("ExampleHealthService");
		
		String topic = getProperties().getProperty(IServiceConstants.KEY_TOPIC);	
		TStream<Observation> data = SubscribeConnector.subscribe(topo, topic);
		
		data.print();
		
		PublishConnector.publishObservation(data, IServiceConstants.PUBLISH_OBSERVATION);
		
		return topo;
	}

}
