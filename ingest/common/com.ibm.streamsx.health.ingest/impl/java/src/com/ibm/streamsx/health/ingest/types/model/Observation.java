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

public class Observation {

	private Device device;
	private PatientId patientId;
	private ReadingSource readingSource;
	private Reading reading;

	public Observation() {
	}

	public Observation(Device device, PatientId patientId, ReadingSource readingSource, Reading reading) {
		this.device = device;
		this.patientId = patientId;
		this.readingSource = readingSource;
		this.reading = reading;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public PatientId getPatientId() {
		return patientId;
	}

	public void setPatientId(PatientId patientId) {
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
