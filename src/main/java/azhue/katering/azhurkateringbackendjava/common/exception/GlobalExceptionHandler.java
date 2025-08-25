package azhue.katering.azhurkateringbackendjava.common.exception;

import azhue.katering.azhurkateringbackendjava.common.dto.ApiResponse;
import azhue.katering.azhurkateringbackendjava.common.exception.general.RateLimitExceededException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений.
 * 
 * <p>Централизованно обрабатывает все исключения приложения,
 * возвращая стандартизированные ответы с кодами ошибок.</p>
 * 
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает ошибки валидации
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            String rejectedValue = ((FieldError) error).getRejectedValue() != null ? 
                    ((FieldError) error).getRejectedValue().toString() : null;
            
            Map<String, Object> fieldError = new HashMap<>();
            fieldError.put("message", errorMessage);
            fieldError.put("rejectedValue", rejectedValue);
            fieldError.put("field", fieldName);
            
            errors.put(fieldName, fieldError);
        });

        log.warn("Validation error: {}", errors);
        
        // Формируем детальное сообщение об ошибке
        String detailedMessage = errors.values().stream()
                .map(error -> {
                    Map<String, Object> errorMap = (Map<String, Object>) error;
                    return (String) errorMap.get("message");
                })
                .findFirst()
                .orElse("Ошибка валидации данных");

        return ResponseEntity.badRequest()
                .body(ApiResponse.<Map<String, Object>>builder()
                        .success(false)
                        .message(detailedMessage)
                        .data(errors)
                        .errorCode("VALIDATION_ERROR")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки базы данных
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex) {
        
        log.error("Data integrity violation: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Data integrity violation")
                        .errorCode("DATA_INTEGRITY_VIOLATION")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки rate limiting
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitExceededException(
            RateLimitExceededException ex) {
        
        log.warn("Rate limit exceeded: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("RATE_LIMIT_EXCEEDED")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки валидации ограничений
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        
        log.warn("Constraint violation: {}", ex.getMessage());
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Constraint violation")
                        .errorCode("CONSTRAINT_VIOLATION")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает все остальные исключения
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex) {
        
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Internal server error")
                        .errorCode("INTERNAL_SERVER_ERROR")
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}