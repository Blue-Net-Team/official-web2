package com.bluenet.web.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DataConflict extends GlobalException {
    private final HttpStatus code = HttpStatus.CONFLICT;

    public DataConflict(String message) {
        super(message);
    }
}
