package com.ibm.streamsx.health.simulate.beacon.generators;

import com.ibm.streamsx.health.ingest.types.model.ReadingType;
import com.ibm.streamsx.health.ingest.types.model.ReadingTypeCode;
import com.ibm.streamsx.health.ingest.types.model.ReadingTypeSystem;

public class ABPSystolicDataGenerator extends AbstractVitalsGenerator {
	private static final long serialVersionUID = 1L;

	public ABPSystolicDataGenerator(String patientId) {
		this(patientId, VitalsDataRange.NORMAL);
	}
	
	public ABPSystolicDataGenerator(String patientId, VitalsDataRange vitalsRange) {
		super(patientId, vitalsRange);
		getVitalsGenerator().setSpeed(2.0);
	}
	
	@Override
	Double getNormalMin() {
		return 100.0;
	}

	@Override
	Double getNormalMax() {
		return 115.0;
	}

	@Override
	Double getHighMin() {
		return 160.0;
	}

	@Override
	Double getHighMax() {
		return 200.0;
	}

	@Override
	Double getLowMin() {
		return 10.0;
	}

	@Override
	Double getLowMax() {
		return 40.0;
	}

	@Override
	ReadingType getReadingType() {
		return new ReadingType(ReadingTypeSystem.STREAMS_CODE_SYSTEM, ReadingTypeCode.BP_SYSTOLIC.getCode());
	}

	@Override
	String getUOM() {
		return "mmHg";
	}

}
