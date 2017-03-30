package com.ibm.streamsx.health.ingest.types.model;

import java.io.Serializable;

public class ReadingType implements Serializable {
	private static final long serialVersionUID = 1L;

	private String system;
	private String code;

	public ReadingType() {

	}

	public ReadingType(String system, String code) {
		this.system = system;
		this.code = code;
	}
	
	/*
	 * Copy Constructor
	 */
	public ReadingType(ReadingType readingType) {
		this.system = readingType.system;
		this.code = readingType.code;
	}

	public String getCode() {
		return code;
	}

	public String getSystem() {
		return system;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	@Override
	public String toString() {
		return "ReadingType [system=" + system + ", code=" + code + "]";
	}

}
