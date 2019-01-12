/*******************************************************************************
* Copyright (C) 2017, International Business Machines Corporation
* All Rights Reserved
*******************************************************************************/


package com.ibm.streamsx.objectstorage;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;

import com.ibm.streams.operator.Attribute;
import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.OperatorContext.ContextCheck;
import com.ibm.streams.operator.OutputTuple;
import com.ibm.streams.operator.StreamSchema;
import com.ibm.streams.operator.StreamingData.Punctuation;
import com.ibm.streams.operator.StreamingInput;
import com.ibm.streams.operator.StreamingOutput;
import com.ibm.streams.operator.Tuple;
import com.ibm.streams.operator.TupleAttribute;
import com.ibm.streams.operator.Type;
import com.ibm.streams.operator.Type.MetaType;
import com.ibm.streams.operator.compile.OperatorContextChecker;
import com.ibm.streams.operator.logging.LoggerNames;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streams.operator.metrics.Metric;
import com.ibm.streams.operator.metrics.OperatorMetrics;
import com.ibm.streams.operator.model.Parameter;
import com.ibm.streams.operator.state.Checkpoint;
import com.ibm.streams.operator.state.CheckpointContext;
import com.ibm.streams.operator.state.ConsistentRegionContext;
import com.ibm.streams.operator.state.StateHandler;
import com.ibm.streamsx.objectstorage.client.Constants;
import com.ibm.streamsx.objectstorage.client.IObjectStorageClient;
import com.ibm.streamsx.objectstorage.internal.sink.OSObject;
import com.ibm.streamsx.objectstorage.internal.sink.OSObjectFactory;
import com.ibm.streamsx.objectstorage.internal.sink.OSObjectRegistry;
import com.ibm.streamsx.objectstorage.internal.sink.OSWritableObject;
import com.ibm.streamsx.objectstorage.internal.sink.StorageFormat;


/**
 * Base Sink operator implementation class. 
 * Used by protocol specific operator implementations.
 * @author streamsadmin
 *
 */

public class BaseObjectStorageSink extends AbstractObjectStorageOperator implements StateHandler  {

	private static final String CLASS_NAME = BaseObjectStorageSink.class.getName();
	
	// operator metrics
	public static final String ACTIVE_OBJECTS_METRIC = "nActiveObjects";
	public static final String CLOSED_OBJECTS_METRIC = "nClosedObjects";
	public static final String EXPIRED_OBJECTS_METRIC = "nExpiredObjects";
	public static final String EVICTED_OBJECTS_METRIC = "nEvictedObjects"; 
	public static final String MAX_CONCURRENT_PARTITIONS_NUM_METRIC = "maxConcurrentPartitionsNum";
	public static final String STARTUP_TIME_MILLISECS_METRIC = "startupTimeMillisecs";
	
	
	/**
	 * Create a logger specific to this class
	 */

	private static Logger TRACE = Logger.getLogger(CLASS_NAME);
	private static Logger LOGGER = Logger.getLogger(LoggerNames.LOG_FACILITY + "." + CLASS_NAME);
	
	private String rawObjectName = ""; 
	private String objectName = null;
	private String timeFormat = "yyyyMMdd_HHmmss"; 
	private String currentObjectName;


	private OSObject fObjectToWrite;

	private long bytesPerObject = -1;
	private long tuplesPerObject = -1;
	private double timePerObject = -1;
	private boolean closeOnPunct = true;
	private int fUploadWorkersNum = 10;
	private String encoding = null;
	private TupleAttribute<Tuple,String> fObjectNameAttr = null;
	private TupleAttribute<Tuple, ?> fDataAttr = null;
	// this will be reset if the object index is 0.
	private int fDataIndex = -1;
	private int objectIndex = -1;
	private boolean dynamicObjectname;
	private MetaType fDataType = null;

	// object num for generating FILENUM variable in filename
	private long objectNum = 0;

	// Variables required by the optional output port
	// hasOutputPort signifies if the operator has output port defined or not
	// assuming in the beginning that the operator does not have a error output
	// port by setting hasOutputPort to false

	private boolean hasOutputPort = false;
	private StreamingOutput<OutputTuple> outputPort;
	
	private LinkedBlockingQueue<OutputTuple> outputPortQueue;
	private Thread outputPortThread;

	private ConsistentRegionContext crContext;
	private boolean fGenOpenObjPunct = false;
	private String fHeaderRow = null;
	private String fStorageFormat = StorageFormat.raw.name(); // by default, the data is stored in the same format as received
	private String fParquetCompression;
	private int fParquetBlockSize;
	private int fParquetPageSize;
	private int fParquetDictPageSize;
	private boolean fParquetEnableDict;
	private boolean fEnableSchemaValidation;
	private String fParquetWriterVersion;
	private OSObjectFactory fOSObjectFactory;
	private OSObjectRegistry fOSObjectRegistry;
	private List<String> fPartitionAttributeNamesList;
	private Boolean fSkipPartitionAttrs = true;
	private String fNullPartitionDefaultValue;
	private long fInitializaStart;
	

	private Set<String> fPartitionKeySet = new HashSet<String>(); 
	
	// metrics
	private Metric nActiveObjects;
	private Metric nClosedObjects;
	private Metric nExpiredObjects;
	private Metric nEvictedObjects;
	private Metric startupTimeMillisecs;
	private Metric nMaxConcurrentParitionsNum;

	/*
	 *   ObjectStoreSink parameter modifiers 
	 */
	
	@Parameter(name = IObjectStorageConstants.PARAM_OBJECT_NAME_ATTR, optional = true, description = "The name of the attribute containing the object name.")
	public void setObjectNameAttribute(TupleAttribute<Tuple,String> name) {
		fObjectNameAttr = name;
	}
	
	public TupleAttribute<Tuple,String> getObjectNameAttribute() {
		return fObjectNameAttr;
	}

	@Parameter(optional = true, description = "Specifies the name of the object that the operator writes to.")
	public void setObjectName(String objectName) {
		TRACE.log(TraceLevel.DEBUG, "setObjectName: " + objectName); 
		this.objectName = objectName;
	}

	public String getObjectName() {
		return objectName;
	}

	
	public String getCurrentObjectName() {
		return currentObjectName;
	}

	// Optional parameter timeFormat
	@Parameter(optional = true, description = "Specifies the time format to use when the objectName parameter value contains %TIME. The parameter value must contain conversion specifications that are supported by the java.text.SimpleDateFormat. The default format is yyyyMMdd_HHmmss.")
	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}

	@Parameter(optional = true, description = "Specifies the approximate size of the output object, in bytes. When the object size exceeds the specified number of bytes, the current output object is closed and a new object is opened.")
	public void setBytesPerObject(long bytesPerObject) {
		this.bytesPerObject = bytesPerObject;
	}

	public long getBytesPerObject() {
		return bytesPerObject;
	}

	@Parameter(optional = true, description = "Specifies the maximum number of tuples that can be received for each output object. When the specified number of tuples are received, the current output object is closed and a new object is opened for writing.")
	public void setTuplesPerObject(long tuplesPerObject) {
		this.tuplesPerObject = tuplesPerObject;
	}

	public long getTuplesPerObject() {
		return tuplesPerObject;
	}

	@Parameter(optional = true, description = "Specifies the approximate time, in seconds, after which the current output object is closed and a new object is opened for writing. ")
	public void setTimePerObject(double timePerObject) {
		this.timePerObject = timePerObject;
	}

	public double getTimePerObject() {
		return timePerObject;
	}

	@Parameter(optional = true, description = "Specifies whether the operator closes the current output object and creates a new object when a punctuation marker is received. The default value is true if parameters timePerObject, tuplesPerObject and bytesPerObject are not set.")
	public void setCloseOnPunct(boolean closeOnPunct) {
		this.closeOnPunct = closeOnPunct;
	}

	public boolean isCloseOnPunct() {
		return closeOnPunct;
	}

	@Parameter(optional = true, description = "Specifies the character set encoding that is used in the output object.")
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return encoding;
	}
	
	@Parameter(optional = true, description = "Specifies if the operator should add header row when starting to write object. By default no header row generated.")
	public void setHeaderRow(String headerRow) {
		fHeaderRow  = headerRow;
	}
	
	public String getHeaderRow() {
		return fHeaderRow;
	}

	// @TODO: make storage format custom literal
	// Currently making it string due to bug in operator model
	// xml generation that creates two subsequent <customLiteral> sections
	@Parameter(optional = true, description = "Specifies storage format operator uses. The default is raw, i.e. the data is stored in the same format as received.")
	public void setStorageFormat(String storageFormat) {
		fStorageFormat  = storageFormat;
	}
	
	public String getStorageFormat() {
		return fStorageFormat;
	}	

	@Parameter(name = IObjectStorageConstants.PARAM_DATA_ATTR, optional = true, description = "The name of the attribute containing the data to be written to the object storage.")
	public void setDataAttribute(TupleAttribute<Tuple,?> name) {
		fDataAttr = name;
	}
	
	public TupleAttribute<Tuple,?> getDataAttribute() {
		return fDataAttr;
	}
	
	@Parameter(name = IObjectStorageConstants.PARAM_PARQUET_COMPRESSION, optional = true, description = "Enum specifying support compressions for parquet storage format. Supported compression types are 'UNCOMPRESSED','SNAPPY','GZIP'")
	public void setParquetCompression(String parquetCompression) {
		fParquetCompression = parquetCompression;
	}
	
	public String getParquetCompression() {
		return fParquetCompression;
	}
	
	@Parameter(name = IObjectStorageConstants.PARAM_PARQUET_BLOCK_SIZE, optional = true, description = "Specifies the block size which is the size of a row group being buffered in memory. The default is 128M.")
	public void setParquetBlockSize(int parquetBlockSize) {
		fParquetBlockSize = parquetBlockSize;
	}
	
	public int getParquetBlockSize() {
		return fParquetBlockSize;
	}
	
	@Parameter(name = IObjectStorageConstants.PARAM_PARQUET_PAGE_SIZE, optional = true, description = "Specifies the page size is for compression. A block is composed of pages. The page is the smallest unit that must be read fully to access a single record. If this value is too small, the compression will deteriorate. The default is 1M.")
	public void setParquetPageSize(int parquetPageSize) {
		fParquetPageSize = parquetPageSize;
	}
	
	public int getParquetPageSize() {
		return fParquetPageSize;
	}

	@Parameter(name = IObjectStorageConstants.PARAM_PARQUET_DICT_PAGE_SIZE, optional = true, description = "There is one dictionary page per column per row group when dictionary encoding is used. The dictionary page size works like the page size but for dictionary.")
	public void setParquetDictPageSize(int parquetDictPageSize) {
		fParquetDictPageSize = parquetDictPageSize;
	}
	
	public int getParquetDictPageSize() {
		return fParquetDictPageSize;
	}

	@Parameter(name = IObjectStorageConstants.PARAM_PARQUET_ENABLE_DICT, optional = true, description = "Specifies if parquet dictionary should be enabled.")
	public void setParquetEnableDict(boolean parquetEnableDict) {
		fParquetEnableDict = parquetEnableDict;
	}
	
	public boolean getParquetEnableDict() {
		return fParquetEnableDict;
	}

	@Parameter(name = IObjectStorageConstants.PARAM_PARQUET_ENABLE_SCHEMA_VALIDATION, optional = true, description = "Specifies of schema validation should be enabled.")
	public void setParquetEnableSchemaValidation(boolean enableSchemaValidation) {
		fEnableSchemaValidation = enableSchemaValidation;
	}
	
	public boolean getParquetEnableSchemaValidation() {
		return fEnableSchemaValidation;
	}

	@Parameter(name = IObjectStorageConstants.PARAM_PARQUET_WRITER_VERSION, optional = true, description = "Specifies parquet writer version.")
	public void setParquetWriterVersion(String parquetWriterVersion) {
		fParquetWriterVersion = parquetWriterVersion;
	}
	
	public String getParquetWriterVersion() {
		return fParquetWriterVersion;
	}

	@Parameter(name = IObjectStorageConstants.PARAM_PARTITION_VALUE_ATTRIBUTES, optional = true, description = "Specifies the list of attributes to be used for partition column values.")
	public void setPartitionValueAttributes(List<String> partitionValueAttrs) {
   			fPartitionAttributeNamesList = partitionValueAttrs;
    }

	@Parameter(name = IObjectStorageConstants.PARAM_UPLOAD_WORKERS_NUM, optional = true, description = "Specifies number of upload workers.")
	public void setUploadWorkersNum(int uploadWorkersNum) {
		fUploadWorkersNum  = uploadWorkersNum;
	}

	public int getUploadWorkersNum() {
		return fUploadWorkersNum;
	}
	
	// @TODO: migrate to the list of attributes back - currently commented 
	// due to testing framework limitations
	//public void setPartitionValueAttributes(List<TupleAttribute<Tuple,?>> partitionValueAttrs) {
    //		fPartitionAttributeNamesList = new LinkedList<String>();
    //		for (TupleAttribute<Tuple, ?> partitionValueAttr: partitionValueAttrs) {
    //			fPartitionAttributeNamesList.add(partitionValueAttr.getAttribute().getName());
    //		}
    //		
    //}

	@Parameter(name = IObjectStorageConstants.PARAM_SKIP_PARTITION_ATTRS, optional = true, description = "Avoids writing of attributes used as partition columns in data files.")
	public void setSkipPartitionAttrs(Boolean skipPartitionAttrs) {
		fSkipPartitionAttrs  = skipPartitionAttrs;
	}
	
	public Boolean getSkipPartitionAttrs() {
		return fSkipPartitionAttrs;
	}
	
	/**
	 *   End of parameter modifiers definition
	 */
	
	protected void setOpConfig(Configuration config) throws IOException, URISyntaxException {
		String autoCreateBucketPropName = Utils.formatProperty(Constants.S3_SERVICE_CREATE_BUCKET_CONFIG_NAME, Utils.getProtocol(getURI()));
		config.set(autoCreateBucketPropName, "true");
	}
		
	@Parameter(name = IObjectStorageConstants.PARAM_NULL_PARTITION_DEFAULT_VALUE, optional = true, description = "Specifies default for partitions with null values.")
	public void setNullPartitionDefaultValue(String nullPartitionDefaultValue) {
		fNullPartitionDefaultValue = nullPartitionDefaultValue;
	}

	public String getNullPartitionDefaultValue() {
		return fNullPartitionDefaultValue;
	}
	
	/*
	 * The method checkOutputPort validates that the stream on output port
	 * contains the mandatory attribute.
	 */
	@ContextCheck(compile = true)
	public static void checkOutputPort(OperatorContextChecker checker) {
		OperatorContext context = checker.getOperatorContext();
		if (context.getNumberOfStreamingOutputs() == 1) {
			StreamingOutput<OutputTuple> streamingOutputPort = context
					.getStreamingOutputs().get(0);
			if (streamingOutputPort.getStreamSchema().getAttributeCount() != 2) {
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_SINK_OUTPUT_PORT"), 
						null);

			} else {
				if (streamingOutputPort.getStreamSchema().getAttribute(0)
						.getType().getMetaType() != Type.MetaType.RSTRING) {
					checker.setInvalidContext(
							Messages.getString("OBJECTSTORAGE_SINK_FIRST_OUTPUT_PORT"), 
							null);
				}
				if (streamingOutputPort.getStreamSchema().getAttribute(1)
						.getType().getMetaType() != Type.MetaType.UINT64) {
					checker.setInvalidContext(
							Messages.getString("OBJECTSTORAGE_SINK_SECOND_OUTPUT_PORT"), 
							null);

				}

			}

		}
	}

	/**
	 * This function checks only things that can be determined at compile time.
	 * 
	 * @param checker
	 * @throws Exception
	 */
	@ContextCheck(compile = true)
	public static void checkInputPortSchema(OperatorContextChecker checker)
			throws Exception {
		// rstring or ustring would need to be provided.
		StreamSchema inputSchema = checker.getOperatorContext()
				.getStreamingInputs().get(0).getStreamSchema();		
		boolean hasDynamic = checker.getOperatorContext().getParameterNames()
				.contains(IObjectStorageConstants.PARAM_OBJECT_NAME_ATTR);
		boolean hasDataAttr = checker.getOperatorContext().getParameterNames()
				.contains(IObjectStorageConstants.PARAM_DATA_ATTR);
		boolean hasStorageFormat = checker.getOperatorContext().getParameterNames()
				.contains(IObjectStorageConstants.PARAM_STORAGE_FORMAT);
		// no data attribute specified and default (raw) storage format is used - apply attribute type validation logic		
		if (!hasDataAttr && !hasStorageFormat) {
			if (!hasDynamic && inputSchema.getAttributeCount() != 1) {
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_SINK_ONE_ATTR_INPUT_PORT", IObjectStorageConstants.PARAM_OBJECT_NAME_ATTR), new Object[] {} ); 
			}
	
			if (hasDynamic && inputSchema.getAttributeCount() != 2) {
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_SINK_TWO_ATTR_INPUT_PORT", IObjectStorageConstants.PARAM_OBJECT_NAME_ATTR, IObjectStorageConstants.PARAM_OBJECT_NAME_ATTR ) , new Object[] {});
			}
	
			if (inputSchema.getAttributeCount() == 1) {
				// check that the attribute type must be a rstring or ustring
				if (MetaType.RSTRING != inputSchema.getAttribute(0).getType()
						.getMetaType()
						&& MetaType.USTRING != inputSchema.getAttribute(0)
								.getType().getMetaType()
						&& MetaType.BLOB != inputSchema.getAttribute(0).getType()
								.getMetaType()) {
					checker.setInvalidContext(
							Messages.getString("OBJECTSTORAGE_SINK_INVALID_ATTR_TYPE", inputSchema.getAttribute(0).getType().getMetaType()), null);
				}
			}
			if (inputSchema.getAttributeCount() == 2) {
				int numString = 0;
				int numBlob = 0;
				for (int i = 0; i < 2; i++) {
					MetaType t = inputSchema.getAttribute(i).getType()
							.getMetaType();
					if (MetaType.USTRING == t || MetaType.RSTRING == t) {
						numString++;
					} else if (MetaType.BLOB == t) {
						numString++;
					}
				} // end for loop;
	
				if (numBlob == 0 && numString == 2 || // data is a string
						numBlob == 1 && numString == 1) { // data is a blob
					// we're golden.
				} else {
					checker.setInvalidContext(
							Messages.getString("OBJECTSTORAGE_SINK_INVALID_NAME_TYPE"), 
							null);
				}
			}
		}
	}

	@ContextCheck(compile = true)
	public static void checkCompileParameters(OperatorContextChecker checker)
			throws Exception {
		checker.checkExcludedParameters(IObjectStorageConstants.PARAM_OS_OBJECT_NAME, 
				IObjectStorageConstants.PARAM_OBJECT_NAME_ATTR);
		checker.checkExcludedParameters(IObjectStorageConstants.PARAM_OBJECT_NAME_ATTR,
				IObjectStorageConstants.PARAM_OS_OBJECT_NAME); 
		checker.checkExcludedParameters(IObjectStorageConstants.PARAM_BYTES_PER_OBJECT,
				IObjectStorageConstants.PARAM_TIME_PER_OBJECT,
				IObjectStorageConstants.PARAM_TUPLES_PER_OBJECT,
				IObjectStorageConstants.PARAM_OBJECT_NAME_ATTR);
		checker.checkExcludedParameters(IObjectStorageConstants.PARAM_TIME_PER_OBJECT,
				IObjectStorageConstants.PARAM_BYTES_PER_OBJECT,
				IObjectStorageConstants.PARAM_TUPLES_PER_OBJECT,
				IObjectStorageConstants.PARAM_OBJECT_NAME_ATTR);
		checker.checkExcludedParameters(IObjectStorageConstants.PARAM_TUPLES_PER_OBJECT,
				IObjectStorageConstants.PARAM_BYTES_PER_OBJECT,
				IObjectStorageConstants.PARAM_TIME_PER_OBJECT,
				IObjectStorageConstants.PARAM_OBJECT_NAME_ATTR);
		checker.checkExcludedParameters(IObjectStorageConstants.PARAM_OBJECT_NAME_ATTR,
				IObjectStorageConstants.PARAM_TUPLES_PER_OBJECT,
				IObjectStorageConstants.PARAM_BYTES_PER_OBJECT,
				IObjectStorageConstants.PARAM_TIME_PER_OBJECT);		
	}
	
	@ContextCheck(compile = true)
	public static void checkCheckpointConfig(OperatorContextChecker checker) {
		OperatorContext opContext = checker.getOperatorContext();		
		CheckpointContext chkptContext = opContext.getOptionalContext(CheckpointContext.class);
		if (chkptContext != null) {
			if (chkptContext.getKind().equals(CheckpointContext.Kind.OPERATOR_DRIVEN)) {
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_NOT_CHECKPOINT_OPERATOR_DRIVEN", "ObjectStorageSink"), null);
			}
			if (chkptContext.getKind().equals(CheckpointContext.Kind.PERIODIC)) {
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_NOT_CHECKPOINT_PERIODIC", "ObjectStorageSink"), null);
			}			
		}
	}
	
	@ContextCheck(compile = true)
	public static void checkConsistentRegion(OperatorContextChecker checker) {
		
		// check that the object store sink is not at the start of the consistent region
		OperatorContext opContext = checker.getOperatorContext();
		ConsistentRegionContext crContext = opContext.getOptionalContext(ConsistentRegionContext.class);
		if (crContext != null) {
			if (crContext.isStartOfRegion()) {
				checker.setInvalidContext(Messages.getString("OBJECTSTORAGE_NOT_CONSISTENT_REGION", "ObjectStorageSink"), null); 
			}
		}
	}
	
	@ContextCheck(compile = true)
	public static void checkPartioningParameters(OperatorContextChecker checker) {
		// partition skipping parameter requires partition attributes to be defined
		checker.checkDependentParameters(IObjectStorageConstants.PARAM_SKIP_PARTITION_ATTRS, IObjectStorageConstants.PARAM_PARTITION_VALUE_ATTRIBUTES);		
	}	
	
	@ContextCheck(compile = false)
	public static void checkParquetParameters(OperatorContextChecker checker) {
		List<String> storageFormatVal = checker.getOperatorContext()
				.getParameterValues(IObjectStorageConstants.PARAM_STORAGE_FORMAT);
		List<String> parquetCompressionVal = checker.getOperatorContext()
				.getParameterValues(IObjectStorageConstants.PARAM_PARQUET_COMPRESSION);
		List<String> parquetBlockSizeVal = checker.getOperatorContext()
				.getParameterValues(IObjectStorageConstants.PARAM_PARQUET_BLOCK_SIZE);
		List<String> parquetDictPageVal = checker.getOperatorContext()
				.getParameterValues(IObjectStorageConstants.PARAM_PARQUET_DICT_PAGE_SIZE);
		List<String> parquetEnableDictVal = checker.getOperatorContext()
				.getParameterValues(IObjectStorageConstants.PARAM_PARQUET_ENABLE_DICT);
		List<String> parquetEnableSchemaVal = checker.getOperatorContext()
				.getParameterValues(IObjectStorageConstants.PARAM_PARQUET_ENABLE_SCHEMA_VALIDATION);
		List<String> parquetPageSizeVal = checker.getOperatorContext()
				.getParameterValues(IObjectStorageConstants.PARAM_PARQUET_PAGE_SIZE);
		List<String> parquetWriterVerVal = checker.getOperatorContext()
				.getParameterValues(IObjectStorageConstants.PARAM_PARQUET_WRITER_VERSION);
		
		
		if (!parquetCompressionVal.isEmpty() || 
		    !parquetBlockSizeVal.isEmpty()   || 
		    !parquetDictPageVal.isEmpty()    || 
		    !parquetEnableDictVal.isEmpty()  || 
		    !parquetEnableSchemaVal.isEmpty()||
		    !parquetPageSizeVal.isEmpty()    || 
		    !parquetWriterVerVal.isEmpty()) {
			if (!storageFormatVal.isEmpty() && !storageFormatVal.get(0).equals(StorageFormat.parquet.name())) {
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_SINK_PARQUET_STORAGE_FORMAT_REQUIRED", IObjectStorageConstants.PARAM_PARQUET_COMPRESSION, IObjectStorageConstants.PARAM_STORAGE_FORMAT, StorageFormat.parquet.name()), 
						null);
			}
		}
	}

	@ContextCheck(compile = false)
	public static void checkParameters(OperatorContextChecker checker)
			throws Exception {
		List<String> objectNameParamValues = checker.getOperatorContext()
				.getParameterValues("objectName"); 
		List<String> timeFormatValue = checker.getOperatorContext()
				.getParameterValues("timeFormat"); 
		if (timeFormatValue != null) {
			if (!timeFormatValue.isEmpty()) {
				if (timeFormatValue.get(0).isEmpty()) {
					throw new Exception("Operator parameter timeFormat should not be empty.");
				}
			}
		}
		
		for (String objectValue : objectNameParamValues) {
			if (objectValue.contains(IObjectStorageConstants.OBJECT_VAR_PREFIX)) {
				String[] objectValueVarSubstrs = objectValue.split(IObjectStorageConstants.OBJECT_VAR_PREFIX);
				// checking each variable independently 
				// to support object names with multiple variables
				// like %PARTITIONS%TIME/object_%OBJECTNUM.parquet
				for (int i = 1; i < objectValueVarSubstrs.length;i++) {
					objectValueVarSubstrs[i] = IObjectStorageConstants.OBJECT_VAR_PREFIX +  objectValueVarSubstrs[i];					
					if (!objectValueVarSubstrs[i].contains(IObjectStorageConstants.OBJECT_VAR_HOST)
							&& !objectValueVarSubstrs[i].contains(IObjectStorageConstants.OBJECT_VAR_PROCID)
							&& !objectValueVarSubstrs[i].contains(IObjectStorageConstants.OBJECT_VAR_PEID)
							&& !objectValueVarSubstrs[i].contains(IObjectStorageConstants.OBJECT_VAR_PELAUNCHNUM)
							&& !objectValueVarSubstrs[i].contains(IObjectStorageConstants.OBJECT_VAR_TIME)
							&& !objectValueVarSubstrs[i].contains(IObjectStorageConstants.OBJECT_VAR_PARTITION)
							&& !objectValueVarSubstrs[i].contains(IObjectStorageConstants.OBJECT_VAR_OBJECTNUM)) {
						throw new Exception(
								"Unsupported % specification provided. Supported values are %HOST, %PEID, %OBJECTNUM, %PROCID, %PELAUNCHNUM, %TIME, %PARTITIONS");
					}
				}
			}
		}

		List<String> bytesPerObjectVal = checker.getOperatorContext()
				.getParameterValues(IObjectStorageConstants.PARAM_BYTES_PER_OBJECT);
		List<String> tuplesPerObjectVal = checker.getOperatorContext()
				.getParameterValues(IObjectStorageConstants.PARAM_TUPLES_PER_OBJECT);
		List<String> timeForObjectVal = checker.getOperatorContext()
				.getParameterValues(IObjectStorageConstants.PARAM_TIME_PER_OBJECT);

		// checks for negative values
		if (!bytesPerObjectVal.isEmpty()) {
			if (Long.valueOf(bytesPerObjectVal.get(0)) < 0) {
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_SINK_INVALID_VALUE_BYTEPEROBJECT"), 
						null);
			}
		}

		if (!tuplesPerObjectVal.isEmpty()) {
			if (Long.valueOf(tuplesPerObjectVal.get(0)) < 0) {
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_SINK_INVALID_VALUE_TUPLESPEROBJECT"), 
						null);
			}
		}

		if (!timeForObjectVal.isEmpty()) {
			if (Float.valueOf(timeForObjectVal.get(0)) < 0.0) {
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_SINK_INVALID_VALUE_TIMEPEROBJECT"), 
						null);
			}
		}

		
		
		int objectAttribute = -1;
		StreamSchema inputSchema = checker.getOperatorContext().getStreamingInputs().get(0).getStreamSchema();
		Set<String> parameterNames = checker.getOperatorContext().getParameterNames();
		if (parameterNames.contains(IObjectStorageConstants.PARAM_OBJECT_NAME_ATTR)) {	
			
			String objectNameParamValue = checker.getOperatorContext()
					.getParameterValues(IObjectStorageConstants.PARAM_OBJECT_NAME_ATTR).get(0);
			int currAttrIndx = 0;
			for(String attrName: inputSchema.getAttributeNames()) {
				if (objectNameParamValue.contains(attrName)) {
					objectAttribute = currAttrIndx;
					break;
				}
				currAttrIndx++;					
			}
		}
	}

	/**
	 * Check that the objectAttributeName parameter is an attribute of the right
	 * type.
	 * 
	 * @param checker
	 */
	@ContextCheck(compile = false)
	public static void checkObjectAttributeName(OperatorContextChecker checker) {
		StreamSchema inputSchema = checker.getOperatorContext()
				.getStreamingInputs().get(0).getStreamSchema();
		List<String> objectAttrNameList = checker.getOperatorContext()
				.getParameterValues(IObjectStorageConstants.PARAM_OBJECT_NAME_ATTR);
		if (objectAttrNameList == null || objectAttrNameList.size() == 0) {
			// Nothing to check, because the parameter doesn't exist.
			return;
		}

		String objectAttrName = objectAttrNameList.get(0);
		
		int objectAttrIndex = 0;
		int currAttrIndx = 0;
		for(String attrName: inputSchema.getAttributeNames()) {
			if (objectAttrName.contains(attrName)) {
				objectAttrIndex = currAttrIndx;
				break;
			}
			currAttrIndx++;					
		}

		Attribute objectAttr = inputSchema.getAttribute(objectAttrIndex);
		if (objectAttr == null) {
			checker.setInvalidContext(Messages.getString("OBJECTSTORAGE_SINK_NO_ATTRIBUTE"), 
					new Object[] { objectAttrName });
		}
		if (MetaType.RSTRING != objectAttr.getType().getMetaType()
				&& MetaType.USTRING != objectAttr.getType().getMetaType()) {
			checker.setInvalidContext(
					Messages.getString("OBJECTSTORAGE_SINK_WRONG_TYPE", objectAttr.getType().getMetaType()), 
					new Object[] {});
		}
	}

	@ContextCheck(compile = false)
	public static void checkUriMatch(OperatorContextChecker checker)
			throws Exception {
		List<String> objectStorageUriParamValues = checker.getOperatorContext()
				.getParameterValues(IObjectStorageConstants.PARAM_OS_URI); 
		List<String> objectParamValues = checker.getOperatorContext()
				.getParameterValues(IObjectStorageConstants.PARAM_OS_OBJECT_NAME); 

		String objectStorageUriValue = null;
		if (objectStorageUriParamValues.size() == 1) {
			objectStorageUriValue = objectStorageUriParamValues.get(0);
			if (false == objectStorageUriValue.endsWith("/")) {
				objectStorageUriValue = objectStorageUriValue + "/";
			}			
		}

		String objectValue = null;
		if (objectParamValues.size() == 1) {
			objectValue = objectParamValues.get(0);
			// replace % with _
			objectValue = objectValue.replace("%", "_");  
		}
		// only need to perform this check if both 'objectStorageUri' and 'objectName' params
		// are set
		if (objectStorageUriValue != null && objectValue != null) {

			// log error message for individual params if invalid URI
			URI objectStorageURI;
			URI objectUri;
			try {
				objectStorageURI = new URI(Utils.getEncodedURIStr(objectStorageUriValue));	
				//objectValue = Utils.getEncodedURIStr(objectStorageUriValue);
			} catch (URISyntaxException e) {
				TRACE.log(TraceLevel.ERROR,
							"'" + IObjectStorageConstants.PARAM_OS_URI + "' parameter contains an invalid URI: " 
								+ objectStorageUriValue);
				throw e;
			}

			try {				
				objectUri = Utils.genObjectURI(objectStorageURI, objectValue);				
			} catch (URISyntaxException e) {
				TRACE.log(TraceLevel.ERROR,						
								"'" + IObjectStorageConstants.PARAM_OS_OBJECT_NAME + "' parameter contains an invalid URI: " 
								+ objectValue);
				throw e;
			}

			if (objectUri.getScheme() != null) {
				// must have the same scheme
				if (!objectStorageURI.getScheme().equals(objectUri.getScheme())) {
					checker.setInvalidContext(
							Messages.getString("OBJECTSTORAGE_SINK_INVALID_SCHEMA", objectUri.getScheme(), objectStorageURI.getScheme()),
							 null); 
					return;
				}

				// must have the same authority
				if ((objectStorageURI.getAuthority() == null && objectUri.getAuthority() != null)
						|| (objectStorageURI.getAuthority() != null && objectUri
								.getAuthority() == null)
						|| (objectStorageURI.getAuthority() != null
								&& objectUri.getAuthority() != null && !objectStorageURI
								.getAuthority().equals(objectUri.getAuthority()))) {
					checker.setInvalidContext(
							Messages.getString("OBJECTSTORAGE_SINK_INVALID_HOST", objectUri.getAuthority(), objectStorageURI.getAuthority()),
							 null); 
					return;
				}
			}
		}
	}

	@Override
	public void initialize(OperatorContext context) throws Exception {
		try {
			fInitializaStart = System.currentTimeMillis();
			
			super.initialize(context);
			
			// if the object contains variable, it will result in an
			// URISyntaxException, replace % with _ so we can parse the URI
			if (TRACE.isLoggable(TraceLevel.TRACE)) {
				TRACE.log(TraceLevel.TRACE, "objectName param: " + objectName);
			}
			
			crContext = context.getOptionalContext(ConsistentRegionContext.class);
			
			if (objectName != null) {
				
				URI uri =  Utils.genObjectURI(new URI(getURI()), objectName);

				String scheme = uri.getScheme();
				if (scheme != null) {
					String fs;
					if (uri.getAuthority() != null)
						fs = scheme + "://" + uri.getAuthority(); 
					else
						fs = scheme + ":///"; 

					// only use the authority from the 'objectName' parameter if the
					// 'objectStorageUri' param is not specified
					if (getURI() == null)						
						setURI(fs);
					
					if (TRACE.isLoggable(TraceLevel.TRACE)) {
						TRACE.log(TraceLevel.TRACE, "fileSystemUri: " + getURI());
					}
					
					// must use original parameter value to preserve the
					// variable
					String path = uri.getPath();

					// since the file contains a scheme, the path is absolute
					// and we
					// need to ensure it starts a "/"
					if (!path.startsWith("/")) 
						path = "/" + path; 

					setObjectName(path);
				}
			}
		} catch (URISyntaxException e) {
	    	if (TRACE.isLoggable(TraceLevel.ERROR)) {
				TRACE.log(TraceLevel.ERROR,	"Failed to construct object storage URI for object name '" + objectName + "'. Exception: '" + e.getMessage() + "'"); 
			}
	    	LOGGER.log(TraceLevel.ERROR, Messages.getString("OBJECTSTORAGE_SINK_INVALID_URL", objectName));
	    	throw new Exception(Messages.getString("OBJECTSTORAGE_SINK_INVALID_URL", objectName) + e);

		}
		
		// register for data governance
		// only register if static objectname mode
		if (objectName != null && getURI() != null) {
			registerForDataGovernance(getURI(), objectName);
		}		

		/*
		 * Set appropriate variables if the optional output port is
		 * specified. Also set outputPort to the output port at index 0
		 */
		if (context.getNumberOfStreamingOutputs() == 1) {

			hasOutputPort = true;

			outputPort = context.getStreamingOutputs().get(0);
			
			// create a queue and thread for submitting tuple on the output port
			// this is done to allow us to properly support consistent region
			// where we must acquire consistent region permit before doing submission.
			// And allow us to submit tuples when a reset happens.
			outputPortQueue = new LinkedBlockingQueue<>();			
			outputPortThread = createProcessThread();
			outputPortThread.start();

		}
		
		if (fObjectNameAttr != null) {
			// We are in dynamic objectName mode.
			dynamicObjectname = true;

			// We have already verified that we aren't using object in a context
			// check.
			// We have also already verified that the input schema has two 
			// attributes.

			// We have also verified that it's in the input scheme and that it's
			// type is okay.
			// What we need to do here is get its index.

			Attribute objectAttr = fObjectNameAttr.getAttribute();
			objectIndex = objectAttr.getIndex();

			// data attribute name unknown - calculate it
			if (fDataAttr == null) {
				if (objectIndex == 1)
					fDataIndex = 0;
				else if (objectIndex == 0) {
					fDataIndex = 1;
				}
				else {
					throw new Exception(
							"Attribute "
									+ fObjectNameAttr
									+ " must be either attribute 0 or 1 on the input stream.");
				}
			}
		}
		
		
		StreamSchema inputSchema = context.getStreamingInputs().get(0).getStreamSchema();
		if (fDataAttr != null || inputSchema.getAttributeCount() == 1) {
			Attribute dataAttrObj = inputSchema.getAttributeCount() == 1 ? 
										inputSchema.getAttribute(0): 
										fDataAttr.getAttribute();
			fDataIndex = dataAttrObj.getIndex();
			if (TRACE.isLoggable(TraceLevel.TRACE)) {
				TRACE.log(TraceLevel.TRACE, "Using data attribute '" + dataAttrObj.getName() + "'. Attribute index in input schema is '" + fDataIndex + "'");
			}
			// Save the data type for later use.
			fDataType = inputSchema.getAttribute(fDataIndex).getType().getMetaType();
		} 
		
		// For dynamic object name and partitions - 
		// its required to have tuple information in hand - skipping 
		// object creation step
		if (!dynamicObjectname && fPartitionAttributeNamesList!= null && fPartitionAttributeNamesList.isEmpty()) {			
			createObject(refreshCurrentFileName(objectName, Calendar.getInstance().getTime(), false, null));
		}
		
		fOSObjectFactory  = new OSObjectFactory(context);
		fOSObjectRegistry = new OSObjectRegistry(context, this);
		
		// initialize metrics
		initMetrics(context);	
	}
	
	@Override
	public void allPortsReady() throws Exception {
		super.allPortsReady();
		startupTimeMillisecs.setValue(System.currentTimeMillis() - fInitializaStart);
	}

	private void registerForDataGovernance(String serverURL, String object) {
		if (TRACE.isLoggable(TraceLevel.INFO)) {
			TRACE.log(TraceLevel.INFO, "ObjectStorageSink - Registering for data governance with server URL: " + serverURL + " and object: " + object);
		}
		try {
			Map<String, String> properties = new HashMap<String, String>();
			properties.put(IGovernanceConstants.TAG_REGISTER_TYPE, IGovernanceConstants.TAG_REGISTER_TYPE_OUTPUT);
			properties.put(IGovernanceConstants.PROPERTY_OUTPUT_OPERATOR_TYPE, "ObjectStorageSink"); 
			properties.put(IGovernanceConstants.PROPERTY_SRC_NAME, object);
			properties.put(IGovernanceConstants.PROPERTY_SRC_TYPE, IGovernanceConstants.ASSET_OBJECTSTORAGE_OBJECT_TYPE);
			properties.put(IGovernanceConstants.PROPERTY_SRC_PARENT_PREFIX, "p1"); 
			properties.put("p1" + IGovernanceConstants.PROPERTY_SRC_NAME, serverURL); 
			properties.put("p1" + IGovernanceConstants.PROPERTY_SRC_TYPE, IGovernanceConstants.ASSET_OBJECTSTORAGE_SERVER_TYPE); 
			properties.put("p1" + IGovernanceConstants.PROPERTY_PARENT_TYPE, IGovernanceConstants.ASSET_OBJECTSTORAGE_SERVER_TYPE_SHORT);
			if (TRACE.isLoggable(TraceLevel.INFO)) {
				TRACE.log(TraceLevel.INFO, "ObjectStorageSink - Data governance: " + properties.toString());
			}			
			setTagData(IGovernanceConstants.TAG_OPERATOR_IGC, properties);
		}
		catch (Exception e) {
			TRACE.log(TraceLevel.ERROR, "Exception received when registering tag data: "+ e.getMessage());
		}
	}	
	
	private void initMetrics(OperatorContext context) {
		OperatorMetrics opMetrics = getOperatorContext().getMetrics();
		
		nActiveObjects = opMetrics.createCustomMetric(ACTIVE_OBJECTS_METRIC, "Number of active (open) objects", Metric.Kind.COUNTER);
		nClosedObjects = opMetrics.createCustomMetric(CLOSED_OBJECTS_METRIC, "Number of closed objects", Metric.Kind.COUNTER);
		nExpiredObjects = opMetrics.createCustomMetric(EXPIRED_OBJECTS_METRIC, "Number of objects expired according to rolling policy", Metric.Kind.COUNTER);
		nEvictedObjects = opMetrics.createCustomMetric(EVICTED_OBJECTS_METRIC, "Number of objects closed by the operator ahead of time due to memory constraints", Metric.Kind.COUNTER);
		nMaxConcurrentParitionsNum = opMetrics.createCustomMetric(MAX_CONCURRENT_PARTITIONS_NUM_METRIC, "Maximum number of concurrent partitions", Metric.Kind.COUNTER);
		nMaxConcurrentParitionsNum.setValue(fOSObjectRegistry.getMaxConcurrentParititionsNum());
		startupTimeMillisecs = opMetrics.createCustomMetric(STARTUP_TIME_MILLISECS_METRIC, "Operator startup time in milliseconds", Metric.Kind.TIME);
	}

	public Metric getActiveObjectsMetric() {
		return nActiveObjects;
	}

	public Metric getCloseObjectsMetric() {
		return nClosedObjects;
	}

	public Metric getExpiredObjectsMetric() {
		return nExpiredObjects;
	}

	public Metric getEvictedObjectsMetric() {
		return nEvictedObjects;
	}

	public Metric getMaxConcurrentPartitionsNumMetric() {
		return nMaxConcurrentParitionsNum;
	}

	public Metric getStartupTimeInSecsMetric() {
		return startupTimeMillisecs;
	}

	private void createObject(String objectname) throws Exception {
		// creates object based on object name only -
		// no partition or tuple required 
		createObject(null, objectname, null, true);
	}

	private void createObject(String partitionPath, String objectname, Tuple tuple) throws Exception {
		// creates WRITABLE object 
		createObject(partitionPath, objectname, tuple, true);	
	}
	
	private void createObject(String partitionPath, String objectname, Tuple tuple, boolean isWritable) throws Exception {
		
		if (TRACE.isLoggable(TraceLevel.TRACE)) {
			TRACE.log(TraceLevel.TRACE,	"Create Object '" + objectname  + "' with storage format '" + getStorageFormat() + "'"); 
		}
		
		// about to create new object - generate window marker if required
		if (fGenOpenObjPunct && getOperatorContext().getNumberOfStreamingOutputs() > 0) {
			getOutput(0).punctuate(Punctuation.WINDOW_MARKER);
			if (TRACE.isLoggable(TraceLevel.TRACE)) {
				TRACE.log(TraceLevel.TRACE,	"Create object punctuation generated for object : " + objectname); 
			}
		}		
						
		// create new OS object 
		// if partitioning required - create object in the proper partition
		if (isWritable) {
			fObjectToWrite = fOSObjectFactory.createWritableObject(partitionPath, objectname, fHeaderRow, fDataIndex, fDataType, tuple, getObjectStorageClient());
		} else {
			fObjectToWrite = fOSObjectFactory.createObject(partitionPath, objectname, fHeaderRow, fDataIndex, fDataType, tuple);
		}
		
		if (TRACE.isLoggable(TraceLevel.TRACE)) {
			TRACE.log(TraceLevel.TRACE,	"Register Object '" + objectname  + "' in partition regitsry using partition key '" +  fObjectToWrite.getPartitionPath() + "'"); 
		}
		
		// 	 in the OS objects registry
		fOSObjectRegistry.register(fObjectToWrite.getPartitionPath(), fObjectToWrite);
	}

	
	private String refreshCurrentFileName(String baseName, Date date, boolean isTempFile, String partitionKey)
			throws UnknownHostException {
			
		// Check if % specification mentioned are valid or not
		String currentFileName = baseName;
		
		// when %PARTITIONS variable defined - partition will be placed 
		// to the variable location, otherwise - it'll be added before
		// object name
		//@TODO perf

		if (currentFileName.contains(IObjectStorageConstants.OBJECT_VAR_PARTITION)) {
			currentFileName = currentFileName.replace(
					IObjectStorageConstants.OBJECT_VAR_PARTITION, partitionKey);				
		} else {
			StringBuilder strBuilder = new StringBuilder(currentFileName);
			if (currentFileName.lastIndexOf(Constants.URI_DELIM) > 0) {
				currentFileName = strBuilder.insert(currentFileName.lastIndexOf(Constants.URI_DELIM) + 1, partitionKey).toString();
			} else {
				String delim =  currentFileName.startsWith(Constants.URI_DELIM) ? "" : Constants.URI_DELIM;
				currentFileName = partitionKey + delim + currentFileName;
			}
		}
		
		if (currentFileName.contains(IObjectStorageConstants.OBJECT_VAR_PREFIX)) {
			// Replace % specifications with relevant values.
			currentFileName = currentFileName.replace(
					IObjectStorageConstants.OBJECT_VAR_HOST, InetAddress.getLocalHost()
							.getHostName());
			currentFileName = currentFileName.replace(
					IObjectStorageConstants.OBJECT_VAR_PROCID, ManagementFactory
							.getRuntimeMXBean().getName());
			currentFileName = currentFileName.replace(
					IObjectStorageConstants.OBJECT_VAR_PEID, getOperatorContext().getPE()
							.getPEId().toString());
			currentFileName = currentFileName.replace(
					IObjectStorageConstants.OBJECT_VAR_PELAUNCHNUM, String
							.valueOf(getOperatorContext().getPE()
									.getRelaunchCount()));
			SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
			currentFileName = currentFileName.replace(
					IObjectStorageConstants.OBJECT_VAR_TIME, sdf.format(date));	
			
			long anumber = objectNum;
			if (isTempFile) anumber--; //temp files get the number of the last generated file name
			currentFileName = currentFileName.replace(
					IObjectStorageConstants.OBJECT_VAR_OBJECTNUM, String.valueOf(anumber));
			if ( ! isTempFile ) { //only the final file names increment 
				objectNum++;
			}
		}
		
		return currentFileName;
	}

	@Override
	public void processPunctuation(StreamingInput<Tuple> arg0, Punctuation punct)
			throws Exception {
		if (TRACE.isLoggable(TraceLevel.TRACE)) {
			TRACE.log(TraceLevel.TRACE, "Punctuation received: "+punct);
		}		
		if (crContext != null) {
			// do not close on punct if consistent region is enabled, close on drain instead
			super.processPunctuation(arg0, punct);
		}
		else {
			if (punct == Punctuation.WINDOW_MARKER && isCloseOnPunct() || punct == Punctuation.FINAL_MARKER) {
				// forward here if no output port is present only
				// otherwise punct needs to be sent after "object close" tuple
				if (!hasOutputPort) {
					// close asynchronously - no need to extract object metadata
					fOSObjectRegistry.closeAll();
				} 
				// output port exists - need to close synchronously in order
				// to retrieve real object size from storage
				else {
					List<String> closedObjectNames = fOSObjectRegistry.closeAllImmediatly();
					for (String closedObjectName: closedObjectNames) {
						submitOnOutputPort(closedObjectName);
					}
				}
				super.processPunctuation(arg0, punct);
			}
		}
	}
	
	public boolean hasOutputPort() {
		return hasOutputPort;
	}
	
	public OSObjectRegistry getOSObjectRegistry() {
		return fOSObjectRegistry;
	}

	public Set<String> getPartitionKeySet() {
		return fPartitionKeySet;
	}

	@Override
	synchronized public void process(StreamingInput<Tuple> stream, Tuple tuple)
			throws Exception {

		String partitionKey = fOSObjectFactory.getPartitionPath(tuple);

		if (dynamicObjectname) {
			String objectNameStr = tuple.getString(objectIndex);
			if (rawObjectName == null || rawObjectName.isEmpty()) {
				// the first tuple. No raw file name is set.
				rawObjectName = objectNameStr;
				Date date = Calendar.getInstance().getTime();
				currentObjectName = refreshCurrentFileName(rawObjectName, date, false, partitionKey);
				// @TODO: WRITABLE object has been created silently
				// Externalize switch from WRITABLE to non-WRITABLE
				// for BIG-PARTITIONING usecase
				createObject(partitionKey, currentObjectName, tuple);
			}

			if (!rawObjectName.equals(objectNameStr)) {
				// the filename has changed. Notice this cannot happen on the
				// first tuple.
				fOSObjectRegistry.closeAll();
				rawObjectName = objectNameStr;
				Date date = Calendar.getInstance().getTime();
				currentObjectName = refreshCurrentFileName(rawObjectName, date, false, partitionKey);
				// @TODO: WRITABLE object has been created silently
				// Externalize switch from WRITABLE to non-WRITABLE
				// for BIG-PARTITIONING usecase
				createObject(partitionKey, currentObjectName, tuple);
			}
			// When we lvalue.fDataBuffer.size()eave this block, we know the file is ready to be written
			// to.
		}
				
		if (TRACE.isLoggable(TraceLevel.TRACE)) {
			TRACE.log(TraceLevel.TRACE,	"Looking for active object for partition with key '" + partitionKey + "'"); 
		}
		
		// check if object for the given partition exists in registry.
		// required to make sure that partition specific object is selected.
		fObjectToWrite = fOSObjectRegistry.find(partitionKey);
		
		// not found in registry
		if (fObjectToWrite == null) {
			if (TRACE.isLoggable(TraceLevel.TRACE)) {		
				TRACE.log(TraceLevel.TRACE,	"No object has found for partition key '" + partitionKey + "'"); 
			}
			
			// this is the first time the object is created for the given partition
			Date date = Calendar.getInstance().getTime();
			currentObjectName = refreshCurrentFileName(objectName, date, false, partitionKey);

			// creates and registers object
			// @TODO: WRITABLE object has been created silently
			// Externalize switch from WRITABLE to non-WRITABLE
			// for BIG-PARTITIONING usecase
			createObject(partitionKey, currentObjectName, tuple);
						
			if (TRACE.isLoggable(TraceLevel.TRACE)) {
				TRACE.log(TraceLevel.TRACE,	"New object '" + fObjectToWrite.getPath() + "' has been created for partition key '" + partitionKey + "'"); 
			}
		} 

		try {		
			fObjectToWrite.writeTuple(tuple, partitionKey, fOSObjectRegistry);
		} catch (Exception e) {
			String errRootCause = com.ibm.streamsx.objectstorage.Utils.getErrorRootCause(e);
			String errMsg = Messages.getString("OBJECTSTORAGE_SINK_OBJECT_WRITE_FAILURE", getBucketName(), fObjectToWrite.getPath(), errRootCause);
	    	if (TRACE.isLoggable(TraceLevel.ERROR)) {
	    		TRACE.log(TraceLevel.ERROR,	errMsg); 
				TRACE.log(TraceLevel.ERROR,	"Failed to write to object '" 
												+ fObjectToWrite.getPath() + "' to bucket '"  + getBucketName() + "'. Exception: " + e.getMessage()); 
			}
	    	LOGGER.log(TraceLevel.ERROR, errMsg);
	    	throw new Exception(e);
		}
	}

	/**
	 * The method invoked from mupliple 
	 * OSRegistry listeners immediatly after objec 
	 * @param objectname
	 * @param size
	 * @throws Exception
	 */
	public synchronized void submitOnOutputPort(String objectname) throws Exception {

		if (!hasOutputPort) return;
		
		long objectSize = getObjectStorageClient().getObjectSize(objectname);
		if (TRACE.isLoggable(TraceLevel.TRACE))
			TRACE.log(TraceLevel.TRACE,
					"Submit filename and size on output port: " + objectname  + " " + objectSize); 

		OutputTuple outputTuple = outputPort.newTuple();
		
		outputTuple.setString(0, objectname);
		outputTuple.setLong(1, objectSize);

		// put the output tuple to the queue... to be submitted on process thread
		if (crContext != null) {
			// if consistent region, queue and submit with permit
			outputPortQueue.put(outputTuple);
		}
		else {
			// otherwise, submit immediately
			if (TRACE.isLoggable(TraceLevel.TRACE))
				TRACE.log(TraceLevel.TRACE,
						"Output port found. Submitting immediatly."); 			
			outputPort.submit(outputTuple);
		}
	}

	@Override
	public void shutdown() throws Exception {
		try {
			if (crContext == null) {			
				// close objects for all active partitions
				fOSObjectRegistry.closeAllImmediatly();
			}
			// clean cache and release all resources
			fOSObjectRegistry.shutdownCache();
			
		} finally {
			// remove client-specific cache if required.
			// For example, hadoop-aws S3A client cleans
			// disk cache if required
			IObjectStorageClient osClient = getObjectStorageClient();		
			osClient.cleanCacheIfRequired();
			
			super.shutdown();

			if (outputPortThread != null) {
				outputPortThread.interrupt();
			}		
		}
	}

	@Override
	protected void process() throws Exception {		
		while (!shutdownRequested) {			
			try {
				OutputTuple tuple = outputPortQueue.take();
				if (outputPort != null) {
					
					if (TRACE.isLoggable(TraceLevel.TRACE))
						TRACE.log(TraceLevel.TRACE, "Submit output tuple: " + tuple.toString()); 
					
					// if operator is in consistent region, acquire permit before submitting
					if (crContext != null) {
						crContext.acquirePermit();
					}					
					outputPort.submit(tuple);
				}
			} catch (InterruptedException ie) {
				// the output port thread has been interrupted.
				// Submit all tuples that are still in the queue before shutdown
				LinkedList<OutputTuple> tuplesList = new LinkedList<OutputTuple>();
				outputPortQueue.drainTo(tuplesList);
				for (OutputTuple ot: tuplesList) {
					outputPort.submit(ot);
				}
			}  catch (Exception e) {
				TRACE.log(TraceLevel.ERROR,
						"Exception in output port thread.", e); 

			} finally {			
				// release permit when done submitting
				if (crContext != null) {
					crContext.releasePermit();
				}			
			}			
		}
	}

	@Override
	public void close() throws IOException {
		// StateHandler implementation
	}	
	
	@Override
	public void checkpoint(Checkpoint checkpoint) throws Exception {
		// StateHandler implementation
		if (TRACE.isLoggable(TraceLevel.DEBUG)) {
			TRACE.log(TraceLevel.DEBUG, "Checkpoint " + checkpoint.getSequenceId());
		}
		long num = objectNum;
		checkpoint.getOutputStream().writeLong(num);
	}

	@Override
	public void drain() throws Exception {
		// StateHandler implementation
		if (TRACE.isLoggable(TraceLevel.DEBUG)) {
			TRACE.log(TraceLevel.DEBUG, "Drain " + currentObjectName);
		}
		// close and flush all objects on drain
		List<String> closedObjectNames = fOSObjectRegistry.closeAllImmediatly();
		for (String closedObjectName: closedObjectNames) {
			submitOnOutputPort(closedObjectName);
		}
	}

	@Override
	public void reset(Checkpoint checkpoint) throws Exception {
		// StateHandler implementation
		long num = checkpoint.getInputStream().readLong();
		objectNum = num;
		
		if (TRACE.isLoggable(TraceLevel.DEBUG)) {
			TRACE.log(TraceLevel.DEBUG, "reset objectNum: " + objectNum);
		}
	}

	@Override
	public void resetToInitialState() throws Exception {
		// StateHandler implementation
		objectNum = 0;
	}

	@Override
	public void retireCheckpoint(long id) throws Exception {
		// StateHandler implementation
	}	
	
}
