/*******************************************************************************
* Copyright (C) 2014, International Business Machines Corporation
* All Rights Reserved
*******************************************************************************/
package com.ibm.streamsx.objectstorage.writer.raw;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.hadoop.fs.FSDataOutputStream;

import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.Tuple;
import com.ibm.streams.operator.Type.MetaType;
import com.ibm.streams.operator.logging.LogLevel;
import com.ibm.streams.operator.logging.LoggerNames;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.objectstorage.Messages;
import com.ibm.streamsx.objectstorage.client.IObjectStorageClient;
import com.ibm.streamsx.objectstorage.writer.IWriter;

public class RawAsyncWriter extends Writer implements IWriter {
	
	private static final String CLASS_NAME = RawAsyncWriter.class.getName();
	private static Logger LOGGER = Logger.getLogger(LoggerNames.LOG_FACILITY + "." + CLASS_NAME); 
	private static Logger TRACE = Logger.getLogger(CLASS_NAME);
	
	//private static final int BUFFER_QUEUE_DEPTH = 1024 * 256; // 256 MB
	private static final int BUFFER_QUEUE_DEPTH = 1; // 1K
	private static final int BUFFER_QUEUE_SIZE = 3;

	
	private byte[] fBuffer;
	private int fBufferSize;
	private byte[] fNewline;
	private OutputStream out;
	private int position;
	private boolean isClosed = false;
	private ExecutorService exService;
	private LinkedBlockingQueue<byte[]> bufferQueue;
	private ThreadFactory fThreadFactory;
	private int fWrittenDataLen = 0;
	
	private Object exServiceLock = new Object();
	
	private class FlushRunnable implements Runnable {
		
		protected byte[] flushBuffer;
		private boolean isAddBuffer;
		private int bufferPosition;
		private boolean newline;
		
		public FlushRunnable(byte[] buffer, boolean addBuffer, int position, boolean newline) {
			flushBuffer = buffer;		
			isAddBuffer = addBuffer;
			bufferPosition = position;
			this.newline = newline;
		}

		@Override
		public void run() {
			try {
				out.write(flushBuffer, 0, bufferPosition);	
				
				if (newline && fNewline.length > 0)
					out.write(fNewline, 0, fNewline.length);
				
				// force HDFS output stream to flush
				if (out instanceof FSDataOutputStream)
				{
					((FSDataOutputStream)out).hflush();
				}
				else {
					out.flush();
				}
			} catch (IOException e) {
				LOGGER.log(LogLevel.ERROR, Messages.getString("OBJECTSTORAGE_ASYNC_UNABLE_WRITE_TO_STREAMS"), e); 
			}		
			finally {
				if (isAddBuffer)
					addBuffer();
			}
		}

		private void addBuffer() {
			try {					
				if (!isClosed && bufferQueue.size() <= BUFFER_QUEUE_SIZE)
					bufferQueue.put(new byte[fBufferSize]);
			} catch (InterruptedException e) {
				LOGGER.log(LogLevel.INFO, Messages.getString("OBJECTSTORAGE_ASYNC_UNABLE_ADD_TO_QUEUE"), e); 
			}
		}		
	}

	public RawAsyncWriter(OutputStream outputStream, int size, ThreadFactory threadFactory, byte[] newline)  {
	
		out = outputStream;
		fBufferSize = size;
		fNewline = newline;
		fThreadFactory = threadFactory;
		
		init();
	}

	public RawAsyncWriter(String objPath, 
					   OperatorContext opContext, 
			           IObjectStorageClient objectStorageClient, 
			           byte[] newLine) throws IOException {
		
		out =  objectStorageClient.getOutputStream(objPath, false);
		fBufferSize = BUFFER_QUEUE_DEPTH;
		fNewline = newLine;
		fThreadFactory = opContext.getThreadFactory();
	
		init();
	}




	private void init() {

		synchronized (exServiceLock) {
			exService = Executors.newSingleThreadExecutor();
			bufferQueue = new LinkedBlockingQueue<byte[]>(BUFFER_QUEUE_SIZE);
			try {
				for (int i = 0; i < BUFFER_QUEUE_SIZE; i++) {
					bufferQueue.put(new byte[fBufferSize]);
				}

				// take one buffer, two left in the queue
				fBuffer = bufferQueue.take();
			} catch (InterruptedException e) {
				LOGGER.log(LogLevel.ERROR,
						Messages.getString("OBJECTSTORAGE_ASYNC_INVALID_BUFFER_QUEUE"), e); 
			}
		}
	}

	@Override
	public void close() throws IOException {		
		synchronized(exServiceLock) {
		if (!isClosed)
		{
			isClosed = true;	
			// buffered data should be flushed
			// prior service shutdown
			flushNow();
			
			// shut down the execution service, so no other flush runnable can be scheduled 
			// and wait for any flush job currently scheduled or running to finish
			exService.shutdown();
			try {
				exService.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				LOGGER.log(LogLevel.WARN, Messages.getString("OBJECTSTORAGE_ASYNC_SERVICE_SHUTDOWN_INTERRUPTED"), e); 
			} finally {
				// do final flushing of buffer
				out.close();
				bufferQueue.clear();
				fWrittenDataLen = 0;
			}
		}		
	}
	}

	@Override
	public void flush() throws IOException {

		if (fBuffer.length > 0) {
			synchronized (exServiceLock) {
				FlushRunnable runnable = new FlushRunnable(fBuffer, true,
						position, false);
				exService.execute(runnable);

				try {
					if (!isClosed)
						fBuffer = bufferQueue.take();
					position = 0;
				} catch (InterruptedException e) {
					LOGGER.log(LogLevel.ERROR,
							Messages.getString("OBJECTSTORAGE_ASYNC_UNABLE_GET_BUFFER_QUEUE"), e); 
				}
			}
		}
	}
	
	protected void flushNow() throws IOException {
		if (fBuffer.length > 0)
		{
			FlushRunnable runnable = new FlushRunnable(fBuffer, false, position, false);
			runnable.run();
			position = 0;
		}
	}
	
	public void flushAll() throws IOException
	{
		synchronized(exServiceLock) {
			// shut down the execution service, so no other flush runnable can be scheduled 
			// and wait for any flush job currently scheduled or running to finish
			exService.shutdown();
			try {
				exService.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				LOGGER.log(LogLevel.WARN, Messages.getString("OBJECTSTORAGE_ASYNC_SERVICE_SHUTDOWN_INTERRUPTED"), e); 
			}finally {

				// do final flushing of buffer
				flushNow();
				
				// after flushing, recreate exService
				init();
			}
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
		// increase written data length counter
		fWrittenDataLen += src.length;
		
		// if exceed buffer
		if((position+src.length+fNewline.length) > fBufferSize)
		{
			// flush the buffer
			flush();
			
			// write new content			
			synchronized (exServiceLock) {
				FlushRunnable runnable = new FlushRunnable(src, false,
						src.length, true);
				exService.execute(runnable);
			}
			
		}
		else {
			// store in buffer			
			System.arraycopy(src, 0, fBuffer, position, src.length);
			position += src.length;
			System.arraycopy(fNewline, 0, fBuffer, position, fNewline.length);
			position+= fNewline.length;

		}		
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
		return fWrittenDataLen;
	}
}
