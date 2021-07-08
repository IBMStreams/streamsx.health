/*******************************************************************************
* Copyright (C) 2017, International Business Machines Corporation
* All Rights Reserved
*******************************************************************************/

package com.ibm.streamsx.objectstorage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;

import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.OperatorContext.ContextCheck;
import com.ibm.streams.operator.OutputTuple;
import com.ibm.streams.operator.StreamSchema;
import com.ibm.streams.operator.StreamingData.Punctuation;
import com.ibm.streams.operator.StreamingInput;
import com.ibm.streams.operator.Tuple;
import com.ibm.streams.operator.Type.MetaType;
import com.ibm.streams.operator.compile.OperatorContextChecker;
import com.ibm.streams.operator.logging.LogLevel;
import com.ibm.streams.operator.logging.LoggerNames;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streams.operator.metrics.Metric;
import com.ibm.streams.operator.metrics.Metric.Kind;
import com.ibm.streams.operator.model.Parameter;
import com.ibm.streams.operator.state.Checkpoint;
import com.ibm.streams.operator.state.CheckpointContext;
import com.ibm.streams.operator.state.ConsistentRegionContext;
import com.ibm.streams.operator.state.StateHandler;

/**
 * Base Scan operator implementation class. 
 * Used by protocol specific operator implementations.
 * @author streamsadmin
 *
 */
public class BaseObjectStorageScan extends AbstractObjectStorageOperator implements StateHandler {

	private static final String CLASS_NAME = "com.ibm.streamsx.objectstorage.ObjectStorageScan";

	// should use logger not tied to LOG_FACILITY to send to trace file instead
	// of log file
	// TODO - Error / Warning messages in the LOG need to be put in the
	// messages.properties
	// file
	private static Logger TRACE = Logger.getLogger(CLASS_NAME);

	private static Logger LOGGER = Logger.getLogger(LoggerNames.LOG_FACILITY + "." + CLASS_NAME);

	private final String NUM_SCANS_METRIC = "nScans";
	private Metric nScans;

	long initDelayMil = 0;

	private long sleepTimeMil = 5000;

	private String pattern;
	private String directory = "";
	private boolean isStrictMode = false;
	private double initDelay;
	private double sleepTime = 5;

	private Object dirLock = new Object();
	private boolean finalPunct = false;

	private ConsistentRegionContext crContext;

	private String fInitialDir;

	// timestamp and objectname of last submitted object
	private long fLastSubmittedTs;
	private String fLastSubmittedObjectName;

	// timestamp and object name to reset to. Only initialized if reset is
	// called
	private long fResetToTs = -1;
	private String fResetToObjectname = "";

	private class ModTimeComparator implements Comparator<Object> {

		@Override
		public int compare(Object arg0, Object arg1) {
			if (arg0 instanceof FileStatus && arg1 instanceof FileStatus) {
				FileStatus s1 = (FileStatus) arg0;
				FileStatus s2 = (FileStatus) arg1;

				long diff = s1.getModificationTime() - s2.getModificationTime();
				if (diff < 0) {
					return -1;
				} else if (diff > 0) {
					return 1;
				}
			}
			return 0;
		}
	}

	private class ObjectNameComparator implements Comparator<Object> {

		@Override
		public int compare(Object arg0, Object arg1) {
			if (arg0 instanceof FileStatus && arg1 instanceof FileStatus) {
				FileStatus s1 = (FileStatus) arg0;
				FileStatus s2 = (FileStatus) arg1;

				return s1.getPath().compareTo(s2.getPath());
			}
			return 0;
		}
	}

	@Parameter(optional = true, description = "Specifies the name of the directory to be scanned. Directory should always be considered in context of bucket or container.")
	public void setDirectory(String directory) {
		TRACE.entering(CLASS_NAME, "setDirectory", directory);
		this.directory = directory;
	}

	public String getDirectory() {
		return directory;
	}

	@Parameter(optional = true, description = "Limits the object names that are listed to the names that match the specified regular expression. The operator ignores object names that do not match the specified regular expression.")
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getPattern() {
		return pattern;
	}

	@Parameter(optional = true, description = "Specifies the time to wait in seconds before the operator scans the bucket/container directory for the first time. The default value is 0.")
	public void setInitDelay(double initDelay) {
		this.initDelay = initDelay;
	}

	@Parameter(optional = true, description = "Specifies the minimum time between bucket/container directory scans. The default value is 5.0 seconds. ")
	public void setSleepTime(double sleepTime) {
		this.sleepTime = sleepTime;
	}

	@Parameter(optional = true, description = "Specifies whether the operator reports an error if the bucket/container directory to be scanned does not exist.")
	public void setStrictMode(boolean strictMode) {
		this.isStrictMode = strictMode;
	}

	public boolean isStrictMode() {
		return isStrictMode;
	}

	@ContextCheck(compile = true)
	public static void checkCheckpointConfig(OperatorContextChecker checker) {
		OperatorContext opContext = checker.getOperatorContext();		
		CheckpointContext chkptContext = opContext.getOptionalContext(CheckpointContext.class);
		if (chkptContext != null) {
			if (chkptContext.getKind().equals(CheckpointContext.Kind.OPERATOR_DRIVEN)) {
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_NOT_CHECKPOINT_OPERATOR_DRIVEN", "ObjectStorageScan"), null);
			}
			if (chkptContext.getKind().equals(CheckpointContext.Kind.PERIODIC)) {
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_NOT_CHECKPOINT_PERIODIC", "ObjectStorageScan"), null);
			}			
		}
	}	
	
	@ContextCheck(compile = true)
	public static void checkConsistentRegion(OperatorContextChecker checker) {

		OperatorContext opContext = checker.getOperatorContext();
		
		ConsistentRegionContext crContext = opContext.getOptionalContext(ConsistentRegionContext.class);
		if (crContext != null) {
			if (crContext.isStartOfRegion() && opContext.getNumberOfStreamingInputs() > 0) {
				checker.setInvalidContext(
						Messages.getString("OBJECTSTORAGE_NOT_CONSISTENT_REGION", "ObjectStorageScan"), null);
			}
		}
	}

	@ContextCheck()
	public static void checkInputPortSchema(OperatorContextChecker checker) {
		List<StreamingInput<Tuple>> streamingInputs = checker.getOperatorContext().getStreamingInputs();
		if (streamingInputs.size() > 0) {
			StreamSchema inputSchema = streamingInputs.get(0).getStreamSchema();
			if (inputSchema.getAttributeCount() > 1) {
				checker.setInvalidContext(Messages.getString("OBJECTSTORAGE_DS_INVALID_INPUT_PORT"), null);
			}

			if (inputSchema.getAttribute(0).getType().getMetaType() != MetaType.RSTRING) {
				checker.setInvalidContext(Messages.getString("OBJECTSTORAGE_DS_INVALID_ATTRIBUTE",
						inputSchema.getAttribute(0).getType().getMetaType()), null);
			}

			ConsistentRegionContext crContext = checker.getOperatorContext()
					.getOptionalContext(ConsistentRegionContext.class);
			if (crContext != null) {
				LOGGER.log(LogLevel.WARNING, Messages.getString("OBJECTSTORAGE_DS_CONSISTENT_REGION_NOT_SUPPORTED"));
			}
		}

	}

	@ContextCheck(compile = true)
	public static void checkOutputPortSchema(OperatorContextChecker checker) throws Exception {
		StreamSchema outputSchema = checker.getOperatorContext().getStreamingOutputs().get(0).getStreamSchema();

		if (outputSchema.getAttributeCount() != 1) {
			checker.setInvalidContext(Messages.getString("OBJECTSTORAGE_DS_INVALID_OUTPUT_PORT"), null);
		}
		if (outputSchema.getAttribute(0).getType().getMetaType() != MetaType.RSTRING) {
			checker.setInvalidContext(Messages.getString("OBJECTSTORAGE_DS_INVALID_ATTRIBUTE")
					+ outputSchema.getAttribute(0).getType().getMetaType(), null);
		}
	}

	@ContextCheck()
	public static void checkParameter(OperatorContextChecker checker) {
		int numInputPorts = checker.getOperatorContext().getNumberOfStreamingInputs();
		if (numInputPorts == 0) {
			Set<String> paramNames = checker.getOperatorContext().getParameterNames();
			if (!paramNames.contains("directory")) {
				checker.setInvalidContext(Messages.getString("OBJECTSTORAGE_DS_INVALID_DIRECTORY_PARAM"), null);
			}
		}
	}

	@ContextCheck(compile = false)
	public static void checkRunTimeError(OperatorContextChecker checker) {
		if (!checker.getOperatorContext().getParameterValues(IObjectStorageConstants.PARAM_SLEEP_TIME).isEmpty()) {
			if (Integer.valueOf(checker.getOperatorContext()
					.getParameterValues(IObjectStorageConstants.PARAM_SLEEP_TIME).get(0)) < 0) {
				checker.setInvalidContext(Messages.getString("OBJECTSTORAGE_DS_INVALID_SLEEP_TIMER_PARAM"), null);
			}
		}

		if (!checker.getOperatorContext().getParameterValues(IObjectStorageConstants.PARAM_INITDELAY).isEmpty()) {
			if (Integer.valueOf(checker.getOperatorContext().getParameterValues(IObjectStorageConstants.PARAM_INITDELAY)
					.get(0)) < 0) {
				checker.setInvalidContext(Messages.getString("OBJECTSTORAGE_DS_INVALID_INIT_DELAY_PARAM"), null);
			}
		}

		/*
		 * Check if the pattern is valid. Set invalid context otherwise.
		 */
		if (checker.getOperatorContext().getParameterNames().contains("pattern")) {
			String pattern = checker.getOperatorContext().getParameterValues("pattern").get(0);
			try {
				java.util.regex.Pattern.compile(pattern);
			} catch (PatternSyntaxException e) {
				checker.setInvalidContext(pattern + Messages.getString("OBJECTSTORAGE_DS_INVALID_PATTERN_PARAM"), null);
			}
		}
	}

	@ContextCheck(compile = false)
	public static void checkUriMatch(OperatorContextChecker checker) throws Exception {
		List<String> objectStorageUriParamValues = checker.getOperatorContext().getParameterValues(IObjectStorageConstants.PARAM_OS_URI);
		List<String> dirParamValues = checker.getOperatorContext().getParameterValues(IObjectStorageConstants.PARAM_OS_OBJECT_NAME);

		String objectStorageURIValue = null;
		if (objectStorageUriParamValues.size() == 1) {
			objectStorageURIValue = objectStorageUriParamValues.get(0);
			if (false == objectStorageURIValue.endsWith("/")) {
				objectStorageURIValue = objectStorageURIValue + "/";
			}
		}

		String dirValue = null;
		if (dirParamValues.size() == 1)
			dirValue = dirParamValues.get(0);

		// only need to perform this check if both 'objectStorageURI' and
		// 'object' params
		// are set
		if (objectStorageURIValue != null && dirValue != null) {
			URI objectStorageUri;
			URI dirUri;
			try {
				objectStorageUri = new URI(objectStorageURIValue);
			} catch (URISyntaxException e) {
				LOGGER.log(TraceLevel.ERROR,
						Messages.getString("OBJECTSTORAGE_DS_INVALID_URL_PARAM", IObjectStorageConstants.PARAM_OS_URI, objectStorageURIValue));
				throw e;
			}

			try {
				dirUri = new URI(dirValue);
			} catch (URISyntaxException e) {
				LOGGER.log(TraceLevel.ERROR,
						Messages.getString("OBJECTSTORAGE_DS_INVALID_URL_PARAM", "dirValue", dirValue));
				throw e;
			}

			if (dirUri.getScheme() != null) {
				// must have the same scheme
				if (!objectStorageUri.getScheme().equals(dirUri.getScheme())) {
					checker.setInvalidContext(Messages.getString("OBJECTSTORAGE_DS_INVALID_DIRECTORY_SCHEMA",
							dirUri.getScheme(), objectStorageUri.getScheme()), null);
					return;
				}

				// must have the same authority
				if ((objectStorageUri.getAuthority() == null && dirUri.getAuthority() != null)
						|| (objectStorageUri.getAuthority() != null && dirUri.getAuthority() == null)
						|| (objectStorageUri.getAuthority() != null && dirUri.getAuthority() != null
								&& !objectStorageUri.getAuthority().equals(dirUri.getAuthority()))) {
					checker.setInvalidContext(Messages.getString("OBJECTSTORAGE_DS_INVALID_HOST_DIRECTORY_SCHEMA",
							dirUri.getAuthority(), objectStorageUri.getAuthority()), null);
					return;
				}
			}
		}
	}

	public void checkStrictMode(OperatorContext context) throws Exception {
		boolean checked = false;
		// directory can be empty

		// When a directory parameter is not specified, check if control input
		// port
		// is present. Warn if so, else throw an exception
		if (!context.getParameterNames().contains("directory")) {
			// if strict mode, directory can be empty if we have an input stream
			if (context.getNumberOfStreamingInputs() == 0) {
				throw new Exception(
						"directory parameter needs to be specified when control input port is not present.");
			} else {
				// warn user that this may be a problem.
				LOGGER.log(LogLevel.WARN, Messages.getString("OBJECTSTORAGE_DS_NOT_SPECIFIED_DIR_PARAM"));
				checked = true;
			}
		}
		if (isStrictMode) {
			if (!checked) {
				if (directory.isEmpty()) {
					throw new Exception(Messages.getString("OBJECTSTORAGE_DS_EMPTY_DIRECTORY_STRICT_MODE"));
				} else if (!getObjectStorageClient().exists(directory)) {
					throw new Exception(
							Messages.getString("OBJECTSTORAGE_DS_DIRECTORY_NOT_EXIST_STRICT_MODE", directory));
				} else if (!getObjectStorageClient().isDirectory(directory)) {
					throw new Exception(Messages.getString("OBJECTSTORAGE_DS_IS_NOT_A_DIRECTORY", directory));
				}
			}
		} else {
			if (!checked) {
				if (directory.isEmpty()) {
					if (context.getNumberOfStreamingInputs() == 1) {
						LOGGER.log(LogLevel.WARN, Messages.getString("OBJECTSTORAGE_DS_EMPTY_DIRECTORY_PARAM"));
						directory = "";
					} else {
						throw new Exception(Messages.getString("OBJECTSTORAGE_DS_EMPTY_DIRECTORY_NOT_CONTROL_PORT"));
					}
				} else if (!getObjectStorageClient().exists(directory)) {
					// TRACE.warning("Directory specified does not exist: " +
					// directory);
					LOGGER.log(LogLevel.WARN, Messages.getString("OBJECTSTORAGE_DS_DIRECTORY_NOT_EXIST", directory));
				} else if (!getObjectStorageClient().isDirectory(directory)) {
					if (context.getNumberOfStreamingInputs() == 1) {
						// throw new
						// Exception("directory parameter value "+directory+"
						// does not refer to a valid directory");
						LOGGER.log(LogLevel.WARN, Messages.getString("OBJECTSTORAGE_DS_IS_NOT_A_DIRECTORY", directory));
						directory = "";// so that it does not break in process
					} else {
						throw new Exception(Messages.getString("OBJECTSTORAGE_DS_IS_NOT_A_DIRECTORY", directory));
					}
				} else {
					try {
						scanDirectory(directory);
					} catch (IOException ex) {
						if (context.getNumberOfStreamingInputs() == 1) {
							LOGGER.log(LogLevel.WARN, ex.getMessage());
							directory = "";
						} else {
							throw ex;
						}
					}
				}
			}
		}
	}

	@Override
	public synchronized void initialize(OperatorContext context) throws Exception {
		if (directory != null) {
			try {
				URI uri = new URI(directory);
				if (TRACE.isLoggable(TraceLevel.TRACE)) {
					TRACE.log(TraceLevel.TRACE, "uri: " + uri.toString());
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
						TRACE.log(TraceLevel.TRACE, "objectStorageUri: " + getURI());
					}

					String path = directory.substring(fs.length());

					if (!path.startsWith("/"))
						path = "/" + path;

					setDirectory(path);
				}
			} catch (URISyntaxException e) {
				TRACE.log(TraceLevel.WARN, "Unable to construct URI: " + e.getMessage());

				throw e;
			}
		}

		super.initialize(context);
		
		// register for data governance
		if ((getDirectory() != null) && (getURI() != null) && (context.getNumberOfStreamingInputs() == 0)) {
			registerForDataGovernance(getURI(), getDirectory());
		}

		// Converstion of the Operator Parameters from seconds to MilliSeconds
		sleepTimeMil = (long) (1000 * sleepTime);
		initDelayMil = (long) (1000 * initDelay);

		nScans = context.getMetrics().createCustomMetric(NUM_SCANS_METRIC, "The number of times operator scans the directory", Kind.COUNTER);

		checkStrictMode(context);

		crContext = context.getOptionalContext(ConsistentRegionContext.class);
		fInitialDir = getDirectory();
		processThread = createProcessThread();
	}

	@Override
	public void allPortsReady() throws Exception {
		super.allPortsReady();

	}

	private void registerForDataGovernance(String serverURL, String directory) {
		if (TRACE.isLoggable(TraceLevel.INFO)) {
			TRACE.log(TraceLevel.INFO, "ObjectStorageScan - Registering for data governance with server URL: " + serverURL + " and directory: " + directory);
		}
		try {
			Map<String, String> properties = new HashMap<String, String>();
			properties.put(IGovernanceConstants.TAG_REGISTER_TYPE, IGovernanceConstants.TAG_REGISTER_TYPE_INPUT);
			properties.put(IGovernanceConstants.PROPERTY_INPUT_OPERATOR_TYPE, "ObjectStorageScan"); 
			properties.put(IGovernanceConstants.PROPERTY_SRC_NAME, directory);
			properties.put(IGovernanceConstants.PROPERTY_SRC_TYPE, IGovernanceConstants.ASSET_OBJECTSTORAGE_OBJECT_TYPE);
			properties.put(IGovernanceConstants.PROPERTY_SRC_PARENT_PREFIX, "p1"); 
			properties.put("p1" + IGovernanceConstants.PROPERTY_SRC_NAME, serverURL); 
			properties.put("p1" + IGovernanceConstants.PROPERTY_SRC_TYPE, IGovernanceConstants.ASSET_OBJECTSTORAGE_SERVER_TYPE); 
			properties.put("p1" + IGovernanceConstants.PROPERTY_PARENT_TYPE, IGovernanceConstants.ASSET_OBJECTSTORAGE_SERVER_TYPE_SHORT);
			if (TRACE.isLoggable(TraceLevel.INFO)) {
				TRACE.log(TraceLevel.INFO, "ObjectStorageScan - Data governance: " + properties.toString());
			}			
			setTagData(IGovernanceConstants.TAG_OPERATOR_IGC, properties);
		}
		catch (Exception e) {
			TRACE.log(TraceLevel.ERROR, "Exception received when registering tag data: "+ e.getMessage());
		}		
	}	
	
	@Override
	public void process(StreamingInput<Tuple> stream, Tuple tuple) throws Exception {
		String newDir = tuple.getString(0);
		boolean dirExists = true;

		if (TRACE.isLoggable(TraceLevel.INFO))
			TRACE.log(TraceLevel.INFO, "Control signal received: " + newDir);

		if (newDir != null) {
			synchronized (dirLock) {
				if (TRACE.isLoggable(TraceLevel.TRACE)) {
					TRACE.log(TraceLevel.TRACE, "Acquired dirLock for control signal");
				}

				if (isStrictMode) {
					if (newDir != null && !getObjectStorageClient().exists(newDir)) {
						dirExists = false;
						throw new Exception("Directory specified from control input port does not exist: " + newDir);
					} else if (newDir != null && !getObjectStorageClient().isDirectory(newDir)) {
						dirExists = false;
						throw new Exception(
								"Directory specified from control input port is not a valid directory: " + newDir);
					} else if (newDir != null && newDir.isEmpty()) {
						dirExists = false;
						throw new Exception("Directory received from input port is empty.");
					}
				} else {

					if (newDir != null && newDir.isEmpty()) {
						dirExists = false;
						LOGGER.log(LogLevel.WARN, Messages.getString("OBJECTSTORAGE_DS_EMPTY_DIRECTORY_INPUT_PORT"));
					} else if (newDir != null && !getObjectStorageClient().exists(newDir)) {
						dirExists = false;
						LOGGER.log(LogLevel.WARN,
								Messages.getString("OBJECTSTORAGE_DS_DIRECTORY_NOT_EXIST_INPUT_PORT", newDir));
					} else if (newDir != null && !getObjectStorageClient().isDirectory(newDir)) {
						dirExists = false;
						LOGGER.log(LogLevel.WARN,
								Messages.getString("OBJECTSTORAGE_DS_INVALID_DIRECTORY_INPUT_PORT", newDir));
					}
				}

				if (newDir != null && !newDir.isEmpty() && !directory.equals(newDir) && dirExists) {
					if (TRACE.isLoggable(TraceLevel.INFO)) {
						TRACE.log(TraceLevel.INFO, "New scan directory is: " + newDir);
					}
					setDirectory(newDir);
				}
				// always notify to allow user to send a signal
				// to force a scan immediately.
				dirLock.notify();
			}
		}
	}

	@Override
	public void processPunctuation(StreamingInput<Tuple> arg0, Punctuation arg1) throws Exception {
		// if final marker
		if (arg1 == Punctuation.FINAL_MARKER) {
			if (TRACE.isLoggable(TraceLevel.TRACE))
				TRACE.log(TraceLevel.TRACE, "Received final punctuation");
			// wake up the process thread
			// cause the process loop to terminate so we do not keep scanning
			synchronized (dirLock) {
				finalPunct = true;
				dirLock.notifyAll();
			}
		}
	}
	
	protected void setOpConfig(Configuration config) throws IOException, URISyntaxException {}

	private void scanDirOnce() throws Exception {
		// unit of work for scanning the directory and submitting tuple.

		String currentDir = null;

		// do not allow reset to happen, while we are trying to figure out the
		// timestamp
		// also disallow changing directory once this has started
		long latestTimeFromLastCycle = 0;
		String latestObjectNameFromLastCycle = "";
		try {
			if (crContext != null) {
				crContext.acquirePermit();
			}
			synchronized (dirLock) {
				currentDir = getDirectory();
			}

			if (fResetToTs != -1) {
				latestTimeFromLastCycle = fResetToTs;
				latestObjectNameFromLastCycle = fResetToObjectname;

				// unset these variables as we are about
				// to actually scan the directory again
				fResetToTs = -1;
				fResetToObjectname = "";
			} else {
				latestTimeFromLastCycle = fLastSubmittedTs;
				latestObjectNameFromLastCycle = fLastSubmittedObjectName;
			}

			if (TRACE.isLoggable(TraceLevel.TRACE)) {
				TRACE.log(TraceLevel.TRACE, "latestTimeFromLastCycle: " + latestTimeFromLastCycle);				
			}
		} finally {
			if (crContext != null) {
				crContext.releasePermit();
			}
		}

		if (currentDir != null) {
			FileStatus[] objects = scanDirectory(currentDir);

			// this returns a list of objects, sorted by modification time.
			for (int i = 0; i < objects.length; i++) {

				FileStatus currentObject = objects[i];

				// if reset, get out of loop immediately
				// next scan will use the reset timestamp and objectname
				if (fResetToTs != -1) {
					// Set the last submitted ts and objectname to the reset
					// value
					// but reset does not happen until the next scan is done.
					// These two variables represent the last fully processed
					// object.
					// If a checkpoint is done before the next can be completed,
					// these two variables will be checkpointed, as they
					// represent
					// the last consistent state.
					fLastSubmittedTs = fResetToTs;
					fLastSubmittedObjectName = fResetToObjectname;
					break;
				}

				if (TRACE.isLoggable(TraceLevel.TRACE)) {
					TRACE.log(TraceLevel.TRACE, "Found Object: " + currentObject.getPath().toString() + " "
							+ currentObject.getModificationTime());
				}
				List<FileStatus> currentSet = new ArrayList<FileStatus>();
				currentSet.add(currentObject);

				// look at the next file, if the next file has the same
				// timestamp... collect all
				// files with same timestamp
				if (i + 1 < objects.length) {
					FileStatus nextFile = objects[i + 1];
					if (nextFile.getModificationTime() == currentObject.getModificationTime()) {
						// this returns a list of files with same timestamp, in
						// alphabetical order
						List<FileStatus> filesWithSameTs = collectObjectsWithSameTimestamp(objects, i);
						i += filesWithSameTs.size() - 1;
						currentSet = filesWithSameTs;
					}
				}

				for (Iterator<FileStatus> iterator = currentSet.iterator(); iterator.hasNext();) {
					FileStatus objectToSubmit = (FileStatus) iterator.next();

					// if reset, get out of loop immediately
					// next scan will use the reset timestamp and filename
					if (fResetToTs != -1)
						break;

					String objectPath = objectToSubmit.getPath().toUri().getPath();

					// if object is newer, always submit
					if (!objectToSubmit.isDirectory() && objectPath != null
							&& objectToSubmit.getModificationTime() > latestTimeFromLastCycle) {
						OutputTuple outputTuple = getOutput(0).newTuple();
						if (TRACE.isLoggable(TraceLevel.TRACE)) {
							TRACE.log(TraceLevel.TRACE, "Submit Object: " + objectToSubmit.getPath().toString() + " "
									+ objectToSubmit.getModificationTime());
						}
						outputTuple.setString(0, objectPath);

						try {
							if (crContext != null) {
								crContext.acquirePermit();
							}
							getOutput(0).submit(outputTuple);
							fLastSubmittedTs = objectToSubmit.getModificationTime();
							fLastSubmittedObjectName = objectToSubmit.getPath().toUri().getPath();

							if (crContext.isTriggerOperator())
								crContext.makeConsistent();
						} finally {
							if (crContext != null) {
								crContext.releasePermit();
							}
						}
					} else if (!objectToSubmit.isDirectory() && objectPath != null
							&& objectToSubmit.getModificationTime() == latestTimeFromLastCycle
							&& currentSet.size() > 1) {

						// if object has same timestamp, then an object should
						// only
						// be submitted if
						// the objectname is > than the last objectname

						if (objectPath.compareTo(latestObjectNameFromLastCycle) > 0) {
							OutputTuple outputTuple = getOutput(0).newTuple();
							if (TRACE.isLoggable(TraceLevel.TRACE)) {
								TRACE.log(TraceLevel.TRACE, "Submit Object: " + objectToSubmit.getPath().toString() + " "
										+ objectToSubmit.getModificationTime());
							}
							outputTuple.setString(0, objectPath);

							try {
								if (crContext != null) {
									crContext.acquirePermit();
								}
								getOutput(0).submit(outputTuple);
								fLastSubmittedTs = objectToSubmit.getModificationTime();
								fLastSubmittedObjectName = objectToSubmit.getPath().toUri().getPath();

								crContext.makeConsistent();
							} finally {
								if (crContext != null) {
									crContext.releasePermit();
								}
							}
						}
					}

				}
			}
		}
	}

	private List<FileStatus> collectObjectsWithSameTimestamp(FileStatus[] objects, int index) {

		ArrayList<FileStatus> toReturn = new ArrayList<FileStatus>();
		FileStatus currentObject = objects[index];
		toReturn.add(currentObject);

		if (TRACE.isLoggable(TraceLevel.INFO))
			TRACE.log(TraceLevel.INFO, "Collect objects with same timestamp: " + currentObject.getPath() + ":"
					+ currentObject.getModificationTime());

		for (int j = index + 1; j < objects.length; j++) {
			FileStatus nextObject = objects[j];
			if (nextObject.getModificationTime() == currentObject.getModificationTime()) {
				toReturn.add(nextObject);
				if (TRACE.isLoggable(TraceLevel.INFO))
					TRACE.log(TraceLevel.INFO, "Collect objects with same timestamp: " + nextObject.getPath() + ":"
							+ nextObject.getModificationTime());
			} else {
				break;
			}
		}

		Collections.sort(toReturn, new ObjectNameComparator());

		return toReturn;
	}

	protected void process() throws IOException {
		if (crContext != null) {
			processConsistent();
		} else {
			processNotConsistent();
		}
	}

	protected void processConsistent() throws IOException {
		if (initDelayMil > 0) {
			try {
				Thread.sleep(initDelayMil);
			} catch (InterruptedException e) {
				TRACE.info("Initial delay interrupted");
			}
		}

		while (!shutdownRequested && !finalPunct) {
			try {
				scanDirOnce();
				synchronized (dirLock) {
					dirLock.wait(sleepTimeMil);
				}
			} catch (Exception e) {
			}
		}

		if (TRACE.isLoggable(TraceLevel.TRACE))
			TRACE.log(TraceLevel.TRACE, "Exited directory scan loop");

		try {
			getOutput(0).punctuate(Punctuation.FINAL_MARKER);
		} catch (Exception e) {
			LOGGER.log(TraceLevel.ERROR, Messages.getString("OBJECTSTORAGE_DS_PUNCTUATION_FAILED") + e);
		}
	}

	protected void processNotConsistent() throws IOException {

		if (initDelayMil > 0) {
			try {
				Thread.sleep(initDelayMil);
			} catch (InterruptedException e) {
				TRACE.info("Initial delay interrupted");
			}
		}
		long lastTimestamp = 0;
		while (!shutdownRequested && !finalPunct) {
			long scanStartTime = System.currentTimeMillis();
			FileStatus[] objects = new FileStatus[0];
			String currentDir = "";
			synchronized (dirLock) {
				if (TRACE.isLoggable(TraceLevel.TRACE))
					TRACE.log(TraceLevel.TRACE, "Acquired dirLock for scanDirectory");
				currentDir = getDirectory();
				// only scan if a directory is specified
				if (!currentDir.isEmpty()) {
					objects = scanDirectory(currentDir);
				}
			}
			if (objects.length > 0) {
				long lastTimestampInThisScan = 0;
				for (FileStatus object : objects) {
					if (object.isDirectory()) {
						if (TRACE.isLoggable(LogLevel.INFO)) {
							TRACE.log(TraceLevel.INFO, "Skipping " + object.toString() + " because it is a directory.");
						}
					} else {
						long objectTimestamp = object.getModificationTime();
						if (TRACE.isLoggable(TraceLevel.TRACE))
							TRACE.log(TraceLevel.TRACE,
									"Object: " + object.getPath().toString() + " " + objectTimestamp);
						if (objectTimestamp > lastTimestamp) {
							lastTimestampInThisScan = Math.max(objectTimestamp, lastTimestampInThisScan);
							// object path can be retrieved from URI without
							// parsing
							String objectPath = object.getPath().toUri().getPath();
							if (objectPath != null) {
								OutputTuple outputTuple = getOutput(0).newTuple();
								if (TRACE.isLoggable(TraceLevel.INFO))
									TRACE.log(TraceLevel.INFO, "Submit Object: " + object.getPath().toString());
								outputTuple.setString(0, objectPath);
								try {
									getOutput(0).submit(outputTuple);
								} catch (Exception e) {
									LOGGER.log(TraceLevel.ERROR,
											Messages.getString("OBJECTSTORAGE_DS_SUBMITTING_FAILED", e));
								}
							}
						}
					}
				}
				lastTimestamp = Math.max(lastTimestamp, lastTimestampInThisScan);
			}
			// TODO: What happens if the scan takes so long
			// to finish, and we start again immediately?
			synchronized (dirLock) {
				if (TRACE.isLoggable(TraceLevel.TRACE))
					TRACE.log(TraceLevel.TRACE, "Acquire dir lock to detect changes in process method");
				// if no control signal has come in, wait...
				if (getDirectory().equals(currentDir)) {
					if (TRACE.isLoggable(TraceLevel.TRACE))
						TRACE.log(TraceLevel.TRACE, "Directory not changed, check if we need to sleep.");
					long currentTime = System.currentTimeMillis();
					long timeBeforeNextScan = sleepTimeMil - (currentTime - scanStartTime);
					if (timeBeforeNextScan > 0) {
						try {
							if (TRACE.isLoggable(TraceLevel.INFO))
								TRACE.log(TraceLevel.INFO, "Sleeping for..." + timeBeforeNextScan);
							dirLock.wait(timeBeforeNextScan);
						} catch (Exception e) {
							TRACE.log(TraceLevel.TRACE, "Sleep time interrupted");
						} finally {
							if (!getDirectory().equals(currentDir)) {
								TRACE.log(TraceLevel.TRACE, "Directory changed, reset lastTimestamp");
								lastTimestamp = 0;
							}
						}
					}
				} else {
					TRACE.log(TraceLevel.TRACE, "Directory changed, reset lastTimestamp");
					lastTimestamp = 0;
				}
			}
		}
		if (TRACE.isLoggable(TraceLevel.TRACE))
			TRACE.log(TraceLevel.TRACE, "Exited directory scan loop");
		try {
			getOutput(0).punctuate(Punctuation.FINAL_MARKER);
		} catch (Exception e) {
			LOGGER.log(TraceLevel.ERROR, Messages.getString("OBJECTSTORAGE_DS_PUNCTUATION_FAILED", e));
		}

	}

	private FileStatus[] scanDirectory(String directory) throws IOException {

		if (TRACE.isLoggable(TraceLevel.INFO))
			TRACE.log(TraceLevel.INFO, "scanDirectory: " + directory);

		FileStatus[] objects = new FileStatus[0];

		nScans.incrementValue(1);
		objects = getObjectStorageClient().scanDirectory(directory, getPattern());

		Arrays.sort(objects, new ModTimeComparator());
		return objects;
	}

	@Override
	public void shutdown() throws Exception {
		if (processThread != null) {
			processThread.interrupt();
			processThread = null;
		}
		OperatorContext context = getOperatorContext();
		TRACE.log(TraceLevel.DEBUG, "Operator " + context.getName() + " shutting down in PE: "
				+ context.getPE().getPEId() + " in Job: " + context.getPE().getJobId());

		// Must call super.shutdown()
		super.shutdown();
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
		// checkpoint scan time and directory
		checkpoint.getOutputStream().writeObject(getDirectory());

		// when checkpoint, save the timestamp - 1 to get the tuples to replay
		// as we are always looking for file that is larger that the last
		// timestamp
		checkpoint.getOutputStream().writeLong(fLastSubmittedTs);
		if (TRACE.isLoggable(TraceLevel.DEBUG)) {
			TRACE.log(TraceLevel.DEBUG,  "Checkpoint timestamp " + fLastSubmittedTs);
		}
		checkpoint.getOutputStream().writeObject(fLastSubmittedObjectName);
		if (TRACE.isLoggable(TraceLevel.DEBUG)) {
			TRACE.log(TraceLevel.DEBUG,  "Checkpoint objectname " + fLastSubmittedObjectName);
		}
	}

	@Override
	public void drain() throws Exception {
		// StateHandler implementation
	}

	@Override
	public void reset(Checkpoint checkpoint) throws Exception {
		// StateHandler implementation
		if (TRACE.isLoggable(TraceLevel.DEBUG)) {
			TRACE.log(TraceLevel.DEBUG, "Reset to checkpoint " + checkpoint.getSequenceId());
		}
		String ckptDir = (String) checkpoint.getInputStream().readObject();

		synchronized (dirLock) {
			setDirectory(ckptDir);
		}

		fResetToTs = checkpoint.getInputStream().readLong();
		if (TRACE.isLoggable(TraceLevel.DEBUG)) {
			TRACE.log(TraceLevel.DEBUG,  "Reset timestamp " + fResetToTs);
		}		
		fResetToObjectname = (String)checkpoint.getInputStream().readObject();
		if (TRACE.isLoggable(TraceLevel.DEBUG)) {
			TRACE.log(TraceLevel.DEBUG,  "Reset objectname " + fResetToObjectname);
		}
	}

	@Override
	public void resetToInitialState() throws Exception {
		// StateHandler implementation		
		synchronized (dirLock) {
			setDirectory(fInitialDir);
		}
		fResetToTs = 0;
		fResetToObjectname = "";
	}

	@Override
	public void retireCheckpoint(long id) throws Exception {
		// StateHandler implementation		
	}	
}