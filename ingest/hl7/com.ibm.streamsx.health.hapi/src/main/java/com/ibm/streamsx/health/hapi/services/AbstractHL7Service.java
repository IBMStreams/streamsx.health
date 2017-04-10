//*******************************************************************************
//* Copyright (C) 2016 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************
package com.ibm.streamsx.health.hapi.services;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.ibm.streamsx.topology.Topology;

abstract public class AbstractHL7Service implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static Logger TRACE = Logger.getLogger(AbstractHL7Service.class);
	
	private int port;
	private String topic;
	
	public AbstractHL7Service() {
		// TODO Auto-generated constructor stub
	}
	

	protected void addDependencies(Topology topology) {
		topology.addJarDependency("opt/lib/hapi-base-2.2.jar");
		topology.addJarDependency("opt/lib/hapi-structures-v21-2.2.jar");
		topology.addJarDependency("opt/lib/hapi-structures-v22-2.2.jar");
		topology.addJarDependency("opt/lib/hapi-structures-v23-2.2.jar");
		topology.addJarDependency("opt/lib/hapi-structures-v231-2.2.jar");
		topology.addJarDependency("opt/lib/hapi-structures-v24-2.2.jar");
		topology.addJarDependency("opt/lib/hapi-structures-v25-2.2.jar");
		topology.addJarDependency("opt/lib/hapi-structures-v251-2.2.jar");
		topology.addJarDependency("opt/lib/hapi-structures-v26-2.2.jar");
		topology.addJarDependency("opt/lib/log4j-1.2.17.jar");
		topology.addJarDependency("opt/lib/slf4j-api-1.6.6.jar");
		topology.addJarDependency("opt/lib/slf4j-log4j12-1.6.6.jar");
		topology.addJarDependency("opt/lib/gson-1.7.2.jar");
		topology.addJarDependency("opt/lib/gson-1.7.2.jar");
		topology.addJarDependency("opt/lib/com.ibm.streamsx.health.ingest.jar");
	}
	
	protected int getPort(){
		return this.port;
	}
	
	protected String getTopic() {
		return this.topic;
	}
	
	protected void setPort(int port)
	{
		this.port = port;
	}
	
	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public abstract void run();

}
