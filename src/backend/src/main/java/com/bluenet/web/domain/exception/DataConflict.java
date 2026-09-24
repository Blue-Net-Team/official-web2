package com.bluenet.web.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * 数据冲突异常，映射 HTTP 409，可携带冲突详情数据。
 */
public class DataConflict extends GlobalException {

    public DataConflict(String message) {
        this(message, null);
    }

    public DataConflict(String message, Object data) {
        super(HttpStatus.CONFLICT, message, data);
    }
}
