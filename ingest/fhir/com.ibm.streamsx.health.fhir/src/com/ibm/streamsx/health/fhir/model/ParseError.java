package com.ibm.streamsx.health.fhir.model;

import java.io.Serializable;

public class ParseError implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String exception;
	private String rawMessage;
	
	
	
	public ParseError setException(String exception) {
		this.exception = exception;
		return this;
	}
	public ParseError setRawMessage(String rawMessage) {
		this.rawMessage = rawMessage;
		return this;
	}
	public String getException() {
		return exception;
	}
	public String getRawMessage() {
		return rawMessage;
	}

}
