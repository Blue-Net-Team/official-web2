package com.bluenet.web.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TooManyRequests extends GlobalException {
    private final HttpStatus code = HttpStatus.TOO_MANY_REQUESTS;

    public TooManyRequests(String message) {
        super(message);
    }
}
