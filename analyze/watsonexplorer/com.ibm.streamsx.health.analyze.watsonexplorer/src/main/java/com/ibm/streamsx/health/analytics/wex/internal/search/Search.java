package com.ibm.streamsx.health.analytics.wex.internal.search;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibm.streams.watsonexplorer.RestParameters;
import com.ibm.streams.watsonexplorer.SearchResult;
import com.ibm.streams.watsonexplorer.WEXConnection;
import com.ibm.streams.watsonexplorer.ca.client.CollectionsUtil;
import com.ibm.streams.watsonexplorer.ca.client.ContentAnalytics;
import com.ibm.streamsx.topology.function.Function;
import com.ibm.streamsx.topology.function.Supplier;

public class Search implements Function<Query, Iterable<ParsedSearchResult>> {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger("Search");
	
	private Supplier<String> host;
	private Supplier<Integer> port;
	private Map<String /* collection name */, String /* collection id */> collectionCache;
	private WEXConnection connection;
	
	public Search(Supplier<String> host, Supplier<Integer> port) {
		super();
		this.host = host;
		this.port = port;
		collectionCache = new HashMap<String, String>();
	}
	
	public Object readResolve() throws ObjectStreamException {
		connection = new WEXConnection(host.get(), port.get(), null);
		return this;
	}
	
	@Override
	public Iterable<ParsedSearchResult> apply(Query query) {
		List<ParsedSearchResult> results = new ArrayList<ParsedSearchResult>();
		
		try {
			results = executeQuery(query);
			logger.trace("Total results: " + results.size());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return results;
	}

	private List<ParsedSearchResult> executeQuery(Query query) throws Exception {
		List<ParsedSearchResult> results = new ArrayList<ParsedSearchResult>();
		ContentAnalytics ca = new ContentAnalytics(connection);

		RestParameters params = new RestParameters();
		params.put("query", query.getQuery());
		params.put("collection", getCollectionId(query.getCollection()));
		params.put("output", "application/json");
		params.put("sortKey", "date");
		params.put("sortOrder", "asc");

		boolean hasMore = true;
		while (hasMore) {
			SearchResult searchResult = ca.search(params);
			hasMore = searchResult.hasMore();

			ParsedSearchResult parsedResult = ParsedSearchResult.parseJson(searchResult.getContent());
			results.add(parsedResult);
		}

		return results;
	}

	private String getCollectionId(String collectionName) throws Exception {
		if (collectionCache.containsKey(collectionName)) {
			return collectionCache.get(collectionName);
		} else {
			String collectionId = CollectionsUtil.getCollectionId(connection, collectionName);
			collectionCache.put(collectionName, collectionId);
			return collectionId;
		}
	}
	
}
