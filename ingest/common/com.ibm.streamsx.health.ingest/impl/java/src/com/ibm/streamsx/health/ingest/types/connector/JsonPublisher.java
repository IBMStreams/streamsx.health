package com.ibm.streamsx.health.ingest.types.connector;

import java.io.Serializable;

import com.ibm.streams.operator.OutputTuple;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.function.BiFunction;
import com.ibm.streamsx.topology.json.JSONSchemas;
import com.ibm.streamsx.topology.spl.SPLStream;
import com.ibm.streamsx.topology.spl.SPLStreams;

public class JsonPublisher implements Serializable {
	private static final long serialVersionUID = 1L;

	public static void publish(TStream<String> jsonInputStream, String topic) {
		SPLStream splStream = SPLStreams.convertStream(jsonInputStream, new JsonToSpl(), JSONSchemas.JSON);
		splStream.publish(topic);
	}
	
	private static class JsonToSpl implements BiFunction<String, OutputTuple, OutputTuple> {
		private static final long serialVersionUID = 1L;

		@Override
		public OutputTuple apply(String jsonString, OutputTuple outTuple) {
			outTuple.setString(0, jsonString);
			return outTuple;
		}
		
	}
}
