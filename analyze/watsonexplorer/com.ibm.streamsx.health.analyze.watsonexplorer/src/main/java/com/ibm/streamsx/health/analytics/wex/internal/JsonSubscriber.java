package com.ibm.streamsx.health.analytics.wex.internal;

import java.io.Serializable;

import com.ibm.streams.operator.Tuple;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.function.Function;
import com.ibm.streamsx.topology.json.JSONSchemas;
import com.ibm.streamsx.topology.spl.SPLStream;
import com.ibm.streamsx.topology.spl.SPLStreams;

public class JsonSubscriber implements Serializable {
	private static final long serialVersionUID = 1L;

	public static TStream<String> subscribe(Topology topo, String topic) {
		SPLStream splStream = SPLStreams.subscribe(topo, topic, JSONSchemas.JSON);
		TStream<String> stringStream = splStream.transform(new SplToTStream());
		
		return stringStream;
	}
	
	private static class SplToTStream implements Function<Tuple, String> {
		private static final long serialVersionUID = 1L;

		@Override
		public String apply(Tuple tuple) {
			return tuple.getString(0);
		}
		
	}
}
