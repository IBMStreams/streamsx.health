//*******************************************************************************
//* Copyright (C) 2016 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.hapi.mapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ibm.streams.operator.OutputTuple;
import com.ibm.streams.operator.log4j.TraceLevel;
import com.ibm.streamsx.health.hapi.model.Observation;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v26.datatype.DTM;
import ca.uhn.hl7v2.model.v26.datatype.PL;
import ca.uhn.hl7v2.model.v26.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v26.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v26.group.ORU_R01_PATIENT;
import ca.uhn.hl7v2.model.v26.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v26.group.ORU_R01_VISIT;
import ca.uhn.hl7v2.model.v26.message.ORU_R01;
import ca.uhn.hl7v2.model.v26.segment.OBR;
import ca.uhn.hl7v2.model.v26.segment.OBX;
import ca.uhn.hl7v2.model.v26.segment.PV1;

public class ObxToSplMapper implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger TRACE = Logger.getLogger(ObxToSplMapper.class);

	@SuppressWarnings("unchecked")
	public <T> Iterable<T> messageToModel(Message message) {

		ArrayList<Observation> observations = new ArrayList<Observation>();

		if (message instanceof ORU_R01) {
			ORU_R01 oruMsg = (ORU_R01) message;

			String obxTs = "";
			String obxLocation = "";

			try {
				List<ORU_R01_PATIENT_RESULT> patient_RESULTAll = ((ORU_R01) message).getPATIENT_RESULTAll();

				for (ORU_R01_PATIENT_RESULT result : patient_RESULTAll) {
					ORU_R01_ORDER_OBSERVATION order_OBSERVATION = result.getORDER_OBSERVATION();

					OBR obr = order_OBSERVATION.getOBR();
					DTM ts = obr.getObservationDateTime();
					obxTs = ts.getValue();

					ORU_R01_PATIENT patient = result.getPATIENT();
					ORU_R01_VISIT visit = patient.getVISIT();
					PL location = visit.getPV1().getAssignedPatientLocation();
					obxLocation = location.encode();

					List<ORU_R01_OBSERVATION> observationAll = order_OBSERVATION.getOBSERVATIONAll();
					for (ORU_R01_OBSERVATION oru_R01_OBSERVATION : observationAll) {
						
						parseOBX(observations, obxTs, obxLocation, oru_R01_OBSERVATION.getOBX());
					}

				}
			} catch (HL7Exception e) {
				if (TRACE.isDebugEnabled())
					TRACE.log(TraceLevel.WARN, e);
			}

			try {
				OBR obr = (OBR) oruMsg.get("OBR");
				DTM ts = obr.getObservationDateTime();
				obxTs = ts.getValue();

				Structure tmp = oruMsg.get("PV1");
				if (tmp != null) {
					PV1 pv1 = (PV1) tmp;
					PL location = pv1.getAssignedPatientLocation();
					obxLocation = location.getRoom().getValue();
				}

				Structure[] structures = oruMsg.getAll("OBX");
				for (Structure structure : structures) {
					parseOBX(observations, obxTs, obxLocation, (OBX)structure);
				}
			} catch (HL7Exception e) {
				if (TRACE.isDebugEnabled())
					TRACE.log(TraceLevel.WARN, e);
			}
		}

		return (Iterable<T>) observations;

	}

	private void parseOBX(ArrayList<Observation> observations, String obxTs, String obxLocation,
			OBX obx) throws HL7Exception {
		
		Observation observation = new Observation();
		observation.setTs(obxTs);
		observation.setLocation(obxLocation);

		String observationId = obx.getObservationIdentifier().getIdentifier().getValue();
		String unit = obx.getUnits().getIdentifier().getValue();

		observation.setObservationId(observationId);
		observation.setUnit(unit);

		Varies[] values = obx.getObservationValue();

		for (Varies value : values) {

			observation.setObservationValue(value.encode());
			observations.add(observation);
		}
	}

	public OutputTuple modelToSpl(Observation observation, OutputTuple outTuple) {
		if (TRACE.isDebugEnabled())
			TRACE.log(TraceLevel.DEBUG, "Convert observation to tuple: " + toString());
		outTuple.setString("ts", observation.getTs());
		outTuple.setString("location", observation.getLocation());
		outTuple.setString("observationId", observation.getObservationId());
		outTuple.setString("observationValue", observation.getObservationValue());
		outTuple.setString("unit", observation.getUnit());
		return outTuple;
	}

}
