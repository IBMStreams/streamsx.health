package com.ibm.streamsx.health.simulate.beacon.generators;

public class SpO2DataGenerator extends AbstractVitalsGenerator {
	private static final long serialVersionUID = 1L;

	public SpO2DataGenerator(String patientId) {
		this(patientId, VitalsDataRange.NORMAL);
	}

	public SpO2DataGenerator(String patientId, VitalsDataRange vitalsRange) {
		super(patientId, vitalsRange);
		getVitalsGenerator().setSpeed(2.0);
	}
	
	@Override
	Double getNormalMin() {
		return 95.0;
	}

	@Override
	Double getNormalMax() {
		return 100.0;
	}

	@Override
	Double getHighMin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	Double getHighMax() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	Double getLowMin() {
		return 50.0;
	}

	@Override
	Double getLowMax() {
		return 70.0;
	}

	@Override
	String getReadingType() {
		return "SpO2";
	}

	@Override
	String getUOM() {
		return "%";
	}

}
