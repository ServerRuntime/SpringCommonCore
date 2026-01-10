package io.commoncore.exception;

public class BaseNotFoundException extends RuntimeException {

    public BaseNotFoundException(String message) {
        super(message);
    }

    public BaseNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
