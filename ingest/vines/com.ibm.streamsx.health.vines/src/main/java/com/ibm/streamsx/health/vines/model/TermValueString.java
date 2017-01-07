package com.ibm.streamsx.health.vines.model;

import java.io.Serializable;

public class TermValueString implements ITermValue, Serializable {

	private static final long serialVersionUID = 1L;

	private String value;
	
	public TermValueString(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "TermValueString [value=" + value + "]";
	}
	
	
}
