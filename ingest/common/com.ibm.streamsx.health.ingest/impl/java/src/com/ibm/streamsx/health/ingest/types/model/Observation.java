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

public class Observation implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Device device;
	private String patientId;
	private ReadingSource readingSource;
	private Reading reading;

	public Observation() {
	}

	public Observation(Device device, String patientId, ReadingSource readingSource, Reading reading) {
		this.device = device;
		this.patientId = patientId;
		this.readingSource = readingSource;
		this.reading = reading;
	}
	
	public Observation(Observation observation) {
		this.device = new Device(observation.device);
		this.patientId = observation.patientId;
		this.readingSource = new ReadingSource(observation.readingSource);
		this.reading = new Reading(observation.reading);
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public ReadingSource getReadingSource() {
		return readingSource;
	}

	public void setReadingSource(ReadingSource readingSource) {
		this.readingSource = readingSource;
	}

	public Reading getReading() {
		return reading;
	}

	public void setReading(Reading reading) {
		this.reading = reading;
	}

	@Override
	public String toString() {
		return "Observation [device=" + device + ", patientId=" + patientId + ", readingSource=" + readingSource
				+ ", reading=" + reading + "]";
	}

}
