package com.ibm.streamsx.health.prepare.uomconverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SupportedUOM {

	public String[] inputUOM();
	public String[] outputUOM();
}
