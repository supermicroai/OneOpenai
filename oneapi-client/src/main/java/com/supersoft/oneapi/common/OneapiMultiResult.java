package com.supersoft.oneapi.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class OneapiMultiResult<T> extends OneapiResult {
    List<T> data;
    int total;

    public static <T> OneapiMultiResult<T> success(List<T> data) {
        OneapiMultiResult multiResult = new OneapiMultiResult();
        multiResult.data = data;
        multiResult.total = data == null ? 0 : data.size();
        multiResult.success = true;
        return multiResult;
    }

    public static <T> OneapiMultiResult<T> success(List<T> data, int total) {
        OneapiMultiResult multiResult = new OneapiMultiResult();
        multiResult.data = data;
        multiResult.total = total;
        multiResult.success = true;
        return multiResult;
    }

    public static <T> OneapiMultiResult<T> fail(String message) {
        OneapiMultiResult multiResult = new OneapiMultiResult();
        multiResult.message = message;
        multiResult.success = false;
        return multiResult;
    }
}
