package com.ibm.streamsx.objectstorage.internal.sink;

import java.util.concurrent.TimeUnit;

import org.ehcache.ValueSupplier;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expiry;

public class TimePerObjectExpiry implements Expiry<Object, Object> {

	private int fTimePerObjectSecs = 0;
	
	public TimePerObjectExpiry(int timePerObjectSecs) {
		fTimePerObjectSecs = timePerObjectSecs;
	}
	
	@Override
	public Duration getExpiryForCreation(Object paramK, Object paramV) {
		OSObject value = (OSObject)paramV;
		value.setExpiryTSMillis(System.currentTimeMillis() + fTimePerObjectSecs * 1000);
		
		// initial based on operator expiratin config
		return Duration.of(fTimePerObjectSecs, TimeUnit.SECONDS);
	}

	@Override
	public Duration getExpiryForAccess(Object paramK, ValueSupplier<? extends Object> paramValueSupplier) {
		OSObject value = (OSObject)paramValueSupplier.value();
		
		long ttl = value.getExpiryTSMillis() - System.currentTimeMillis();
	
		return ttl <= 0 ? Duration.ZERO : Duration.of(ttl, TimeUnit.MILLISECONDS);	
	}

	@Override
	public Duration getExpiryForUpdate(Object paramK, ValueSupplier<? extends Object> paramValueSupplier,
			Object paramV) {
		
		OSObject value = (OSObject)paramValueSupplier.value();
		
		long ttl = value.getExpiryTSMillis() - System.currentTimeMillis();
	
		return ttl <= 0 ? Duration.ZERO : Duration.of(ttl, TimeUnit.MILLISECONDS);				
	}

}


