package com.ibm.streamsx.health.ingest.hl7.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.ibm.streams.operator.log4j.TraceLevel;
import com.ibm.streamsx.health.ingest.hl7.parser.AbstractOruR01ToObservationParser;
import com.ibm.streamsx.health.ingest.hl7.parser.ParserResult;
import com.ibm.streamsx.health.ingest.types.model.Device;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.health.ingest.types.model.Reading;
import com.ibm.streamsx.health.ingest.types.model.ReadingSource;
import com.ibm.streamsx.health.ingest.types.model.ReadingType;

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
import ca.uhn.hl7v2.model.v26.segment.MSH;
import ca.uhn.hl7v2.model.v26.segment.OBR;
import ca.uhn.hl7v2.model.v26.segment.OBX;
import ca.uhn.hl7v2.model.v26.segment.PID;
import ca.uhn.hl7v2.model.v26.segment.PV1;

public class OruR01ToObservationParser extends AbstractOruR01ToObservationParser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger TRACE = Logger.getLogger(OruR01ToObservationParser.class);
	
	transient SimpleDateFormat formatter = new SimpleDateFormat("YYYYMMDDHHmmss");

	@Override
	public ParserResult apply(Message message) {
		
		ParserResult parseResult = new ParserResult(message);
		
		if (message instanceof ORU_R01) {
			ORU_R01 oruMsg = (ORU_R01) message;
			
			String obxTs = "";
			String obxLocation = "";
			String sendingApp = "";
			String sendingFacility = "";

			try {
				MSH msh = oruMsg.getMSH();
				sendingApp = msh.getSendingApplication().encode();
				sendingFacility = msh.getSendingFacility().encode();
				
				List<ORU_R01_PATIENT_RESULT> patient_RESULTAll = ((ORU_R01) message).getPATIENT_RESULTAll();

				for (ORU_R01_PATIENT_RESULT result : patient_RESULTAll) {
					ORU_R01_ORDER_OBSERVATION order_OBSERVATION = result.getORDER_OBSERVATION();

					OBR obr = order_OBSERVATION.getOBR();
					DTM ts = obr.getObservationDateTime();
					obxTs = ts.getValue();

					ORU_R01_PATIENT patient = result.getPATIENT();
					
					String patientId = patient.getPID().getPatientID().getIDNumber().getValue();
					
					ORU_R01_VISIT visit = patient.getVISIT();
					PL location = visit.getPV1().getAssignedPatientLocation();
					obxLocation = location.encode();

					List<ORU_R01_OBSERVATION> observationAll = order_OBSERVATION.getOBSERVATIONAll();
					for (ORU_R01_OBSERVATION oru_R01_OBSERVATION : observationAll) {
						
						parseOBX(parseResult, patientId, obxTs, obxLocation, oru_R01_OBSERVATION.getOBX(), sendingApp, sendingFacility);
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
					obxLocation = location.encode();
				}
				
				
				String patientId = "";
				Structure tmpPatientId = null;
				
				try {
					tmpPatientId = oruMsg.get("PID");
				} catch (Exception e) {
					if (TRACE.isDebugEnabled())
						TRACE.log(TraceLevel.WARN, e);
				}
				if (tmpPatientId != null)
				{
					PID pid = (PID)tmp;
					patientId = pid.getPatientID().getIDNumber().getValue();
				}
				
				if (patientId == null)
				{
					patientId = "";
				}

				Structure[] structures = oruMsg.getAll("OBX");
				for (Structure structure : structures) {
					parseOBX(parseResult, patientId, obxTs, obxLocation, (OBX)structure, sendingApp, sendingFacility);
				}
			} catch (HL7Exception e) {
				if (TRACE.isDebugEnabled())
					TRACE.log(TraceLevel.WARN, e);
			}
		}

		return parseResult;
	}
	
	private void parseOBX(ParserResult result, String patientId, String obxTs, String obxLocation,
			OBX obx, String sendingApp, String sendingFacility) throws HL7Exception {
				
		ReadingSource rSrc = new ReadingSource();
		rSrc.setDeviceId(sendingFacility);
		rSrc.setId(sendingApp);
		
		Date date;
		long ts = 0;
		try {
			if (obxTs != null)
			{
				if (formatter == null)
					formatter = new SimpleDateFormat("YYYYMMDDHHmmss");
				
				date = formatter.parse(obxTs);
				ts = date.getTime();
			}
		} catch (ParseException e1) {
			TRACE.log(TraceLevel.ERROR, e1);
		}
		
		Device device = new Device();
		device.setLocationId(obxLocation);
		device.setId(sendingFacility);
		
		ReadingType readingType = new ReadingType("", obx.getObservationIdentifier().getIdentifier().getValue());

		// TODO:  What to do with non numeric readings?
		Varies[] values = obx.getObservationValue();

		for (Varies value : values) {
			
			try {
				Reading reading = new Reading();
				reading.setTimestamp(ts);
				reading.setValue(Double.parseDouble(value.encode()));
				reading.setUom(obx.getUnits().getIdentifier().getValue());
				reading.setReadingType(readingType);
				
				Observation observation = new Observation();
				observation.setPatientId(patientId!=null?patientId:"");
				observation.setReadingSource(rSrc);
				observation.setReading(reading);
				observation.setDevice(device);
				
				result.addObservation(observation);
			
			} catch (Exception e) {
				TRACE.log(TraceLevel.ERROR, e);
				
				String errorMessage = "Unable to parse value: " + value.encode();
				result.addErrorMesage(errorMessage);
			}
		}
	}
}
