package com.bluenet.web.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * TooManyRequests 业务异常，映射 HTTP TOO_MANY_REQUESTS。
 */
public class TooManyRequests extends GlobalException {

    public TooManyRequests(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
    }
}
