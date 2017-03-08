package com.ibm.streamsx.health.vines;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.health.vines.model.Vines;

public class VinesParserResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private List<Observation> observations;
	private List<String> errorMessages;
	private String rawMessage;
	
		
    public VinesParserResult(Vines vines) {
        this.rawMessage = vines.getRawMessage();
        this.observations = new ArrayList<Observation>();
        this.errorMessages = new ArrayList<String>();
    }   
    
    public void addObservation(Observation observation) {
        this.observations.add(observation);
    }   

    public void addObservations(List<Observation> observations) {
        this.observations.addAll(observations);
    }   
    
    public void addErrorMesage(String message) {
        this.errorMessages.add(message);
    }   
    
    public void setRawMessage(String rawMessage) {
        this.rawMessage = rawMessage;
    }   
    
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public String getRawMessage() {
		return rawMessage;
	}

    public List<Observation> getObservations() {
        return observations;
    }

}
