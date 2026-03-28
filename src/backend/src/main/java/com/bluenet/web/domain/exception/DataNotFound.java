package com.bluenet.web.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DataNotFound extends GlobalException {
    private final HttpStatus code = HttpStatus.NOT_FOUND;

    public DataNotFound(String message) {
        super(message);
    }
}
