package com.ibm.streamsx.health.hapi.internal;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.app.SimpleServer;

public class StreamsHapiContext extends DefaultHapiContext {
			
		@Override
		public SimpleServer newServer(int port, boolean tls) {
			return new StreamsHL7Service(this, port, tls);
		}
}
