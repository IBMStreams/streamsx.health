package com.ibm.streamsx.health.hapi.model;

import java.io.Serializable;

public class Message implements Serializable {
	
	String sendingApp;
	String sendingFacility;
	String receivingApp;
	String receivingFacility;
	String messageTs;
	String messageType;

}
