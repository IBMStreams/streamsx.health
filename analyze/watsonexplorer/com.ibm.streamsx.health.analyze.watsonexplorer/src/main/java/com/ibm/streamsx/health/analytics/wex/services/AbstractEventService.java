package com.ibm.streamsx.health.analytics.wex.services;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.streams.operator.logging.TraceLevel;
import com.ibm.streamsx.health.analytics.wex.internal.JsonPublisher;
import com.ibm.streamsx.health.analytics.wex.internal.JsonSubscriber;
import com.ibm.streamsx.health.analytics.wex.internal.search.ParsedSearchResult;
import com.ibm.streamsx.health.analytics.wex.internal.search.Query;
import com.ibm.streamsx.health.analytics.wex.internal.search.Search;
import com.ibm.streamsx.health.ingest.types.model.ClinicalNoteEvent;
import com.ibm.streamsx.health.ingest.types.model.Observation;
import com.ibm.streamsx.topology.TStream;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.context.StreamsContext.Type;
import com.ibm.streamsx.topology.function.Function;
import com.ibm.streamsx.topology.function.Supplier;

public abstract class AbstractEventService extends AbstractWEXHealthService {

	private static final long serialVersionUID = 1L;

	public AbstractEventService(String serviceName, String wexToolkitPath) throws Exception {
		super(serviceName, wexToolkitPath);
	}

	@Override
	public void build() throws Exception {
		super.build();
		Topology topo = getTopology();
		topo.addClassDependency(Observation.class);

		TStream<Query> src = JsonSubscriber.subscribe(topo, getSubscriptionTopic())
				.transform(new QueryFormatter(getWexPatientFieldName(), getCollectionName()));

		TStream<ParsedSearchResult> resultStream = src.multiTransform(new Search(getWexHostParam(), getWexPortParam()));

		TStream<String> eventStream = resultStream.multiTransform(getEventSearch()).transform(new TupleToOutputJson());

		JsonPublisher.publish(eventStream, getPublishedTopic());
	}

	abstract Function<ParsedSearchResult, Iterable<ClinicalNoteEvent>> getEventSearch();

	private static class TupleToOutputJson implements Function<ClinicalNoteEvent, String> {
		private static final long serialVersionUID = 1L;
		private static Gson gson = new Gson();

		@Override
		public String apply(ClinicalNoteEvent event) {
			return gson.toJson(event);
		}
	}

	private static class QueryFormatter implements Function<String, Query> {

		private static final long serialVersionUID = 1L;
		private Gson gson;
		private Supplier<String> collectionName;
		private Supplier<String> patientIdFieldName;

		public QueryFormatter(Supplier<String> patientIdFieldName, Supplier<String> collectionName) {
			this.patientIdFieldName = patientIdFieldName;
			this.collectionName = collectionName;
		}

		public Object readResolve() {
			gson = new Gson();
			return this;
		}

		@Override
		public Query apply(String jsonStr) {
			JsonObject jsonObj = gson.fromJson(jsonStr, JsonObject.class);

			String symptomQueryTemplate = "(*:*) AND (keyword::/\"%s\"/\"%s\")";

			if (jsonObj.has("query")) {
				String q = jsonObj.get("query").getAsString();
				if (q != null && !q.isEmpty()) {
					symptomQueryTemplate += " AND " + q;
				}
			}

			String startDate = jsonObj.has("startDate") ? jsonObj.get("startDate").getAsString() : null;
			String endDate = jsonObj.has("endDate") ? jsonObj.get("endDate").getAsString() : null;

			if (startDate != null || endDate != null) {
				String datePredicates = getDatePredicates(startDate, endDate);
				symptomQueryTemplate += datePredicates;
			}

			String query = String.format(symptomQueryTemplate, patientIdFieldName.get(),
					jsonObj.get("patientId").getAsString());
			return new Query(query, collectionName.get());
		}

		private String getDatePredicates(String startDate, String endDate) {
			String predicates = "";
			if (startDate != null && !startDate.isEmpty()) {
				predicates += " AND (date >= " + startDate + ")";
			}

			if (endDate != null && !endDate.isEmpty()) {
				predicates += " AND (date < " + endDate + ")";
			}

			return predicates;
		}
	}

	protected static void launchService(String[] args, Class<? extends AbstractEventService> eventServiceClass) throws Exception {
		Option hostOption = Option.builder("x").longOpt("host").hasArg().argName("watson explorer host").required()
				.build();

		Option portOption = Option.builder("p").longOpt("port").hasArg().argName("watson explorer port").required()
				.build();

		Option collectionOption = Option.builder("c").longOpt("collection").hasArg().argName("collection name")
				.required().build();

		Option patientIdFieldOption = Option.builder("f").longOpt("patient-field").hasArg()
				.argName("patient ID field name").required(false).build();

		Option toolkitOption = Option.builder("t").longOpt("toolkit-path").hasArg()
				.argName("watson explorer toolkit path").required().build();

		Option subscribeOption = Option.builder("s").longOpt("subscription-topic").hasArg()
				.argName("subscription topic").required().build();

		Option debugOption = Option.builder("d").longOpt("debug").hasArg().argName("isDebugEnabled").required(false)
				.type(Boolean.class).build();

		Options options = new Options();
		options.addOption(hostOption);
		options.addOption(portOption);
		options.addOption(collectionOption);
		options.addOption(patientIdFieldOption);
		options.addOption(toolkitOption);
		options.addOption(subscribeOption);
		options.addOption(debugOption);

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("help", options);

			throw (e);
		}

		boolean isDebug = cmd.getOptionValue("d", "false").equals("true");

		AbstractEventService svc = eventServiceClass.getConstructor(String.class).newInstance(cmd.getOptionValue("t"));
		//MedicationEventService svc = new MedicationEventService(cmd.getOptionValue("t"));
		svc.setContextType(Type.DISTRIBUTED);
		svc.addSubmissionTimeParam("wex.host", cmd.getOptionValue("x"));
		svc.addSubmissionTimeParam("wex.port", Integer.valueOf(cmd.getOptionValue("p")));
		svc.addSubmissionTimeParam("wex.patient.field.name", cmd.getOptionValue("f", "patient_id"));
		svc.addSubmissionTimeParam("collectionName", cmd.getOptionValue("c"));
		svc.setSubscriptionTopic(cmd.getOptionValue("s"));

		if (isDebug) {
			svc.setTraceLevel(TraceLevel.TRACE);
		}
		svc.buildAndRun();

		if (isDebug) {
			new EventBeacon(svc.getPublishedTopic()).run();
		}
	}
}
