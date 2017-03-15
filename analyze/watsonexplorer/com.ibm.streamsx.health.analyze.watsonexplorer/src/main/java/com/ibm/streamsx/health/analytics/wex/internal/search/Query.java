package com.ibm.streamsx.health.analytics.wex.internal.search;

import java.io.Serializable;

public class Query implements Serializable {
	private static final long serialVersionUID = 1L;
	protected String query;
	protected String collection;
	
	public Query() {
		
	}
	
	public Query(String query, String collection) {
		this.query = query;
		this.collection = collection;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setCollection(String collection) {
		this.collection = collection;
	}
	
	public String getCollection() {
		return collection;
	}

	@Override
	public String toString() {
		return "Query [query=" + query + ", collection=" + collection + "]";
	}	
}
