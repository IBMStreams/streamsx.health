package com.ibm.streamsx.health.analyze.rules.model;

public class Patient {
	
	private String name;
	private String id;
	
	// vitals
	
	// body temperature  (C)
	private float temperature;
	
	// respiratory rate (breaths per minute)
	private float respRate;
	
	// heart rate in bits per minute (bpm)
	private float hr;
	
	// systolic blood pressure (mmHg) 
	private float bpSystolic;
	
	// diastolic blood pressure (mmHg)
	private float bpDiastolic;
	
	// SpO2 (%)
	private float spo2;
	
	// patient's consciousness
	private Consciousness avpu;
	
	// return true if patient is provided with supplement oxygen
	private boolean isSupplementOxygen;
	
	
	
	
	// EWS score based on vital signs (https://en.wikipedia.org/wiki/Early_warning_score)
	private int scoreEws;
	
	// National Early Warning Score (https://www.mdcalc.com/national-early-warning-score-news)
	private int scoreNews;
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public float getTemperature() {
		return temperature;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	public float getRespRate() {
		return respRate;
	}

	public void setRespRate(float respRate) {
		this.respRate = respRate;
	}

	public float getHr() {
		return hr;
	}

	public void setHr(float hr) {
		this.hr = hr;
	}

	public float getBpSystolic() {
		return bpSystolic;
	}

	public void setBpSystolic(float bpSystolic) {
		this.bpSystolic = bpSystolic;
	}

	public float getBpDiastolic() {
		return bpDiastolic;
	}

	public void setBpDiastolic(float bpDiastolic) {
		this.bpDiastolic = bpDiastolic;
	}

	public float getSpo2() {
		return spo2;
	}

	public void setSpo2(float spo2) {
		this.spo2 = spo2;
	}


	public int getEwsScore() {
		return scoreEws;
	}

	public void incrementEwsScore(int score) {
		scoreEws+=score;
	}
	
	public int getNewsScore() {
		return scoreNews;
	}
	
	public void incrementNewsScore(int score) {
		scoreNews+=score;
	}
	
	public void setAvpu(Consciousness avpu) {
		this.avpu = avpu;
	}
	
	public Consciousness getAvpu() {
		return avpu;
	}
	
	public boolean isSupplementOxygen() {
		return isSupplementOxygen;
	}
	
	public void setSupplementOxygen(boolean isSupplementOxygen) {
		this.isSupplementOxygen = isSupplementOxygen;
	}
	

}
