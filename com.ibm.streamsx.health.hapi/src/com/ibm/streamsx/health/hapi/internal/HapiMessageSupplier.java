// *******************************************************************************
// * Copyright (C) 2016 International Business Machines Corporation
// * All Rights Reserved
// *******************************************************************************
package com.ibm.streamsx.health.hapi.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.ibm.streamsx.topology.function.Supplier;

import ca.uhn.hl7v2.model.Message;

public class HapiMessageSupplier implements Supplier<Message> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private transient HapiServer server;
	
	private int serverPort;
	
	public HapiMessageSupplier(int port) {
		
		serverPort = port;
	}
	
	private void initServer() {
		if (server == null)
		{
			server = new HapiServer();
			server.start(serverPort);
		}
	}

	@Override
	public Message get() {
		return server.getMessage();
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initServer();
	}
	
	@SuppressWarnings("unused")
	private void write(ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
	}
	

}
