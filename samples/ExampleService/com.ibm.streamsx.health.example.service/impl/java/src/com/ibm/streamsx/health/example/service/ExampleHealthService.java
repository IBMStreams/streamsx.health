package com.ibm.streamsx.health.example.service;

import com.ibm.streamsx.health.ingest.types.connector.SubscribeConnector;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;

public class ExampleHealthService extends AbstractService{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		ExampleHealthService service = new ExampleHealthService();
		service.run();
	}

	@Override
	protected Topology createTopology() {

		Topology topo = new Topology("ExampleHealthService");
		
		String topic = getProperties().getProperty("topic");	
		TStream<Observation> data = SubscribeConnector.subscribe(topo, topic);
		
		data.print();
		
		return topo;
	}

}
