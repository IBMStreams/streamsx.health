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
	private Class<? extends Message> messageClass;
	
	public HapiMessageSupplier(int port) {
		
		serverPort = port;
	}
	
	private void initServer() {
		if (server == null)
		{
			if (messageClass != null)
				server = new HapiServer(messageClass);
			else
				server = new HapiServer();
			
			System.out.println("Listening on: " + serverPort);
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
	
	public void setMessageClass(Class<? extends Message> class1) {
		this.messageClass = class1;
	}
	

}
