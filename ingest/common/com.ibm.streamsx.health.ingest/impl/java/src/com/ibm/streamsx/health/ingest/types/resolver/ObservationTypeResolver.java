package com.ibm.streamsx.health.ingest.types.resolver;

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
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_I.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead II
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead II
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadII(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_II.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead III
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead III
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadIII(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_III.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V1
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V1
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV1(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V1.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V2
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V2
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV2(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V2.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V3
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V3
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV3(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V3.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V4
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V4
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV4(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V4.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V5
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V5
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV5(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V5.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V6
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V6
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV6(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V6.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V7
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V7
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV7(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V7.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V8
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V8
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV8(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V8.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead V9
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead V9
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadV9(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_V9.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead AVF
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead AVF
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadAVF(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_AVF.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead AVL
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead AVL
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadAVL(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_AVL.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an ECG Lead AVR
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents an ECG Lead AVR
	 *         waveform, `false` otherwise
	 */
	public static boolean isECGLeadAVR(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.ECG_LEAD_AVR.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents a Heart Rate
	 * vital.
	 * 
	 * @return `true` if the Observation object represents a Heart Rate vital,
	 *         `false` otherwise
	 */
	public static boolean isHeartRate(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.HEART_RATE.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents a Respiratory
	 * Rate vital.
	 * 
	 * @return `true` if the Observation object represents an Respiratory Rate
	 *         vital, `false` otherwise
	 */
	public static boolean isRespiratoryRate(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.RESP_RATE.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents a Temperature
	 * vital.
	 * 
	 * @return `true` if the Observation object represents a Temperature vital,
	 *         `false` otherwise
	 */
	public static boolean isTemperature(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.TEMPERATURE.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents an SpO2 vital.
	 * 
	 * @return `true` if the Observation object represents an SpO2 vital,
	 *         `false` otherwise
	 */
	public static boolean isSpO2(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.SPO2.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents a Systolic
	 * Blood Pressure vital.
	 * 
	 * @return `true` if the Observation object represents a Systolic Blood
	 *         Pressure vital, `false` otherwise
	 */
	public static boolean isBPSystolic(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.BP_SYSTOLIC.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents a Diastolic
	 * Blood Pressure vital.
	 * 
	 * @return `true` if the Observation object represents a Diastolic Blood
	 *         Pressure vital, `false` otherwise
	 */
	public static boolean isBPDiastolic(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.BP_DIASTOLIC.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents a Pleth
	 * waveform.
	 * 
	 * @return `true` if the Observation object represents a Pleth waveform,
	 *         `false` otherwise
	 */
	public static boolean isPleth(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.PLETH.getCode());
	}

	/**
	 * Returns `true` if the given Observation object represents a Pulse vital.
	 * 
	 * @return `true` if the Observation object represents a Pulse vital,
	 *         `false` otherwise
	 */
	public static boolean isPulse(Observation observation) {
		return getCode(observation).equalsIgnoreCase(ReadingTypeCode.PULSE.getCode());
	}

	/**
	 * Returns the human-readable label of the reading type for the given
	 * Observation object
	 * 
	 * @return the human readable label of the reading type, otherwise returns
	 *         the value for Observation.ReadingType.code
	 */
	public static String getLabel(Observation observation) {
		String obsCode = getCode(observation);
		for (ReadingTypeCode readingTypeCode : ReadingTypeCode.values()) {
			if (obsCode.equalsIgnoreCase(readingTypeCode.getCode())) {
				return readingTypeCode.name().toLowerCase();
			}
		}

		return obsCode;
	}

}
