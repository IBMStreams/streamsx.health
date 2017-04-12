package com.ibm.streamsx.health.hapi.internal;

import com.ibm.streamsx.health.ingest.types.connector.JsonToModelConverter;
import com.ibm.streamsx.health.ingest.types.model.ADTEvent;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.TopologyElement;
import com.ibm.streamsx.topology.json.JSONSchemas;
import com.ibm.streamsx.topology.spl.SPLStreams;

public class SubscribeAdtEvent {
	
	public static TStream<ADTEvent> subscribe(TopologyElement topo, String topic) {
		return SPLStreams.subscribe(topo, topic, JSONSchemas.JSON)
				.transform(new JsonToModelConverter<ADTEvent>(ADTEvent.class));
	}
}
