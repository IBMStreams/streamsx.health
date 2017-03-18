// *******************************************************************************
// * Copyright (C) 2016 International Business Machines Corporation
// * All Rights Reserved
// *******************************************************************************

package com.ibm.streamsx.health.ingest.hl7.internal;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibm.streams.operator.log4j.TraceLevel;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;

public class HapiMessageHandler implements ReceivingApplication {
	
	private HapiServer server;
	
	private static final Logger TRACE = Logger.getLogger(HapiMessageHandler.class);
	
	public HapiMessageHandler(HapiServer server) {
		this.server = server;
	}

	@Override
	public boolean canProcess(Message arg0) {
		return true;
	}

	@Override
	public Message processMessage(Message theMessage, Map<String, Object> arg1)
			throws ReceivingApplicationException, HL7Exception {
		server.messageArrived(theMessage);
		Message ack;
		try {
			ack = theMessage.generateACK();
			return ack;
		} catch (IOException e) {
			TRACE.log(TraceLevel.ERROR, "Unable to generate ack message", e);
		}
		return theMessage;
	
		
	}

}
