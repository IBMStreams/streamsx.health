package com.ibm.streamsx.health.beacon.generators;

public class ABPDiastolicDataGenerator extends AbstractVitalsGenerator {
	private static final long serialVersionUID = 1L;

	public ABPDiastolicDataGenerator(String patientId) {
		this(patientId, VitalsDataRange.NORMAL);
	}

	public ABPDiastolicDataGenerator(String patientId, VitalsDataRange vitalsDataRange) {
		super(patientId, vitalsDataRange);
		getVitalsGenerator().setSpeed(2.0);
	}
	
	@Override
	Double getNormalMin() {
		return 70.0;
	}

	@Override
	Double getNormalMax() {
		return 78.0;
	}

	@Override
	Double getHighMin() {
		return 90.0;
	}

	@Override
	Double getHighMax() {
		return 110.0;
	}

	@Override
	Double getLowMin() {
		return 10.0;
	}

	@Override
	Double getLowMax() {
		return 30.0;
	}

	@Override
	String getReadingType() {
		return "ABPdias";
	}

	@Override
	String getUOM() {
		return "mmHg";
	}

}
