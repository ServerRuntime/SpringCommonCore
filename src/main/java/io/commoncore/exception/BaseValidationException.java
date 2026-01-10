package io.commoncore.exception;

public class BaseValidationException extends RuntimeException {

    public BaseValidationException(String message) {
        super(message);
    }

    public BaseValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
