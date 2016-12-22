package com.ibm.streamsx.health.vines.model;

import java.io.Serializable;

public class Term implements ITerm, Serializable {

	private static final long serialVersionUID = 1L;

	private String Code;
	private String LowerValue;
	private String UpperValue;
	private ITermValue Value;
	private String UOM;
	private String UOMCode;
	private String Date;

	public String getCode() {
		return Code;
	}

	public String getLowerValue() {
		return LowerValue;
	}

	public String getUpperValue() {
		return UpperValue;
	}

	public ITermValue getValue() {
		return Value;
	}

	public String getUOM() {
		return UOM;
	}

	public String getUOMCode() {
		return UOMCode;
	}

	public String getDate() {
		return Date;
	}

	@Override
	public String toString() {
		return "Term [Code=" + Code + ", LowerValue=" + LowerValue + ", UpperValue=" + UpperValue + ", Value=" + Value
				+ ", UOM=" + UOM + ", UOMCode=" + UOMCode + ", Date=" + Date + "]";
	}

}
