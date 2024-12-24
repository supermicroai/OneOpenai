package com.supersoft.oneapi.common.exception;

/**
 * 余额不足异常
 */
public class OneapiOutOfCreditException extends RuntimeException {

    public OneapiOutOfCreditException(String message) {
        super(message);
    }
}
