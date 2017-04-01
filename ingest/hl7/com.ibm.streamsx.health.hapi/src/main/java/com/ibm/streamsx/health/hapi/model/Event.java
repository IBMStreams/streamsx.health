package com.ibm.streamsx.health.hapi.model;

import java.io.Serializable;

public class Event implements Serializable {

	String eventType;
	String recordTs;
	String eventTs;
}
