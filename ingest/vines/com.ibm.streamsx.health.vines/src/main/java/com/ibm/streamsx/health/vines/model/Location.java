/*******************************************************************************
 * Copyright (C) 2017 International Business Machines Corporation
 * All Rights Reserved
 *******************************************************************************/
package com.ibm.streamsx.health.vines.model;

import java.io.Serializable;

public class Location implements Serializable {

	private static final long serialVersionUID = 1L;

	private String _id;
	private String Name;
	private Boolean IsDeleted;
	private String PatientId;
	private Boolean IsDefault;

	public String get_id() {
		return _id;
	}

	public String getName() {
		return Name;
	}

	public Boolean getIsDeleted() {
		return IsDeleted;
	}

	public String getPatientId() {
		return PatientId;
	}

	public Boolean getIsDefault() {
		return IsDefault;
	}

	@Override
	public String toString() {
		return "Location [_id=" + _id + ", Name=" + Name + ", IsDeleted=" + IsDeleted + ", PatientId=" + PatientId
				+ ", IsDefault=" + IsDefault + "]";
	}

}
