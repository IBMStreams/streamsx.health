/* begin_generated_IBM_copyright_prolog                                       */
/*                                                                            */
/* This is an automatically generated copyright prolog.                       */
/* After initializing,  DO NOT MODIFY OR MOVE                                 */
/******************************************************************************/
/* Copyright (C) 2016 International Business Machines Corporation             */
/* All Rights Reserved                                                        */
/******************************************************************************/
/* end_generated_IBM_copyright_prolog                                         */
package com.ibm.streamsx.health.ingest.types.connector;

import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.topology.TStream;

public class PublishConnector {

	public static void publishObservation(TStream<Observation> inputStream, String publishTopic) {
		mapAndPublish(inputStream, new IdentityMapper(), publishTopic);
	}
	
	private static <T> void mapAndPublish(TStream<T> inputStream, AbstractObservationMapper<T> mapper, String publishTopic) {
		TStream<String> jsonStream = inputStream.multiTransform(mapper).transform(new ModelToJsonConverter<Observation>());
		JsonPublisher.publish(jsonStream, publishTopic);
	}

}
