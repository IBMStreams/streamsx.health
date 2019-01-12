package com.ibm.streamsx.objectstorage.writer.raw;


import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.ibm.streams.operator.Tuple;
import com.ibm.streams.operator.Type.MetaType;
import com.ibm.streams.operator.types.RString;
import com.ibm.streamsx.objectstorage.writer.Constants;

public class SPLConverter {
	
	
	/*
	 * Auxiliary methods
	 */
	public static byte[] SPLPrimitiveToByteArray(Tuple tuple, int attrIndex, MetaType attrType, String encoding) throws UnsupportedEncodingException {
		byte[] tupleBytes = null;
		switch (attrType) {
		case BLOB: 
			ByteBuffer buffer = tuple.getBlob(attrIndex).getByteBuffer();
			tupleBytes = new byte[buffer.limit()];
			buffer.get(tupleBytes);
			break;
		case RSTRING:
			Object attrObj = tuple.getObject(attrIndex);
			if (encoding.equals(Constants.UTF_8))
			{
				tupleBytes = ((RString)attrObj).getData();
			}
			else
			{
				tupleBytes = ((RString)attrObj).getData();
				tupleBytes = new String(tupleBytes, Constants.UTF_8).getBytes(encoding);
			}
			break;
		case USTRING:
			String attrString = tuple.getString(attrIndex);
			tupleBytes = attrString.getBytes(encoding);
			break;		
		default: {
			String attrStrValue = tuple.getObject(attrIndex).toString();
			tupleBytes = attrStrValue.getBytes();
		}
		}
		return tupleBytes;
	}
}
