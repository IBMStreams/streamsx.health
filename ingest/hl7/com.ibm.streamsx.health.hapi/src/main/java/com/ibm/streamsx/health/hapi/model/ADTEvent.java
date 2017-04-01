package com.ibm.streamsx.health.hapi.model;

import java.io.Serializable;

public class ADTEvent implements Serializable{
	
	private Message msg;
	private Event evt;
	private Patient patient;
	private PatientVisit pv;

}
