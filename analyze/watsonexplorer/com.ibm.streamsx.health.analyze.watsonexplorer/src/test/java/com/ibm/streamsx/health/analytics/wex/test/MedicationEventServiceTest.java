package com.ibm.streamsx.health.analytics.wex.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import com.ibm.streamsx.health.analytics.wex.services.EventBeacon;
import com.ibm.streamsx.health.analytics.wex.services.MedicationEventService;
import com.ibm.streamsx.topology.context.StreamsContext.Type;

public class MedicationEventServiceTest {

	public static void main(String[] args) throws Exception {
		Properties props = new Properties();
		props.load(new FileInputStream(new File("medication.service.properties")));

		MedicationEventService svc = new MedicationEventService(props.getProperty("wexToolkitPath"));
		svc.setContextType(Type.DISTRIBUTED);
		svc.addSubmissionTimeParam("wex.host", props.getProperty("host"));
		svc.addSubmissionTimeParam("wex.port", Integer.valueOf(props.getProperty("port")));
		svc.addSubmissionTimeParam("wex.patient.field.name", props.getProperty("patientFieldName"));
		svc.addSubmissionTimeParam("collectionName", props.getProperty("medicationCollection"));
		
		svc.setSubscriptionTopic(EventBeacon.EVENT_BEACON_TOPIC);
		svc.buildAndRun();

		new EventBeacon(svc.getPublishedTopic()).run();
	}
}
