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

public class Location implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;
	private String name;
	private String locationType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocationType() {
		return locationType;
	}

	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}

	@Override
	public String toString() {
		return "Location [id=" + id + ", name=" + name + ", locationType=" + locationType + "]";
	}

}
