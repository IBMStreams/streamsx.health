//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.example.java.service;

public interface IServiceConstants {
	
	String PREFIX = "com.ibm.streamsx.health.example.java.service";
	
	// Global Properties
	String KEY_DEBUG = "debug";
	String KEY_STREAMS_CONTEXT = "streamscontext";
	String KEY_VMARGS = "vmargs";
	
	// Properties specific to this service.  Property start with package prefix
	String KEY_TOPIC = PREFIX + ".topic";
	
	// Topic should follow MQTT-Style.  It is recommended each data stream end with
	// a version number.  This helps to maintain backwards compatibility.  
	// Clients subscribe by using the following topic filter:  /com/ibm/streamsx/health/example/java/observation/v1/#
	//
	// If changes in data schema are backwards compatible (e.g. additional of new attributes), the service
	// will increment the version number to a/b/c/v1/1, a/b/c/c/v1/2, etc.  
	// 
	// If changes in data schema are not backwards compatible,
	// (e.g. changing of data types or removal of attributes), then the service may update the data
	// stream version number by incrementing the major version as followings:  a/b/c/v1 becomes a/b/c/v2.
	//
	// Clients may choose to subscribe to a newer version of data stream when they choose to
	// when it becomes available.
	String PUBLISH_OBSERVATION = "/com/ibm/streamsx/health/example/java/service/observation/v1";

}
