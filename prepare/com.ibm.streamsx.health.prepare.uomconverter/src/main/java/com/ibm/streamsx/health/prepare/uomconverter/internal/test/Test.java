package com.ibm.streamsx.health.prepare.uomconverter.internal.test;

import com.ibm.streamsx.health.prepare.uomconverter.services.UOMConverterService;
import com.ibm.streamsx.topology.context.StreamsContext.Type;

public class Test {
	public static void main(String[] args) throws Exception {
		UOMConverterService svc = new UOMConverterService("ingest-beacon");
		svc.setSubmissionParameter("uom.map.file", "/tmp/mapfile.txt");
		svc.run(Type.DISTRIBUTED);
	}
}
