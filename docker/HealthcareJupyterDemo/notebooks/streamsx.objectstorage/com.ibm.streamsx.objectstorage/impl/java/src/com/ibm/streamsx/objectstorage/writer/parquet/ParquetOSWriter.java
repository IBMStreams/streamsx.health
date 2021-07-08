package com.ibm.streamsx.objectstorage.writer.parquet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.ParquetWriter;

import com.ibm.streams.operator.Attribute;
import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.StreamSchema;
import com.ibm.streams.operator.Tuple;
import com.ibm.streams.operator.Type.MetaType;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.objectstorage.writer.IWriter;

public class ParquetOSWriter implements IWriter {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1293553921281565295L;
	private ParquetWriter<List<String>> fParquetWriter = null;
	private boolean fIsClosed = true;
	private Path fOutObjPath;
	
	private final static Logger TRACE = Logger.getLogger(ParquetOSWriter.class.getName());

    public static final int DEFAULT_BLOCK_SIZE = 134217728;
	public static final int DEFAULT_PAGE_SIZE = 1048576;
 
	private static final int DATA_PORT_INDEX = 0;
	
	/**
	 * Ctor
	 * @throws IOException 
	 */
	@SuppressWarnings( "deprecation")
	public ParquetOSWriter(final Path outFilePath, 
			  			   final OperatorContext context,
			  			   final Configuration osConfig) throws Exception {		
		this(outFilePath, context, osConfig, null);
	}
	

	/**
	 * Ctor
	 * @throws IOException 
	 */
	@SuppressWarnings( "deprecation")
	public ParquetOSWriter(final Path outFilePath, 
			  			   final OperatorContext context,			  			   
			  			   final Configuration osConfig,
			  			   final ParquetWriterConfig pwConfig) throws Exception {
		
		ParquetWriterConfig config = pwConfig == null ?  getDefaultPWConfig() : pwConfig;		
		fOutObjPath = outFilePath;
		
		// generate schema from an output tuple format
		String parquetSchemaStr = ParquetSchemaGenerator.getInstance().generateParquetSchema(context, DATA_PORT_INDEX);
		
		fParquetWriter = ParquetHadoopWriter.build(outFilePath, parquetSchemaStr, config, osConfig);
		
		fIsClosed = false;
	}
	
	public static ParquetWriterConfig getDefaultPWConfig() {
		return new ParquetWriterConfig(ParquetWriter.DEFAULT_COMPRESSION_CODEC_NAME,
									   ParquetWriter.DEFAULT_BLOCK_SIZE,
									   ParquetWriter.DEFAULT_PAGE_SIZE,
									   ParquetWriter.DEFAULT_PAGE_SIZE,
									   ParquetWriter.DEFAULT_IS_DICTIONARY_ENABLED,
									   ParquetWriter.DEFAULT_IS_VALIDATING_ENABLED,
									   ParquetWriter.DEFAULT_WRITER_VERSION);
	}

	
	@Override
	public void write(Tuple tuple) throws Exception {
		if  (isClosed()) return;
		
		StringBuffer msg = new StringBuffer(); 
    	StreamSchema schema = tuple.getStreamSchema();
    	String val = null;
        int attrCount = schema.getAttributeCount();
        List<String> tupleValues = new ArrayList<String>();
        //if (TRACE.isLoggable(TraceLevel.TRACE)) {
		//	msg.append("Tuple converted to writable values :\n");
        //}
		for (int i=0; i < attrCount;i++) {
			Attribute attr = schema.getAttribute(i);
			if (attr.getType().getMetaType() == MetaType.TIMESTAMP) {
				com.ibm.streams.operator.types.Timestamp tupeTS = tuple.getTimestamp(i);
				if (tupeTS.getSeconds() + tupeTS.getNanoseconds() == 0)
					val = "";
				else
					val = tuple.getTimestamp(i).getSQLTimestamp().toString();
			} else {
				val = tuple.getObject(i).toString();
			}
			//if (TRACE.isLoggable(TraceLevel.TRACE)) {
			//	msg.append("\t" + attr.getName() + " [" + attr.getType().toString() + "(" + val.length() + ")]" + val + "\n");
			//}
			tupleValues.add(val);
		}
		//if (TRACE.isLoggable(TraceLevel.TRACE)) {
		//	TRACE.log(TraceLevel.TRACE, msg.toString());
		//}			
		fParquetWriter.write(tupleValues);
	}

	@Override
	public void write(Tuple tuple, int attrIndex, MetaType attrType, String fEncoding) throws Exception {
		write(tuple);		
	}
	
	@Override
	public void write(byte[] src) throws IOException {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public void write(char[] src, int offset, int len) throws IOException {
		throw new UnsupportedOperationException();		
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void flushAll() throws IOException {		
	}
	
	@Override
	public void close() throws IOException {
		
		fIsClosed = true;
		if (fParquetWriter != null) {			
			if (TRACE.isLoggable(TraceLevel.TRACE)) {
				TRACE.log(TraceLevel.TRACE,	"Closing parquet writer for path '" + fOutObjPath + "'"); 
			}			
			fParquetWriter.close();
		}
	}

	@Override
	public boolean isClosed() {
		return fIsClosed;
	}


	@Override
	public long getDataSize() {
		return fParquetWriter.getDataSize();
	}
}
