package com.ibm.streamsx.health.analytics.wex.internal.facetsearch;

import com.ibm.streamsx.health.analytics.wex.internal.search.Query;

public class FacetQuery extends Query {
	private static final long serialVersionUID = 1L;
	private String facet;
	
	public FacetQuery() {
		
	}
	
	public FacetQuery(String query, String collection, String facet) {
		super(query, collection);
		this.facet = facet;
	}
	
	public String getFacet() {
		return facet;
	}
	
	public void setFacet(String facet) {
		this.facet = facet;
	}

	@Override
	public String toString() {
		return "FacetQuery [facet=" + facet + ", query=" + query + ", collection=" + collection + "]";
	}

	
}
