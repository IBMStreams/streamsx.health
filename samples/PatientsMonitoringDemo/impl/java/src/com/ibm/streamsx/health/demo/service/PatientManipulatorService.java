package com.ibm.streamsx.health.demo.service;

import static com.ibm.streamsx.health.ingest.types.resolver.ObservationTypeResolver.isBPDiastolic;
import static com.ibm.streamsx.health.ingest.types.resolver.ObservationTypeResolver.isBPSystolic;
import static com.ibm.streamsx.health.ingest.types.resolver.ObservationTypeResolver.isHeartRate;
import static com.ibm.streamsx.health.ingest.types.resolver.ObservationTypeResolver.isSpO2;
import static com.ibm.streamsx.health.ingest.types.resolver.ObservationTypeResolver.isTemperature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.streamsx.health.ingest.types.connector.PublishConnector;
import com.ibm.streamsx.health.ingest.types.connector.SubscribeConnector;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.health.ingest.types.model.Reading;
import com.ibm.streamsx.health.ingest.types.model.ReadingTypeCode;
import com.ibm.streamsx.health.simulate.beacon.generators.ABPDiastolicDataGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.ABPSystolicDataGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.AbstractVitalsGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.HeartRateDataGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.SpO2DataGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.TemperatureDataGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.VitalsDataRange;
import com.ibm.streamsx.health.simulate.beacon.services.HealthDataBeaconService;
import com.ibm.streamsx.topology.TSink;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.StreamsContext.Type;
import com.ibm.streamsx.topology.context.StreamsContextFactory;
import com.ibm.streamsx.topology.function.Consumer;
import com.ibm.streamsx.topology.function.UnaryOperator;
import com.ibm.streamsx.topology.json.JSONSchemas;
import com.ibm.streamsx.topology.spl.SPLStreams;

/**
 * Description: This service will modify the value of an observation tuple
 * for specific patients. The control port is used to specify which patient
 * observations should be modified.
 * 
 * The values are modified by adding random Gaussian noise to each observation
 * value. 
 * 
 * Control port schema:
 * 
 * {
 * 	"patientId" : "string", 
 *  "isManipulationEnabled" : Boolean
 * }
 */
public class PatientManipulatorService {

	public static final String PATIENT_MANIPULATOR_TOPIC = "patient-manipulator-topic"; 
	public static final String CONTROL_INPUT_TOPIC = "manipulator.control.input";
	
	private Topology topo;
	private ArrayList<String> topics = new ArrayList<String>();
	
	public PatientManipulatorService(String... topics) {
		topo = new Topology("PatientManipulatorService");
		topo.addClassDependency(Observation.class);
		topo.addClassDependency(HealthDataBeaconService.class);
		
		for (int i = 0; i < topics.length; i++) {
			if (!topics[i].isEmpty())
				this.topics.add(topics[i]);
		}
	}

	public void build() {
				
		TStream<Observation> obsStream = null;
		
		for (String topic  : topics) {
			TStream<Observation> tstream = SubscribeConnector.subscribe(topo, topic.trim());
			
			if (obsStream == null)
				obsStream = tstream;
			else
				obsStream = obsStream.union(tstream);
		}
		
		TSink controlSink = SPLStreams.subscribe(topo, CONTROL_INPUT_TOPIC, JSONSchemas.JSON).convert(t -> t.getString(0)).asType(String.class).sink(new PatientController());

		TStream<Observation> modifiedObs = obsStream.modify(new Manipulator());
		controlSink.colocate(modifiedObs);
		
		PublishConnector.publishObservation(modifiedObs, getPublishedTopic());
	}

	public String getPublishedTopic() {
		return PATIENT_MANIPULATOR_TOPIC;
	}
	
	public void run(Type type, Map<String, Object> submissionParams) throws Exception {
		build();
		StreamsContextFactory.getStreamsContext(type).submit(topo).get();
	}

	private static class Manipulator implements UnaryOperator<Observation> {
		private static final long serialVersionUID = 1L;
		
		private Map<String, AbstractVitalsGenerator> generators;
		
		public Manipulator() {
		
		}
		
		public Object readResolve() {
			generators = new HashMap<String, AbstractVitalsGenerator>();
			generators.put(ReadingTypeCode.HEART_RATE.name(), new HeartRateDataGenerator("", VitalsDataRange.HIGH));
			generators.put(ReadingTypeCode.BP_SYSTOLIC.name(), new ABPSystolicDataGenerator("", VitalsDataRange.HIGH));
			generators.put(ReadingTypeCode.BP_DIASTOLIC.name(), new ABPDiastolicDataGenerator("", VitalsDataRange.HIGH));
			generators.put(ReadingTypeCode.TEMPERATURE.name(), new TemperatureDataGenerator("", VitalsDataRange.HIGH));
			generators.put(ReadingTypeCode.SPO2.name(), new SpO2DataGenerator("", VitalsDataRange.LOW));
			
			return this;
		}
		
		@Override
		public Observation apply(Observation obs) {
			String patientId = obs.getPatientId();			
			if(PatientsManager.getInstance().has(patientId)) {
				Reading reading = obs.getReading();
				if(isHeartRate(obs)) {
					reading.setValue(generators.get(ReadingTypeCode.HEART_RATE.name()).get().getReading().getValue());
				} else if(isBPSystolic(obs)) {
					reading.setValue(generators.get(ReadingTypeCode.BP_SYSTOLIC.name()).get().getReading().getValue());
				} else if(isBPDiastolic(obs)) {
					reading.setValue(generators.get(ReadingTypeCode.BP_DIASTOLIC.name()).get().getReading().getValue());
				} else if(isTemperature(obs)) {
					reading.setValue(generators.get(ReadingTypeCode.TEMPERATURE.name()).get().getReading().getValue());
				} else if(isSpO2(obs)) {
					reading.setValue(generators.get(ReadingTypeCode.SPO2.name()).get().getReading().getValue());
				}
			}
			
			return obs;
		}
	}
	
	private static class PatientController implements Consumer<String> {
		private static final long serialVersionUID = 1L;

		private transient Gson gson;
		
		public Object readResolve() {
			gson = new Gson();
			return this;
		}
		
		@Override
		public void accept(String controlData) {
			JsonObject jsonObj = gson.fromJson(controlData, JsonObject.class);
			String patientId = jsonObj.get("patientId").getAsString();
			boolean isManipulating = jsonObj.get("isManipulationEnabled").getAsBoolean();
			if(isManipulating) {
				PatientsManager.getInstance().addPatient(patientId);
			} else {
				PatientsManager.getInstance().removePatient(patientId);
			}
		} 		
	}
	
	public static class PatientsManager implements Serializable {
		private static final long serialVersionUID = 1L; 

		private static PatientsManager instance;

		private Set<String> patients;
		
		public static PatientsManager getInstance() {
			if(instance == null)
				instance = new PatientsManager();
			return instance;
		}

		public PatientsManager() {
			patients = new HashSet<String>();
		}

		public boolean has(String patientId) {
			return patients.contains(patientId);
		}
		
		public void addPatient(String patientId) {
			patients.add(patientId);
		}

		public void removePatient(String patientId) {
			patients.remove(patientId);
		}
		
		public Set<String> getPatients() {
			return patients;
		}
	}
}
