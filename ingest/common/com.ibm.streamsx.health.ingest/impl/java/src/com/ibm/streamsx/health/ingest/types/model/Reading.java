/* begin_generated_IBM_copyright_prolog                                       */
/*                                                                            */
/* This is an automatically generated copyright prolog.                       */
/* After initializing,  DO NOT MODIFY OR MOVE                                 */
/******************************************************************************/
/* Copyright (C) 2016 International Business Machines Corporation             */
/* All Rights Reserved                                                        */
/******************************************************************************/
/* end_generated_IBM_copyright_prolog                                         */
package com.ibm.streamsx.health.ingest.types.model;

import java.io.Serializable;

public class Reading implements Serializable {
	private static final long serialVersionUID = 1L;

	private long ts;
	private ReadingType readingType;
	private double value;
	private String uom;
	private String valueString;

	public Reading() {
		ts = -1;
		readingType = new ReadingType();
		value = -1;
		uom = "";
		valueString = "";
	}
	
	public Reading(Reading reading) {
		this.ts = reading.ts;
		this.readingType = reading.readingType;
		this.uom = reading.uom;
		
		setValue(reading.value);
	}

	public long getTimestamp() {
		return ts;
	}

	public void setTimestamp(long timestamp) {
		this.ts = timestamp;
	}

	public ReadingType getReadingType() {
		return readingType;
	}

	public void setReadingType(ReadingType readingType) {
		this.readingType = readingType;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
		
		if (valueString == null || valueString.isEmpty())
			setValueString(String.valueOf(value));
	}

	public String getUom() {
		return uom;
	}

	public void setUom(String uom) {
		this.uom = uom;
	}
	
	public void setValueString(String valueString) {
		this.valueString = valueString;
	}
	
	public String getValueString() {
		return valueString;
	}

	@Override
	public String toString() {
		return "Reading [ts=" + ts + ", readingType=" + readingType + ", value=" + value + ", uom=" + uom
				+ ", valueString=" + valueString + "]";
	}

}
