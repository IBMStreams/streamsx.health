/*******************************************************************************
 * Copyright (C) 2017 International Business Machines Corporation
 * All Rights Reserved
 *******************************************************************************/
package com.ibm.streamsx.health.vines.model;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class RootId implements Serializable {

	private static final long serialVersionUID = 1L;

	@SerializedName("$oid")
	private String oid;
	
	public String getOid() {
		return oid;
	}

	@Override
	public String toString() {
		return "RootId [oid=" + oid + "]";
	}

	


}
