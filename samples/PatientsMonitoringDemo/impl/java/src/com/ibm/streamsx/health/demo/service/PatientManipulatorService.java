package com.ibm.streamsx.health.demo.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.random.RandomDataImpl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.streamsx.health.ingest.types.connector.IdentityMapper;
import com.ibm.streamsx.health.ingest.types.connector.PublishConnector;
import com.ibm.streamsx.health.ingest.types.connector.SubscribeConnector;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.health.ingest.types.model.Reading;
import com.ibm.streamsx.health.simulate.beacon.generators.ABPDiastolicDataGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.ABPSystolicDataGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.AbstractVitalsGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.HeartRateDataGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.SpO2DataGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.TemperatureDataGenerator;
import com.ibm.streamsx.health.simulate.beacon.generators.VitalsDataRange;
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
	private String subscriptionTopic;
	
	public PatientManipulatorService(String subscriptionTopic) {
		topo = new Topology("PatientManipulatorService");
		topo.addJarDependency(System.getProperty("user.dir") + "/../../ingest/common/com.ibm.streamsx.health.ingest/lib/com.ibm.streamsx.health.ingest.jar");
		topo.addJarDependency(System.getProperty("user.dir") + "/../../simulate/com.ibm.streamsx.health.simulate.beacon/build/libs/com.ibm.streamsx.health.simulate.beacon.jar");
		this.subscriptionTopic = subscriptionTopic;
	}

	public void build() {
		TStream<Observation> obsStream = SubscribeConnector.subscribe(topo, subscriptionTopic);
		TSink controlSink = SPLStreams.subscribe(topo, CONTROL_INPUT_TOPIC, JSONSchemas.JSON).convert(t -> t.getString(0)).asType(String.class).sink(new PatientController());

		TStream<Observation> modifiedObs = obsStream.modify(new Manipulator());
		controlSink.colocate(modifiedObs);
		
		PublishConnector<Observation> connector = new PublishConnector<>(new IdentityMapper(), getPublishedTopic());
		connector.mapAndPublish(modifiedObs);
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
			generators.put("HR", new HeartRateDataGenerator("", VitalsDataRange.HIGH));
			generators.put("ABPsys", new ABPSystolicDataGenerator("", VitalsDataRange.HIGH));
			generators.put("ABPdias", new ABPDiastolicDataGenerator("", VitalsDataRange.HIGH));
			generators.put("Temperature", new TemperatureDataGenerator("", VitalsDataRange.HIGH));
			generators.put("SpO2", new SpO2DataGenerator("", VitalsDataRange.LOW));
			
			return this;
		}
		
		@Override
		public Observation apply(Observation obs) {
			String patientId = obs.getPatientId();			
			if(PatientsManager.getInstance().has(patientId)) {
				Reading reading = obs.getReading();
				switch(obs.getReading().getReadingType()) {
				case "HR":
					reading.setValue(generators.get("HR").get().getReading().getValue());
					break;
				case "ABPsys":
					reading.setValue(generators.get("ABPsys").get().getReading().getValue());
					break;
				case "ABPdias":
					reading.setValue(generators.get("ABPdias").get().getReading().getValue());
					break;
				case "Temperature":
					reading.setValue(generators.get("Temperature").get().getReading().getValue());
					break;
				case "SpO2":
					reading.setValue(generators.get("SpO2").get().getReading().getValue());
					break;
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
