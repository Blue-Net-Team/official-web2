package com.bluenet.web.domain.exception;

import io.github.ivencn.infra.core.exception.BizException;

import org.springframework.http.HttpStatus;

/**
 * 应用业务异常基类，继承框架 {@link BizException} 以复用统一异常处理。 默认映射 HTTP 500。
 */
public class GlobalException extends BizException {

    public GlobalException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    protected GlobalException(HttpStatus code, String message) {
        super(code, message);
    }

    protected GlobalException(HttpStatus code, String message, Object data) {
        super(code, message, data);
    }
}
