package com.ibm.streamsx.health.hapi.internal;

import java.io.ObjectStreamException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.streamsx.health.hapi.model.ADTEvent;
import com.ibm.streamsx.health.ingest.types.connector.JsonPublisher;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.function.Function;

public class PublishAdtEvent {
	
	public static void publishAdtEvents(TStream<ADTEvent> inputStream, String publishTopic) {
		mapAndPublish(inputStream, publishTopic);
	}
	
	public static <T> void mapAndPublish(TStream<T> inputStream, String publishTopic) {
		TStream<String> jsonStream = inputStream.multiTransform(new IdentityMapper<>()).transform(new AdtToJson());
		JsonPublisher.publish(jsonStream, publishTopic);
	}
	
	private static class AdtToJson implements Function<ADTEvent, String> {

		private static final long serialVersionUID = 1L;

		private transient Gson gson;

		public AdtToJson() {
			initGson();
		}

		private void initGson() {
			GsonBuilder builder = new GsonBuilder();
			gson = builder.create();
		}

		@Override
		public String apply(ADTEvent v) {
			return gson.toJson(v);
		}

		public Object readResolve() throws ObjectStreamException {
			initGson();
			return this;
		}
	}

}
