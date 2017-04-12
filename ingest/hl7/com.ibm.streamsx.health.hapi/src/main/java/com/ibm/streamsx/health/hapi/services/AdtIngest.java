//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************


package com.ibm.streamsx.health.hapi.services;

import com.ibm.streamsx.health.hapi.internal.HapiMessageSupplier;
import com.ibm.streamsx.health.hapi.internal.PublishAdtEvent;
import com.ibm.streamsx.health.hapi.mapper.AdtToModelMapper;
import com.ibm.streamsx.health.ingest.types.model.ADTEvent;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.StreamsContext;
import com.ibm.streamsx.topology.context.StreamsContextFactory;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v26.message.ADT_AXX;

public class AdtIngest extends AbstractHL7Service {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {

		String port = System.getProperty("port", "8080");
		String topic = System.getProperty("topic", "adt");

		AdtIngest service = new AdtIngest();
		service.setPort(Integer.parseInt(port));
		service.setTopic(topic);
		service.run();
	}

	@Override
	public void run() {
		Topology topology = new Topology("AdtIngest");
		AdtToModelMapper mapper = new AdtToModelMapper();

		addDependencies(topology);
		
		HapiMessageSupplier supplier = new HapiMessageSupplier(getPort());
		supplier.setMessageClass(ADT_AXX.class);

		TStream<Message> messages = topology.endlessSource(supplier);
		
		
		// transform message to ADTEvent object
		TStream<ADTEvent> adtEvents = messages.multiTransform(message -> {
			return mapper.messageToModel(message);
		});
		
		
		// publish data as JSON
		PublishAdtEvent.publish(adtEvents, getTopic());

		try {
			StreamsContextFactory.getStreamsContext(StreamsContext.Type.DISTRIBUTED).submit(topology);
		} catch (Exception e) {
			TRACE.error("Unable to submit topology", e);
		}

	}
}
