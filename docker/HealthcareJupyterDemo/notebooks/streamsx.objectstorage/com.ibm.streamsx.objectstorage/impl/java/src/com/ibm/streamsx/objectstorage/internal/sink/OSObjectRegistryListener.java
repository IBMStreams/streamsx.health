package com.ibm.streamsx.objectstorage.internal.sink;

import java.util.logging.Logger;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

import com.ibm.streams.operator.logging.LoggerNames;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.objectstorage.BaseObjectStorageSink;
import com.ibm.streamsx.objectstorage.Messages;

/**
 * Listener for cache lifecycle events
 * @author streamsadmin
 *
 */
public class OSObjectRegistryListener implements CacheEventListener<String, OSObject> {
	
	private BaseObjectStorageSink fParent;
	
	private static final String CLASS_NAME = OSObjectRegistryListener.class.getName();
	
	private static Logger TRACE = Logger.getLogger(CLASS_NAME);
		
	private static Logger LOGGER = Logger.getLogger(LoggerNames.LOG_FACILITY + "." + CLASS_NAME);

	private boolean enableTrhresholdNotification = true;
	
	public OSObjectRegistryListener(BaseObjectStorageSink parent) {
		fParent = parent;
	}

	@Override
	public void onEvent(CacheEvent<? extends String, ? extends OSObject> event)  {
		OSObject osObject = event.getOldValue();
		
		if (TRACE.isLoggable(TraceLevel.TRACE)) {			
			TRACE.log(TraceLevel.TRACE,	"Event received for partition '" + event.getKey() + "' of type '" + event.getType() + "'");
			if (osObject != null) TRACE.log(TraceLevel.TRACE,	"About to process OSObject: \n  '" + osObject.toString() + "'");
		}		
		// when active objects number is less than number of allowed concurrent partitions then
		// allow notifications generation.
		// this prevents repeatable notifications generation when active objects number
		// is greater than maximum number of concurrent partitions
		if (fParent.getActiveObjectsMetric().getValue() < fParent.getMaxConcurrentPartitionsNumMetric().getValue()) { 
			enableTrhresholdNotification = true;
		} 			
		
		switch (event.getType()) {
		case CREATED: // new entry added
			fParent.getActiveObjectsMetric().increment();
			break;
		
		case REMOVED: // entry has been removed
			writeObject(osObject);
			break;		
		case EXPIRED: // OSObject is expired according to Expiry
			          // derived from operator rolling policy 
			writeObject(osObject);
			break;
		case EVICTED: // no space left for new entries
			writeObject(osObject);
			// active objects number is greater than max concurrent partitions -  happens 
			// for partitions case only
			if ((fParent.getActiveObjectsMetric().getValue() > fParent.getMaxConcurrentPartitionsNumMetric().getValue()) && enableTrhresholdNotification) {
				String generalWarningMsg = Messages.getString("OBJECTSTORAGE_SINK_MAX_PARTITIONS_COUNT_REACHED", 													
														fParent.getActiveObjectsMetric().getValue(), 
														fParent.getMaxConcurrentPartitionsNumMetric().getValue());
	
				// Should be WARNING - NOT ERROR 
				//if (TRACE.isLoggable(TraceLevel.WARNING)) {
				//	TRACE.log(TraceLevel.WARNING,	generalWarningMsg);
				//}
				if (TRACE.isLoggable(TraceLevel.ERROR)) {
					TRACE.log(TraceLevel.ERROR,	generalWarningMsg);
				}
		    	LOGGER.log(TraceLevel.WARNING, generalWarningMsg);
		    	enableTrhresholdNotification = false;
			}
			// warn about closing object ahead of time
			String objectSpecificWarningMsg = Messages.getString("OBJECTSTORAGE_SINK_OBJECT_CLOSED_PRIOR_RP_EXPIRY", osObject.getPath());
			if (TRACE.isLoggable(TraceLevel.WARNING)) {
				TRACE.log(TraceLevel.WARNING,	objectSpecificWarningMsg);
			}
	    	LOGGER.log(TraceLevel.WARNING, objectSpecificWarningMsg);
		    break;			
		default:
			String errMsg = "Unknown event type '" + event.getType() + "' for key '" + event.getKey() + "' has been received.";
			LOGGER.log(TraceLevel.ERROR, Messages.getString(errMsg));
			break;
		}
	}

	
	private void writeObject(OSObject osObject) {
		try {
			// create writable OSObject
			OSWritableObject writableObject = osObject.isWritable() ? 
															(OSWritableObject)osObject : 
															new OSWritableObject(osObject, fParent.getOperatorContext(), fParent.getObjectStorageClient());
			// flush buffer
			writableObject.flushBuffer();
			// close object
			writableObject.close();
			
			// update metrics			
			fParent.getActiveObjectsMetric().incrementValue(-1);
			fParent.getCloseObjectsMetric().increment();
			
			// submit output 
			fParent.submitOnOutputPort(osObject.getPath());	
		} 
		catch (Exception e) {
			// for more detailed error analysis - implement logic for AmazonS3Exception analysis
			// the key parameters that should be taken into account are: error code and status code			
			String errRootCause = com.ibm.streamsx.objectstorage.Utils.getErrorRootCause(e);
			String errMsg = Messages.getString("OBJECTSTORAGE_SINK_OBJECT_CLOSE_FAILURE", fParent.getBucketName(), osObject.getPath(), errRootCause);
			if (TRACE.isLoggable(TraceLevel.ERROR)) {
				TRACE.log(TraceLevel.ERROR,	errMsg);
				TRACE.log(TraceLevel.ERROR,	"Failed to write object '" + osObject.getPath() + "' to bucket '"
						 + fParent.getBucketName() + "'. Exception: " + e.getMessage());
			}
	    	LOGGER.log(TraceLevel.ERROR, errMsg);
	    	
	    	// decrement active objects metric even 
 			// when the close failed
	    	fParent.getActiveObjectsMetric().incrementValue(-1);
	    			
			throw new RuntimeException(e);
		} 
		
	}
	
}
