/*******************************************************************************
* Copyright (C) 2014, International Business Machines Corporation
* All Rights Reserved
*******************************************************************************/
package com.ibm.streamsx.objectstorage.writer.raw;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.logging.Logger;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;

import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.Tuple;
import com.ibm.streams.operator.Type.MetaType;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.objectstorage.client.IObjectStorageClient;
import com.ibm.streamsx.objectstorage.writer.IWriter;

public class RawSyncWriter extends Writer implements IWriter {
	
	private static final String CLASS_NAME = RawSyncWriter.class.getName();
	private static Logger TRACE = Logger.getLogger(CLASS_NAME);
	
	private byte[] fNewline;
	private OutputStream out;
	private String fOutObjPath;
	private boolean isClosed = false;

	public RawSyncWriter(OutputStream outputStream, int size,  byte[] newline)  {
	
		out = outputStream;
		fNewline = newline;
	}

	public RawSyncWriter(String objPath, 
					   OperatorContext opContext, 
			           IObjectStorageClient objectStorageClient, 
			           byte[] newLine) throws IOException {
		fOutObjPath = objPath;
		out =  objectStorageClient.getOutputStream(objPath, false);
		fNewline = newLine;
	}

	@Override
	public void close() throws IOException {		
		isClosed = true;
		// do final flushing of buffer
		if (out != null) {
			if (TRACE.isLoggable(TraceLevel.TRACE)) {
				TRACE.log(TraceLevel.TRACE,	"Closing raw writer for path '" + fOutObjPath + "'"); 
			}
			flush();
			out.close();
		}
	}

	@Override
	public void flush() throws IOException {
		if  (isClosed()) return;
		
		// force HDFS output stream to flush
		if (out instanceof FSDataOutputStream)
		{
			((FSDataOutputStream)out).hflush();
		}
		else {
			out.flush();
		}
	}

	@Override
	public void write(char[] src, int offset, int len) throws IOException {		
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void write(Tuple tulpe) throws IOException {		
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * Write the byte array to underlying output stream when buffer is full
	 * For each call to write method, a newline is appended
	 * @param src byte array to write
	 * @throws IOException 
	 */
	public void write(byte[] src) throws IOException {		
		if  (isClosed()) return;
		
		// write line
		out.write(src);
		
		// add new line if required
		if (fNewline != null && fNewline.length > 0)
			out.write(fNewline, 0, fNewline.length);
	}
	
	public boolean isClosed() {
		return isClosed;
	}

	

	@Override
	public void write(Tuple tuple, int attrIndex, MetaType attrType, String encoding) throws Exception {
		
		byte[] tupleBytes = SPLConverter.SPLPrimitiveToByteArray(tuple, attrIndex, attrType, encoding);		
		if (TRACE.isLoggable(TraceLevel.TRACE)) {
			TRACE.log(TraceLevel.TRACE, tupleBytes.length + " bytes about to be written.");
		}
		write(tupleBytes);		
	}

	@Override
	public long getDataSize() {
		// @TODO
		return 0;
	}

	@Override
	public void flushAll() throws IOException {
		out.flush();
		
	}
}
