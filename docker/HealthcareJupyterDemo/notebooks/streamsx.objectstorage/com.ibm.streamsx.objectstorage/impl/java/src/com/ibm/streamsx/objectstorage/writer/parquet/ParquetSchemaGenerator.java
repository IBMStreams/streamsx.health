package com.ibm.streamsx.objectstorage.writer.parquet;


import org.apache.log4j.Logger;
   
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
   
import com.ibm.streams.operator.Attribute;
import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.StreamSchema;
import com.ibm.streams.operator.StreamingInput;
import com.ibm.streams.operator.Tuple;
import com.ibm.streams.operator.Type;
import com.ibm.streams.operator.meta.CollectionType;
import com.ibm.streams.operator.meta.MapType;


public class ParquetSchemaGenerator {
	
	private static ParquetSchemaGenerator instance = null;
	
	/**
	 * Trace
	 */
	private final static Logger TRACE = Logger.getLogger(ParquetSchemaGenerator.class.getName());		

	
	public static ParquetSchemaGenerator getInstance() {
		if (instance == null) {
			instance = new ParquetSchemaGenerator();
		}
		
		return instance;
	}
	
	
	private String generateParquetSchema(StreamSchema schema) throws Exception {
		StringBuffer parquetSchema = new StringBuffer();    	
    	java.util.List<Type> attrTypesList = new ArrayList<Type>();

    	Attribute attr;
    	Type attrType;
    	String attrName = null;    	
		parquetSchema.append("message tupleParquetSchema  { \n");    					   
		int attrCount = schema.getAttributeCount();
		for (int i=0; i < attrCount;i++) {
			attr = schema.getAttribute(i);
			attrName = attr.getName();
			attrType = attr.getType();
			
			if (attrType.getMetaType().isCollectionType()) {
				parquetSchema.append(SPLCollectionToParquetType(attr));
			} else {								   
				parquetSchema.append("optional " + SPLPrimitiveToParquetType(attrType, attrName) + ";\n");   
				attrTypesList.add(attrType);			
			}			
		}
		parquetSchema.append("}\n");
    	
		if (TRACE.isInfoEnabled()) {
    		TRACE.info("Generated parquet schema: \n'" + parquetSchema + "'");
		}
		
    	return parquetSchema.toString();
	}
	
	/**
     * Generates parquet schema from an input tuple 
     * @param tuple
     * @return
     * @throws Exception 
     */
    public String generateParquetSchema(Tuple tuple) throws Exception { 
    	return generateParquetSchema(tuple.getStreamSchema());
    }
	
	/**
     * Generates parquet schema from an input tuple 
     * @param tuple
     * @return
     * @throws Exception 
     */
    public String generateParquetSchema(OperatorContext context, int portIdx) throws Exception { 
    	return generateParquetSchema(context.getStreamingInputs().get(portIdx).getStreamSchema());
    }
	
    public String SPLCollectionToParquetType(Attribute attr) throws Exception  {
		StringBuffer parquetSchema = new StringBuffer();
		Type.MetaType attrMetaType = attr.getType().getMetaType();	
		
		switch (attrMetaType) {
			// list and set are the same from Parquet schema perspective
			case LIST: 
			case SET:
			{
				parquetSchema.append("\toptional group " + attr.getName() + " (LIST) { \n" );
				parquetSchema.append("\t\trepeated group bag { \n" );
				parquetSchema.append("\t\t\toptional " +  SPLPrimitiveToParquetType(((CollectionType)attr.getType()).getElementType(), "array_element") + ";\n" );
				parquetSchema.append("\t\t} \n" );
				parquetSchema.append("\t} \n" );
				break;
			}
			case MAP: {
				parquetSchema.append("\trepeated group " + attr.getName() + "{ \n" );
				parquetSchema.append("\t\trequired " + SPLPrimitiveToParquetType(((MapType)attr.getType()).getKeyType(), "key") + ";\n" );
				parquetSchema.append("\t\toptional " + SPLPrimitiveToParquetType(((MapType)attr.getType()).getValueType(), "value") + ";\n" );
				parquetSchema.append("} \n" );
				break;
			}
			default: {				
				throw new Exception("Parquet schema generation failure: unsupported type '" + attrMetaType + "'");
			}
		}
		
		return parquetSchema.toString();
	}
	
	public String SPLPrimitiveToParquetType(Type attrType, String attrName) {
		Type.MetaType attrMetaType = attrType.getMetaType();
		
		switch (attrMetaType) {
			case BOOLEAN: {
				return "boolean " + attrName;
			}
			case INT8: 
			case UINT8:
			case INT16:
			case UINT16:
			case INT32:
			case UINT32: {
				return "int32 " + attrName;
			}
			case INT64:
			case UINT64: {
				return "int64 " + attrName;
			}
			case FLOAT32: {
				return "float " + attrName;
			}
			case FLOAT64: {
				return "double " + attrName;
			}
			case RSTRING: {
				return "binary " + attrName + " (UTF8)";
			}
			case USTRING: {
				return "binary " + attrName;
			}
			case TIMESTAMP: {
				return "int96 " + attrName;
			}
			case BLOB: {
				return "binary " + attrName;
			}
			default: {			
				return "binary " + String.valueOf(attrMetaType) + " " + attrName;			
			}
		}
	}

}