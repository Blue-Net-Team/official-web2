package com.bluenet.web.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * DataNotFound 业务异常，映射 HTTP NOT_FOUND。
 */
public class DataNotFound extends GlobalException {

    public DataNotFound(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
