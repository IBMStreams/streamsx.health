// *******************************************************************************
// * Copyright (C) 2016 International Business Machines Corporation
// * All Rights Reserved
// *******************************************************************************
package com.ibm.streamsx.health.hapi.internal;

import java.io.IOException;
import java.util.concurrent.Executors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.ConnectionListener;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory;
import ca.uhn.hl7v2.validation.builder.support.NoValidationBuilder;

public class TestServer {
public static void main(String[] args) {
		
		try {
			HapiContext ctx = new DefaultHapiContext();
			CanonicalModelClassFactory mcf = new CanonicalModelClassFactory("2.6");
			ctx.setModelClassFactory(mcf);
			
			ctx.setValidationRuleBuilder(new NoValidationBuilder());
			ctx.setExecutorService(Executors.newCachedThreadPool());
			
			HL7Service server = ctx.newServer(8082, false);
			
			server.registerApplication(new HapiMessageHandler(null));
			server.registerConnectionListener(new ConnectionListener() {
				
				@Override
				public void connectionReceived(Connection c) {
					System.out.println("Connection received.");
					
				}
				
				@Override
				public void connectionDiscarded(Connection c) {
					System.out.println("Connection discarded.");
					
				}
			});
			
			server.startAndWait();
			
			ctx.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
