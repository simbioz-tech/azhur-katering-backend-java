package azhue.katering.azhurkateringbackendjava.auth.exception;

import azhue.katering.azhurkateringbackendjava.auth.exception.account.*;
import azhue.katering.azhurkateringbackendjava.auth.exception.email.EmailException;
import azhue.katering.azhurkateringbackendjava.auth.exception.email.VereficationCodeException;
import azhue.katering.azhurkateringbackendjava.auth.exception.email.VerifiedException;
import azhue.katering.azhurkateringbackendjava.auth.exception.token.*;
import azhue.katering.azhurkateringbackendjava.common.dto.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Обработчик исключений аутентификации и авторизации.
 * 
 * <p>Централизованно обрабатывает все исключения, связанные с аутентификацией,
 * авторизацией, управлением пользователями, токенами и email верификацией.</p>
 *
 * 
 * @version 1.0.0
 */
@RestControllerAdvice
public class AuthExceptionHandler {

    /**
     * Обрабатывает общие ошибки аутентификации.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("AUTH_001")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки неверных учетных данных.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("AUTH_002")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки доступа.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("AUTH_003")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки отсутствия пользователя.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("USER_001")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки неаутентифицированного пользователя.
     */
    @ExceptionHandler(UserNotAuthenticatedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotAuthenticatedException(UserNotAuthenticatedException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("USER_002")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки заблокированного аккаунта.
     */
    @ExceptionHandler(AccountIsLockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountIsLockedException(AccountIsLockedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("USER_003")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки неподтвержденного аккаунта.
     */
    @ExceptionHandler(AccountIsNotVerified.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountIsNotVerifiedException(AccountIsNotVerified ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("USER_004")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки неправильного пароля.
     */
    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<ApiResponse<Void>> handleIncorrectPasswordException(IncorrectPasswordException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("USER_005")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки существующего username.
     */
    @ExceptionHandler(UsernameExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameExistsException(UsernameExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("USER_006")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает общие ошибки JWT токенов.
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtException(JwtException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("TOKEN_001")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки истекшего токена.
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenExpiredException(TokenExpiredException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("TOKEN_002")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки отсутствующего токена.
     */
    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenNotFoundException(TokenNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("TOKEN_003")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки невалидного токена.
     */
    @ExceptionHandler(TokenNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenNotValidException(TokenNotValidException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("TOKEN_004")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки типа токена.
     */
    @ExceptionHandler(TypeTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeTokenException(TypeTokenException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("TOKEN_005")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки email сервиса.
     */
    @ExceptionHandler(EmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailException(EmailException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("EMAIL_001")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки кода верификации.
     */
    @ExceptionHandler(VereficationCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleVerificationCodeException(VereficationCodeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("EMAIL_002")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки уже верифицированного аккаунта.
     */
    @ExceptionHandler(VerifiedException.class)
    public ResponseEntity<ApiResponse<Void>> handleVerifiedException(VerifiedException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("EMAIL_003")
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}
