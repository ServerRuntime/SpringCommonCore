package io.commoncore.advice;

import io.commoncore.dto.CustomResponse;
import io.commoncore.exception.BaseNotFoundException;
import io.commoncore.exception.BaseValidationException;
import io.commoncore.exception.RateLimitExceededException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseNotFoundException.class)
    public ResponseEntity<CustomResponse<Map<String, Object>>> handleBaseNotFoundException(BaseNotFoundException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("error", "Not Found");
        errorDetails.put("message", ex.getMessage());
        
        CustomResponse<Map<String, Object>> response = CustomResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        response.setData(errorDetails);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BaseValidationException.class)
    public ResponseEntity<CustomResponse<Map<String, Object>>> handleBaseValidationException(BaseValidationException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("error", "Validation Error");
        errorDetails.put("message", ex.getMessage());
        
        CustomResponse<Map<String, Object>> response = CustomResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        response.setData(errorDetails);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<CustomResponse<Map<String, Object>>> handleRateLimitExceededException(RateLimitExceededException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("error", "Too Many Requests");
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("retryAfterSeconds", ex.getRetryAfterSeconds());
        
        CustomResponse<Map<String, Object>> response = CustomResponse.error(HttpStatus.TOO_MANY_REQUESTS.value(), ex.getMessage());
        response.setData(errorDetails);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomResponse<Map<String, Object>>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("error", "Bad Request");
        errorDetails.put("message", ex.getMessage());
        
        CustomResponse<Map<String, Object>> response = CustomResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        response.setData(errorDetails);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomResponse<Map<String, Object>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("error", "Validation Failed");
        errorDetails.put("message", "Request validation failed");
        errorDetails.put("fieldErrors", fieldErrors);
        
        CustomResponse<Map<String, Object>> response = CustomResponse.error(
                HttpStatus.BAD_REQUEST.value(), 
                "Validation failed"
        );
        response.setData(errorDetails);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomResponse<Map<String, Object>>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("error", "Validation Failed");
        errorDetails.put("message", "Constraint validation failed");
        errorDetails.put("fieldErrors", errors);
        
        CustomResponse<Map<String, Object>> response = CustomResponse.error(
                HttpStatus.BAD_REQUEST.value(), 
                "Validation failed"
        );
        response.setData(errorDetails);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponse<Map<String, Object>>> handleGenericException(Exception ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("error", "Internal Server Error");
        errorDetails.put("message", "An unexpected error occurred: " + ex.getMessage());
        
        CustomResponse<Map<String, Object>> response = CustomResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                "An unexpected error occurred: " + ex.getMessage()
        );
        response.setData(errorDetails);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
