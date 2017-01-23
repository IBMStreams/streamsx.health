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

public class Patient implements Serializable {
	private static final long serialVersionUID = 1L;

	private PatientId id;
	private String name;
	private String gender;
	private String dateOfBirth;
	private String status;

	public Patient() {
		id = new PatientId();
		name = "";
		gender = "";
		dateOfBirth = "";
		status = "";
	}

	public Patient(Patient patient) {
		this.id = new PatientId(patient.id);
		this.name = patient.name;
		this.gender = patient.gender;
		this.dateOfBirth = patient.dateOfBirth;
		this.status = patient.status;
	}
	
	public void setId(PatientId id) {
		this.id = id;
	}

	public PatientId getId() {
		return id;
	}

	public void addId(String id) {
		this.id.add(id);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Patient [id=" + id + ", name=" + name + ", gender=" + gender + ", dateOfBirth=" + dateOfBirth
				+ ", status=" + status + "]";
	}

}
