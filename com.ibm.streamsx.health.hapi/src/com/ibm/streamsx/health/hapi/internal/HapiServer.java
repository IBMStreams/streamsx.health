// *******************************************************************************
// * Copyright (C) 2016 International Business Machines Corporation
// * All Rights Reserved
// *******************************************************************************
package com.ibm.streamsx.health.hapi.internal;

import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

import com.ibm.streams.operator.log4j.TraceLevel;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory;
import ca.uhn.hl7v2.validation.builder.support.NoValidationBuilder;

public class HapiServer {
	private static final boolean TLS=false;
	private static final String VERSION  = "2.6";
	
	private static final Logger TRACE = Logger.getLogger(HapiServer.class);
	
	private HapiContext context;
	private HL7Service server;
	private ArrayBlockingQueue<Message> messageQueue;
	
	public HapiServer() {
		context = new DefaultHapiContext();		
		CanonicalModelClassFactory mcf = new CanonicalModelClassFactory(VERSION);
		context.setModelClassFactory(mcf);
		context.setValidationRuleBuilder(new NoValidationBuilder());
		
		
		messageQueue = new ArrayBlockingQueue<>(100);
	}
	
	public void start(int port)
	{
		TRACE.debug("Listening on " + port);
		server = context.newServer(port, TLS);
		// start server and set up receiving application
		server.registerApplication(new HapiMessageHandler(this));
		server.start();
	}
	
	public void stop()
	{
		server.stop();
	}
	
	public void messageArrived(Message message)
	{
		try {
			messageQueue.put(message);
		} catch (InterruptedException e) {
			TRACE.log(TraceLevel.ERROR, "Unable to write to message queue.", e);
		}
	}
	
	public Message getMessage()
	{
		return messageQueue.poll();
	}
}
