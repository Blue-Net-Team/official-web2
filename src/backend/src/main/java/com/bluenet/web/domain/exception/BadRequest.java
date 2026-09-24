package com.bluenet.web.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * BadRequest 业务异常，映射 HTTP BAD_REQUEST。
 */
public class BadRequest extends GlobalException {

    public BadRequest(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
