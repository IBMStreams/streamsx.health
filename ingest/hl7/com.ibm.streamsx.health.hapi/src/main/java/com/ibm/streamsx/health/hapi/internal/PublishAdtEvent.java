package com.ibm.streamsx.health.hapi.internal;

import com.ibm.streamsx.health.ingest.types.connector.JsonPublisher;
import com.ibm.streamsx.health.ingest.types.connector.ModelToJsonConverter;
import com.ibm.streamsx.health.ingest.types.model.ADTEvent;
import com.ibm.streamsx.topology.TStream;

public class PublishAdtEvent {
	
	public static <T> void publish(TStream<T> inputStream, String publishTopic) {
		TStream<String> jsonStream = inputStream.multiTransform(new IdentityMapper<>()).transform(new ModelToJsonConverter<ADTEvent>());
		JsonPublisher.publish(jsonStream, publishTopic);
	}

}
