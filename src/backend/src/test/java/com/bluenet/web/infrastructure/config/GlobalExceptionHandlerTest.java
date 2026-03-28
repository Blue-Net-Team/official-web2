package com.bluenet.web.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * GlobalExceptionHandler 测试。
 */
class GlobalExceptionHandlerTest {

    /**
     * 验证绑定异常返回 400。
     */
    @Test
    void bindExceptionMapsToBadRequest() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        var response = handler
                .handleBindException(new org.springframework.validation.BindException(new Object(), "obj"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getCode());
    }

    /**
     * 验证系统异常返回 500。
     */
    @Test
    void unhandledExceptionMapsToSystemError() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        var response = handler.handleException(new RuntimeException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getCode());
    }
}
