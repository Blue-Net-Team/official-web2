package com.bluenet.web.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Forbidden 业务异常，映射 HTTP FORBIDDEN。
 */
public class Forbidden extends GlobalException {

    public Forbidden(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
