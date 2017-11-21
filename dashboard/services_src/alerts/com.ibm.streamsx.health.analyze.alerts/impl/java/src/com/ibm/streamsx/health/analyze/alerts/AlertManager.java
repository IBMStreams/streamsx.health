package com.ibm.streamsx.health.analyze.alerts;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.OutputTuple;
import com.ibm.streams.operator.StreamingOutput;
import com.ibm.streams.operator.Tuple;
import com.ibm.streamsx.health.analyze.alerts.rules.VitalRule;

public class AlertManager {

	private static final String ALERT_TRIGGERED = "ALERT_TRIGGERED";
	private static final String ALERT_CANCELED = "ALERT_CANCELED";
	
	private static class AlertMessage {
		@SuppressWarnings("unused")
		String type;
		@SuppressWarnings("unused")
		String patientId;
		@SuppressWarnings("unused")
		String ruleName;
		@SuppressWarnings("unused")
		Long epochSeconds;
		@SuppressWarnings("unused")
		String notes;
		@SuppressWarnings("unused")
		String description;
	}
	
	private Map<String /* patientId */, Map<String /* ruleName */, Boolean /* isAlerted */>> alertMap;
	private OperatorContext context;
	private Gson gson;
	
	public AlertManager(OperatorContext context) {
		this.context = context;
		alertMap = new HashMap<String, Map<String,Boolean>>();
		gson = new Gson();
	}
	
	public void triggerAlert(String patientId, VitalRule vitalRule, Long epochSeconds, Tuple inputTuple, String additionalMessage) {
		String msg = createAlertMessage(patientId, vitalRule, ALERT_TRIGGERED, epochSeconds, additionalMessage);
		
		setAlert(patientId, vitalRule, true);
		submitTuple(patientId, msg, inputTuple);
	}

	public void cancelAlert(String patientId, VitalRule vitalRule, Long epochSeconds, Tuple inputTuple, String additionalMessage) {
		String msg = createAlertMessage(patientId, vitalRule, ALERT_CANCELED, epochSeconds, additionalMessage);
		
		setAlert(patientId, vitalRule, false);
		submitTuple(patientId, msg, inputTuple);
	}

	
	public Map<String, Boolean> getAlertsForPatient(String patientId) {
		Map<String, Boolean> alerts = alertMap.get(patientId);
		if(alerts == null) {
			alerts = new HashMap<String, Boolean>();
			alertMap.put(patientId, alerts);
		}
		
		return alerts;
	}
	
	public boolean isAlertTriggeredForRule(String patientId, VitalRule rule) {
		Map<String, Boolean> alertsForPatient = getAlertsForPatient(patientId);
		
		Boolean isAlerted = alertsForPatient.get(rule.getParentRuleName());
		if(isAlerted == null) {
			isAlerted = false; 
			alertsForPatient.put(rule.getParentRuleName(), isAlerted);
		}
		
		return isAlerted;
	}
	
	private String createAlertMessage(String patientId, VitalRule vitalRule, 
			String alertType, Long epochSeconds, String additionalMessage) {
		AlertMessage msg = new AlertMessage();
		msg.type = alertType;
		msg.patientId = patientId;
		msg.ruleName = vitalRule.getParentRuleName();
		msg.epochSeconds = epochSeconds;
		msg.notes = additionalMessage;
		msg.description = vitalRule.getName();
		
		return gson.toJson(msg);
	}
	
	private void setAlert(String patientId, VitalRule vitalRule, boolean isAlerted) {
		alertMap.get(patientId).put(vitalRule.getParentRuleName(), isAlerted);
	}
	
	private void submitTuple(String patientId, String alertMessage, Tuple inputTuple) {
		StreamingOutput<OutputTuple> outputPort = this.context.getStreamingOutputs().get(0);
		
		OutputTuple tuple = outputPort.newTuple();
		if(inputTuple != null) {
			tuple.assign(inputTuple);
		} else {
			tuple.setString("patientId", patientId);			
		}
		tuple.setString("alert", alertMessage);
		
		try {
			outputPort.submit(tuple);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
