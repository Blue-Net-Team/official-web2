package com.bluenet.web.infrastructure.email;

import com.bluenet.web.domain.exception.GlobalException;
import org.springframework.http.HttpStatus;

public class EmailSendException extends GlobalException {

    @Override
    public HttpStatus getCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public EmailSendException(String message) {
        super(message);
    }

    public EmailSendException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
