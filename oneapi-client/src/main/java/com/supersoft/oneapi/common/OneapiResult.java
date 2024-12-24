package com.supersoft.oneapi.common;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class OneapiResult implements Serializable {
    boolean success;
    String message;
    Map<String, Object> params;

    public void addParam(String key, Object value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
    }
}
