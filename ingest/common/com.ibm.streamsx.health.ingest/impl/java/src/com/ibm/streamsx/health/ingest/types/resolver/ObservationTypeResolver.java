package com.ibm.streamsx.health.ingest.types.resolver;

import com.ibm.streams.function.model.Function;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.health.ingest.types.model.ReadingTypeCode;

public class ObservationTypeResolver {

	private static String getCode(Observation observation) {
		return observation.getReading().getReadingType().getCode();
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead I
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead I
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadI(Observation observation) {
		return isECGLeadI(getCode(observation));
	}
	
	/**
	 * Returns `true` if the given Observation object represents an ECG Lead II
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead II
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadII(Observation observation) {
		return isECGLeadII(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead III
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead III
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadIII(Observation observation) {
		return isECGLeadIII(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V1
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V1
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV1(Observation observation) {
		return isECGLeadV1(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V2
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V2
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV2(Observation observation) {
		return isECGLeadV2(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V3
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V3
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV3(Observation observation) {
		return isECGLeadV3(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V4
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V4
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV4(Observation observation) {
		return isECGLeadV4(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V5
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V5
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV5(Observation observation) {
		return isECGLeadV5(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V6
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V6
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV6(Observation observation) {
		return isECGLeadV6(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V7
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V7
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV7(Observation observation) {
		return isECGLeadV7(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V8
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V8
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV8(Observation observation) {
		return isECGLeadV8(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V9
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V9
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV9(Observation observation) {
		return isECGLeadV9(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead AVF
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead AVF
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadAVF(Observation observation) {
		return isECGLeadAVF(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead AVL
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead AVL
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadAVL(Observation observation) {
		return isECGLeadAVL(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead AVR
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead AVR
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadAVR(Observation observation) {
		return isECGLeadAVR(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents a Heart Rate
	 * vital.
	 * 
	 * @return `true` if the Observation object represents a Heart Rate vital,
	 *         `false` otherwise
	 */
	public static boolean isHeartRate(Observation observation) {
		return isHeartRate(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents a Respiratory
	 * Rate vital.
	 * 
	 * @return `true` if the Observation object represents an Respiratory Rate
	 *         vital, `false` otherwise
	 */
	public static boolean isRespiratoryRate(Observation observation) {
		return isRespiratoryRate(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents an Impedance Respiratory
	 * wave form
	 * 
	 * @return `true` if the Observation object represents an Impedance Respiratory
	 *         waveform, `false` otherwise
	 */
	public static boolean isResp(Observation observation) {
		return isResp(getCode(observation));
	}
	
	/**
	 * Returns `true` if the given Observation object represents a Temperature
	 * vital.
	 * 
	 * @return `true` if the Observation object represents a Temperature vital,
	 *         `false` otherwise
	 */
	public static boolean isTemperature(Observation observation) {
		return isTemperature(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents an SpO2 vital.
	 * 
	 * @return `true` if the Observation object represents an SpO2 vital,
	 *         `false` otherwise
	 */
	public static boolean isSpO2(Observation observation) {
		return isSpO2(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents a Systolic
	 * Blood Pressure vital.
	 * 
	 * @return `true` if the Observation object represents a Systolic Blood
	 *         Pressure vital, `false` otherwise
	 */
	public static boolean isBPSystolic(Observation observation) {
		return isBPSystolic(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents a Diastolic
	 * Blood Pressure vital.
	 * 
	 * @return `true` if the Observation object represents a Diastolic Blood
	 *         Pressure vital, `false` otherwise
	 */
	public static boolean isBPDiastolic(Observation observation) {
		return isBPDiastolic(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents a Pleth
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents a Pleth waveform,
	 *         `false` otherwise
	 */
	public static boolean isPleth(Observation observation) {
		return isPleth(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents a Pulse vital.
	 * 
	 * @return `true` if the Observation object represents a Pulse vital,
	 *         `false` otherwise
	 */
	public static boolean isPulse(Observation observation) {
		return isPulse(getCode(observation));
	}

	/**
	 * Returns the human-readable label of the reading type for the given
	 * Observation object
	 * 
	 * @return the human readable label of the reading type, otherwise returns
	 *         the value for Observation.ReadingType.code
	 */
	public static String getLabel(Observation observation) {
		return getLabel(getCode(observation));
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead I
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead I
	 *         waveform, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isECGLeadI(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_I.getCode());
	}
	
	/**
	 * Returns `true` if the given Observation object represents an ECG Lead II
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead II
	 *         waveform, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isECGLeadII(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_II.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead III
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead III
	 *         waveform, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isECGLeadIII(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_III.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V1
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V1
	 *         waveform, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isECGLeadV1(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V1.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V2
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V2
	 *         waveform, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isECGLeadV2(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V2.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V3
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V3
	 *         waveform, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isECGLeadV3(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V3.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V4
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V4
	 *         waveform, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isECGLeadV4(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V4.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V5
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V5
	 *         waveform, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isECGLeadV5(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V5.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V6
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V6
	 *         waveform, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isECGLeadV6(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V6.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V7
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V7
	 *         waveform, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isECGLeadV7(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V7.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V8
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V8
	 *         waveform, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isECGLeadV8(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V8.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V9
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V9
	 *         waveform, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isECGLeadV9(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V9.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead AVF
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead AVF
	 *         waveform, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isECGLeadAVF(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_AVF.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead AVL
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead AVL
	 *         waveform, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isECGLeadAVL(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_AVL.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead AVR
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead AVR
	 *         waveform, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isECGLeadAVR(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_AVR.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents a Heart Rate
	 * vital.
	 * 
	 * @return `true` if the Observation object represents a Heart Rate vital,
	 *         `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isHeartRate(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.HEART_RATE.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents a Respiratory
	 * Rate vital.
	 * 
	 * @return `true` if the Observation object represents an Respiratory Rate
	 *         vital, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isRespiratoryRate(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.RESP_RATE.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an Impedance Respiratory
	 * wave form.
	 * 
	 * @return `true` if the Observation object represents an Impedance Respiratory wave form
	 *         vital, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isResp(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.RESP.getCode());
	}
	
	/**
	 * Returns `true` if the given Observation object represents a Temperature
	 * vital.
	 * 
	 * @return `true` if the Observation object represents a Temperature vital,
	 *         `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isTemperature(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.TEMPERATURE.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an SpO2 vital.
	 * 
	 * @return `true` if the Observation object represents an SpO2 vital,
	 *         `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isSpO2(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.SPO2.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents a Systolic
	 * Blood Pressure vital.
	 * 
	 * @return `true` if the Observation object represents a Systolic Blood
	 *         Pressure vital, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isBPSystolic(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.BP_SYSTOLIC.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents a Diastolic
	 * Blood Pressure vital.
	 * 
	 * @return `true` if the Observation object represents a Diastolic Blood
	 *         Pressure vital, `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isBPDiastolic(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.BP_DIASTOLIC.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents a Pleth
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents a Pleth waveform,
	 *         `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isPleth(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.PLETH.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents a Pulse vital.
	 * 
	 * @return `true` if the Observation object represents a Pulse vital,
	 *         `false` otherwise
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static boolean isPulse(String code) {
		return code.equalsIgnoreCase(ReadingTypeCode.PULSE.getCode());
	}

	/**
	 * Returns the human-readable label of the reading type for the given
	 * Observation object
	 * 
	 * @return the human readable label of the reading type, otherwise returns
	 *         the value for Observation.ReadingType.code
	 */
	@Function(namespace="com.ibm.streamsx.health.ingest.types.resolver")
	public static String getLabel(String code) {
		String obsCode = code;
		for (ReadingTypeCode readingTypeCode : ReadingTypeCode.values()) {
			if (obsCode.equalsIgnoreCase(readingTypeCode.getCode())) {
				return readingTypeCode.name().toLowerCase();
			}
		}

		return obsCode;
	}
	
}
