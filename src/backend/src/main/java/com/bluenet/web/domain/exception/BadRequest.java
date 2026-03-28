package com.bluenet.web.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BadRequest extends GlobalException {
    private final HttpStatus code = HttpStatus.BAD_REQUEST;
    public BadRequest(String message) {
        super(message);
    }
}
