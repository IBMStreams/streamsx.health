package com.ibm.streamsx.health.beacon.generators;

public class TemperatureDataGenerator extends AbstractVitalsGenerator {
	private static final long serialVersionUID = 1L;

	public static final double NORMAL_MIN = 97;
	public static final double NORMAL_MAX = 99;
	
	public static final double LOW_MIN = 90;
	public static final double LOW_MAX = 95;
	
	public static final double HIGH_MIN = 101;
	public static final double HIGH_MAX = 110;
	
	
	public TemperatureDataGenerator(String patientId) {
		this(patientId, VitalsDataRange.NORMAL);
	}
	
	public TemperatureDataGenerator(String patientId, VitalsDataRange vitalsDataRange) {
		super(patientId, vitalsDataRange);
		getVitalsGenerator().setSpeed(2.0);
	}

	@Override
	Double getNormalMin() {
		return NORMAL_MIN;
	}

	@Override
	Double getNormalMax() {
		return NORMAL_MAX;
	}

	@Override
	Double getHighMin() {
		return HIGH_MIN;
	}

	@Override
	Double getHighMax() {
		return HIGH_MAX;
	}

	@Override
	Double getLowMin() {
		return LOW_MIN;
	}

	@Override
	Double getLowMax() {
		return LOW_MAX;
	}

	@Override
	String getReadingType() {
		return "Temperature";
	}

	@Override
	String getUOM() {
		return "F";
	}
}
