package com.ibm.streamsx.health.simulate.edgent.service;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.edgent.connectors.iot.IotDevice;
import org.apache.edgent.connectors.iot.QoS;
import org.apache.edgent.connectors.iotp.IotpDevice;
import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

import com.google.gson.JsonObject;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.health.ingest.types.model.ReadingTypeCode;
import com.ibm.streamsx.health.simulate.beacon.generators.ABPDiastolicDataGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.ABPSystolicDataGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.HealthcareDataGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.HeartRateDataGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.SpO2DataGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.TemperatureDataGenerator;


public class HealthDataGenerator {

	
	public static final String BEACON_INGEST_SERVICE_TOPIC = "ingest-beacon";
	public static final Integer DEFAULT_NUM_PATIENTS = 1;
	public static final String DEFAULT_PATIENT_PREFIX = "patient-";
	private static final String DEFAULT_PATIENT_ID = "patient-100";

	private static final long ECG_PERIOD = 8;
	private static final TimeUnit ECG_PERIOD_TIMEUNIT = TimeUnit.MILLISECONDS;
	private static final long VITALS_PERIOD = 100;
	private static final TimeUnit VITALS_PERIOD_TIMEUNIT = TimeUnit.MILLISECONDS;
	private static final long RESP_PERIOD = 1024;
	private static final TimeUnit RESP_PERIOD_TIMEUNIT = TimeUnit.MILLISECONDS;
	

	public static void main(String[] args) {
		
		DirectProvider dp = new DirectProvider();
		Topology topo = dp.newTopology("HealthDataGenerator");
		
		TStream<Observation> ecgIStream = topo.poll(new HealthcareDataGenerator(DEFAULT_PATIENT_ID, "resources/ecgI.csv", ReadingTypeCode.ECG_LEAD_I.getCode()), ECG_PERIOD, ECG_PERIOD_TIMEUNIT);
		TStream<Observation> respStream = topo.poll(new HealthcareDataGenerator(DEFAULT_PATIENT_ID, "resources/resp.csv", ReadingTypeCode.RESP_RATE.getCode()), RESP_PERIOD, RESP_PERIOD_TIMEUNIT);
		TStream<Observation> abpDiasStream = topo.poll(new ABPDiastolicDataGenerator(DEFAULT_PATIENT_ID), VITALS_PERIOD, VITALS_PERIOD_TIMEUNIT);
		TStream<Observation> abpSysStream = topo.poll(new ABPSystolicDataGenerator(DEFAULT_PATIENT_ID), VITALS_PERIOD, VITALS_PERIOD_TIMEUNIT);
		TStream<Observation> hrStream = topo.poll(new HeartRateDataGenerator(DEFAULT_PATIENT_ID), VITALS_PERIOD, VITALS_PERIOD_TIMEUNIT);
		TStream<Observation> spo2Stream = topo.poll(new SpO2DataGenerator(DEFAULT_PATIENT_ID), VITALS_PERIOD, VITALS_PERIOD_TIMEUNIT);
		TStream<Observation> temperatureStream = topo.poll(new TemperatureDataGenerator(DEFAULT_PATIENT_ID), VITALS_PERIOD, VITALS_PERIOD_TIMEUNIT);
		
		Set<TStream<Observation>> observations = new HashSet<TStream<Observation>>();
		observations.add(respStream);
		observations.add(abpDiasStream);
		observations.add(abpSysStream);
		observations.add(hrStream);
		observations.add(spo2Stream);
		observations.add(temperatureStream);
		
		TStream<Observation> allStreams = ecgIStream.union(observations);
		
		TStream<JsonObject> json = allStreams.map(new ObservationToJsonConverter());
		json.print();
		
		IotDevice device = new IotpDevice(topo, new File("device.cfg"));
		device.events(json, "patientData", QoS.FIRE_AND_FORGET);
		
		dp.submit(topo);
	}
}
