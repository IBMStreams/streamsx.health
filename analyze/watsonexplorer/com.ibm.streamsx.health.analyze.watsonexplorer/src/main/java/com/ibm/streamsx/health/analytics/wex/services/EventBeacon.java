package com.ibm.streamsx.health.analytics.wex.services;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.ibm.streamsx.health.analytics.wex.internal.JsonPublisher;
import com.ibm.streamsx.health.analytics.wex.internal.JsonSubscriber;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.StreamsContext.Type;
import com.ibm.streamsx.topology.context.StreamsContextFactory;
import com.ibm.streamsx.topology.function.Supplier;

public class EventBeacon implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String EVENT_BEACON_TOPIC = "event-beacon";
	
	private Topology t;
	
	public EventBeacon(String subscriptionTopic) {
		t = new Topology("EventBeacon");
		
		TStream<String> queryStream = t.endlessSource(new Source()).throttle(1, TimeUnit.SECONDS);
		
		JsonPublisher.publish(queryStream, EVENT_BEACON_TOPIC);		
		JsonSubscriber.subscribe(t, subscriptionTopic).print();
	}
	
	public void run() throws Exception {
		Map<String, Object> config = new HashMap<String, Object>();
		StreamsContextFactory.getStreamsContext(Type.DISTRIBUTED).submit(t, config).get();
	}
	
	private static class Source implements Supplier<String> {
		private static final long serialVersionUID = 1L;

		@Override
		public String get() {
			return "{ \"patientId\" : \"9261417\", \"startDate\" : \"2016-01-29\", \"endDate\" : \"2016-02-04\"}";
		}
	}
}
