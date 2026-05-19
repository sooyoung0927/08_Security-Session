package com.ohgiraffers.security.exception;

public class InvalidJwtCustomException extends RuntimeException {
    public InvalidJwtCustomException(String message) {
        super(message);
    }

    public InvalidJwtCustomException(String message, Throwable cause) {
        super(message, cause);
    }
}
