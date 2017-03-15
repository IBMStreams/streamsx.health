package com.ibm.streamsx.health.ingest.types.resolve.test;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.health.ingest.types.model.Reading;
import com.ibm.streamsx.health.ingest.types.model.ReadingType;
import com.ibm.streamsx.health.ingest.types.resolver.ObservationTypeResolver;

public class ResolverTests {

	private Observation obs;
	private Reading reading;
	private ReadingType readingType;
	
	@Before
	public void setup() {
		readingType = new ReadingType();
		
		reading = new Reading();
		reading.setReadingType(readingType);
		
		obs = new Observation();
		obs.setReading(reading);
	}
	
	
	@Test
	public void testResolver() {
		readingType.setCode("8867-4");
		assertTrue(ObservationTypeResolver.isHeartRate(obs));
		
		readingType.setCode("9279-1");
		assertTrue(ObservationTypeResolver.isRespiratoryRate(obs));
		
		readingType.setCode("8310-5");
		assertTrue(ObservationTypeResolver.isTemperature(obs));
		
		readingType.setCode("2710-2");
		assertTrue(ObservationTypeResolver.isSpO2(obs));
		
		readingType.setCode("8480-6");
		assertTrue(ObservationTypeResolver.isBPSystolic(obs));
		
		readingType.setCode("8462-4");
		assertTrue(ObservationTypeResolver.isBPDiastolic(obs));
		
		readingType.setCode("X200-6");
		assertTrue(ObservationTypeResolver.isPleth(obs));
		
		readingType.setCode("X201-4");
		assertTrue(ObservationTypeResolver.isPulse(obs));
		
		readingType.setCode("X100-8");
		assertTrue(ObservationTypeResolver.isECGLeadI(obs));
		
		readingType.setCode("X101-6");
		assertTrue(ObservationTypeResolver.isECGLeadII(obs));
		
		readingType.setCode("X102-4");
		assertTrue(ObservationTypeResolver.isECGLeadIII(obs));
		
		readingType.setCode("X103-2");
		assertTrue(ObservationTypeResolver.isECGLeadV1(obs));
		
		readingType.setCode("X104-0");
		assertTrue(ObservationTypeResolver.isECGLeadV2(obs));
		
		readingType.setCode("X105-7");
		assertTrue(ObservationTypeResolver.isECGLeadV3(obs));
		
		readingType.setCode("X106-5");
		assertTrue(ObservationTypeResolver.isECGLeadV4(obs));
		
		readingType.setCode("X107-3");
		assertTrue(ObservationTypeResolver.isECGLeadV5(obs));
		
		readingType.setCode("X108-1");
		assertTrue(ObservationTypeResolver.isECGLeadV6(obs));
		
		readingType.setCode("X109-9");
		assertTrue(ObservationTypeResolver.isECGLeadV7(obs));
		
		readingType.setCode("X110-7");
		assertTrue(ObservationTypeResolver.isECGLeadV8(obs));
		
		readingType.setCode("X111-5");
		assertTrue(ObservationTypeResolver.isECGLeadV9(obs));
		
		readingType.setCode("X112-3");
		assertTrue(ObservationTypeResolver.isECGLeadAVF(obs));
		
		readingType.setCode("X113-1");
		assertTrue(ObservationTypeResolver.isECGLeadAVL(obs));
		
		readingType.setCode("X114-9");
		assertTrue(ObservationTypeResolver.isECGLeadAVR(obs));
	}	
}














