package com.supersoft.oneapi.common;

import lombok.Data;

@Data
public class OneapiSingleResult<T> extends OneapiResult {
    T data;

    public static <T> OneapiSingleResult<T> success() {
        OneapiSingleResult singleResult = new OneapiSingleResult();
        singleResult.success = true;
        return singleResult;
    }

    public static <T> OneapiSingleResult<T> success(T data) {
        OneapiSingleResult singleResult = new OneapiSingleResult();
        singleResult.data = data;
        singleResult.success = true;
        return singleResult;
    }

    public static <T> OneapiSingleResult<T> fail(String message) {
        OneapiSingleResult singleResult = new OneapiSingleResult();
        singleResult.message = message;
        singleResult.success = false;
        return singleResult;
    }
}
