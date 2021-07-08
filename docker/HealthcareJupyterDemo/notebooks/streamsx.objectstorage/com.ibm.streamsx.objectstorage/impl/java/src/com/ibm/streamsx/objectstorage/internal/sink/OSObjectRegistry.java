package com.ibm.streamsx.objectstorage.internal.sink;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.UserManagedCache;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.UserManagedCacheBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.event.EventFiring;
import org.ehcache.event.EventOrdering;
import org.ehcache.event.EventType;
import org.ehcache.expiry.Expiry;
import org.ehcache.impl.config.event.DefaultCacheEventListenerConfiguration;

import com.ibm.streams.function.samples.jvm.SystemFunctions;
import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.logging.LoggerNames;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streams.operator.state.ConsistentRegionContext;
import com.ibm.streamsx.objectstorage.BaseObjectStorageSink;
import com.ibm.streamsx.objectstorage.IObjectStorageConstants;
import com.ibm.streamsx.objectstorage.Utils;
import com.ibm.streamsx.objectstorage.writer.parquet.ParquetOSWriter;

/**
 * Manages registry of open objects
 * per partition.
 * @author streamsadmin
 *
 */
public class OSObjectRegistry {
	
	private static final String CLASS_NAME = OSObjectRegistry.class.getName(); 
	private static Logger LOGGER = Logger.getLogger(LoggerNames.LOG_FACILITY + "." + CLASS_NAME); 

	/**
	 * EHCache configuration
	 */
	
	private static final String OS_OBJECT_CACHE_NAME_PREFIX = "OSObjectCache";
	
	
	// dispatcher concurrency
	private static final int CACHE_DISPATCHER_CONCURRENCY = 1;
	private static final long SIZE_OF_MAX_OBJECT_GRAPH = 1024 * 512;
	
	// number of workers that defines the level of upload parallelity
	// @TODO: make this parameter dynamic and dependent on the 
	// upload (= object close) task queue depth
	private static final int UPLOAD_WORKERS_CORE_POOL_SIZE = 15;
	private static final int UPLOAD_WORKERS_DELTA_TO_MAX_POOL_SIZE = 5;
	private static final int UPLOAD_WORKERS_KEEP_ALIVE_TIME = 100;
	private static final TimeUnit UPLOAD_WORKERS_KEEP_ALIVE_TIME_UNIT = TimeUnit.MILLISECONDS;

	//@TODO: currently keeping EHCACHE task queue size
	// static. Consider to use SystemFunctions.totalMemory();
	// dynamic percentage instead. Once the limit is reached
	// the submit call is about to be blocked and the operator
	// is going to create a back pressure
	private static final int TASK_QUEUE_MAX_SIZE = 1000;
	
	private static final int MAX_CONCURRENT_ACTIVE_PARTITIONS_DEFAULT = 10;
	private static final double MAX_CONCURRENT_PARTITIONS_MEM_FACTOR = 0.5;
	
	// represents object registry: partition is a key, object is a value	
	private Cache<String, OSObject>  fCache = null;
	
	
	CacheManager fCacheManager = null;
	private String fCacheName = null;
	private OSObjectRegistryListener fOSObjectRegistryListener = null;
	
	private Integer fTimePerObject  = 0;
	private Integer fDataBytesPerObject = 0;
	private Integer fTuplesPerObject = 0;
	private boolean fCloseOnPunct = true;
	private int fParquetPageSize = 0;
	private String fStorageFormat = StorageFormat.raw.name();
	private String fPartitionValueAttrs = "";
	private final int fMaxConcurrentPartitionsNum;
	
	private long osRegistryMaxMemory = 0;
	private int fUploadWorkersNum = UPLOAD_WORKERS_CORE_POOL_SIZE;

	
	
	private static Logger TRACE = Logger.getLogger(CLASS_NAME);
	
	public OSObjectRegistry(OperatorContext opContext, BaseObjectStorageSink parent) {

		fOSObjectRegistryListener = new OSObjectRegistryListener(parent);
				
		fTimePerObject = Utils.getParamSingleIntValue(opContext, IObjectStorageConstants.PARAM_TIME_PER_OBJECT, 0);
		fDataBytesPerObject = Utils.getParamSingleIntValue(opContext, IObjectStorageConstants.PARAM_BYTES_PER_OBJECT, 0);
		fTuplesPerObject = Utils.getParamSingleIntValue(opContext, IObjectStorageConstants.PARAM_TUPLES_PER_OBJECT, 0);
		fCloseOnPunct = Utils.getParamSingleBoolValue(opContext, IObjectStorageConstants.PARAM_CLOSE_ON_PUNCT, true);
		fUploadWorkersNum  = Utils.getParamSingleIntValue(opContext, IObjectStorageConstants.PARAM_UPLOAD_WORKERS_NUM, 10);
		fStorageFormat = Utils.getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_STORAGE_FORMAT, StorageFormat.raw.name());
		fPartitionValueAttrs = Utils.getParamSingleStringValue(opContext, IObjectStorageConstants.PARAM_PARTITION_VALUE_ATTRIBUTES, "");
		
		fCacheName = Utils.genCacheName(OS_OBJECT_CACHE_NAME_PREFIX, opContext);

		Expiry<Object, Object> expiry = null;

		// if CR, then expiry is set to punct and all other options are ignored		
		ConsistentRegionContext crContext = opContext.getOptionalContext(ConsistentRegionContext.class);
		if (crContext != null) {
			expiry = new OnPunctExpiry();
		}
		else {		
			if (fTimePerObject > 0) {
				if (TRACE.isLoggable(TraceLevel.TRACE)) {
					TRACE.log(TraceLevel.TRACE,	"Set expiration policy for cache '" + fCacheName  + "' on '" + fTimePerObject + "' seconds"); 
				}
				expiry = new TimePerObjectExpiry(fTimePerObject);
			} 
			else if (fDataBytesPerObject > 0) {
				expiry = new DataBytesPerObjectExpiry(fDataBytesPerObject);
			} 
			else if (fTuplesPerObject > 0) {
				expiry = new TuplesPerObjectExpiry(fTuplesPerObject);
			} else if (fCloseOnPunct) {
				expiry = new OnPunctExpiry();
			}
		}

		if (TRACE.isLoggable(TraceLevel.TRACE)) {
			TRACE.log(TraceLevel.TRACE,	"OSObject registry memory limit '" + osRegistryMaxMemory + "'");
		}
			
		// register listener for the OSObject's lifecycle inside EHCache 
		CacheEventListenerConfigurationBuilder cacheEventListenerConfiguration;
		
		// when output port defined "REMOVED" notification should not be used, i.e. 
		// the objects are closed synchronously. 
		if (parent.hasOutputPort()) {
			cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder
    			.newEventListenerConfiguration(fOSObjectRegistryListener,EventType.CREATED , EventType.EVICTED, EventType.EXPIRED
    			 ) 
    			.ordered().asynchronous();
		} 
		// when output port is not defined, all the objects are closed asynchronously, 
		// so "REMOVED" notification should be sent
		else {
			cacheEventListenerConfiguration = CacheEventListenerConfigurationBuilder
	    			.newEventListenerConfiguration(fOSObjectRegistryListener,EventType.CREATED , EventType.REMOVED, EventType.EVICTED, EventType.EXPIRED
	    			 ) 
	    			.ordered().asynchronous();
			
		}
		// @TODO:  improve the control over task queue by proper blocking queue implementation 
		ThreadPoolExecutor tpe = new ThreadPoolExecutor(fUploadWorkersNum, 
														fUploadWorkersNum + UPLOAD_WORKERS_DELTA_TO_MAX_POOL_SIZE, 
														UPLOAD_WORKERS_KEEP_ALIVE_TIME, 
														UPLOAD_WORKERS_KEEP_ALIVE_TIME_UNIT, 
														new LinkedBlockingQueue<Runnable>(TASK_QUEUE_MAX_SIZE), 
                                parent.getOperatorContext().getThreadFactory(),
														new ThreadPoolExecutor.CallerRunsPolicy());
		
		
		
		// custom dispatcher is required for performance optimization:
		// OOTB EHCache keeps submitting UPDATE events even if no listeners
		// is registered for it. 
		OSObjectCacheEventDispatcher<String, OSObject> eventDispatcher = new OSObjectCacheEventDispatcher<String, OSObject>(parent.getOperatorContext().getScheduledExecutorService(), tpe);
				
		fMaxConcurrentPartitionsNum = calcMaxConcurrentPartitionsNum(fStorageFormat, opContext, fPartitionValueAttrs.length() > 0);
		
		if (TRACE.isLoggable(TraceLevel.WARNING)) {
			TRACE.log(TraceLevel.WARNING,	"Setting max concurrent partitions number to '" + fMaxConcurrentPartitionsNum  + "'"); 
		}
		
		
		UserManagedCacheBuilder<String, OSObject, UserManagedCache<String, OSObject>> umcb = UserManagedCacheBuilder.newUserManagedCacheBuilder(String.class, OSObject.class)
				.withEventListeners(cacheEventListenerConfiguration)
				.withEventDispatcher(eventDispatcher)
				.withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder().heap(fMaxConcurrentPartitionsNum, EntryUnit.ENTRIES))
				.withDispatcherConcurrency(CACHE_DISPATCHER_CONCURRENCY)
				.withExpiry(expiry)									
				.withSizeOfMaxObjectGraph(SIZE_OF_MAX_OBJECT_GRAPH);
		
		fCache = umcb.build(true);
		
		if (TRACE.isLoggable(TraceLevel.TRACE)) {
			TRACE.log(TraceLevel.TRACE,	"Using '" + fCacheName  + "' cache as internal objects registry"); 
		}
		
		
	}
	
	/**
	 * Finds object in registry
	 * @param key partition
	 * @return object if exists, null otherwise
	 */
	public OSObject find(String key) {
		return fCache.get(key);			
	}

	/**
	 * Registers new object 
	 * @param key object partition
	 * @param value object to register
	 * @param nActivePartitions 
	 */
	public void register(String key, OSObject value) {
		fCache.put(key, value);
	}
	
	/**
	 * Removed object from regitsry
	 * @param key partition
	 * @param nActivePartitions 
	 */
	public void remove(String key) {
		if (fCache.containsKey(key)) {
			fCache.remove(key);
		}
	}
	
	
	public String toString() {
		StringBuffer res = new StringBuffer();
		org.ehcache.Cache.Entry<String, OSObject> cacheEntry = null;
		Iterator<org.ehcache.Cache.Entry<String, OSObject>> cacheIterator = fCache.iterator();
		int cacheEntryCount = 0;
		while (cacheIterator.hasNext()) {
			cacheEntry = ((org.ehcache.Cache.Entry<String, OSObject>)cacheIterator.next());
		    res.append("key=" + cacheEntry.getKey() + ", object=" + cacheEntry.getValue().getPath() + "\n");		    
		    cacheEntryCount++;
		}
		
		res.append("Cache stats: entries number='" + cacheEntryCount + "'");
		
		return res.toString();				
	}

	
	/**
	 * Closes all active objects
	 */
	public void closeAll() {
		Iterator<org.ehcache.Cache.Entry<String, OSObject>> cacheIterator = fCache.iterator();
		org.ehcache.Cache.Entry<String, OSObject> cacheEntry = null;
		String cacheKey = null;
		while (cacheIterator.hasNext()) {
			cacheEntry = ((org.ehcache.Cache.Entry<String, OSObject>)cacheIterator.next());
			if (cacheEntry != null) {
				cacheKey = cacheEntry.getKey();
				remove(cacheKey); // triggers REMOVED event responsible for object closing and metrics update
			}
		}
	}

	/**
	 * Closes all active objects immediatly.
	 * Required for shutdown case when all cache objects
	 * must be closed in the current thread.
	 * @throws Exception 
	 */
	public List<String> closeAllImmediatly() throws Exception {
		List<String> closedObjectNames = new LinkedList<String>();
		Iterator<org.ehcache.Cache.Entry<String, OSObject>> cacheIterator = fCache.iterator();
		org.ehcache.Cache.Entry<String, OSObject> cacheEntry = null;
		while (cacheIterator.hasNext()) {
			cacheEntry = ((org.ehcache.Cache.Entry<String, OSObject>)cacheIterator.next());
			if (cacheEntry != null) {
				OSWritableObject cacheValue = (OSWritableObject)cacheEntry.getValue();
				if (cacheValue != null) {
					// flush buffer
					cacheValue.flushBuffer();
					// close object
					cacheValue.close();
					closedObjectNames.add(cacheValue.getPath());
					// clean cache
					remove(cacheEntry.getKey());
				}
			}
		}
		
		return closedObjectNames;
	}
	
	

	public void shutdownCache() {
		if (fCacheManager != null) {
			fCacheManager.removeCache(fCacheName);
			fCacheManager.close();
		}
	}

	public void update(String key, OSObject osObject) {		
		// replace equivalent to get + put
		// so, required to update expiration if time used
		fCache.replace(key, osObject);		

		
	}

	private int calcMaxConcurrentPartitionsNum(String storageFormat, OperatorContext opContext, boolean partitioningEnabled) {
		int res = MAX_CONCURRENT_ACTIVE_PARTITIONS_DEFAULT;
		
		// For partitioning case calculate partitions number 
		// according to page size
		// For plan (no partitions) usecase - only ONE object 
		// is ALWAYS active in cache
		if (storageFormat.equals(StorageFormat.parquet.name()) && partitioningEnabled) {
			long totalMemory = SystemFunctions.maxMemory();
			int parquetPageSize  = Utils.getParamSingleIntValue(opContext, IObjectStorageConstants.PARAM_PARQUET_PAGE_SIZE, ParquetOSWriter.getDefaultPWConfig().getPageSize());
			res = (int)((totalMemory/parquetPageSize) * MAX_CONCURRENT_PARTITIONS_MEM_FACTOR);
		}
		
		return res;
	}
	
	public int getMaxConcurrentParititionsNum() {
		return fMaxConcurrentPartitionsNum;
	}
	
}
