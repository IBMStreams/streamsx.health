//*******************************************************************************
//* Copyright (C) 2016 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.hapi.services;

import com.ibm.streams.operator.OutputTuple;
import com.ibm.streams.operator.StreamSchema;
import com.ibm.streams.operator.Type;
import com.ibm.streamsx.health.hapi.internal.HapiMessageSupplier;
import com.ibm.streamsx.health.hapi.mapper.ObxToSplMapper;
import com.ibm.streamsx.health.hapi.model.Observation;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.StreamsContext;
import com.ibm.streamsx.topology.context.StreamsContextFactory;
import com.ibm.streamsx.topology.function.BiFunction;
import com.ibm.streamsx.topology.spl.SPLStream;
import com.ibm.streamsx.topology.spl.SPLStreams;

import ca.uhn.hl7v2.model.Message;

public class OruR01Ingest extends AbstractHL7Service {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		
		String port = System.getProperty("port", "8080");
		String topic = System.getProperty("topic", "oru01");
		
		OruR01Ingest service = new OruR01Ingest();
		service.setPort(Integer.parseInt(port));
		service.setTopic(topic);
		service.run();
	}
	
	@Override
	public void run() {
		Topology topology = new Topology("OruR01Ingest");
		ObxToSplMapper mapper = new ObxToSplMapper();

		addDependencies(topology);

		
		TStream<Message> messages = topology.endlessSource(new HapiMessageSupplier(getPort()));
		
		// transform message to Observation object
		
		TStream<Observation> observationStream = messages.multiTransform(message -> {
			return mapper.messageToModel(message);			
		});
		
		StreamSchema schema = Type.Factory.getStreamSchema(Observation.OBSERVATION_SCHEMA_SPL);
		
		SPLStream splObservations = SPLStreams.convertStream(observationStream, new BiFunction<Observation, OutputTuple, OutputTuple>() {

			@Override
			public OutputTuple apply(Observation observation, OutputTuple outTuple) {
				return mapper.modelToSpl(observation, outTuple);
			}
		}, schema);

		splObservations.print();
		
		splObservations.publish(getTopic());
		
		try {
			StreamsContextFactory.getStreamsContext(StreamsContext.Type.DISTRIBUTED).submit(topology);
		} catch (Exception e) {
			TRACE.error("Unable to submit topology", e);
		}
		
	}
}
