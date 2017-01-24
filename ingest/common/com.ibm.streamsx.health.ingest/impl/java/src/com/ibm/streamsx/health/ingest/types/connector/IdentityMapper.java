package com.ibm.streamsx.health.ingest.types.connector;

import java.util.ArrayList;
import java.util.List;

import com.ibm.streamsx.health.ingest.types.model.Observation;

public class IdentityMapper extends AbstractObservationMapper<Observation> {
	private static final long serialVersionUID = 1L;

	@Override
	public List<Observation> map(Observation obs) {
		List<Observation> observations = new ArrayList<Observation>();
		observations.add(obs);
		
		return observations;
	}

}
