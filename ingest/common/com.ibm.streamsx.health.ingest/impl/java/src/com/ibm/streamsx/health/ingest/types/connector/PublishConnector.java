/* begin_generated_IBM_copyright_prolog                                       */
/*                                                                            */
/* This is an automatically generated copyright prolog.                       */
/* After initializing,  DO NOT MODIFY OR MOVE                                 */
/******************************************************************************/
/* Copyright (C) 2016 International Business Machines Corporation             */
/* All Rights Reserved                                                        */
/******************************************************************************/
/* end_generated_IBM_copyright_prolog                                         */
package com.ibm.streamsx.health.ingest.types.connector;

import java.io.ObjectStreamException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.streams.operator.OutputTuple;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.function.BiFunction;
import com.ibm.streamsx.topology.function.Function;
import com.ibm.streamsx.topology.json.JSONSchemas;
import com.ibm.streamsx.topology.spl.SPLStream;
import com.ibm.streamsx.topology.spl.SPLStreams;

public class PublishConnector<T> {

	private AbstractObservationMapper<T> mapper;
	private String publishTopic;
		
	public PublishConnector(AbstractObservationMapper<T> mapper, String publishTopic) {
		this.mapper = mapper;
		this.publishTopic = publishTopic;
	}
	
	public void mapAndPublish(TStream<T> inputStream) {
		TStream<String> jsonStream = inputStream.multiTransform(mapper).transform(new ObservationToJsonConverter());
		SPLStream splStream = SPLStreams.convertStream(jsonStream, new GsonToSPLConverter(), JSONSchemas.JSON);
		//splStream.print();
		splStream.publish(publishTopic);
	}
	
	private static class GsonToSPLConverter implements BiFunction<String, OutputTuple, OutputTuple> {

		private static final long serialVersionUID = 1L;

		@Override
		public OutputTuple apply(String gsonStr, OutputTuple outTuple) {
			outTuple.setString("jsonString", gsonStr);
			return outTuple;
		}		
	}
	
	private static class ObservationToJsonConverter implements Function<Observation, String> {

		private static final long serialVersionUID = 1L;

		private transient Gson gson;

		public ObservationToJsonConverter() {
			initGson();
		}

		private void initGson() {
			GsonBuilder builder = new GsonBuilder();
			gson = builder.create();
		}

		@Override
		public String apply(Observation v) {
			return gson.toJson(v);
		}

		public Object readResolve() throws ObjectStreamException {
			initGson();
			return this;
		}
	}

}
