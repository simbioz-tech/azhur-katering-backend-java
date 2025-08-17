package azhue.katering.azhurkateringbackendjava.common.exception;

import azhue.katering.azhurkateringbackendjava.common.dto.ApiResponse;
import azhue.katering.azhurkateringbackendjava.common.exception.account.*;
import azhue.katering.azhurkateringbackendjava.common.exception.email.EmailException;
import azhue.katering.azhurkateringbackendjava.common.exception.email.VereficationCodeException;
import azhue.katering.azhurkateringbackendjava.common.exception.email.VerifiedException;
import azhue.katering.azhurkateringbackendjava.common.exception.general.RateLimitExceededException;
import azhue.katering.azhurkateringbackendjava.common.exception.token.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

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
            MethodArgumentNotValidException ex, WebRequest request) {
        
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
     * Обрабатывает ошибки аутентификации
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            Exception ex, WebRequest request) {
        
        log.warn("Authentication error: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("AUTHENTICATION_ERROR")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки авторизации
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        log.warn("Access denied: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("ACCESS_DENIED")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки JWT
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtException(
            JwtException ex, WebRequest request) {
        
        log.warn("JWT error: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("JWT_ERROR")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки пользователя
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {
        
        log.warn("User not found: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("USER_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки неаутентифицированного пользователя
     */
    @ExceptionHandler(UserNotAuthenticatedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotAuthenticatedException(
            UserNotAuthenticatedException ex, WebRequest request) {
        
        log.warn("User not authenticated: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("USER_NOT_AUTHENTICATED")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки заблокированного аккаунта
     */
    @ExceptionHandler(AccountIsLockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountIsLockedException(
            AccountIsLockedException ex, WebRequest request) {
        
        log.warn("Account locked: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("ACCOUNT_LOCKED")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки неподтвержденного аккаунта
     */
    @ExceptionHandler(AccountIsNotVerified.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountIsNotVerifiedException(
            AccountIsNotVerified ex, WebRequest request) {
        
        log.warn("Account not verified: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("ACCOUNT_NOT_VERIFIED")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки неправильного пароля
     */
    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<ApiResponse<Void>> handleIncorrectPasswordException(
            IncorrectPasswordException ex, WebRequest request) {
        
        log.warn("Incorrect password: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("INCORRECT_PASSWORD")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки email
     */
    @ExceptionHandler(EmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailException(
            EmailException ex, WebRequest request) {
        
        log.error("Email error: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Email service error")
                        .errorCode("EMAIL_SERVICE_ERROR")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки кода верификации
     */
    @ExceptionHandler(VereficationCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleVereficationCodeException(
            VereficationCodeException ex, WebRequest request) {
        
        log.warn("Verification code error: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("VERIFICATION_CODE_ERROR")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки уже верифицированного аккаунта
     */
    @ExceptionHandler(VerifiedException.class)
    public ResponseEntity<ApiResponse<Void>> handleVerifiedException(
            VerifiedException ex, WebRequest request) {
        
        log.warn("Account already verified: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("ACCOUNT_ALREADY_VERIFIED")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки истекшего токена
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenExpiredException(
            TokenExpiredException ex, WebRequest request) {
        
        log.warn("Token expired: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("TOKEN_EXPIRED")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки отсутствующего токена
     */
    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenNotFoundException(
            TokenNotFoundException ex, WebRequest request) {
        
        log.warn("Token not found: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("TOKEN_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки невалидного токена
     */
    @ExceptionHandler(TokenNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenNotValidException(
            TokenNotValidException ex, WebRequest request) {
        
        log.warn("Token not valid: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("TOKEN_NOT_VALID")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки типа токена
     */
    @ExceptionHandler(TypeTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeTokenException(
            TypeTokenException ex, WebRequest request) {
        
        log.warn("Token type error: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("TOKEN_TYPE_ERROR")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки базы данных
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {
        
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
            RateLimitExceededException ex, WebRequest request) {
        
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
            ConstraintViolationException ex, WebRequest request) {
        
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
            Exception ex, WebRequest request) {
        
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