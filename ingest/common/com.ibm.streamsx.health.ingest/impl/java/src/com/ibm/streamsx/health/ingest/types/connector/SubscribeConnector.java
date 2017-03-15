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
import com.ibm.streams.operator.Tuple;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.TopologyElement;
import com.ibm.streamsx.topology.function.Function;
import com.ibm.streamsx.topology.json.JSONSchemas;
import com.ibm.streamsx.topology.spl.SPLStreams;

public class SubscribeConnector {
	
	public static TStream<Observation> subscribe(TopologyElement te, String topic) {
		return SPLStreams.subscribe(te, topic, JSONSchemas.JSON)
				.transform(new JsonToObservationConverter());
	}
	
	private static class JsonToObservationConverter implements Function<Tuple, Observation> {

		private static final long serialVersionUID = 1L;

		private transient Gson gson;

		public JsonToObservationConverter() {
			initGson();
		}

		private void initGson() {
			GsonBuilder builder = new GsonBuilder();
			gson = builder.create();
		}

		@Override
		public Observation apply(Tuple v) {
			return gson.fromJson(v.getString("jsonString"), Observation.class);
		}

		public Object readResolve() throws ObjectStreamException {
			initGson();
			return this;
		}
		
	}
}
