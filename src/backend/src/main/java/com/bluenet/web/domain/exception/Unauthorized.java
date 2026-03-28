package com.bluenet.web.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class Unauthorized extends GlobalException {
    private final HttpStatus code = HttpStatus.UNAUTHORIZED;

    public Unauthorized(String message) {
        super(message);
    }
}
