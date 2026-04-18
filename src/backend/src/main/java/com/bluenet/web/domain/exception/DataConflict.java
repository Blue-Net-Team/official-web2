package com.bluenet.web.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DataConflict extends GlobalException {
    private final HttpStatus code = HttpStatus.CONFLICT;
    private final Object data;

    public DataConflict(String message) {
        super(message);
        this.data = null;
    }

    public DataConflict(String message, Object data) {
        super(message);
        this.data = data;
    }
}
