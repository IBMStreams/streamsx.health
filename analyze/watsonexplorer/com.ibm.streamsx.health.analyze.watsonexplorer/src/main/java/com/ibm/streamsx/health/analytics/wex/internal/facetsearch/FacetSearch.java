package com.ibm.streamsx.health.analytics.wex.internal.facetsearch;

import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibm.streams.watsonexplorer.RestParameters;
import com.ibm.streams.watsonexplorer.SearchResult;
import com.ibm.streams.watsonexplorer.WEXConnection;
import com.ibm.streams.watsonexplorer.ca.client.CollectionsUtil;
import com.ibm.streams.watsonexplorer.ca.client.ContentAnalytics;
import com.ibm.streamsx.topology.function.Function;
import com.ibm.streamsx.topology.function.Supplier;

public class FacetSearch implements Function<FacetQuery, ParsedFacetSearchResult>{
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger("FacetSearch");
	
	private Supplier<String> host;
	private Supplier<Integer> port;
	private Map<String /* collection name */, String /* collection id */> collectionCache;
	private WEXConnection connection;
	
	public FacetSearch(Supplier<String> host, Supplier<Integer> port) {
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
	public ParsedFacetSearchResult apply(FacetQuery query) {
		ParsedFacetSearchResult result = null;
		
		try {
			result = executeQuery(query);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	private ParsedFacetSearchResult executeQuery(FacetQuery query) throws Exception {
		ContentAnalytics ca = new ContentAnalytics(connection);

		RestParameters params = new RestParameters();
		params.put("query", query.getQuery());
		params.put("facet", query.getFacet());
		params.put("collection", getCollectionId(query.getCollection()));
		params.put("output", "application/json");

		SearchResult searchResult = ca.searchFacet(params);
		ParsedFacetSearchResult parsedResult = ParsedFacetSearchResult.parseJson(searchResult.getContent());

		return parsedResult;
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
