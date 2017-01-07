package com.ibm.streamsx.health.vines;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import com.ibm.streamsx.datetime.convert.ISO8601;
import com.ibm.streamsx.health.ingest.types.connector.AbstractObservationMapper;
import com.ibm.streamsx.health.ingest.types.model.Device;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.health.ingest.types.model.PatientId;
import com.ibm.streamsx.health.ingest.types.model.Reading;
import com.ibm.streamsx.health.ingest.types.model.ReadingSource;
import com.ibm.streamsx.health.vines.model.Channel;
import com.ibm.streamsx.health.vines.model.ITerm;
import com.ibm.streamsx.health.vines.model.ITermValue;
import com.ibm.streamsx.health.vines.model.Patient;
import com.ibm.streamsx.health.vines.model.Term;
import com.ibm.streamsx.health.vines.model.TermArray;
import com.ibm.streamsx.health.vines.model.TermValueMap;
import com.ibm.streamsx.health.vines.model.TermValueString;
import com.ibm.streamsx.health.vines.model.Terms;
import com.ibm.streamsx.health.vines.model.Vines;
import com.ibm.streamsx.health.vines.model.WaveformHelper;

public class VinesToChefMapper extends AbstractObservationMapper<Vines> {

	private static final long serialVersionUID = 1L;
	public static final String SOURCE_TYPE = "channel";
	
	@Override
	public List<Observation> map(Vines v) {
		List<Observation> observations = null;
		
		// determine if message is a vitals or waveform message		
		Terms terms = v.getData().getBody().getTerms();
		
		// waveform messages only contain a single channel,
		// vitals messages can contain more than one channel
		if(terms.size() > 1) {
			// more than 1 channel indicates 
			// this is a vitals message
			observations = mapVitalMessage(v);	
		} else if(terms.size() == 1) {
			// may be either vitals or waveform so check for
			// MDC_ATTR_WAV term, which is always present in
			// waveform message
			Channel channel = terms.get(terms.getChannelNames().get(0));
			if(channel.containsKey("MDC_ATTR_WAV")) {
				observations = mapWaveformMessage(v);
			} else {
				observations = mapVitalMessage(v);
			}
		}
		return observations;
	}
	
	private List<Observation> mapVitalMessage(Vines v) {
		List<Observation> observations = new ArrayList<Observation>();

		// generate Patient ID
		PatientId patientId = new PatientId();
		
		Patient patient = v.getData().getPatient();
		if(patient != null) {
			patientId.add(patient.get_id());			
		}
		
		// generate device type (same for all observations)
		Device device = new Device();
		device.setId(getDeviceId(v));
		
		Terms terms = v.getData().getBody().getTerms();
		for(String channelName : terms.getChannelNames()) {
			Channel channel = terms.getChannel(channelName);
			
			// generate ReadingSource
			ReadingSource readingSource = new ReadingSource();
			readingSource.setId(channelName);
			readingSource.setSourceType(SOURCE_TYPE);
			readingSource.setDeviceId(getDeviceId(v));
			
			// iterate over all terms and generate Readings & Observations
			for(String termName : channel.getTermNames()) {
				ITerm term = channel.getTerm(termName);
				if(term instanceof Term) {
					Reading reading = new Reading();

					Term t = (Term)term;
					ITermValue itv = t.getValue();
					if(itv instanceof TermValueString) {
					
						String value = ((TermValueString)itv).getValue();
						if(!NumberUtils.isNumber(value)) {
							continue; // skip term as there is no value
						}
						
						reading.setValue(Double.valueOf(value));
						reading.setUom(t.getUOM());
						reading.setReadingType(termName);
						try {
							reading.setTimestamp(ISO8601.fromIso8601ToMillis(t.getDate()));
						} catch(ParseException e) {
							// TODO - handle exception
						}
						
						observations.add(new Observation(device, patientId, readingSource, reading));
					}
				} else {
					// Array terms not expected in normal vines messages
				}
			}
		}
		
		return observations;
	}
	
	private List<Observation> mapWaveformMessage(Vines v) {
		List<Observation> observations = new ArrayList<Observation>();
		
		// generate Patient ID
		PatientId patientId = new PatientId();
		Patient patient = v.getData().getPatient();
		if(patient != null) {
			patientId.add(patient.get_id());			
		}
		
		// generate device type (same for all observations)
		Device device = new Device();
		device.setId(getDeviceId(v));

		long startTime = 0;
		long period = 0;
		try {
			startTime = ISO8601.fromIso8601ToMillis(v.getData().getBody().getStartTime());			
		} catch(ParseException e) {
			// TODO - handle exception
		}
		
		Terms terms = v.getData().getBody().getTerms();
		Channel channel = terms.getChannel(terms.getChannelNames().get(0));

		ReadingSource readingSource = new ReadingSource();
		readingSource.setSourceType(SOURCE_TYPE);
		readingSource.setId(terms.getChannelNames().get(0));
		readingSource.setDeviceId(getDeviceId(v));
		
		// get the sample rate
		ITerm iterm = channel.getTerm("MDC_ATTR_SAMP_RATE");
		if(iterm instanceof Term) {
			Term term = (Term)iterm;
			
			// set the multiplier based on the UOM
			// in order to convert the sample rate
			// milliseconds
			double dividend = 1;
			if(term.getUOM().equals("MDC_DIM_SEC")) {
				dividend = 1000;
			}
			
			ITermValue itv = term.getValue();
			if(itv instanceof TermValueString) {
				String value = ((TermValueString)itv).getValue();
				if(NumberUtils.isNumber(value)) {
					period = Math.round((dividend / Double.valueOf(value)));
				}
			}
		}

		// map waveform samples to observations
		iterm = channel.getTerm("MDC_ATTR_WAV");
		if(iterm instanceof TermArray) {
			TermArray term = (TermArray)iterm;
			for(ITermValue itv : term) {
				if(itv instanceof TermValueMap) {
					TermValueMap tvm = (TermValueMap)itv;
					for(String waveName : tvm.keySet()) {
						ITerm waveITerm = tvm.get(waveName);
						if(waveITerm instanceof Term) {
							Term waveTerm = (Term)waveITerm;
							ITermValue waveTermValue = waveTerm.getValue();
							if(waveTermValue instanceof TermValueString) {
								String waveStr = ((TermValueString)waveTermValue).getValue();
								List<Double> waveform = WaveformHelper.parseWaveform(waveStr);
								
								for(int i = 0; i < waveform.size(); ++i) {
									Reading reading = new Reading();
									reading.setReadingType(waveName);
									reading.setValue(waveform.get(i));
									reading.setTimestamp(startTime + period*i);
									
									observations.add(new Observation(device, patientId, readingSource, reading));
								}
							}
						}
					}
				}
			}
		}
		
		return observations;
	}
	
	private String getDeviceId(Vines v) {
		return v.getData().getHeader().getDeviceId();
	}
}










