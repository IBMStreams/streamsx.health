package com.ibm.streamsx.health.analytics.wex.internal.facetsearch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ParsedFacetSearchResult implements Serializable {

	private static Gson gson = new Gson();
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger("CASearchFacetResult");

	private List<Facet> facets;
	private String patientId;

	public static ParsedFacetSearchResult parseJson(String jsonStr) {
		ParsedFacetSearchResult result = new ParsedFacetSearchResult();
		try {
			JsonObject jsonObj = gson.fromJson(jsonStr, JsonObject.class);
			
			if (!jsonObj.has("es_apiResponse") || jsonObj.get("es_apiResponse").isJsonNull()) {
				logger.error("jsonStr=" + gson.toJson(jsonObj));
				throw new IllegalStateException("Missing \"es_apiResponse\" in json result.");
			}
			
			JsonElement facetElem = jsonObj.getAsJsonObject("es_apiResponse").getAsJsonObject("ibmsc_facet")
					.get("ibmsc_facetValue");
			JsonArray facetArr = null;
			if(facetElem instanceof JsonObject) {
				facetArr = new JsonArray();
				facetArr.add(facetElem);
			} else if(facetElem instanceof JsonArray) {
				facetArr = (JsonArray)facetElem;
			}

			if(facetArr != null) {
				for (int i = 0; i < facetArr.size(); ++i) {
					String facetLabel = facetArr.get(i).getAsJsonObject().get("label").getAsString();
					result.getFacets().add(new Facet(facetLabel));
				}	
			}

		} catch (Exception e) {
			logger.error("jsonStr=" + jsonStr);
			logger.error(e.getLocalizedMessage(), e);
			throw e;
		}

		return result;
	}

	public ParsedFacetSearchResult() {
		facets = new ArrayList<Facet>();
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}

	public String getPatientId() {
		return patientId;
	}

	public List<Facet> getFacets() {
		return facets;
	}

	public void setFacets(List<Facet> facets) {
		this.facets = facets;
	}

	public static class Facet implements Serializable {
		private static final long serialVersionUID = 1L;
		private String label;

		public Facet() {

		}

		public Facet(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
	}
}
