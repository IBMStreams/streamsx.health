package com.ibm.streamsx.health.simulate.beacon.generators;

import org.apache.edgent.function.Supplier;

import com.ibm.streamsx.health.ingest.types.model.Device;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.health.ingest.types.model.Reading;
import com.ibm.streamsx.health.ingest.types.model.ReadingSource;
import com.ibm.streamsx.health.ingest.types.model.ReadingType;


public abstract class AbstractVitalsGenerator implements Supplier<Observation> {
	private static final long serialVersionUID = 1L;

	private int count = 0;

	private String patientId;
	private VitalsDataGenerator vitalsGen;
	
	public AbstractVitalsGenerator(String patientId, VitalsDataRange vitalsDataRange) {
		this.patientId = patientId;
		
		switch(vitalsDataRange) {
		case NORMAL:
			vitalsGen = new VitalsDataGenerator(getNormalMin(), getNormalMax());
			break;
		case HIGH:
			vitalsGen = new VitalsDataGenerator(getHighMin(), getHighMax());
			break;
		case LOW:
			vitalsGen = new VitalsDataGenerator(getLowMin(), getLowMax());
			break;
		}
	}

	protected VitalsDataGenerator getVitalsGenerator() {
		return vitalsGen;
	}
	
	@Override
	public Observation get() {
		Reading reading = new Reading();
		reading.setReadingType(getReadingType());
		reading.setTimestamp(++count);
		reading.setUom(getUOM());
		reading.setValue(vitalsGen.next());

		Device device = new Device();
		device.setId(getDeviceId());
		
		ReadingSource readingSource = new ReadingSource();
		readingSource.setDeviceId(getDeviceId());
		readingSource.setSourceType("generated");

		return new Observation(device, patientId, readingSource, reading);
	}
	
	abstract Double getNormalMin();
	abstract Double getNormalMax();
	abstract Double getHighMin();
	abstract Double getHighMax();
	abstract Double getLowMin();
	abstract Double getLowMax();
	abstract ReadingType getReadingType();
	abstract String getUOM();
	
	public String getDeviceId() {
		return "VitalsGenerator";
	}
	
}
