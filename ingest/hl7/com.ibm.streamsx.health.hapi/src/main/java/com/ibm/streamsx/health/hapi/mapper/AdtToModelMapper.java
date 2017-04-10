//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.hapi.mapper;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.ibm.streamsx.health.ingest.types.model.ADTEvent;
import com.ibm.streamsx.health.ingest.types.model.Clinician;
import com.ibm.streamsx.health.ingest.types.model.EventDetails;
import com.ibm.streamsx.health.ingest.types.model.IInjestServicesConstants;
import com.ibm.streamsx.health.ingest.types.model.MessageInfo;
import com.ibm.streamsx.health.ingest.types.model.Patient;
import com.ibm.streamsx.health.ingest.types.model.PatientVisit;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v26.datatype.CX;
import ca.uhn.hl7v2.model.v26.datatype.XCN;
import ca.uhn.hl7v2.model.v26.datatype.XPN;
import ca.uhn.hl7v2.model.v26.message.ADT_AXX;
import ca.uhn.hl7v2.model.v26.segment.EVN;
import ca.uhn.hl7v2.model.v26.segment.MSH;
import ca.uhn.hl7v2.model.v26.segment.PID;
import ca.uhn.hl7v2.model.v26.segment.PV1;

public class AdtToModelMapper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger TRACE = Logger.getLogger(AdtToModelMapper.class);

	@SuppressWarnings("unchecked")
	public <T> Iterable<T> messageToModel(Message message) {

		ArrayList<ADTEvent> adtEvents = new ArrayList<ADTEvent>();

		if (message instanceof ADT_AXX) {

			ADT_AXX superMsg = (ADT_AXX) message;

			MessageInfo msg = new MessageInfo();
			Patient patient = new Patient();
			try {
				MSH header = superMsg.getMSH();
				msg.setSendingApp(header.getSendingApplication().encode());
				msg.setSendingFacility(header.getSendingFacility().encode());
				msg.setReceivingApp(header.getReceivingApplication().encode());
				msg.setReceivingFacility(header.getReceivingFacility().encode());
				msg.setMessageTs(header.getDateTimeOfMessage().encode());
				msg.setMessageType(header.getMessageType().encode());

				PID pid = superMsg.getPID();
				patient.setId(pid.getPatientID().encode());

				XPN[] patientNames = pid.getPatientName();
				for (XPN name : patientNames) {

					if (patient.getName().equals(IInjestServicesConstants.EMPTYSTR)) {
						patient.setName(getPatientFullName(name));
					} else {
						patient.addAlternateName(getPatientFullName(name));
					}

				}

				patient.setGender(pid.getAdministrativeSex().encode());
				patient.setDateOfBirth(pid.getDateTimeOfBirth().encode());

				CX[] ids = pid.getAlternatePatientIDPID();
				if (ids.length > 0) {
					for (CX cx : ids) {
						patient.addAlternativeId(cx.encode());
					}
				}

				EventDetails evt = new EventDetails();
				EVN evn = superMsg.getEVN();
				evt.setEventType(evn.getEventTypeCode().encode());
				evt.setEventTs(evn.getEvn6_EventOccurred().encode());
				evt.setRecordTs(evn.getEvn2_RecordedDateTime().encode());

				PV1 pv1 = superMsg.getPV1();
				PatientVisit patientVisit = new PatientVisit();
				patientVisit.setPatientClass(pv1.getPatientClass().encode());
				patientVisit.setLocation(pv1.getAssignedPatientLocation().encode());
				patientVisit.setPriorLocation(pv1.getPriorPatientLocation().encode());
				patientVisit.setVisitNumber(pv1.getVisitNumber().encode());

				patient.setStatus(pv1.getBedStatus().encode());

				XCN[] doctors = pv1.getAttendingDoctor();
				for (XCN xcn : doctors) {
					String id = xcn.getIDNumber().encode();
					String name = xcn.getFamilyName().encode() + " " + xcn.getGivenName().encode();

					Clinician clinician = new Clinician();
					clinician.setId(id);
					clinician.setName(name);

					patientVisit.addAttendingDoctor(clinician);
				}

				doctors = pv1.getConsultingDoctor();
				for (XCN xcn : doctors) {
					String id = xcn.getIDNumber().encode();
					String name = xcn.getFamilyName().encode() + " " + xcn.getGivenName().encode();

					Clinician clinician = new Clinician();
					clinician.setId(id);
					clinician.setName(name);

					patientVisit.addConsultingDoctor(clinician);
				}

				ADTEvent adtEvent = new ADTEvent();
				adtEvent.setEvt(evt);
				adtEvent.setPatient(patient);
				adtEvent.setMsg(msg);
				adtEvent.setPv(patientVisit);

				adtEvents.add(adtEvent);

			} catch (HL7Exception e) {
				TRACE.error("Unable to parse HL7 message", e);
			}

		}

		return (Iterable<T>) adtEvents;

	}

	private String getPatientFullName(XPN name) throws HL7Exception {
		return name.getGivenName().encode() + " " + name.getFamilyName().encode();
	}

}
