/* begin_generated_IBM_copyright_prolog                                       */
/*                                                                            */
/* This is an automatically generated copyright prolog.                       */
/* After initializing,  DO NOT MODIFY OR MOVE                                 */
/******************************************************************************/
/* Copyright (C) 2016 International Business Machines Corporation             */
/* All Rights Reserved                                                        */
/******************************************************************************/
/* end_generated_IBM_copyright_prolog                                         */
package com.ibm.streamsx.health.ingest.types.model;

import java.io.Serializable;

public class Device implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;
	private String locationId;

	public Device() {
		id = "";
		locationId = "";
	}

	public Device(Device device) {
		this.id = device.id;
		this.locationId = device.locationId;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	@Override
	public String toString() {
		return "Device [id=" + id + ", locationId=" + locationId + "]";
	}

}
