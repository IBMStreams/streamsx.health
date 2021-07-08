/*******************************************************************************
* Copyright (C) 2017, International Business Machines Corporation
* All Rights Reserved
*******************************************************************************/
package com.ibm.streamsx.objectstorage.internal.sink;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.ibm.streams.operator.Tuple;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.objectstorage.Utils;


public class OSObject   {

	protected String fPath;
	protected String fHeader;

	private boolean fIsExpired = false;
	private long fExpiryTSMillis = 0; 
	
	private static final String UTF_8 = "UTF-8";
	
	protected String fEncoding = UTF_8;
	
	protected byte[] fNewLine;
	
	/// The index of the attribute that matters.
	protected int fDataAttrIndex = -1;
	
	protected String fPartitionPath;
	
	private boolean isAppend = false; 	// default is false, overwrite file
	
	protected String fStorageFormat = StorageFormat.parquet.toString();
	protected String fRollingPolicyType = null;

	protected ArrayList<Tuple> fDataBuffer;
	
	protected long fDataBufferSize = 0;
	protected int fDataBufferCount = 0;
	
	private static Logger TRACE = Logger.getLogger(OSObject.class.getName());

	
	/**
	 * Ctor required for kryo desiralization
	 * DO NOT USE IT FROM CODE
	 */
	public OSObject() {}
	
	/**
	 * Copy ctor
	 */
	public OSObject(OSObject osObject) {
	    this(osObject.fPath, 
	    	 osObject.fHeader, 
	    	 osObject.fEncoding, 
	    	 osObject.fDataAttrIndex, 
	    	 osObject.fStorageFormat.toString(),
	    	 osObject.fPartitionPath,
	    	 osObject.fRollingPolicyType);
	    
	    fDataBuffer = osObject.fDataBuffer;
	}

	/**
	 * Create an instance of OSObject with empty data buffer	
	 */
	public OSObject(final String path, 
				    final String header, 				    
				    String dataEncoding,
				    final int dataAttrIndex, 				    
				    final String storageFormat)  {
		
		//@TODO perf
		if (TRACE.isLoggable(TraceLevel.TRACE)) {
			TRACE.log(TraceLevel.TRACE, "Initializing OSObject with path '"  + path + "' and storage format '" + storageFormat + "'");
		}
		
		fPath = path;
		fHeader = header;

		if (dataEncoding == null)
		{
			dataEncoding = UTF_8;
		}
		
		fEncoding = dataEncoding;

		//@TODO perf
		try {
			fNewLine = System.getProperty("line.separator").getBytes(fEncoding);
		} catch (UnsupportedEncodingException e) {
			fNewLine = System.getProperty("line.separator").getBytes();
		}
		fDataAttrIndex = dataAttrIndex;
		fStorageFormat = storageFormat;
	
		fDataBuffer = new ArrayList<Tuple>();
	}

	public OSObject(final String path, 
		    final String header, 				    
		    String dataEncoding,
		    final int dataAttrIndex, 				    
		    final String storageFormat,
		    final String partitionPath,
		    final String rollingPolicyType)  {
		
		this(path, 
		  	 header, 
		  	 dataEncoding, 
		   	 dataAttrIndex, 
		   	 storageFormat.toString());
		
		setPartitionPath(partitionPath != null ? partitionPath : "");
		setRollingPolicyType(rollingPolicyType.toString());
	}
	
	public void writeTuple(Tuple tuple, String registryKey, OSObjectRegistry fOSObjectRegistry) throws Exception {
				
		fDataBuffer.add(tuple);
		
		updateRollingPolicyMetrics(tuple);
		
		fOSObjectRegistry.update(registryKey, this);
	}

	protected void updateRollingPolicyMetrics(Tuple tuple) throws IOException {
		// tuple size detection only for size-based rolling
		// policy
		if (RollingPolicyType.valueOf(fRollingPolicyType) == RollingPolicyType.SIZE) {
			if (fDataAttrIndex >= 0) {
				fDataBufferSize += Utils.getAttrSize(tuple, fDataAttrIndex) + fNewLine.length;
			} else {
				fDataBufferSize += Utils.getTupleDataSize(tuple);
			}
		} else if (RollingPolicyType.valueOf(fRollingPolicyType) == RollingPolicyType.TUPLES_NUM){
			fDataBufferCount++;
		}
	}
	

	public void setExpired() {
		fIsExpired = true;
	}

	public boolean isExpired() {
		return fIsExpired;
	}

	
	public void setAppend(boolean append) {
		this.isAppend = append;
	}
	
	public void setPartitionPath(String path) {
		this.fPartitionPath = path;
	}
	
	public String getPartitionPath() {
		return this.fPartitionPath;
	}
	
	public boolean isAppend() {
		return isAppend;
	}
	
	public String getPath() {
		return fPath;
	}

	public String getHeader() {
		return fHeader;
	}

	public String getDataEncoding() {
//		return fEncoding;
		return "";
	}
	
	public void setExpiryTSMillis(long expiryTSMillis) {
		fExpiryTSMillis = expiryTSMillis;
	}
	
	public long getExpiryTSMillis() {
		return fExpiryTSMillis;
	}
	
	public long getDataSize() {
		return fDataBufferSize;
	}
	
	public ArrayList<Tuple> getDataBuffer() {
		return fDataBuffer;
	}
	
	public String toString() {
		StringBuffer res = new StringBuffer();
		res.append("fPath = " + fPath + "\n");
		res.append("fHeader = " + fHeader + "\n");
		//res.append("fEncoding = " + fEncoding + "\n");
		res.append("fDataAttrIndex = " + fDataAttrIndex + "\n");
		res.append("fStorageFormat = " + fStorageFormat + "\n");
		res.append("fDataBuffer.size()  = " + fDataBuffer + "\n");

		//res.append("fDataBuffer.size()  = " + fDataBuffer == null? 0 : fDataBuffer.size() + "\n");
		
		return res.toString();
	}

	public void setRollingPolicyType(String rollingPolicyType) {
		fRollingPolicyType = rollingPolicyType;
		
	}

	public boolean isWritable() {
		return false;
	}
}
