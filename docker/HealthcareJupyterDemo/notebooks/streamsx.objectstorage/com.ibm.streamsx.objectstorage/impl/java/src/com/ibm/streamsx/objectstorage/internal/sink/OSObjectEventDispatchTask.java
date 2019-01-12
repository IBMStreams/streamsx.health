package com.ibm.streamsx.objectstorage.internal.sink;

import org.ehcache.core.internal.events.EventListenerWrapper;
import org.ehcache.event.CacheEvent;

public class OSObjectEventDispatchTask<K, V> implements Runnable {
	private final CacheEvent<K, V> cacheEvent;
	private final Iterable<EventListenerWrapper<K, V>> listenerWrappers;

	public OSObjectEventDispatchTask(CacheEvent<K, V> cacheEvent, Iterable<EventListenerWrapper<K, V>> listener) {
		if (cacheEvent == null) {
			throw new NullPointerException("cache event cannot be null");
		}
		if (listener == null) {
			throw new NullPointerException("listener cannot be null");
		}
		this.cacheEvent = cacheEvent;
		this.listenerWrappers = listener;
	}

	@Override
	public void run() {
		for (EventListenerWrapper<K, V> listenerWrapper : listenerWrappers) {
			if (listenerWrapper.isForEventType(cacheEvent.getType())) {
				listenerWrapper.onEvent(cacheEvent);
			}
		}
	}
}