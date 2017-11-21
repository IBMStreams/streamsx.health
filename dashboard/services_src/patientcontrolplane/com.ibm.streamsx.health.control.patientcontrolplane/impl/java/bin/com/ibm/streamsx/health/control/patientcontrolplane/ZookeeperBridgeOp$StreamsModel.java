package com.ibm.streamsx.health.control.patientcontrolplane;
import com.ibm.streams.operator.metrics.Metric.Kind;
import com.ibm.streams.operator.model.InputPortSet.WindowMode;
import com.ibm.streams.operator.model.InputPortSet.WindowPunctuationInputMode;
import com.ibm.streams.operator.model.OutputPortSet.WindowPunctuationOutputMode;

@com.ibm.streams.operator.model.PrimitiveOperator(name="ZookeeperBridgeOp", namespace="com.ibm.streamsx.health.control.patientcontrolplane", description="Monitors a Zookeeper node and sends updates to the JCP")
@com.ibm.streams.operator.model.Libraries(value={"opt/downloaded/*"})
@com.ibm.streams.operator.internal.model.ShadowClass("com.ibm.streamsx.health.control.patientcontrolplane.ZookeeperBridgeOp")
@javax.annotation.Generated("com.ibm.streams.operator.internal.model.processors.ShadowClassGenerator")
public class ZookeeperBridgeOp$StreamsModel extends com.ibm.streams.operator.AbstractOperator
 {

@com.ibm.streams.operator.model.Parameter(optional=true)
@com.ibm.streams.operator.internal.model.MethodParameters({"zookeeperConnection"})
public void setZookeeperConnection(java.lang.String zookeeperConnection) {}

@com.ibm.streams.operator.model.Parameter(optional=false)
@com.ibm.streams.operator.internal.model.MethodParameters({"serviceName"})
public void setServiceName(java.lang.String serviceName) {}

@com.ibm.streams.operator.model.Parameter(optional=true)
@com.ibm.streams.operator.internal.model.MethodParameters({"outputTopics"})
public void setOutputTopics(java.lang.String outputTopics) {}

@com.ibm.streams.operator.model.Parameter(optional=false)
@com.ibm.streams.operator.internal.model.MethodParameters({"serviceType"})
public void setServiceType(java.lang.String serviceType) {}
}