package com.ibm.streamsx.health.beacon.generators;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	Double getLowMax() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	String getReadingType() {
		return "ABPsys";
	}

	@Override
	String getUOM() {
		return "mmHg";
	}

}
