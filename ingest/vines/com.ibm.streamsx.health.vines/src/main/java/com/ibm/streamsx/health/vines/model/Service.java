package com.ibm.streamsx.health.vines.model;

import java.io.Serializable;

public class Service implements Serializable {

	private static final long serialVersionUID = 1L;

	private String Code;
	
	public String getCode() {
		return Code;
	}

	@Override
	public String toString() {
		return "Service [code=" + Code + "]";
	}
	
	
	
}
