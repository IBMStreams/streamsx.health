/*******************************************************************************
* Copyright (C) 2017, International Business Machines Corporation
* All Rights Reserved
*******************************************************************************/


package com.ibm.streamsx.objectstorage;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;

import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.OperatorContext.ContextCheck;
import com.ibm.streams.operator.OutputTuple;
import com.ibm.streams.operator.StreamSchema;
import com.ibm.streams.operator.StreamingData.Punctuation;
import com.ibm.streams.operator.StreamingInput;
import com.ibm.streams.operator.StreamingOutput;
import com.ibm.streams.operator.Tuple;
import com.ibm.streams.operator.Type.MetaType;
import com.ibm.streams.operator.compile.OperatorContextChecker;
import com.ibm.streams.operator.logging.LogLevel;
import com.ibm.streams.operator.logging.LoggerNames;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streams.operator.metrics.Metric;
import com.ibm.streams.operator.model.Parameter;
import com.ibm.streams.operator.state.Checkpoint;
import com.ibm.streams.operator.state.CheckpointContext;
import com.ibm.streams.operator.state.ConsistentRegionContext;
import com.ibm.streams.operator.state.StateHandler;
import com.ibm.streams.operator.types.ValueFactory;
import com.ibm.streamsx.objectstorage.client.IObjectStorageClient;

/**
 * Base Source operator implementation class. 
 * Used by protocol specific operator implementations.
 * @author streamsadmin
 *
 */
public class BaseObjectStorageSource extends AbstractObjectStorageOperator implements StateHandler {

	private static final String CLASS_NAME = "com.ibm.streamsx.objectstorage.ObjectStorageSource"; 
	private static Logger LOGGER = Logger.getLogger(LoggerNames.LOG_FACILITY + "." + CLASS_NAME); 
	private static Logger TRACE = Logger.getLogger(BaseObjectStorageSource.class.getName());

	private static final int BUFFER_SIZE = 1024*1024*8;

	// TODO check that name matches object source change required
	private static final String OBJECTS_OPENED_METRIC = "nObjectsOpened"; 
	private static final String BLOCKSIZE_PARAM = "blockSize"; 
	private Metric nObjectsOpened;

	private String fObjectName;
	private double fInitDelay;

	boolean fIsFirstTuple = true;
	boolean fBinaryObject = false;
	private int fBlockSize = 1024 * 4;

	private String fEncoding = "UTF-8"; 

	private ConsistentRegionContext fCrContext;

	private InputStream fDataStream;
	
	private long fSeekPosition = -1;
	private long fSeekToLine = -1;
	private long fLineNum = -1;
	private boolean fProcessThreadDone = false;
	private boolean fGenOpenObjPunct = false;


	@Override
	public synchronized void initialize(OperatorContext context)
			throws Exception {
		if (fObjectName != null) {
			try {
				URI uri = new URI(fObjectName);
				if (TRACE.isLoggable(TraceLevel.TRACE)) {
					LOGGER.log(TraceLevel.TRACE, "uri: " + uri.toString());
				}

				String scheme = uri.getScheme();
				if (scheme != null) {
					String fs;
					if (uri.getAuthority() != null)
						fs = scheme + "://" + uri.getAuthority(); 
					else
						fs = scheme + ":///"; 

					if (getURI() == null)
						setURI(fs);
					if (TRACE.isLoggable(TraceLevel.TRACE)) {
						LOGGER.log(TraceLevel.TRACE, "objectStorageUri: " + getURI());
					}
					// Use original parameter value
					String path = fObjectName.substring(fs.length());

					if (!path.startsWith("/")) 
						path = "/" + path; 

					setObjectName(path);
				}
			} catch (URISyntaxException e) {
				LOGGER.log(TraceLevel.WARN,
						Messages.getString("OBJECTSTORAGE_SOURCE_INVALID_URL", e.getMessage())); 

				throw e;
			}
		}

		super.initialize(context);
		
		// register for data governance
		// only register if static objectname mode
		if (fObjectName != null && getURI() != null) {
			registerForDataGovernance(getURI(), fObjectName);
		}

		// Associate the aspect Log with messages from the SPL log
		// logger.
		setLoggerAspects(LOGGER.getName(), "OSObjectSource"); 

		initMetrics(context);

		if (fObjectName != null) {
			processThread = createProcessThread();
		}

		StreamSchema outputSchema = context.getStreamingOutputs().get(0)
				.getStreamSchema();
		MetaType outType = outputSchema.getAttribute(0).getType().getMetaType();
		// If we ever switch to the generated xml files, we'll be able to delete
		// this.
		if (context.getParameterNames().contains(BLOCKSIZE_PARAM)) {
			TRACE.fine("Blocksize parameter is supplied, setting blocksize based on that."); 
			fBlockSize = Integer.parseInt(context.getParameterValues(
					BLOCKSIZE_PARAM).get(0));
		} else {
			TRACE.fine("Blocksize parameter not supplied, using default " 
					+ fBlockSize);
		}
		if (MetaType.BLOB == outType) {
			fBinaryObject = true;
			TRACE.info("Object will be read as a binary blobs of size " 
					+ fBlockSize);
		} else {
			TRACE.info("Objects will be read as text objects, with one tuple per line."); 
		}

		fCrContext = context.getOptionalContext(ConsistentRegionContext.class);
	}

	protected void setOpConfig(Configuration config) throws IOException, URISyntaxException {}
	
	private void registerForDataGovernance(String serverURL, String object) {
		if (TRACE.isLoggable(TraceLevel.INFO)) {
			TRACE.log(TraceLevel.INFO, "ObjectStorageSource - Registering for data governance with server URL: " + serverURL + " and object: " + object);
		}
		try {		
			Map<String, String> properties = new HashMap<String, String>();
			properties.put(IGovernanceConstants.TAG_REGISTER_TYPE, IGovernanceConstants.TAG_REGISTER_TYPE_INPUT);
			properties.put(IGovernanceConstants.PROPERTY_INPUT_OPERATOR_TYPE, "ObjectStorageSource"); 
			properties.put(IGovernanceConstants.PROPERTY_SRC_NAME, object);
			properties.put(IGovernanceConstants.PROPERTY_SRC_TYPE, IGovernanceConstants.ASSET_OBJECTSTORAGE_OBJECT_TYPE);
			properties.put(IGovernanceConstants.PROPERTY_SRC_PARENT_PREFIX, "p1"); 
			properties.put("p1" + IGovernanceConstants.PROPERTY_SRC_NAME, serverURL); 
			properties.put("p1" + IGovernanceConstants.PROPERTY_SRC_TYPE, IGovernanceConstants.ASSET_OBJECTSTORAGE_SERVER_TYPE); 
			properties.put("p1" + IGovernanceConstants.PROPERTY_PARENT_TYPE, IGovernanceConstants.ASSET_OBJECTSTORAGE_SERVER_TYPE_SHORT);
			if (TRACE.isLoggable(TraceLevel.INFO)) {
				TRACE.log(TraceLevel.INFO, "ObjectStorageSource - Data governance: " + properties.toString());
			}
			setTagData(IGovernanceConstants.TAG_OPERATOR_IGC, properties);
		}
		catch (Exception e) {
			TRACE.log(TraceLevel.ERROR, "Exception received when registering tag data: "+ e.getMessage());
		}
	}
	
	@ContextCheck(compile = true)
	public static void validateParameters(OperatorContextChecker checker)
			throws Exception {
		List<StreamingInput<Tuple>> streamingInputs = checker
				.getOperatorContext().getStreamingInputs();

		/*
		 * If there is no input port, then, the parameter object would become
		 * mandatory. Set context as invalid otherwise
		 */
		if (streamingInputs.size() == 0
				&& !checker.getOperatorContext().getParameterNames()
						.contains(IObjectStorageConstants.PARAM_OS_OBJECT_NAME)) { 
			checker.setInvalidContext(
					Messages.getString("OBJECTSTORAGE_SOURCE_INVALID_PARAM"), 
					null);
		}

		/*
		 * If both input port and object parameter is specified, throw an
		 * exception
		 */
		if (streamingInputs.size() == 1
				&& checker.getOperatorContext().getParameterNames()
						.contains(IObjectStorageConstants.PARAM_OS_OBJECT_NAME)) { 
			checker.setInvalidContext(
					Messages.getString("OBJECTSTORAGE_SOURCE_ONLY_ONE_PARAM"), 
					null);
		}
	}
	
	@ContextCheck(compile = true)
	public static void checkCheckpointConfig(OperatorContextChecker checker) {
		OperatorContext opContext = checker.getOperatorContext();		
		CheckpointContext chkptContext = opContext.getOptionalContext(CheckpointContext.class);
		if (chkptContext != null) {
			if (chkptContext.getKind().equals(CheckpointContext.Kind.OPERATOR_DRIVEN)) {
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_NOT_CHECKPOINT_OPERATOR_DRIVEN", "ObjectStorageSource"), null);
			}
			if (chkptContext.getKind().equals(CheckpointContext.Kind.PERIODIC)) {
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_NOT_CHECKPOINT_PERIODIC", "ObjectStorageSource"), null);
			}			
		}
	}
	
	@ContextCheck(compile = true)
	public static void checkConsistentRegion(OperatorContextChecker checker) {		
		OperatorContext opContext = checker.getOperatorContext();
		ConsistentRegionContext crContext = opContext.getOptionalContext(ConsistentRegionContext.class);
		if (crContext != null)
		{
			if (crContext.isStartOfRegion() && opContext.getNumberOfStreamingInputs()>0)
			{
				checker.setInvalidContext(Messages.getString("OBJECTSTORAGE_NOT_CONSISTENT_REGION", "ObjectStorageSource"), null); 
			}
		}
	}

	@ContextCheck(compile = false)
	public static void validateParametersRuntime(OperatorContextChecker checker)
			throws Exception {
		OperatorContext context = checker.getOperatorContext();

		/*
		 * Check if initDelay is negative
		 */
		if (context.getParameterNames().contains("initDelay")) { 
			if (Integer.valueOf(context.getParameterValues("initDelay").get(0)) < 0) { 
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_SOURCE_INVALID_INIT_DELAY_PARAM"), 
						new String[] { context.getParameterValues("initDelay") 
								.get(0).trim() });
			}
		}
	}

	@ContextCheck(compile = true)
	public static void checkOutputPortSchema(OperatorContextChecker checker)
			throws Exception {
		StreamSchema outputSchema = checker.getOperatorContext()
				.getStreamingOutputs().get(0).getStreamSchema();

		// check that number of attributes is 1
		if (outputSchema.getAttributeCount() != 1) {
			checker.setInvalidContext(
					Messages.getString("OBJECTSTORAGE_SOURCE_INVALID_OUTPUT"), 
					null);
		}

		if (outputSchema.getAttribute(0).getType().getMetaType() != MetaType.RSTRING
				&& outputSchema.getAttribute(0).getType().getMetaType() != MetaType.USTRING
				&& outputSchema.getAttribute(0).getType().getMetaType() != MetaType.BLOB) {
			checker.setInvalidContext(
					Messages.getString("OBJECTSTORAGE_SOURCE_INVALID_ATTR_TYPE", outputSchema.getAttribute(0).getType().getMetaType()), 
					null);
		}

		if (MetaType.BLOB != outputSchema.getAttribute(0).getType()
				.getMetaType()
				&& checker.getOperatorContext().getParameterNames()
						.contains(BLOCKSIZE_PARAM)) {
			checker.setInvalidContext(Messages.getString("OBJECTSTORAGE_SOURCE_INVALID_BLOCKSIZE_PARAM", "BLOCKSIZE_PARAM"), 
					null);
		}
	}

	@ContextCheck(compile = true)
	public static void checkInputPortSchema(OperatorContextChecker checker)
			throws Exception {
		List<StreamingInput<Tuple>> streamingInputs = checker
				.getOperatorContext().getStreamingInputs();

		// check that we have max of one input port
		if (streamingInputs.size() > 1) {
			throw new Exception("OSObjectSource can only have one input port"); 
		}

		// if we have an input port
		if (streamingInputs.size() == 1) {

			StreamSchema inputSchema = checker.getOperatorContext()
					.getStreamingInputs().get(0).getStreamSchema();

			// check that number of attributes is 1
			if (inputSchema.getAttributeCount() != 1) {
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_SOURCE_INVALID_FILENAME_ATTR"), 
						null);
			}

			// check that the attribute type must be a rstring
			if (MetaType.RSTRING != inputSchema.getAttribute(0).getType()
					.getMetaType()) {
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_SOURCE_INVALID_STRING_ATTR", inputSchema.getAttribute(0).getType().getMetaType()), 
										null);
			}
		}
	}

	@ContextCheck(compile = false)
	public static void checkUriMatch(OperatorContextChecker checker)
			throws Exception {
		List<String> objectStorageUriParamValues = checker.getOperatorContext()
				.getParameterValues(IObjectStorageConstants.PARAM_OS_URI); 
		List<String> objectNameParamValues = checker.getOperatorContext()
				.getParameterValues(IObjectStorageConstants.PARAM_OS_OBJECT_NAME); 

		String objectStorageUriValue = null;
		if (objectStorageUriParamValues.size() == 1) {
			objectStorageUriValue = objectStorageUriParamValues.get(0);
			if (false == objectStorageUriValue.endsWith("/")) {
				objectStorageUriValue = objectStorageUriValue + "/";
			}
		}

		String objectValue = null;
		if (objectNameParamValues.size() == 1)
			objectValue = objectNameParamValues.get(0);

		// only need to perform this check if both 'objectStorageUri' and 'file' params
		// are set
		if (objectStorageUriValue != null && objectValue != null) {
			URI objectStorageUri;
			URI objectUri;
			try {
				objectStorageUri = new URI(objectStorageUriValue);
			} catch (URISyntaxException e) {
				LOGGER.log(TraceLevel.ERROR,
						Messages.getString("OBJECTSTORAGE_SOURCE_INVALID_OBJECTSTORAGE_URL", objectStorageUriValue));
				throw e;
			}

			try {
				objectUri = new URI(objectValue);
			} catch (URISyntaxException e) {
				LOGGER.log(TraceLevel.ERROR,
						Messages.getString("OBJECTSTORAGE_SOURCE_INVALID_FILE_URL", objectValue));
				throw e;
			}

			if (objectUri.getScheme() != null) {
				// must have the same scheme
				if (!objectStorageUri.getScheme().equals(objectUri.getScheme())) {
					checker.setInvalidContext(
							Messages.getString("OBJECTSTORAGE_SOURCE_INVALID_SCHEMA", objectUri.getScheme(), objectStorageUri.getScheme()), 
							null); 
					return;
				}

				// must have the same authority
				if ((objectStorageUri.getAuthority() == null && objectUri.getAuthority() != null)
						|| (objectStorageUri.getAuthority() != null && objectUri
								.getAuthority() == null)
						|| (objectStorageUri.getAuthority() != null
								&& objectUri.getAuthority() != null && !objectStorageUri
								.getAuthority().equals(objectUri.getAuthority()))) {
					checker.setInvalidContext(
							Messages.getString("OBJECTSTORAGE_SOURCE_INVALID_HOST", objectUri.getAuthority(), objectStorageUri.getAuthority()), 
									null); 
					return;
				}
			}
		}
	}

	private void initMetrics(OperatorContext context) {
		nObjectsOpened = context.getMetrics()
				.createCustomMetric(OBJECTS_OPENED_METRIC, "The number of opjects that are opened by the operator for reading data.", Metric.Kind.COUNTER);
	}

	private void processObject(String objectname) throws Exception {
		if (LOGGER.isLoggable(LogLevel.INFO)) {
			LOGGER.log(LogLevel.INFO, Messages.getString("OBJECTSTORAGE_SOURCE_PROCESS_OBJECT", objectname)); 
		}
		IObjectStorageClient objectStorageClient = getObjectStorageClient();
		if (fBlockSize == 0) {
			fBlockSize = (int)objectStorageClient.getObjectSize(objectname);
			TRACE.fine("Blocksize parameter is zero, setting blocksize based on object size in object storage. Blocksize value is '" + fBlockSize + "'"); 
		}

		try {
			if (fCrContext != null) {
				fCrContext.acquirePermit();
			}
			openObject(objectStorageClient, objectname);

		} finally {
			if (fCrContext != null) {
				fCrContext.releasePermit();
			}
		}
		
		if (fDataStream == null) {
			LOGGER.log(LogLevel.ERROR, Messages.getString("OBJECTSTORAGE_SOURCE_NOT_OPENING_OBJECT", objectname)); 
			return;
		}
		
		nObjectsOpened.incrementValue(1);
		StreamingOutput<OutputTuple> outputPort = getOutput(0);
		try {
			if (fBinaryObject) {
				doReadBinaryObject(fDataStream, outputPort);
			} else {
				doReadTextObject(fDataStream, outputPort, objectname);
			}
		} catch (IOException e) {
			LOGGER.log(LogLevel.ERROR,
					Messages.getString("OBJECTSTORAGE_SOURCE_EXCEPTION_READ_OBJECT"), e.getMessage()); 
		} finally {
			closeObject();
		}
		outputPort.punctuate(Punctuation.WINDOW_MARKER);
		
		if (fCrContext != null && fCrContext.isStartOfRegion() && fCrContext.isTriggerOperator()) {
			try  {
				fCrContext.acquirePermit();					
				fCrContext.makeConsistent();
			}
			finally {
				fCrContext.releasePermit();
			}
		}
	}

	private void closeObject() throws IOException {
		if (fDataStream != null)
			fDataStream.close();
	}

	private InputStream openObject(IObjectStorageClient objectStorageClient, String objectName)
			throws IOException {
		
		if (objectName != null)
			fDataStream = objectStorageClient.getInputStream(objectName);
		
		// reset counter every time we open an object
		fLineNum = 0;
		return fDataStream;
	}

	private void doReadTextObject(InputStream dataStream,
			StreamingOutput<OutputTuple> outputPort, String objectname)
			throws UnsupportedEncodingException, IOException, Exception {				
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				dataStream, fEncoding), BUFFER_SIZE);			

		String line = null;
		do {
			try {
				
				if (fCrContext != null) {
					fCrContext.acquirePermit();
				}
				
				if (fSeekToLine >=0) {

					TRACE.info("Process Object Seek to position: " + fSeekToLine);					 
					
					reader.close();
					closeObject();
					dataStream = openObject(getObjectStorageClient(), objectname);									
									
					// create new reader and start reading at beginning
					reader = new BufferedReader(new InputStreamReader(
							dataStream, fEncoding), BUFFER_SIZE);
					
					for (int i=0; i<fSeekToLine; i++) {
						// skip the lines that have already been processed
						reader.readLine();
						fLineNum++;
					}
					
					fSeekToLine = -1;
				}
								
				line = reader.readLine();
				fLineNum ++;				
	
				if (line != null) {
					// submit tuple
					OutputTuple outputTuple = outputPort.newTuple();
					outputTuple.setString(0, line);
					outputPort.submit(outputTuple);
				}
				
			} finally {
				if (fCrContext != null)
				{
					fCrContext.releasePermit();
				}
			}

		} while (line != null);

		reader.close();
	}

	private void doReadBinaryObject(InputStream dataStream,
			StreamingOutput<OutputTuple> outputPort) throws IOException,
			Exception {
		
		byte readBuffer[] = new byte[fBlockSize];		
		// allocate a direct byte buffer
	    ByteArrayOutputStream localOutStream = new ByteArrayOutputStream(fBlockSize);
		
		int numRead = 0;		
		do {			
			try {
				
				if (fCrContext != null) {
					fCrContext.acquirePermit();
				}
				
				if (fSeekPosition >=0) {
					TRACE.info("reset to position: " + fSeekPosition); 
					((FSDataInputStream)dataStream).seek(fSeekPosition);
					fSeekPosition = -1;
				}
				
				numRead = dataStream.read(readBuffer);
				if (numRead > 0) localOutStream.write(readBuffer, 0, numRead);
				TRACE.info("buffer size " + readBuffer.length + ", numRead = " + numRead +  ", localOutStream.size: " + localOutStream.size());
				// block size or file end has been reached
				// skips empty files for now
				if ((localOutStream.size() >= fBlockSize) || ((numRead <= 0) && (localOutStream.size() > 0))) {
					OutputTuple toSend = outputPort.newTuple();					
					toSend.setBlob(0, ValueFactory.newBlob(localOutStream.toByteArray(), 0, localOutStream.size()));
					TRACE.info("submitting blob of size " + localOutStream.size());
					outputPort.submit(toSend);
					localOutStream.reset();
				}
				
			}
			finally {
				if (fCrContext != null) {
					fCrContext.releasePermit();
				}
			}			
		} while (numRead > 0);
		
		try {
			localOutStream.close();
		} catch (Exception e) {
			LOGGER.log(LogLevel.ERROR, Messages.getString("OBJECTSTORAGE_ASYNC_UNABLE_WRITE_TO_STREAMS"), e.getMessage()); 

		}
	}

	
	
	// called on background thread
	protected void process() throws Exception {
		
		fProcessThreadDone = false;
		if (fInitDelay > 0) {
			try {
				Thread.sleep((long) (fInitDelay * 1000));
			} catch (InterruptedException e) {
				LOGGER.log(LogLevel.INFO, Messages.getString("OBJECTSTORAGE_SOURCE_INIT_DELAY_INTERRUPTED")); 
			}
		}
		try {
			if (!shutdownRequested) {
				processObject(fObjectName);
			}
		}finally {
			fProcessThreadDone = true;
		}
	}

	@Override
	public void process(StreamingInput<Tuple> stream, Tuple tuple)
			throws Exception {
		if (shutdownRequested)
			return;
		
		// object name to read received - generated window marker when required
		if (fGenOpenObjPunct && getOperatorContext().getNumberOfStreamingOutputs() > 0) {
			getOutput(0).punctuate(Punctuation.WINDOW_MARKER);
		}
		
		if (fIsFirstTuple && fInitDelay > 0) {
			try {
				Thread.sleep((long) (fInitDelay * 1000));
			} catch (InterruptedException e) {
				LOGGER.log(LogLevel.INFO, Messages.getString("OBJECTSTORAGE_SOURCE_INIT_DELAY_INTERRUPTED")); 
			}
		}
		fIsFirstTuple = false;
		String objectname = tuple.getString(0);

		// check if file name is an empty string. If so, log a warning and
		// continue with the next tuple
		if (objectname.isEmpty()) {
			LOGGER.log(LogLevel.WARN, Messages.getString("OBJECTSTORAGE_SOURCE_EMPTY_OBJECT_NAME")); 
		} else {
			try {
				processObject(objectname);
			} catch (IOException ioException) {
				LOGGER.log(LogLevel.WARN, ioException.getMessage());
			}
		}
	}

	@Parameter(optional = true, description = "Specifies the name of the object that the operator opens and reads. This parameter must be specified when the optional input port is not configured. If the optional input port is used and the object name is specified, the operator generates an error.")
	public void setObjectName(String objectName) {
		this.fObjectName = objectName;
	}

	@Parameter(optional = true, description = "Specifies the time to wait in seconds before the operator starts to read object. The default value is 0.")
	public void setInitDelay(double initDelay) {
		this.fInitDelay = initDelay;
	}

	@Parameter(optional = true, description = "Specifies the encoding to use when reading files. The default value is UTF-8.")
	public void setEncoding(String encoding) {
		this.fEncoding = encoding;
	}
	
	@Parameter(name=BLOCKSIZE_PARAM,optional=true, description  = "Specifies the maximum number of bytes to be read at one time when reading an object into binary mode (ie, into a blob); thus, it is the maximum size of the blobs on the output stream. The parameter is optional, and defaults to 4096.")
	public void setBlockSize (int inBlockSize) {
		fBlockSize = inBlockSize;
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
		
		if (!isDynamicObject()) {
			long pos = -1;
			if (fBinaryObject) {
				// for binary object
				FSDataInputStream fsDataStream = (FSDataInputStream)fDataStream;
				pos = fsDataStream.getPos();
			}
			if (TRACE.isLoggable(TraceLevel.DEBUG)) {
				TRACE.log(TraceLevel.DEBUG, "checkpoint position: " + pos);
			}			
			checkpoint.getOutputStream().writeLong(pos);
			
			// for text object
			if (TRACE.isLoggable(TraceLevel.DEBUG)) {
				TRACE.log(TraceLevel.DEBUG, "checkpoint lineNumber: " + fLineNum);
			}
			checkpoint.getOutputStream().writeLong(fLineNum);
		}
	}

	@Override
	public void drain() throws Exception {
		// StateHandler implementation
	}

	@Override
	public void reset(Checkpoint checkpoint) throws Exception {
		// StateHandler implementation
		if (!isDynamicObject()) {
			if (TRACE.isLoggable(TraceLevel.DEBUG)) {
				TRACE.log(TraceLevel.DEBUG, "Reset " + checkpoint.getSequenceId());
			}
			// for binary object
			long pos = checkpoint.getInputStream().readLong();
			fSeekPosition = pos;					
			// for text object
			fSeekToLine = checkpoint.getInputStream().readLong();
			
			if (TRACE.isLoggable(TraceLevel.DEBUG)) {
				TRACE.log(TraceLevel.DEBUG, "reset position: " + fSeekPosition);
				TRACE.log(TraceLevel.DEBUG, "reset lineNumber: " + fSeekToLine);
			}			
			// if thread is not running anymore, restart thread
			if (fProcessThreadDone) {
				if (TRACE.isLoggable(TraceLevel.DEBUG)) {
					TRACE.log(TraceLevel.DEBUG, "reset process thread");
				}
				processThread = createProcessThread();
				startProcessing();
			}
		}
	}

	@Override
	public void resetToInitialState() throws Exception {
		// StateHandler implementation
		if (!isDynamicObject()) {
			if (TRACE.isLoggable(TraceLevel.DEBUG)) {
				TRACE.log(TraceLevel.DEBUG, "Seek to 0");
			}
			fSeekPosition = 0;
			fSeekToLine = 0;
			if (TRACE.isLoggable(TraceLevel.DEBUG)) {
				TRACE.log(TraceLevel.DEBUG, "reset position: " + fSeekPosition);
				TRACE.log(TraceLevel.DEBUG, "reset lineNumber: " + fSeekToLine);
			}
			// if thread is not running anymore, restart thread
			if (fProcessThreadDone) {
				if (TRACE.isLoggable(TraceLevel.DEBUG)) {
					TRACE.log(TraceLevel.DEBUG, "reset process thread");
				}
				processThread = createProcessThread();
				startProcessing();
			}
		}
	}

	@Override
	public void retireCheckpoint(long id) throws Exception {
		// StateHandler implementation
	}
	
	private boolean isDynamicObject() {
		return getOperatorContext().getNumberOfStreamingInputs() > 0;
	}	
	
}
