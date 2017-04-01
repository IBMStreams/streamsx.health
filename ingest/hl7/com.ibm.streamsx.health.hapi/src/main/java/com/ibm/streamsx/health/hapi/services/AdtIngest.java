package com.ibm.streamsx.health.hapi.services;

import com.ibm.streamsx.health.hapi.internal.HapiMessageSupplier;
import com.ibm.streamsx.health.hapi.mapper.ObxToSplMapper;
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
		String topic = System.getProperty("topic", "oru01");

		AdtIngest service = new AdtIngest();
		service.setPort(Integer.parseInt(port));
		service.setTopic(topic);
		service.run();
	}

	@Override
	public void run() {
		Topology topology = new Topology("AdtIngest");
		ObxToSplMapper mapper = new ObxToSplMapper();

		addDependencies(topology);
		
		HapiMessageSupplier supplier = new HapiMessageSupplier(getPort());
		supplier.setMessageClass(ADT_AXX.class);

		TStream<Message> messages = topology.endlessSource(supplier);
		
		messages.print();

		// transform message to Observation object

//		TStream<Observation> observationStream = messages.multiTransform(message -> {
//			return mapper.messageToModel(message);
//		});
//
//		StreamSchema schema = Type.Factory.getStreamSchema(Observation.OBSERVATION_SCHEMA_SPL);
//
//		@SuppressWarnings("serial")
//		SPLStream splObservations = SPLStreams.convertStream(observationStream,
//				new BiFunction<Observation, OutputTuple, OutputTuple>() {
//
//					@Override
//					public OutputTuple apply(Observation observation, OutputTuple outTuple) {
//						return mapper.modelToSpl(observation, outTuple);
//					}
//				}, schema);
//
//		splObservations.print();
//
//		splObservations.publish(getTopic());

		try {
			StreamsContextFactory.getStreamsContext(StreamsContext.Type.DISTRIBUTED).submit(topology);
		} catch (Exception e) {
			TRACE.error("Unable to submit topology", e);
		}

	}
}
