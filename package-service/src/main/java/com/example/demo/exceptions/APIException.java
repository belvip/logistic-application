package com.example.demo.exceptions;

public class APIException extends RuntimeException {
    // To verify if category name exists
    private static final long serialVersionUID = 1L;

    public APIException() {
    }

    public APIException(String message) {
        super(message);
    }
}