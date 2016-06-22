// *******************************************************************************
// * Copyright (C) 2016 International Business Machines Corporation
// * All Rights Reserved
// *******************************************************************************

package com.ibm.streamsx.health.hapi.internal;

import java.io.IOException;
import java.util.Map;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;

public class HapiMessageHandler implements ReceivingApplication {
	
	private HapiServer server;
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return theMessage;
	
		
	}

}
