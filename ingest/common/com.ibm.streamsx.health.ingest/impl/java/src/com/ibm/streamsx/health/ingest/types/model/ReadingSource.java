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

public class ReadingSource implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;
	private String sourceType;
	private String deviceId;

	public ReadingSource() {
		id = "";
		sourceType = "";
		deviceId = "";
	}

	public ReadingSource(ReadingSource readingSource) {
		this.id = readingSource.id;
		this.sourceType = readingSource.sourceType;
		this.deviceId = readingSource.deviceId;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	@Override
	public String toString() {
		return "ReadingSource [id=" + id + ", sourceType=" + sourceType + ", deviceId=" + deviceId + "]";
	}

}
