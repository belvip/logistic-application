package com.example.demo.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
@Hidden
public class MyGlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        return buildErrorResponse("Validation Error", errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = ex.getCause() instanceof InvalidFormatException invalidFormat 
            ? buildInvalidFormatMessage(invalidFormat)
            : "Malformed JSON request. Use valid values and double quotes for field names.";
        return buildErrorResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        return buildResponse(ex.getMessage(), "Resource Not Found", request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(APIException.class)
    public ResponseEntity<ErrorResponse> handleAPIException(APIException ex) {
        return buildErrorResponse(ex.getMessage(), "API Error", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> 
            errors.put(
                violation.getPropertyPath().toString(), 
                violation.getMessage()
            )
        );
        return buildErrorResponse("Constraint Violation", errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        return buildResponse(
            ex.getMessage(), 
            "Internal Server Error", 
            request, 
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private String buildInvalidFormatMessage(InvalidFormatException ex) {
        String targetType = ex.getTargetType().getSimpleName();
        String invalidValue = ex.getValue().toString();
        return String.format(
            "Invalid value '%s' for type %s. Allowed values: e.g., PROCESSING, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED_DELIVERY, RETURNED...",
            invalidValue, 
            targetType
        );
    }

    private ResponseEntity<Object> buildResponse(String message, String errorType, WebRequest request, HttpStatus status) {
        if (isJsonRequest(request)) {
            return new ResponseEntity<>(
                buildErrorResponse(message, errorType, status).getBody(), 
                status
            );
        }
        return new ResponseEntity<>(
            new ErrorDetails(new Date(), message, request.getDescription(false)), 
            status
        );
    }

    private boolean isJsonRequest(WebRequest request) {
        String acceptHeader = request.getHeader("Accept");
        return acceptHeader != null && acceptHeader.contains("application/json");
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status) {
        return buildErrorResponse(message, status.getReasonPhrase(), status);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, String errorType, HttpStatus status) {
        return buildErrorResponse(message, errorType, Map.of("error", message), status);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String errorType, Map<String, String> errors, HttpStatus status) {
        return buildErrorResponse(status.getReasonPhrase(), errorType, errors, status);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
        String message, 
        String errorType, 
        Map<String, String> errors, 
        HttpStatus status
    ) {
        return new ResponseEntity<>(
            new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                errorType,
                errors
            ),
            status
        );
    }
}