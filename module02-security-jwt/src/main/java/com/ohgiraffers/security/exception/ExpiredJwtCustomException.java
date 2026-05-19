package com.ohgiraffers.security.exception;

import io.jsonwebtoken.JwtException;

public class ExpiredJwtCustomException extends JwtException {

    public ExpiredJwtCustomException(String message) {
        super(message);
    }
}
