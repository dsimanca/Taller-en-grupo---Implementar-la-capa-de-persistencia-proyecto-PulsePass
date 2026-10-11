package com.pulsepass.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
        return build(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Bad Request", "Validation failed", details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                details.put(violation.getPropertyPath().toString(), violation.getMessage())
        );
        return build(HttpStatus.BAD_REQUEST, "Bad Request", "Validation failed", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", "Malformed JSON request", Map.of());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleInvalidParameter(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", "Invalid request parameter", Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Internal server error", Map.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message, Map<String, String> details) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                details
        );
        return ResponseEntity.status(status).body(body);
    }
}
