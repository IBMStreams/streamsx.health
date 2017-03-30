/*******************************************************************************
 * Copyright (C) 2017 International Business Machines Corporation
 * All Rights Reserved
 *******************************************************************************/
package com.ibm.streamsx.health.vines.model;

import java.io.Serializable;

public class Data implements Serializable {

	private static final long serialVersionUID = 1L;

	private Body Body;
	private Header Header;
	private Location Location;
	private Patient Patient;
	private String Exchange;

	public Body getBody() {
		return Body;
	}

	public Header getHeader() {
		return Header;
	}

	public Location getLocation() {
		return Location;
	}

	public Patient getPatient() {
		return Patient;
	}

	public String getExchange() {
		return Exchange;
	}

	@Override
	public String toString() {
		return "Data [Body=" + Body + ", Header=" + Header + ", Location=" + Location + ", Patient=" + Patient
				+ ", Exchange=" + Exchange + "]";
	}

}
