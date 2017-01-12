package com.ibm.streamsx.health.beacon.generators;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.ibm.streamsx.health.ingest.types.model.Device;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.health.ingest.types.model.PatientId;
import com.ibm.streamsx.health.ingest.types.model.Reading;
import com.ibm.streamsx.health.ingest.types.model.ReadingSource;
import com.ibm.streamsx.topology.function.Supplier;

public class HealthcareDataGenerator implements Supplier<Observation> {

	private static final long serialVersionUID = 1L;
		
	private int index = 0;
	
	private PatientId patientId;
	private String filename;
	private transient List<Observation> observations;
	
	public HealthcareDataGenerator(String patientId, String filename) {
		this.patientId = new PatientId();
		this.patientId.add(patientId);
		this.filename = filename;
	}
	
	public Object readResolve() throws Exception {
		observations = new ArrayList<Observation>();
		loadData(getInputStreamFromFile(filename), filename);

		return this;
	}

	private InputStream getInputStreamFromFile(String filename) throws FileNotFoundException {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(filename);
		if(is == null)
			throw new FileNotFoundException("Unable to find '" + this.filename + "' in any loaded libraries.");
		
		return is;
	}
	
	public void loadData(InputStream is, String filename) throws Exception {
		Device device = new Device();
		device.setId("HealthcareDataGenerator");

		ReadingSource readingSource = new ReadingSource();
		readingSource.setDeviceId(device.getId());
		readingSource.setSourceType("file");
		readingSource.setId(filename);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		
		// skip first 2 lines
		String[] obsTypes = reader.readLine().replace("'", "").split(",");
		String[] uom = reader.readLine().replace("'", "").split(",");
		
		String line = null;
		while((line = reader.readLine()) != null) {
			String[] values = line.split(",");
			for(int i = 1; i < values.length; i++) {
				Reading reading = new Reading();
				reading.setTimestamp(Long.valueOf(values[0]));
				reading.setReadingType(obsTypes[i]);
				reading.setUom(uom[i]);
				reading.setValue(Double.valueOf(values[i]));	
				
				observations.add(new Observation(device, patientId, readingSource, reading));
			}
		}
	}
	
	@Override
	public Observation get() {
		index = index+1 == observations.size() ? 0 : index+1;
		
		return observations.get(index);
	}

}
