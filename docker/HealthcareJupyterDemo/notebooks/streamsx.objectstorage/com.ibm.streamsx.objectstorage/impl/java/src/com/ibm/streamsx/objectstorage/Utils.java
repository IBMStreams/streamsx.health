package com.ibm.streamsx.objectstorage;

import static com.ibm.streamsx.objectstorage.client.Constants.PROTOCOL_URI_DELIM;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Logger;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.StreamSchema;
import com.ibm.streams.operator.Tuple;
import com.ibm.streams.operator.Type.MetaType;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.objectstorage.s3.S3Protocol;


public class Utils {
	
	/**
	 * Create a logger specific to this class
	 */
	private static final String CLASS_NAME = "com.ibm.streamsx.objectstorage.Utils";

	private static Logger TRACE = Logger.getLogger(CLASS_NAME);


	public static String getObjectStorageS3URI(S3Protocol protocol, String bucket) {
		return protocol + "://" + bucket + "/";
	}

	public static URI genObjectURI(URI objectStorageURI, String objectName) throws URISyntaxException {
		if (objectName.startsWith(objectStorageURI.getScheme() + PROTOCOL_URI_DELIM)) {			
			return new URI(objectName);
		} else {
			String objectValue = objectName.startsWith("/") ? objectName : "/" + objectName;
			URI res = new URI(objectStorageURI.getScheme(), objectStorageURI.getAuthority(), objectValue, null, null);
			return res;
		}
	}

	/**
	 * Encode URI if not encoded yet
	 * 
	 * @param objectStorageUriStr
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static URI getEncodedURI(String objectStorageUriStr) throws IOException, URISyntaxException {
		String scheme = getProtocol(objectStorageUriStr);
		@SuppressWarnings("deprecation")
		String host = URLEncoder.encode(Utils.getBucket(URLDecoder.decode(objectStorageUriStr)));		
		@SuppressWarnings("deprecation")
		String path = Utils.getPath(URLDecoder.decode(objectStorageUriStr));

		// this specific URI ctor encodes the result
		// return new URI(scheme, host, path, null, null);
		return new URI(scheme + "://" + host + "/" + path);
	}

	public static String getEncodedURIStr(String objectStorageUriStr) throws IOException, URISyntaxException {
		return getEncodedURI(objectStorageUriStr).toString();
	}

	public static String getBucket(String uriStr) {
		int sInd = uriStr.indexOf("//") + 2;
		String host = uriStr.substring(sInd);
		int eInd = host.indexOf("/");

		return host.substring(0, eInd);
	}

	public static String getPath(String uriStr) {
		return uriStr.substring(getProtocol(uriStr).length() + getBucket(uriStr).length() + 3, uriStr.length());
	}

	/**
	 * Extracts protocol from object storage URI
	 */
	public static final String getProtocol(String objectStorageURI) {
		return objectStorageURI.substring(0, objectStorageURI.toString().indexOf(PROTOCOL_URI_DELIM));
	}

	public static boolean isValidObjectStorageUser(String user) {
		if (user == null || user.trim().isEmpty())
			return false;

		return true;
	}
	
	
	/**
	 * Stocator expects property names to be container dependent. The method populates 
	 * template with container name configured on operator level.
	 * @throws URISyntaxException 
	 */
	public static String formatProperty(String propTemplate, String templateValue) throws IOException, URISyntaxException {
		String res = String.format(propTemplate, templateValue);
		if (TRACE.isLoggable(TraceLevel.TRACE)) {
			TRACE.log(TraceLevel.TRACE,	"Formatted property template '" + propTemplate + "' with value '" + templateValue + "': " + res); 
		}	
		return res;
	}

	/**
	 * Gets param single String value from context
	 * @param opContext operator context
	 * @param paramName parameter name
	 * @return single parameter value
	 */
	public static String getParamSingleStringValue(OperatorContext opContext, String paramName, String paramDefault) {
		String res = paramDefault;
		
		if (opContext.getParameterNames().contains(paramName)) {
			res = opContext.getParameterValues(paramName).get(0);
		}
		
		return res;
	}
	
	
	public static MetaType getAttrMetaType(OperatorContext opContext, int paramIndex) {
		return opContext.getStreamingInputs().get(0).getStreamSchema().getAttribute(paramIndex).getType().getMetaType();
	}
	
	/**
	 * Gets param single int value from context
	 * @param opContext operator context
	 * @param paramName parameter name
	 * @return single parameter value
	 */
	public static int getParamSingleIntValue(OperatorContext opContext, String paramName, int paramDefault) {
		return Integer.parseInt(getParamSingleStringValue(opContext, paramName, String.valueOf(paramDefault)));
	}
	
	/**
	 * Gets param boolean value from context
	 * @param opContext operator context
	 * @param paramName parameter name
	 * @return single parameter value
	 */
	public static boolean getParamSingleBoolValue(OperatorContext opContext, String paramName, boolean paramDefault) {
		return Boolean.parseBoolean(getParamSingleStringValue(opContext, paramName, String.valueOf(paramDefault)));
	}

	/**
	 * Gets param list value from context
	 * @param opContext operator context
	 * @param paramName parameter name
	 * @param paramDefault parameter default value
	 * @return parameter value list
	 */
	public static List<String> getParamListValue(OperatorContext opContext, String paramName, List<String> paramDefault) {
		List<String> res = paramDefault;
		if (opContext.getParameterNames().contains(paramName)) {
			res = opContext.getParameterValues(paramName);
		}
		
		return res;
	}
	
	/**
	 * Gets current size of heap in bytes
	 */
	public static long getTotalMemory() {
		return Runtime.getRuntime().totalMemory(); 
	}
	
	/**
	 * Gets maximum size of heap in bytes
	 */
	public static long getMaxMemory() {
		return Runtime.getRuntime().totalMemory(); 
	}
	
	/**
	 * Get amount of free memory within the heap in bytes.  
	 * @return
	 */
	public static long getHeapFreeSize() {
		return Runtime.getRuntime().freeMemory();
	}

	/**
	 * Returns unique cache name 
	 * @param osObjectCacheNamePrefix - constant cache name prefix
	 * @param opContext - operator context
	 * @return unique cache name based on prefix and unique operator name
	 */
	public static String genCacheName(String osObjectCacheNamePrefix, OperatorContext opContext) {
		// The operator name is unique across the application.
		String operatorName = opContext.getName();
		
		String cacheName = osObjectCacheNamePrefix + "-" + operatorName + "-" + opContext.getPE();
		
		return cacheName.replace("[", "_").replace("]", "_");
	}
	
	
	public static final byte[] serialize(Object obj) throws IOException {
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

	public static int getTupleAttrSize(Tuple tuple, int attrIndex, StreamSchema schema) throws IOException {
		int res = 0;
		
		switch (schema.getAttribute(attrIndex).getType().getMetaType()) {
		case BLOB:
		case RSTRING:
			res += tuple.getBuffer(attrIndex).capacity();
			break;
		case ENUM:
		case USTRING:
			res += tuple.getString(attrIndex).getBytes().length;
			break;
		case FLOAT32:
		case FLOAT64:
		case INT16:
		case UINT16:
		case UINT32:
		case INT32:
		case UINT64:
		case INT64:
		case UINT8:
		case INT8:
			res += tuple.getObject(attrIndex).toString().getBytes().length;
			break;			
		default:
			res += tuple.getObject(attrIndex).toString().getBytes().length;
			break;				
		}
		
		return res;
	}

	
	public static long getAttrSize(Tuple tuple, int fDataAttrIndex) throws IOException {		
		return getTupleAttrSize(tuple, fDataAttrIndex, tuple.getStreamSchema());
	}
	
	/**
	 * Returns tuple size 
	 * @param tuple
	 * @return
	 * @throws IOException 
	 */
	public static long getTupleDataSize(Tuple tuple) throws IOException {
		long res = 0;
				
		StreamSchema streamSchema = tuple.getStreamSchema();
		
		for (int i = 0; i < streamSchema.getAttributeCount(); i++) {
			res += getTupleAttrSize(tuple, i, streamSchema);
		}	
		
		return res;
	}

	/**
	 * Estimates given object size.
	 * Warning: the function couldn't be used for precise 
	 * object size estimation and has a significant performance impact.
	 * Use it carefullly!
	 */
	public static int estimateObjectSize(Object obj) throws IOException {
		int res = -1;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			oos.close();
			
			res = baos.size();
		}
		
		return res;
	 }

	public static String getErrorRootCause(Exception e) {
		String res = e.getCause().getMessage();
		Throwable cause = e.getCause();
		if (cause instanceof AmazonS3Exception) {
			res = ((AmazonS3Exception) cause).getErrorMessage();
		}
		
		return res;
	}
}