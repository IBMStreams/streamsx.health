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
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.function.Function;

public class PublishConnector {

	public static void publishObservation(TStream<Observation> inputStream, String publishTopic) {
		mapAndPublish(inputStream, new IdentityMapper(), publishTopic);
	}
	
	public static <T> void mapAndPublish(TStream<T> inputStream, AbstractObservationMapper<T> mapper, String publishTopic) {
		TStream<String> jsonStream = inputStream.multiTransform(mapper).transform(new ObservationToJsonConverter());
		JsonPublisher.publish(jsonStream, publishTopic);
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
