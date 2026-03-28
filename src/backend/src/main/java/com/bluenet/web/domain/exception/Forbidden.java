package com.bluenet.web.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class Forbidden extends GlobalException {
    private final HttpStatus code = HttpStatus.FORBIDDEN;

    public Forbidden(String message) {
        super(message);
    }
}
