package com.ibm.streamsx.health.analytics.wex.internal.search;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

public class ParsedSearchResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private String patientId;
	private String searchId;
	private Object queryObj;
	
	@SerializedName("es_apiResponse")
	private ApiResponse apiResponse;

	private static Gson gson;
	static {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(ApiResponse.class, new ApiResponseTypeAdapter());
		gson = builder.create();		
	}
	
	private static Logger logger = Logger.getLogger(ParsedSearchResult.class);

	public Object getQueryObj() {
		return queryObj;
	}
	
	public void setQueryObj(Object queryObj) {
		this.queryObj = queryObj;
	}
	
	public ApiResponse getApiResponse() {
		return apiResponse;
	}

	public String getPatientId() {
		return patientId;
	}
	
	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}
	
	public void setSearchId(String searchId) {
		this.searchId = searchId;
	}
	
	public String getSearchId() {
		return searchId;
	}
	
	public static ParsedSearchResult parseJson(String jsonStr) {
		ParsedSearchResult result = null;
		try {
			result = gson.fromJson(jsonStr, ParsedSearchResult.class); 
		} catch(IllegalStateException e) {
			logger.error("jsonStr=" + jsonStr);
			logger.error(e.getLocalizedMessage(), e);
			throw e;
		}
		
		return result;
	}

	public String toJson() {
		return gson.toJson(this);
	}

	private static class ApiResponseTypeAdapter implements JsonDeserializer<ApiResponse> {

		@Override
		public ApiResponse deserialize(JsonElement elem, Type typeOfT, JsonDeserializationContext ctx)
				throws JsonParseException {
			ApiResponse resp = null;
			if(elem.isJsonObject()) {
				resp = new ApiResponse();
				JsonObject jsonObj = (JsonObject)elem;
				ResultGroups rgs = gson.fromJson(jsonObj.get("es_resultGroups"), ResultGroups.class);
				resp.resultGroups = rgs;

				if(jsonObj.has("es_result")) {
					JsonElement resultsElem = jsonObj.get("es_result");
					if(resultsElem.isJsonArray()) {
						Result[] resultArr = gson.fromJson(resultsElem, Result[].class);
						resp.result = Arrays.asList(resultArr);
					} else if(resultsElem.isJsonObject()) {
						List<Result> results = new ArrayList<ParsedSearchResult.Result>();
						Result r = gson.fromJson(resultsElem, Result.class);
						results.add(r);
						resp.result = results;
					}	
				}
			}
			
			return resp;
		}
		
	}
	
	public static class ApiResponse implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		@SerializedName("es_result")
		private List<Result> result;

		@SerializedName("es_resultGroups")
		private ResultGroups resultGroups;
		
		public ApiResponse() {
			result = new ArrayList<>();
		}
		
		public List<Result> getResult() {
			return result;
		}
		
		public ResultGroups getResultGroups() {
			return resultGroups;
		}
	}

	public static class ResultGroups implements Serializable {

		private static final long serialVersionUID = 1L;

		@SerializedName("es_resultGroup")
		private ResultGroup resultGroup;
		
		public ResultGroup getResultGroup() {
			return resultGroup;
		}
	}

	public static class ResultGroup implements Serializable {

		private static final long serialVersionUID = 1L;

		@SerializedName("id")
		private String id;
		
		@SerializedName("label")
		private String label;
		
		@SerializedName("href")
		private String href;
		
		public String getHref() {
			String href_ = href;
			try {
				href_ = URLDecoder.decode(href, StandardCharsets.UTF_8.name());
			} catch(UnsupportedEncodingException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		
			return href_;
		}
		
		public String getId() {
			return id;
		}
		
		public String getLabel() {
			return label;
		}
	}
	
	public static class Result implements Serializable {
		
		private static final long serialVersionUID = 1L;

		@SerializedName("es_title")
		private String title;
		
		@SerializedName("es_relevance")
		private Double relevance;
		
		@SerializedName("es_id")
		private String id;
		
		@SerializedName("es_link")
		private List<ContentLink> links;

		@SerializedName("es_updated")
		private String date;
		
		@SerializedName("es_summary")
		private String summary;
		
		public Result() {
			links = new ArrayList<>();
		}

		public String getTitle() {
			return title;
		}
		
		public String getDate() {
			return date;
		}
		
		public String getId() {
			return id;
		}
		
		public List<ContentLink> getLinks() {
			return links;
		}
		
		public Double getRelevance() {
			return relevance;
		}
		
		public String getSummary() {
			return summary;
		}
	}
	
	public static class ContentLink implements Serializable {
		
		private static final long serialVersionUID = 1L;

		@SerializedName("rel")
		private String rel;
		
		@SerializedName("href")
		private String href;
		
		public String getHref() {
			return href;
		}
		
		public String getRel() {
			return rel;
		}
	}
}
