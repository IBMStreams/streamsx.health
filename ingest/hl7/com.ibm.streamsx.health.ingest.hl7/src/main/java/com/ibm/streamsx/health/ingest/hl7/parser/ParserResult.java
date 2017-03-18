package com.ibm.streamsx.health.ingest.hl7.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ibm.streamsx.health.ingest.types.model.Observation;

import ca.uhn.hl7v2.model.Message;

public class ParserResult implements Serializable {

	private List<Observation> observations;
	private List<String> errorMessages;
	private transient Message hl7Message;
	
	public ParserResult(Message hl7Message) {
		this.hl7Message = hl7Message;
		this.observations = new ArrayList<Observation>();
		this.errorMessages = new ArrayList<String>();
	}
	
	public void addObservation(Observation observation) {
		this.observations.add(observation);
	}

	public void addObservations(List<Observation> observations) {
		this.observations.addAll(observations);
	}
	
	public void addErrorMesage(String message) {
		this.errorMessages.add(message);
	}
	
	public void setHl7Message(Message hl7Message) {
		this.hl7Message = hl7Message;
	}
	
	public List<String> getErrorMessages() {
		return errorMessages;
	}
	
	public Message getHl7Message() {
		return hl7Message;
	}
	
	public List<Observation> getObservations() {
		return observations;
	}
}
