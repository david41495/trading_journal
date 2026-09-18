package com.davidserrano.tradejournal.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TradeNotFoundException.class)
    public ResponseEntity<ApiError> handleTradeNotFound(
            TradeNotFoundException exception,
            HttpServletRequest request) {

        ApiError error = createError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError -> fieldErrors.putIfAbsent(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()));

        ApiError error = createError(
                HttpStatus.BAD_REQUEST,
                "One or more fields contain invalid information.",
                request.getRequestURI(),
                fieldErrors);

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        ApiError error = createError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                Map.of());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {

        String message = "Invalid value '" + exception.getValue()
                + "' for parameter '" + exception.getName() + "'.";

        ApiError error = createError(
                HttpStatus.BAD_REQUEST,
                message,
                request.getRequestURI(),
                Map.of());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        ApiError error = createError(
                HttpStatus.BAD_REQUEST,
                "The request contains malformed JSON or an invalid value.",
                request.getRequestURI(),
                Map.of());

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedError(
            Exception exception,
            HttpServletRequest request) {

        ApiError error = createError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred.",
                request.getRequestURI(),
                Map.of());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    private ApiError createError(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> validationErrors) {

        return new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                validationErrors);
    }
}