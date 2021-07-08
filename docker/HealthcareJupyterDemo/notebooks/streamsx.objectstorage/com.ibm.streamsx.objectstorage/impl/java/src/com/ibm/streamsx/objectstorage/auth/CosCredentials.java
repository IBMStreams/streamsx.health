package com.ibm.streamsx.objectstorage.auth;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class CosCredentials implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("apikey")
    private String apiKey;

    @SerializedName("resource_instance_id")
    private String resourceInstanceId;

    @SerializedName("endpoints")
    private String endpoints; 
    
    private CosCredentials() {
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getResourceInstanceId() {
        return resourceInstanceId;
    }

    public String getEndpoints() {
        return endpoints;
    }
    
}
