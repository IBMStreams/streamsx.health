package com.ibm.streamsx.health.control.patientcontrolplane.operator;
import com.ibm.streams.operator.metrics.Metric.Kind;
import com.ibm.streams.operator.model.InputPortSet.WindowMode;
import com.ibm.streams.operator.model.InputPortSet.WindowPunctuationInputMode;
import com.ibm.streams.operator.model.OutputPortSet.WindowPunctuationOutputMode;

@com.ibm.streams.operator.model.PrimitiveOperator(name="BridgeOp", namespace="com.ibm.streamsx.health.control.patientcontrolplane", description="Provides a bridge between the JCP and a backend server (i.e. Redis)")
@com.ibm.streams.operator.model.Libraries(value={"opt/downloaded/*"})
@com.ibm.streams.operator.internal.model.ShadowClass("com.ibm.streamsx.health.control.patientcontrolplane.operator.BridgeOp")
@javax.annotation.Generated("com.ibm.streams.operator.internal.model.processors.ShadowClassGenerator")
public class BridgeOp$StreamsModel extends com.ibm.streams.operator.AbstractOperator
 {

@com.ibm.streams.operator.model.Parameter(optional=false)
@com.ibm.streams.operator.internal.model.MethodParameters({"serviceType"})
public void setServiceType(java.lang.String serviceType) {}

@com.ibm.streams.operator.model.Parameter(optional=true)
@com.ibm.streams.operator.internal.model.MethodParameters({"connection"})
public void setConnection(java.lang.String connection) {}

@com.ibm.streams.operator.model.Parameter(optional=false)
@com.ibm.streams.operator.internal.model.MethodParameters({"serviceName"})
public void setServiceName(java.lang.String serviceName) {}

@com.ibm.streams.operator.model.Parameter(optional=true)
@com.ibm.streams.operator.internal.model.MethodParameters({"outputTopics"})
public void setOutputTopics(java.lang.String outputTopics) {}

@com.ibm.streams.operator.model.Parameter(optional=false)
@com.ibm.streams.operator.internal.model.MethodParameters({"appConfigName"})
public void setAppConfigName(java.lang.String appConfigName) {}
}