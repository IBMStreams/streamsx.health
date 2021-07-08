package com.ibm.streamsx.objectstorage.internal.sink;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.StreamSchema;
import com.ibm.streams.operator.Tuple;
import com.ibm.streams.operator.Type.MetaType;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.objectstorage.IObjectStorageConstants;
import com.ibm.streamsx.objectstorage.Utils;
import com.ibm.streamsx.objectstorage.client.Constants;
import com.ibm.streamsx.objectstorage.client.IObjectStorageClient;

/**
 * Contains object creation
 * logic based on operator parameters
 * 
 * @author streamsadmin
 *
 */
@SuppressWarnings("unused")
public class OSObjectFactory {

	private String fEncoding = null;
	private String fStorageFormat = null;
	private Integer fTimePerObject  = 0;
	private Integer fDataBytesPerObject = 0;
	private Integer fTuplesPerObject = 0;
	private List<String> fPartitionAttributeNames = null;
	private String fNullPartitionDefaultValue;
	private OperatorContext fOpContext;
	
	private static final String CLASS_NAME = OSObjectFactory.class.getName(); 
	private static Logger TRACE = Logger.getLogger(CLASS_NAME);

	/**
	 * Ctor
	 * @param context
	 */
	public OSObjectFactory(OperatorContext context) {
		
		fOpContext = context;
		
		// Parameters relevant for OSObject creation	 
		fEncoding = Utils.getParamSingleStringValue(context, IObjectStorageConstants.PARAM_ENCODING, "UTF-8");
		fStorageFormat = Utils.getParamSingleStringValue(context, IObjectStorageConstants.PARAM_STORAGE_FORMAT, StorageFormat.raw.name());
		fTimePerObject = Utils.getParamSingleIntValue(context, IObjectStorageConstants.PARAM_TIME_PER_OBJECT, 0);
		fDataBytesPerObject = Utils.getParamSingleIntValue(context, IObjectStorageConstants.PARAM_BYTES_PER_OBJECT, 0);
		fTuplesPerObject = Utils.getParamSingleIntValue(context, IObjectStorageConstants.PARAM_TUPLES_PER_OBJECT, 0);
		fPartitionAttributeNames  = Utils.getParamListValue(context, IObjectStorageConstants.PARAM_PARTITION_VALUE_ATTRIBUTES, null);
		fNullPartitionDefaultValue  = Utils.getParamSingleStringValue(context, IObjectStorageConstants.PARAM_NULL_PARTITION_DEFAULT_VALUE, "__HIVE_DEFAULT_PARTITION__");		
	}
	
	
	public OSObject createObject(final String partitionPath,
			                     final String objectname, 
			                     final String fHeaderRow, 
			                     final int dataIndex, 
			                     final MetaType dataType,			                     
			                     final Tuple tuple) throws IOException, Exception {
		if (TRACE.isLoggable(TraceLevel.TRACE)) {
			TRACE.log(TraceLevel.TRACE,	"Partition attribute names: '" + fPartitionAttributeNames  + "'"); 
		}

				
		RollingPolicyType rollingPolicyType = getRollingPolicyType(fTimePerObject, fDataBytesPerObject, fTuplesPerObject);
		
		OSObject res = new OSObject(
		  objectname,
		  fHeaderRow, 
		  fEncoding, 
		  dataIndex,
		  fStorageFormat);
		
		res.setPartitionPath(partitionPath != null ? partitionPath : "");
		res.setRollingPolicyType(rollingPolicyType.toString());
		
		return res;
	}
	
	public OSWritableObject createWritableObject(final String partitionPath,
            final String objectname, 
            final String fHeaderRow, 
            final int dataIndex, 
            final MetaType dataType,			                     
            final Tuple tuple, 
            IObjectStorageClient objectStorageClient) throws IOException, Exception {

		OSObject osObject = createObject(partitionPath, objectname, fHeaderRow, dataIndex, dataType, tuple);
		
		// create writable OSObject
		return new OSWritableObject(osObject, fOpContext, objectStorageClient);
	}
	
	private RollingPolicyType getRollingPolicyType(Integer timePerObject, Integer dataBytesPerObject, Integer tuplesPerObject) {
		if (timePerObject > 0) return RollingPolicyType.TIME;
		if (dataBytesPerObject > 0) return RollingPolicyType.SIZE;
		if (tuplesPerObject > 0) return RollingPolicyType.TUPLES_NUM;
		
		return RollingPolicyType.UNDEFINED;
	}


	public String getPartitionPath(Tuple tuple) {
		StringBuffer res = new StringBuffer();
		StreamSchema tupleSchema = tuple.getStreamSchema();
		if (fPartitionAttributeNames != null && !fPartitionAttributeNames.isEmpty() && tuple != null) {
			// concatenate object name with partition attributes.
			// This will automatically create path if not exists
			String tupleValue = null, quote = "";
			boolean nonEmptyAttrVal = false;
			for (String attrName: fPartitionAttributeNames) {
				nonEmptyAttrVal = tuple.getObject(attrName) != null && tuple.getObject(attrName).toString().length() > 0;
				tupleValue = nonEmptyAttrVal ? tuple.getObject(attrName).toString() : fNullPartitionDefaultValue;
				// add key
				res.append(attrName + "=");
//				quote = (tupleSchema.getAttribute(attrName).getType().getMetaType().equals(MetaType.RSTRING)	||
//						tupleSchema.getAttribute(attrName).getType().getMetaType().equals(MetaType.USTRING)) && nonEmptyAttrVal ? "'" : "";
				// add value
				res.append(quote + tupleValue + quote + Constants.URI_DELIM);
			}			
		}
		
		return res.toString();
	}
}
