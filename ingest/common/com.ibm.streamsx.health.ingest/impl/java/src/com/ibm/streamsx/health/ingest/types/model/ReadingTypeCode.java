package com.ibm.streamsx.health.ingest.types.model;

public enum ReadingTypeCode {

	HEART_RATE("8867-4"),
	RESP_RATE("9279-1"),
	TEMPERATURE("8310-5"),
	SPO2("2710-2"),
	BP_SYSTOLIC("8480-6"),
	BP_DIASTOLIC("8462-4"),
	PLETH("X200-6"),
	PULSE("X201-4"),
	ECG_LEAD_I("X100-8"),
	ECG_LEAD_II("X101-6"),
	ECG_LEAD_III("X102-4"),
	ECG_LEAD_V1("X103-2"),
	ECG_LEAD_V2("X104-0"),
	ECG_LEAD_V3("X105-7"),
	ECG_LEAD_V4("X106-5"),
	ECG_LEAD_V5("X107-3"),
	ECG_LEAD_V6("X108-1"),
	ECG_LEAD_V7("X109-9"),
	ECG_LEAD_V8("X110-7"),
	ECG_LEAD_V9("X111-5"),
	ECG_LEAD_AVF("X112-3"),
	ECG_LEAD_AVL("X113-1"),
	ECG_LEAD_AVR("X114-9");	
	
	private String code;
	
	private ReadingTypeCode(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	public static ReadingTypeCode fromCode(String code) {
		for(ReadingTypeCode value : values()) {
			if(value.code.equals(code))
				return value;
		}
		
		return null;
	}
}
