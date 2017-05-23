package com.ibm.streamsx.health.hapi.internal;

import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.SimpleServer;

public class StreamsHL7Service extends SimpleServer {

	public StreamsHL7Service(HapiContext hapiContext, int port, boolean tls) {
		super(hapiContext, port, tls);
	}

	@Override
	protected void handle() {
		super.handle();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			
		}

	}

}
