package com.ibm.streamsx.objectstorage.writer;

import java.io.IOException;

import com.ibm.streams.operator.Tuple;
import com.ibm.streams.operator.Type.MetaType;

public interface IWriter {

	public void write(Tuple tulpe) throws Exception;
	
	public void write(Tuple tuple, int attrIndex, MetaType attrType, String fEncoding) throws Exception;

	public void write(byte[] src) throws IOException;
	
	public void write(char[] src, int offset, int len) throws IOException;
	
	public void flush() throws IOException;
	
	public void close() throws IOException;

	public void flushAll() throws IOException;

	public boolean isClosed();

	/**
	 * Total size of of data written to the object and buffered in memory
	 * @return the total size in bytes
	 */
	public long getDataSize();
	
}
