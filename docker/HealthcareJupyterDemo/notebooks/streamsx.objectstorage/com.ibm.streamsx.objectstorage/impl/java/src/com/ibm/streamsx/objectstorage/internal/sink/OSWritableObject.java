/*******************************************************************************
* Copyright (C) 2017, International Business Machines Corporation
* All Rights Reserved
*******************************************************************************/
package com.ibm.streamsx.objectstorage.internal.sink;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.Tuple;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.objectstorage.Utils;
import com.ibm.streamsx.objectstorage.client.IObjectStorageClient;
import com.ibm.streamsx.objectstorage.writer.IWriter;
import com.ibm.streamsx.objectstorage.writer.WriterFactory;

public class OSWritableObject extends OSObject {
	
	private OperatorContext fOpContext;
	private IObjectStorageClient fObjectStorageClient;
	private IWriter fWriter;

	private static Logger TRACE = Logger.getLogger(OSWritableObject.class.getName());

	public OSWritableObject(OSObject osObject, OperatorContext opContext, IObjectStorageClient osStorageClient)
					throws IOException, Exception {
		super(osObject);
		fOpContext = opContext;
		fObjectStorageClient = osStorageClient;
		initWriter(fDataAttrIndex, fNewLine);
	}

	
	@Override
	public void writeTuple(Tuple tuple, String registryKey, OSObjectRegistry fOSObjectRegistry) throws Exception {		
		
		// send tuple to the relevant writer
		if (StorageFormat.valueOf(fStorageFormat).equals(StorageFormat.parquet)) {
			fWriter.write(tuple);
		} else {				
			fWriter.write(tuple, fDataAttrIndex, Utils.getAttrMetaType(fOpContext, fDataAttrIndex), fEncoding);	
		}
		
		updateRollingPolicyMetrics(tuple);
		
		fOSObjectRegistry.update(registryKey, this);
	}

	/**
	 * Flushes buffer to the writer
	 * @throws Exception
	 */
	public void flushBuffer() throws Exception {
		Iterator<Tuple> dataBufferIt = fDataBuffer.iterator();
		while (dataBufferIt.hasNext()) {
			if (StorageFormat.valueOf(fStorageFormat).equals(StorageFormat.parquet)) {
				fWriter.write(dataBufferIt.next());
			} else {				
				fWriter.write(dataBufferIt.next(), fDataAttrIndex, Utils.getAttrMetaType(fOpContext, fDataAttrIndex), fEncoding);	
			}
		}
	}
	

	/**
	 * Init the writer. Only one thread can create a new writer. Write can be
	 * created by init, write or flush. The synchronized keyword prevents write
	 * and flush to create writer at the same time.
	 * 
	 * @param dataAttrIndex
	 * @param dataAttrType
	 * @param newLine
	 * @param isBinary
	 *            If true, file is considered a binary file. If not, it is
	 *            assumed to be a text file, and a newline is added after each
	 *            write.
	 * @throws IOException
	 * @throws Exception
	 */
	synchronized private void initWriter(int dataAttrIndex, byte[] newLine)
			throws IOException, Exception {
		// generates writer (raw or parquet) according to the
		// given settings
		fWriter = WriterFactory.getInstance().getWriter(fPath, fOpContext, dataAttrIndex, 
				fObjectStorageClient, StorageFormat.valueOf(fStorageFormat), newLine);

		if (TRACE.isLoggable(TraceLevel.TRACE)) {
			TRACE.log(TraceLevel.TRACE, "Writer of type '" + StorageFormat.valueOf(fStorageFormat) +  "' for path '" + fPath + "' has been initialized succefully.");
		}

		if (fHeader != null) {
			//fWriter.write(fHeader.getBytes(fEncoding));
			fWriter.write(fHeader.getBytes("UTF-8"));
		}
	}

	public void close() throws Exception {
		if (TRACE.isLoggable(TraceLevel.TRACE)) {
			TRACE.log(TraceLevel.TRACE, "About to close object '" + fPath + "'");
		}
		
		// mark object as expired
		setExpired();

		if (TRACE.isLoggable(TraceLevel.TRACE)) {
			TRACE.log(TraceLevel.TRACE, "fWriter: '" + fWriter + "'");
		}
		if (fWriter != null && !fWriter.isClosed()) {
			if (TRACE.isLoggable(TraceLevel.TRACE)) {
				TRACE.log(TraceLevel.TRACE, "Closing writer : '" + fWriter + "'");
			}
			fWriter.close();			
		}
	}


	// called by drain method for consistent region
	public void flush() throws Exception {
		// close the current writer and recreate

		if (fWriter != null) {
			fWriter.flushAll();
		}

	}

	public boolean isClosed() {
		if (fWriter != null)
			return fWriter.isClosed();

		return true;
	}

	public IObjectStorageClient getObjectStoreClient() {
		return fObjectStorageClient;
	}

	@Override
	public boolean isWritable() {
		return true;
	}

}
