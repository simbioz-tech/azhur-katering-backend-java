package azhue.katering.azhurkateringbackendjava.auth.controller;

import azhue.katering.azhurkateringbackendjava.auth.model.dto.request.ChangePasswordRequest;
import azhue.katering.azhurkateringbackendjava.auth.model.dto.request.LoginRequest;
import azhue.katering.azhurkateringbackendjava.auth.model.dto.request.RegisterRequest;
import azhue.katering.azhurkateringbackendjava.auth.model.dto.response.AuthResponse;
import azhue.katering.azhurkateringbackendjava.auth.service.contract.AuthService;
import azhue.katering.azhurkateringbackendjava.auth.service.contract.CurrentUserService;
import azhue.katering.azhurkateringbackendjava.auth.service.contract.EmailService;
import azhue.katering.azhurkateringbackendjava.common.service.contract.CookieService;
import azhue.katering.azhurkateringbackendjava.common.util.contract.HttpUtils;
import azhue.katering.azhurkateringbackendjava.common.annotation.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import azhue.katering.azhurkateringbackendjava.common.exception.token.TokenNotFoundException;

/**
 * REST контроллер для управления аутентификацией и авторизацией пользователей.
 * 
 * <p>Этот контроллер предоставляет эндпоинты для:</p>
 * <ul>
 *   <li>Регистрации новых пользователей</li>
 *   <li>Входа в систему</li>
 *   <li>Обновления токенов доступа</li>
 *   <li>Выхода из системы</li>
 *   <li>Смены пароля</li>
 *   <li>Отправки кода верификации email</li>
 *   <li>Подтверждения email адреса</li>
 * </ul>
 * 
 * <p>Все эндпоинты защищены rate limiting для предотвращения злоупотреблений.
 * Токены доступа и обновления передаются через HTTP-only cookies для
 * обеспечения безопасности.</p>
 * 
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "API для аутентификации и авторизации пользователей")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "https://your-frontend-domain.com"}, 
             allowCredentials = "true")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final CurrentUserService currentUserService;
    private final HttpUtils httpUtils;
    private final CookieService cookieService;

    /**
     * Регистрирует нового пользователя в системе.
     * 
     * <p>Создает нового пользователя с указанными данными и отправляет
     * код подтверждения на указанный email адрес. Пользователь не сможет войти
     * в систему до подтверждения email адреса.</p>
     * 
     * <p>Метод защищен rate limiting для предотвращения массовой регистрации
     * фейковых аккаунтов.</p>
     */
    @PostMapping("/register")
    @RateLimit(value = "authRateLimiter",
                message = "Слишком много попыток регистрации, попробуйте позже!")
    @Operation(
        summary = "Регистрация пользователя",
        description = "Регистрирует нового пользователя и отправляет код подтверждения на email. " +
                     "Пользователь не сможет войти в систему до подтверждения email адреса.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для регистрации пользователя",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RegisterRequest.class),
                examples = @ExampleObject(
                    name = "Пример регистрации",
                    value = """
                    {
                        "username": "ivan_petrov",
                        "email": "ivan@example.com",
                        "password": "Pass123!"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Пользователь успешно зарегистрирован",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(
                    value = "Код для подтверждения отправлен на ivan@example.com"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации данных или пользователь уже существует",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "success": false,
                        "message": "Пользователь с таким email уже существует",
                        "errorCode": "VALIDATION_ERROR"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Превышен лимит запросов"
        )
    })
    public String register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        log.info("Попытка регистрации пользователя: {}", request.getEmail());
        
        HttpUtils.RequestInfo requestInfo = httpUtils.getRequestInfo(httpRequest);
        authService.register(request, requestInfo.ipAddress(), requestInfo.userAgent());
        
        return String.format("Код для подтверждения отправлен на %s", request.getEmail());
    }

    /**
     * Аутентифицирует пользователя и создает сессию.
     * 
     * <p>Проверяет учетные данные пользователя и при успешной аутентификации
     * создает JWT токены доступа и обновления. Токены сохраняются в HTTP-only
     * cookies для безопасности.</p>
     * 
     * <p>Метод защищен rate limiting для предотвращения брутфорс атак.</p>
     */
    @PostMapping("/login")
    @RateLimit(value = "authRateLimiter",
                message = "Слишком много попыток входа, попробуйте позже!")
    @Operation(
        summary = "Вход пользователя",
        description = "Аутентифицирует пользователя и возвращает JWT токены. " +
                     "Токены автоматически сохраняются в HTTP-only cookies для безопасности.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Учетные данные пользователя",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginRequest.class),
                examples = @ExampleObject(
                    name = "Пример входа",
                    value = """
                    {
                        "email": "ivan@example.com",
                        "password": "Pass123!"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Успешная аутентификация",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "userId": "123e4567-e89b-12d3-a456-426614174000",
                        "email": "ivan@example.com",
                        "username": "ivan_petrov",
                        "role": "USER",
                        "isVerified": true,
                        "tokenType": "Bearer",
                        "expiresIn": 900
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Неверные учетные данные или ошибка валидации"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Email не подтвержден или аккаунт заблокирован"
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Превышен лимит запросов"
        )
    })
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request, 
            HttpServletRequest httpRequest, 
            HttpServletResponse response) {
        log.info("Попытка входа пользователя: {}", request.getEmail());
        
        HttpUtils.RequestInfo requestInfo = httpUtils.getRequestInfo(httpRequest);
        AuthResponse authResponse = authService.login(request, requestInfo.ipAddress(), requestInfo.userAgent());
        
        // Если требуется верификация, не устанавливаем токены
        if (authResponse.getRequiresVerification() != null && authResponse.getRequiresVerification()) {
            log.info("Пользователь {} требует верификации email. Возвращаем ответ с requiresVerification=true", request.getEmail());
            return authResponse;
        }
        
        log.info("Успешный вход пользователя: {}. Устанавливаем токены в cookies", request.getEmail());
        
        // Устанавливаем токены в cookies только для успешного входа
        cookieService.setAccessTokenCookie(response, authResponse.getAccessToken());
        cookieService.setRefreshTokenCookie(response, authResponse.getRefreshToken());
        
        // Убираем токены из ответа
        authResponse.setAccessToken(null);
        authResponse.setRefreshToken(null);
        
        return authResponse;
    }

    /**
     * Обновляет токен доступа с помощью refresh токена.
     * 
     * <p>Проверяет валидность refresh токена и генерирует новую пару
     * токенов доступа и обновления. Новые токены сохраняются в cookies.</p>
     * 
     * <p>Метод защищен rate limiting для предотвращения злоупотреблений.</p>
     */
    @PostMapping("/refresh")
    @RateLimit("refreshTokenRateLimiter")
    @Operation(
        summary = "Обновление токена",
        description = "Обновляет токен доступа с помощью refresh токена. " +
                     "Refresh токен автоматически извлекается из cookies.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Токен успешно обновлен",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuthResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Refresh токен не найден или недействителен"
            ),
            @ApiResponse(
                responseCode = "429",
                description = "Превышен лимит запросов"
            )
        }
    )
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("Попытка обновления токена");
        
        String refreshToken = cookieService.getRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            throw new TokenNotFoundException("Refresh token не найден в cookies");
        }
        
        HttpUtils.RequestInfo requestInfo = httpUtils.getRequestInfo(request);
        AuthResponse authResponse = authService.refreshToken(refreshToken, requestInfo.ipAddress(), requestInfo.userAgent());
        
        // Обновляем токены в cookies
        cookieService.setAccessTokenCookie(response, authResponse.getAccessToken());
        cookieService.setRefreshTokenCookie(response, authResponse.getRefreshToken());
        
        // Убираем токены из ответа
        authResponse.setAccessToken(null);
        authResponse.setRefreshToken(null);
        
        return authResponse;
    }

    /**
     * Отправляет код подтверждения на email пользователя.
     * 
     * <p>Генерирует новый код подтверждения и отправляет его на
     * указанный email адрес. Код действителен в течение ограниченного времени.</p>
     * 
     * <p>Метод защищен rate limiting для предотвращения спама.</p>
     */
    @PostMapping("/send-verification")
    @RateLimit(value = "emailVerificationRateLimiter",
            message = "Слишком много попыток запроса, попробуйте позже!")
    @Operation(
        summary = "Отправка кода подтверждения",
        description = "Отправляет код подтверждения на указанный email адрес. " +
                     "Код действителен в течение 15 минут.",
        parameters = {
            @Parameter(
                name = "email",
                description = "Email адрес для отправки кода подтверждения",
                required = true,
                example = "ivan@example.com"
            )
        }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Код подтверждения отправлен",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "message": "Код для подтверждения почты отправлен повторно на ivan@example.com"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Пользователь с таким email не найден"
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Превышен лимит запросов"
        )
    })
    public Map<String, String> sendVerificationCode(
            @RequestParam String email,
            HttpServletRequest httpRequest) {
        log.info("Запрос на отправку кода подтверждения: {}", email);
        
        String ipAddress = httpUtils.getClientIpAddress(httpRequest);
        emailService.sendVerificationCode(email, ipAddress);
        
        return Map.of("message", String.format("Код для подтверждения почты отправлен повторно на %s", email));
    }

    /**
     * Подтверждает email адрес пользователя с помощью кода.
     * 
     * <p>Проверяет код подтверждения и активирует аккаунт пользователя.
     * После подтверждения пользователь сможет войти в систему.</p>
     * 
     * <p>Метод защищен rate limiting для предотвращения брутфорс атак.</p>
     */
    @PostMapping("/verify-email")
    @RateLimit(value = "emailVerificationRateLimiter",
            message = "Слишком много попыток, попробуйте позже!")
    @Operation(
        summary = "Подтверждение email",
        description = "Подтверждает email адрес с помощью кода. " +
                     "После подтверждения пользователь сможет войти в систему.",
        parameters = {
            @Parameter(
                name = "email",
                description = "Email адрес пользователя",
                required = true,
                example = "ivan@example.com"
            ),
            @Parameter(
                name = "code",
                description = "Код подтверждения (6 цифр)",
                required = true,
                example = "123456"
            )
        }
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Email успешно подтвержден",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "message": "Аккаунт ivan@example.com подтвержден"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Неверный код подтверждения или код истек"
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Превышен лимит запросов"
        )
    })
    public Map<String, String> verifyEmail(
            @RequestParam String email,
            @RequestParam String code,
            HttpServletRequest httpRequest) {
        log.info("Попытка верификации email: {}", email);
        
        HttpUtils.RequestInfo requestInfo = httpUtils.getRequestInfo(httpRequest);
        emailService.verifyEmail(email, code, requestInfo.ipAddress(), requestInfo.userAgent());
        
        return Map.of("message", String.format("Аккаунт %s подтвержден", email));
    }

    /**
     * Завершает сессию пользователя.
     * 
     * <p>Отзывает refresh токен и очищает cookies с токенами.
     * После вызова этого метода пользователь должен будет войти в систему заново.</p>
     */
    @PostMapping("/logout")
    @Operation(
        summary = "Выход пользователя",
        description = "Завершает сессию пользователя и отзывает токены. " +
                     "Refresh токен автоматически извлекается из cookies."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Успешный выход из системы",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "message": "Выход выполнен успешно"
                    }
                    """
                )
            )
        )
    })
    public Map<String, String> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.getRefreshTokenFromCookie(request);
        
        if (refreshToken != null) {
            HttpUtils.RequestInfo requestInfo = httpUtils.getRequestInfo(request);
            authService.logout(refreshToken, requestInfo.ipAddress(), requestInfo.userAgent());
        }

        // Удаляем все токены из cookies
        cookieService.removeAllTokenCookies(response);

        return Map.of("message", "Выход выполнен успешно");
    }

    /**
     * Изменяет пароль текущего пользователя.
     * 
     * <p>Проверяет текущий пароль и устанавливает новый.
     * После смены пароля все токены отзываются,
     * и пользователю выдаются новые.</p>
     * 
     * <p>Метод защищен rate limiting для предотвращения брутфорс атак.</p>
     */
    @PostMapping("/change-password")
    @RateLimit(value = "passwordChangeRateLimiter",
        message = "Много попыток, попробуйте позже!")
    @Operation(
        summary = "Смена пароля",
        description = "Изменяет пароль текущего пользователя. " +
                     "После смены пароля все существующие токены отзываются " +
                     "и выдаются новые.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Данные для смены пароля",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChangePasswordRequest.class),
                examples = @ExampleObject(
                    name = "Пример смены пароля",
                    value = """
                    {
                        "oldPassword": "OldPass123!",
                        "newPassword": "NewPass456!"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Пароль успешно изменен",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Неверный текущий пароль или ошибка валидации"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Пользователь не аутентифицирован"
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Превышен лимит запросов"
        )
    })
    public AuthResponse changePassword(
            HttpServletRequest request, 
            @RequestBody ChangePasswordRequest passwordRequest, 
            Authentication authentication,
            HttpServletResponse response) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        HttpUtils.RequestInfo requestInfo = httpUtils.getRequestInfo(request);

        AuthResponse authResponse = authService.changePassword(userId, 
                                                              passwordRequest.getOldPassword(), 
                                                              passwordRequest.getNewPassword(), 
                                                              requestInfo.ipAddress(), 
                                                              requestInfo.userAgent());
        
        // Устанавливаем новые токены в cookies
        cookieService.setAccessTokenCookie(response, authResponse.getAccessToken());
        cookieService.setRefreshTokenCookie(response, authResponse.getRefreshToken());
        
        // Убираем токены из ответа
        authResponse.setAccessToken(null);
        authResponse.setRefreshToken(null);
        
        return authResponse;
    }

    /**
     * Получает информацию о текущем аутентифицированном пользователе.
     * 
     * <p>Возвращает данные пользователя, включая роли и права доступа.</p>
     */
    @GetMapping("/me")
    @Operation(
        summary = "Информация о текущем пользователе",
        description = "Возвращает информацию о текущем аутентифицированном пользователе."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Информация о пользователе",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "authenticated": true,
                        "user": {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "email": "ivan@example.com",
                            "username": "ivan_petrov",
                            "role": "USER"
                        },
                        "authorities": ["ROLE_USER"]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Пользователь не аутентифицирован",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "authenticated": false,
                        "message": "Пользователь не аутентифицирован"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        log.info("Получение информации о пользователе");
        if (!currentUserService.isAuthenticated(authentication)) {
            return ResponseEntity.status(401).body(Map.of(
                "authenticated", false,
                "message", "Пользователь не аутентифицирован"
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "authenticated", true,
            "user", authentication.getPrincipal(),
            "authorities", authentication.getAuthorities()
        ));
    }
}