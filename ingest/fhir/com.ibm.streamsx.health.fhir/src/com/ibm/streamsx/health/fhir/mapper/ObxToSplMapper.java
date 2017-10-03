//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.mapper;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.Device;
import org.hl7.fhir.dstu3.model.DeviceMetric;
import org.hl7.fhir.dstu3.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.exceptions.FHIRException;

import com.ibm.streamsx.health.fhir.model.ObxParseResult;
import com.ibm.streamsx.health.fhir.service.FhirObservationIngestService;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.health.ingest.types.model.Reading;
import com.ibm.streamsx.health.ingest.types.model.ReadingSource;
import com.ibm.streamsx.health.ingest.types.model.ReadingType;


public class ObxToSplMapper implements Serializable {

	private static final long serialVersionUID = 1L;


	@SuppressWarnings("unchecked")
	public ObxParseResult messageToModel(BundleEntryComponent entry)  {

		ArrayList<Observation> observations = new ArrayList<Observation>();
		ObxParseResult result = new ObxParseResult();
		
		try {
			if (entry.getResource() instanceof org.hl7.fhir.dstu3.model.Observation) {
				org.hl7.fhir.dstu3.model.Observation obx = (org.hl7.fhir.dstu3.model.Observation) entry.getResource();
				
				result.setBundle(entry);
				
				// time stamp is the Observation Effective Time
				DateTimeType effectiveDateTimeType = obx.getEffectiveDateTimeType();
				long ts = 0;
				if (effectiveDateTimeType != null)
				{
					ts = effectiveDateTimeType.getValue().getTime();
				}
				
				// TODO:  How to handle more than one code in an observation?
				
				// Create ReadingType object if we have everything
				ReadingType rType = null;
				
				if (obx.getCode()!= null && obx.getCode().getCodingFirstRep() != null)
				{
					String code = obx.getCode().getCodingFirstRep().getCode();
					String system = obx.getCode().getCodingFirstRep().getSystem();
					rType = new ReadingType(system, code);
				}
				
				ReadingSource rSource = new ReadingSource();
				com.ibm.streamsx.health.ingest.types.model.Device sDevice = new com.ibm.streamsx.health.ingest.types.model.Device();
				if (obx.getDevice() != null){
					if (obx.getDevice().getResource() instanceof Device)
					{
						Device fhirDevice = (Device)obx.getDevice().getResource();
						rSource.setDeviceId(fhirDevice.getId());
						sDevice.setId(fhirDevice.getId());
						sDevice.setLocationId(fhirDevice.getLocation().getId());
					}
					else if (obx.getDevice().getResource() instanceof DeviceMetric)
					{
						DeviceMetric channel = (DeviceMetric)obx.getDevice().getResource();
						rSource.setId(channel.getId());
						rSource.setSourceType(channel.getType().getCodingFirstRep().getCode());
						rSource.setDeviceId(channel.getParent().getId());
						sDevice.setId(obx.getDevice().getId());
					} else if (obx.getDevice().getReference() != null)
					{
						String deviceId = inferId(obx.getDevice().getReference());
						rSource.setDeviceId(deviceId);
						sDevice.setId(deviceId);;
					}
				}
				
				String patientId = "";
				if (obx.getSubject() != null){
					if(obx.getSubject().getResource() instanceof Patient)
					{
						Patient patient = (Patient)obx.getSubject().getResource();
						patientId = patient.getId();
					}
					else if (obx.getSubject().getReference() != null)
					{
						patientId=inferId(obx.getSubject().getReference());
					}
				}
				
//				if (patientId.equals("191025"))
//					throw new FHIRException("Unable to parse patient 191025");
				
				// Create Reading object 
				if (obx.getValue() instanceof Quantity)
				{
					Observation streamsObx = createObservation(obx.getValueQuantity(), ts, rType, rSource, sDevice, patientId);
					
					observations.add(streamsObx);
				}
				
				// Observation can also come in as a component
				if (obx.getComponent() != null && obx.getComponent().size() > 0)
				{
					List<ObservationComponentComponent> components = obx.getComponent();
					for (ObservationComponentComponent obComponent : components) {
						if (obComponent.getCode() != null && obComponent.getCode().getCodingFirstRep() != null)
						{
							String code = obx.getCode().getCodingFirstRep().getCode();
							String system = obx.getCode().getCodingFirstRep().getSystem();
							rType = new ReadingType(system, code);
						}
						
						if (obComponent.getValue() != null && obComponent.getValue() instanceof Quantity) {
							Observation streamsObx = createObservation(obComponent.getValueQuantity(), ts, rType, rSource, sDevice, patientId);
							observations.add(streamsObx);
						}
					}
				}
				
				
			}
		} catch (FHIRException e) {
			FhirObservationIngestService.TRACE.error("Unable to parser component bundle", e);
			result.setExceptions(e);			
		}
		
		return result.setObservations(observations);

	}

	private Observation createObservation(Quantity quantity, long ts, ReadingType rType,
			ReadingSource rSource, com.ibm.streamsx.health.ingest.types.model.Device sDevice, String patientId) {
		Reading reading;
		BigDecimal dValue = quantity.getValue();
		
		String uom = quantity.getUnit();
		
		reading = new Reading();
		reading.setReadingType(rType);
		reading.setTimestamp(ts);
		reading.setUom(uom);
		reading.setValue(dValue.doubleValue());
		
		Observation streamsObx = new Observation();
		streamsObx.setPatientId(patientId);
		streamsObx.setDevice(sDevice);
		streamsObx.setReadingSource(rSource);
		streamsObx.setReading(reading);
		return streamsObx;
	}

	private String inferId(String reference)
	{
		String[] segments = reference.split("/");
		return segments[segments.length-1];
	}

}
