package com.ibm.streamsx.objectstorage;

public interface IGovernanceConstants {
	public static final String TAG_OPERATOR_IGC = "OperatorIGC";
	public static final String TAG_REGISTER_TYPE = "registerType";
	public static final String TAG_REGISTER_TYPE_INPUT = "input";
	public static final String TAG_REGISTER_TYPE_OUTPUT = "output";
	
	
	public static final String ASSET_OBJECTSTORAGE_OBJECT_TYPE = "$Streams-OSObject";
	public static final String ASSET_OBJECTSTORAGE_SERVER_TYPE = "$Streams-OSServer";
	public static final String ASSET_OBJECTSTORAGE_SERVER_TYPE_SHORT = "$OSServer";
		
	public static final String PROPERTY_SRC_NAME = "srcName";
	public static final String PROPERTY_SRC_TYPE = "srcType";
	
//	public static final String PROPERTY_SRC_PARENT_PREFIX = "srcParentPrefix";
	public static final String PROPERTY_SRC_PARENT_PREFIX = "srcParent";
	public static final String PROPERTY_PARENT_TYPE = "parentType";
	
	public static final String PROPERTY_PARENT_PREFIX = "p1";
	public static final String PROPERTY_GRANDPARENT_PREFIX = "p2";
	
	public static final String PROPERTY_INPUT_OPERATOR_TYPE = "inputOperatorType";
	public static final String PROPERTY_OUTPUT_OPERATOR_TYPE = "outputOperatorType";
	
}
