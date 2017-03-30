/*******************************************************************************
 * Copyright (C) 2017 International Business Machines Corporation
 * All Rights Reserved
 *******************************************************************************/
package com.ibm.streamsx.health.vines.model;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class Patient implements Serializable {

	private static final long serialVersionUID = 1L;

	@SerializedName("_id")
	private String _id;

	private String MRN;
	private String MRNSource;
	private String Sex;
	private NameList NameList;

	public String get_id() {
		return _id;
	}

	public String getMRN() {
		return MRN;
	}

	public String getMRNSource() {
		return MRNSource;
	}

	public String getSex() {
		return Sex;
	}

	public NameList getNameList() {
		return NameList;
	}

	@Override
	public String toString() {
		return "Patient [_id=" + _id + ", MRN=" + MRN + ", MRNSource=" + MRNSource + ", Sex=" + Sex + ", NameList="
				+ NameList + "]";
	}

}
