package com.bluenet.web.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Unauthorized 业务异常，映射 HTTP UNAUTHORIZED。
 */
public class Unauthorized extends GlobalException {

    public Unauthorized(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
