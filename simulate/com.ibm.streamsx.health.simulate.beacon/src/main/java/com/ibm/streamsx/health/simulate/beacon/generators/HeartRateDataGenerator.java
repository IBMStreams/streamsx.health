package com.ibm.streamsx.health.simulate.beacon.generators;

import com.ibm.streamsx.health.ingest.types.model.ReadingType;
import com.ibm.streamsx.health.ingest.types.model.ReadingTypeCode;
import com.ibm.streamsx.health.ingest.types.model.ReadingTypeSystem;

public class HeartRateDataGenerator extends AbstractVitalsGenerator {
	private static final long serialVersionUID = 1L;
	
	public HeartRateDataGenerator(String patientId) {
		this(patientId, VitalsDataRange.NORMAL);
	}

	public HeartRateDataGenerator(String patientId, VitalsDataRange range) {
		super(patientId, range);
	}
	
	@Override
	Double getNormalMin() {
		return 60.0;
	}

	@Override
	Double getNormalMax() {
		return 100.0;
	}

	@Override
	Double getHighMin() {
		return 150.0;
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
		return new ReadingType(ReadingTypeSystem.STREAMS_CODE_SYSTEM, ReadingTypeCode.HEART_RATE.getCode());
	}

	@Override
	String getUOM() {
		return "bpm";
	}

}
