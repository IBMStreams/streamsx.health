//*******************************************************************************
//* Copyright (C) 2017 International Business Machines Corporation
//* All Rights Reserved
//*******************************************************************************

package com.ibm.streamsx.health.fhir.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Type;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.client.IGenericClient;

public class FhirIngest {

	public static void main(String[] args) {
		// Topology topology = new Topology("FhirIngest");
		//
		// TStream<JSONObject> json = HTTPStreams.getJSON(topology,
		// "http://9.108.122.168:8080/baseDstu3/Observation/", 10,
		// TimeUnit.SECONDS);
		// json.print();
		//
		// try {
		// StreamsContextFactory.getStreamsContext(StreamsContext.Type.BUNDLE).submit(topology);
		// } catch (Exception e) {
		// e.printStackTrace();
		//
		// }

		FhirContext ctx = FhirContext.forDstu3();
		// String serverBase = "http://9.108.122.168:8080/baseDstu3";
		// http://9.108.122.168:8080/baseDstu3

		// String serverBase =
		// "https://fhir-open.sandboxcerner.com/dstu2/0b8a0111-e8e6-4c26-a91c-5069cbc6b1ca";
		// String serverBase = "http://9.108.122.168:8080/baseDstu3";
		String serverBase = "http://fhirtest.uhn.ca/baseDstu3";
		IGenericClient client = ctx.newRestfulGenericClient(serverBase);

		long now = System.currentTimeMillis();
		long tenMinsAgo = now - 24 * 60 * 60 * 1000;
		Date newCheck = new Date(now);
		Date lastCheck = new Date(tenMinsAgo);

		// Bundle results =
		// client.search().forResource(Patient.class).where(Patient.FAMILY.matches().value("PETERS"))
		// .returnBundle(Bundle.class).execute();

		ArrayList ids = new ArrayList<>();
		ids.add("725945");
		ids.add("123");

		// lastUpdated(new DateRangeParam(lastCheck, newCheck))

		// Bundle results =
		// client.search().forResource(Observation.class).where(Observation.SUBJECT.hasId("725945"))
		// .include(new
		// Include("Observation:Subject")).returnBundle(Bundle.class).prettyPrint().execute();

		Bundle results = client.search().forResource(Observation.class).include(new Include("Observation:Subject"))
				.include(new Include("Observation:Device")).include(new Include("Observation:Device:location"))
				.returnBundle(Bundle.class).prettyPrint().execute();

		// Bundle results =
		// client.search().forResource(Observation.class).where(Observation.CATEGORY.exactly().code("vital-signs")).returnBundle(Bundle.class).prettyPrint().execute();

		List<BundleEntryComponent> entries = results.getEntry();

		for (BundleEntryComponent entry : entries) {
			if (entry.getResource() instanceof Observation) {
				Observation obx = (Observation) entry.getResource();

				Type value = obx.getValue();
				if (value instanceof Quantity) {
					// get code
					List<Coding> coding = obx.getCode().getCoding();
					for (Coding coding2 : coding) {
						System.out.println(coding2.getSystem() + " " + coding2.getCodeElement().asStringValue());
					}

					Quantity qValue = (Quantity) value;
					String unit = qValue.getUnit();
					BigDecimal d = qValue.getValue();

					if (obx.hasEffective()) {
						Type effective = obx.getEffective();
						System.out.println(effective);
					}

					IBaseResource subject = obx.getSubject().getResource();

					System.out.println(subject != null ? subject.getIdElement().toString()
							: obx.getSubject().getReference() + " " + d + " " + unit);
					
					IBaseResource device = obx.getDevice().getResource();
					if (device != null)
					{
						System.out.println("Has device");
					}
				}

			} else {
				System.out.println("some other resource");
			}
		}

		while (results.getLink(Bundle.LINK_NEXT) != null) {
			results = client.loadPage().next(results).execute();
		}

		// List<BundleEntryComponent> entry = results.getEntry();
		//
		//
		//
		// List<BundleLinkComponent> link = results.getLink();

		System.out.println("Found: " + results.getTotal());

	}

}
