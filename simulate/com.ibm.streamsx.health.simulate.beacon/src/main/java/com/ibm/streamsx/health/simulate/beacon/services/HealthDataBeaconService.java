package com.ibm.streamsx.health.simulate.beacon.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.ibm.streamsx.health.ingest.types.connector.PublishConnector;
import com.ibm.streamsx.health.ingest.types.connector.SubscribeConnector;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.health.ingest.types.model.ReadingTypeCode;
import com.ibm.streamsx.health.simulate.beacon.generators.*;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.ContextProperties;
import com.ibm.streamsx.topology.context.StreamsContext.Type;
import com.ibm.streamsx.topology.context.StreamsContextFactory;
import com.ibm.streamsx.topology.function.Function;
import com.ibm.streamsx.topology.function.Supplier;

/**
 * 
 * Topic: 
 * 	"ingest-beacon";
 *
 */
public class HealthDataBeaconService {

	public static final String BEACON_INGEST_SERVICE_TOPIC = "ingest-beacon";
	public static final Integer DEFAULT_NUM_PATIENTS = 1;
	public static final String DEFAULT_PATIENT_PREFIX = "patient-";
	private static final String DEFAULT_PATIENT_ID = "patient-1";

	private static final long ECG_PERIOD = 8;
	private static final TimeUnit ECG_PERIOD_TIMEUNIT = TimeUnit.MILLISECONDS;
	private static final long VITALS_PERIOD = 100;
	private static final TimeUnit VITALS_PERIOD_TIMEUNIT = TimeUnit.MILLISECONDS;
	private static final long RESP_PERIOD = 1024;
	private static final TimeUnit RESP_PERIOD_TIMEUNIT = TimeUnit.MILLISECONDS;
	
	private Topology topo;
	private Supplier<String> patientPrefixSupplier;
	private Supplier<Integer> numPatientsSupplier;
	
	public HealthDataBeaconService(String beaconProjectPath) throws Exception {
		topo = new Topology("HealthDataBeaconService");
		topo.addJarDependency(beaconProjectPath + "/src/main/resources/data.jar");
		
		patientPrefixSupplier = topo.createSubmissionParameter("patient.prefix", DEFAULT_PATIENT_PREFIX);
		numPatientsSupplier = topo.createSubmissionParameter("num.patients", DEFAULT_NUM_PATIENTS);
	}

	public void build() {
		TStream<Observation> ecgIStream = topo.periodicSource(new HealthcareDataGenerator(DEFAULT_PATIENT_ID, "src/main/resources/ecgI.csv", ReadingTypeCode.ECG_LEAD_I.getCode()), ECG_PERIOD, ECG_PERIOD_TIMEUNIT);
		TStream<Observation> respStream = topo.periodicSource(new HealthcareDataGenerator(DEFAULT_PATIENT_ID, "src/main/resources/resp.csv", ReadingTypeCode.RESP_RATE.getCode()), RESP_PERIOD, RESP_PERIOD_TIMEUNIT);
		TStream<Observation> abpDiasStream = topo.periodicSource(new ABPDiastolicDataGenerator(DEFAULT_PATIENT_ID), VITALS_PERIOD, VITALS_PERIOD_TIMEUNIT);
		TStream<Observation> abpSysStream = topo.periodicSource(new ABPSystolicDataGenerator(DEFAULT_PATIENT_ID), VITALS_PERIOD, VITALS_PERIOD_TIMEUNIT);
		TStream<Observation> hrStream = topo.periodicSource(new HeartRateDataGenerator(DEFAULT_PATIENT_ID), VITALS_PERIOD, VITALS_PERIOD_TIMEUNIT);
		TStream<Observation> spo2Stream = topo.periodicSource(new SpO2DataGenerator(DEFAULT_PATIENT_ID), VITALS_PERIOD, VITALS_PERIOD_TIMEUNIT);
		TStream<Observation> temperatureStream = topo.periodicSource(new TemperatureDataGenerator(DEFAULT_PATIENT_ID), VITALS_PERIOD, VITALS_PERIOD_TIMEUNIT);
		
		Set<TStream<Observation>> observations = new HashSet<TStream<Observation>>();
		observations.add(respStream);
		observations.add(abpDiasStream);
		observations.add(abpSysStream);
		observations.add(hrStream);
		observations.add(spo2Stream);
		observations.add(temperatureStream);
		
		TStream<Observation> allStreams = ecgIStream.union(observations);
		TStream<Observation> multiplePatientStream = allStreams.multiTransform(new Multiplier(numPatientsSupplier, patientPrefixSupplier));		
		multiplePatientStream.print();
		
		PublishConnector.publishObservation(multiplePatientStream, getPublishedTopic());
	}
	
	public void run(Type contextType, Map<String, Object> submissionParams) throws Exception {
		build();
		
		Map<String, Object> configParams = new HashMap<String, Object>();
		configParams.put(ContextProperties.SUBMISSION_PARAMS, submissionParams);
		
		StreamsContextFactory.getStreamsContext(contextType).submit(topo, configParams).get();
	}
		
	public String getPublishedTopic() {
		return BEACON_INGEST_SERVICE_TOPIC;
	}
	
	private static class Multiplier implements Function<Observation, Iterable<Observation>> {

		private static final long serialVersionUID = 1L;

		private Supplier<Integer> numPatientsSupplier;
		private Supplier<String> patientPrefixSupplier;
		private int numPatients;
		private String patientPrefix;

		public Multiplier(Supplier<Integer> numPatientsSupplier, Supplier<String> patientPrefixSupplier) {
			this.numPatientsSupplier = numPatientsSupplier;
			this.patientPrefixSupplier = patientPrefixSupplier;
		}

		public Object readResolve() {
			numPatients = numPatientsSupplier.get();
			patientPrefix = patientPrefixSupplier.get();
			return this;
		}
		
		@Override
		public Iterable<Observation> apply(Observation obs) {
			List<Observation> observations = new ArrayList<Observation>();
			
			for(int i = 0; i < numPatients; i++) {
				String patientId = patientPrefix + (i+1);
				Observation clone = new Observation(obs);
				clone.setPatientId(patientId);
				observations.add(clone);
			}
			return observations;
		}
	}
	
	public static void main(String[] args) throws Exception {
		Map<String, Object> params = new HashMap<>();
		new HealthDataBeaconService(System.getProperty("user.dir")).run(Type.BUNDLE, params);
	}
}
