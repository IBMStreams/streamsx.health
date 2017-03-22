/* Copyright (C) 2016, International Business Machines Corporation */
/* All Rights Reserved */

package demo;

import java.util.ArrayList;
import java.util.List;

public class Patient {
	private String name;
	private int age;
	private String roomInfo;
	
	//Stats
	private float temperature;
	private float heartRate;
	
	private float hrAverage;
	
	private int bpSystolic;
	private int bpDiastolic;
	private int spO2;
	
	
	//Alerts
	private boolean alert = false;
	
	public List<String> messages = new ArrayList<>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getRoomInfo() {
		return roomInfo;
	}
	public void setRoomInfo(String roomInfo) {
		this.roomInfo = roomInfo;
	}
	public boolean isAlert() {
		return alert;
	}
	public void setAlert() {
		this.alert = true;
	}
	
	public void addToMessages(String message) {
		messages.add(message);
	}
	
	public float getTemperature() {
		return temperature;
	}
	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}
	public float getHeartRate() {
		return heartRate;
	}
	public void setHeartRate(float heartRate) {
		this.heartRate = heartRate;
	}
	public int getBpSystolic() {
		return bpSystolic;
	}
	public void setBpSystolic(int bpSystolic) {
		this.bpSystolic = bpSystolic;
	}
	public int getBpDiastolic() {
		return bpDiastolic;
	}
	public void setBpDiastolic(int bpDiastolic) {
		this.bpDiastolic = bpDiastolic;
	}
	public int getSpO2() {
		return spO2;
	}
	public void setSpO2(int spO2) {
		this.spO2 = spO2;
	}
	public float getHrAverage() {
		return hrAverage;
	}
	public void setHrAverage(float hrAverage) {
		this.hrAverage = hrAverage;
	}
	
}
