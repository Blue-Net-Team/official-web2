package com.bluenet.web.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GlobalException extends RuntimeException {
    private final HttpStatus code = HttpStatus.INTERNAL_SERVER_ERROR;

    public GlobalException(String message) {
        super(message);
    }
}
