package com.ibm.streamsx.health.beacon.generators;

public class HeartRateDataGenerator extends AbstractVitalsGenerator {
	private static final long serialVersionUID = 1L;
	
	public HeartRateDataGenerator(String patientId) {
		super(patientId, VitalsDataRange.NORMAL);
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
	String getReadingType() {
		return "HR";
	}

	@Override
	String getUOM() {
		return "bpm";
	}

}
