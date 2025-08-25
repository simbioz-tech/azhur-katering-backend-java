package azhue.katering.azhurkateringbackendjava.auth.service;

import azhue.katering.azhurkateringbackendjava.auth.exception.account.AccountIsLockedException;
import azhue.katering.azhurkateringbackendjava.auth.exception.account.IncorrectPasswordException;
import azhue.katering.azhurkateringbackendjava.auth.exception.account.UserNotFoundException;
import azhue.katering.azhurkateringbackendjava.auth.exception.account.UsernameExistsException;
import azhue.katering.azhurkateringbackendjava.auth.exception.token.TokenExpiredException;
import azhue.katering.azhurkateringbackendjava.auth.exception.token.TokenNotFoundException;
import azhue.katering.azhurkateringbackendjava.auth.exception.token.TokenNotValidException;
import azhue.katering.azhurkateringbackendjava.auth.exception.token.TypeTokenException;
import azhue.katering.azhurkateringbackendjava.auth.model.dto.request.LoginRequest;
import azhue.katering.azhurkateringbackendjava.auth.model.dto.request.RegisterRequest;
import azhue.katering.azhurkateringbackendjava.auth.model.dto.response.AuthResponse;
import azhue.katering.azhurkateringbackendjava.auth.model.entity.RefreshToken;
import azhue.katering.azhurkateringbackendjava.auth.model.entity.User;
import azhue.katering.azhurkateringbackendjava.auth.repository.RefreshTokenRepository;
import azhue.katering.azhurkateringbackendjava.auth.repository.UserRepository;
import azhue.katering.azhurkateringbackendjava.auth.service.contract.AuthService;
import azhue.katering.azhurkateringbackendjava.auth.service.contract.EmailService;
import azhue.katering.azhurkateringbackendjava.common.service.MetricsService;
import azhue.katering.azhurkateringbackendjava.common.util.LogUtils;
import azhue.katering.azhurkateringbackendjava.security.jwt.util.JwtUtil;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Основной сервис аутентификации и авторизации.
 * 
 * <p>Управляет регистрацией, входом, выходом, сменой пароля и обновлением токенов.
 * Обеспечивает безопасность через блокировку аккаунтов и верификацию email.</p>
 * 
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final MetricsService metricsService;
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_TIME_MINUTES = 30;

    /**
     * Регистрирует нового пользователя
     */
    @Override
    @Transactional
    public void register(RegisterRequest request, String ipAddress, String userAgent) {
        LogUtils.setOperationTags(LogUtils.OPERATION_REGISTER, null, request.getEmail(), ipAddress, LogUtils.STATUS_START);
        log.info("Начинаем регистрацию пользователя: email={}, username={}", request.getEmail(), request.getUsername());
        Timer.Sample timer = metricsService.startRegisterProcessingTimer();
        
        try {
            // Проверяем, существует ли пользователь
            if (userRepository.existsByEmail(request.getEmail())) {
                LogUtils.setOperationTags(LogUtils.OPERATION_REGISTER, null, request.getEmail(), ipAddress, LogUtils.STATUS_FAILED);
                log.warn("Попытка регистрации с существующим email: email={}", request.getEmail());
                throw new UserNotFoundException("Пользователь с таким email уже существует");
            }

            if (userRepository.existsByUsername(request.getUsername())) {
                LogUtils.setOperationTags(LogUtils.OPERATION_REGISTER, null, request.getEmail(), ipAddress, LogUtils.STATUS_FAILED);
                log.warn("Попытка регистрации с существующим username: username={}", request.getUsername());
                throw new UsernameExistsException("Пользователь с таким username уже существует");
            }

            log.info("Создаем нового пользователя: email={}, username={}", request.getEmail(), request.getUsername());
            // Создаем пользователя
            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .role(User.Role.USER)
                    .isActive(true)
                    .isVerified(false) // Требуется верификация email
                    .isAccountNonLocked(true)
                    .failedAttempts(0)
                    .build();

            user = userRepository.save(user);
            LogUtils.setOperationTags(LogUtils.OPERATION_REGISTER, user.getId().toString(), user.getEmail(), ipAddress, LogUtils.STATUS_SUCCESS);
            log.info("Пользователь сохранен: userId={}, email={}, username={}", user.getId(), user.getEmail(), user.getUsername());

            log.info("Отправляем код верификации: userId={}, email={}", user.getId(), user.getEmail());
            emailService.sendVerificationCodeAsync(user, ipAddress);
            log.info("Код верификации отправлен: userId={}, email={}", user.getId(), user.getEmail());

            log.info("Регистрация завершена успешно: userId={}, email={}, username={}", user.getId(), user.getEmail(), user.getUsername());
            
            // Увеличиваем счетчик регистраций
            metricsService.incrementRegistrations();
            
        } finally {
            LogUtils.clearTags();
            metricsService.stopRegisterProcessingTimer(timer);
        }
    }

    /**
     * Выполняет вход пользователя
     */
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        LogUtils.setOperationTags(LogUtils.OPERATION_LOGIN, null, request.getEmail(), ipAddress, LogUtils.STATUS_START);
        log.info("Попытка входа: email={}", request.getEmail());
        Timer.Sample timer = metricsService.startLoginProcessingTimer();
        metricsService.incrementLoginAttempts();
        
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        LogUtils.setOperationTags(LogUtils.OPERATION_LOGIN, null, request.getEmail(), ipAddress, LogUtils.STATUS_FAILED);
                        log.warn("Попытка входа с несуществующим email: email={}", request.getEmail());
                        metricsService.incrementFailedLogins();
                        return new UserNotFoundException("Пользователь с таким email не найден");
                    });

            log.info("Пользователь найден: userId={}, email={}, isVerified={}, isLocked={}", 
                    user.getId(), user.getEmail(), user.getIsVerified(), user.isAccountLocked());

            // Проверяем, заблокирован ли аккаунт
            if (user.isAccountLocked()) {
                LogUtils.setOperationTags(LogUtils.OPERATION_LOGIN, user.getId().toString(), user.getEmail(), ipAddress, LogUtils.STATUS_FAILED);
                log.warn("Попытка входа в заблокированный аккаунт: userId={}, email={}", user.getId(), user.getEmail());
                metricsService.incrementFailedLogins();
                throw new AccountIsLockedException("Аккаунт заблокирован. Попробуйте позже.");
            }

            // Проверяем пароль
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                LogUtils.setOperationTags(LogUtils.OPERATION_LOGIN, user.getId().toString(), user.getEmail(), ipAddress, LogUtils.STATUS_FAILED);
                log.warn("Неверный пароль: userId={}, email={}", user.getId(), user.getEmail());
                handleFailedLogin(user);
                metricsService.incrementFailedLogins();
                throw new IncorrectPasswordException("Неверный пароль");
            }

            log.info("Пароль проверен успешно: userId={}, email={}", user.getId(), user.getEmail());

            // Проверяем, верифицирован ли email
            if (!user.getIsVerified()) {
                log.info("Попытка входа неверифицированного пользователя: userId={}, email={}, ip={}", user.getId(), user.getEmail(), ipAddress);
                
                // Отправляем новый код подтверждения
                emailService.sendVerificationCodeAsync(user, ipAddress);
                log.info("Новый код подтверждения отправлен: userId={}, email={}", user.getId(), user.getEmail());

                metricsService.incrementFailedLogins();
                
                // Возвращаем специальный ответ вместо исключения
                return AuthResponse.builder()
                        .requiresVerification(true)
                        .verificationMessage("Email не верифицирован. Новый код подтверждения отправлен на вашу почту.")
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .isVerified(false)
                        .build();
            }

            log.info("Email верифицирован, сбрасываем счетчик неудачных попыток: userId={}, email={}", user.getId(), user.getEmail());

            // Сбрасываем счетчик неудачных попыток
            user.resetFailedAttempts();
            userRepository.save(user);

            log.info("Генерируем токены: userId={}, email={}", user.getId(), user.getEmail());

            // Генерируем токены
            String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getId().toString(), user.getRole().name());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getId().toString());

            // Сохраняем refresh token
            saveRefreshToken(user, refreshToken, ipAddress, userAgent);

            LogUtils.setOperationTags(LogUtils.OPERATION_LOGIN, user.getId().toString(), user.getEmail(), ipAddress, LogUtils.STATUS_SUCCESS);
            log.info("Вход выполнен успешно: userId={}, email={}, role={}", user.getId(), user.getEmail(), user.getRole());
            
            // Увеличиваем счетчик успешных входов
            metricsService.incrementSuccessfulLogins();

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getTimeUntilExpiration(accessToken))
                    .userId(user.getId().toString())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .role(user.getRole().name())
                    .isVerified(user.getIsVerified())
                    .build();
                    
        } finally {
            LogUtils.clearTags();
            metricsService.stopLoginProcessingTimer(timer);
        }
    }

    /**
     * Обновляет access token используя refresh token из cookie
     */
    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken, String ipAddress, String userAgent) {
        LogUtils.setOperationTags(LogUtils.OPERATION_REFRESH_TOKEN, null, null, ipAddress, LogUtils.STATUS_START);
        log.info("Начинаем обновление токенов");
        Timer.Sample timer = metricsService.startRefreshTokenProcessingTimer();
        
        try {
            // Проверяем, что это refresh token
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                throw new TypeTokenException("Неверный тип токена");
            }

        log.info("Получаем email из refresh token");
        String email = jwtUtil.extractEmail(refreshToken);
        LogUtils.setEmail(email);

        if (jwtUtil.isTokenExpired(refreshToken)) {
            LogUtils.setOperationTags(LogUtils.OPERATION_REFRESH_TOKEN, null, email, ipAddress, LogUtils.STATUS_FAILED);
            log.warn("Refresh token истек: email={}", email);
            throw new TokenExpiredException("Refresh token истек");
        }

        log.info("Ищем пользователя в БД: email={}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        LogUtils.setUserId(user.getId().toString());

        log.info("Проверяем существование refresh токена: userId={}, email={}", user.getId(), user.getEmail());
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new TokenNotFoundException("Refresh token не найден"));

        if (!storedToken.isValid()) {
            LogUtils.setOperationTags(LogUtils.OPERATION_REFRESH_TOKEN, user.getId().toString(), user.getEmail(), ipAddress, LogUtils.STATUS_FAILED);
            log.warn("Refresh token невалиден: userId={}, email={}", user.getId(), user.getEmail());
            throw new TokenNotValidException("Refresh token отозван или истек");
        }

        log.info("Генерируем новую пару токенов: userId={}, email={}", user.getId(), user.getEmail());
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getId().toString(), user.getRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getId().toString());

        log.info("Отзываем старый refresh токен: userId={}, email={}", user.getId(), user.getEmail());
        storedToken.revoke();
        refreshTokenRepository.save(storedToken);

        saveRefreshToken(user, newRefreshToken, ipAddress, userAgent);

        LogUtils.setOperationTags(LogUtils.OPERATION_REFRESH_TOKEN, user.getId().toString(), user.getEmail(), ipAddress, LogUtils.STATUS_SUCCESS);
        log.info("Обновление токенов завершено успешно: userId={}, email={}", user.getId(), user.getEmail());
        
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getTimeUntilExpiration(newAccessToken))
                .userId(user.getId().toString())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().name())
                .isVerified(user.getIsVerified())
                .build();
        } finally {
            LogUtils.clearTags();
            metricsService.stopRefreshTokenProcessingTimer(timer);
        }
    }

    /**
     * Выполняет выход пользователя
     */
    @Override
    @Transactional
    public void logout(String refreshToken, String ipAddress, String userAgent) {
        LogUtils.setOperationTags(LogUtils.OPERATION_LOGOUT, null, null, ipAddress, LogUtils.STATUS_START);
        log.info("Пользователь выходит из системы");
        Timer.Sample timer = metricsService.startLogoutProcessingTimer();
        
        try {
            String email = jwtUtil.extractEmail(refreshToken);
            LogUtils.setEmail(email);

            log.info("Отзываем refresh токен: email={}, ip={}", email, ipAddress);
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(token -> {
                        token.revoke();
                        refreshTokenRepository.save(token);
                    });

            LogUtils.setOperationTags(LogUtils.OPERATION_LOGOUT, null, email, ipAddress, LogUtils.STATUS_SUCCESS);
            log.info("Пользователь вышел из системы: email={}", email);
        } finally {
            LogUtils.clearTags();
            metricsService.stopLogoutProcessingTimer(timer);
        }
    }

    /**
     * Изменяет пароль пользователя
     */
    @Override
    @Transactional
    public AuthResponse changePassword(UUID userId, String oldPassword, String newPassword, String ipAddress, String userAgent) {
        LogUtils.setOperationTags(LogUtils.OPERATION_CHANGE_PASSWORD, userId.toString(), null, ipAddress, LogUtils.STATUS_START);
        log.info("Начинаем смену пароля: userId={}", userId);
        Timer.Sample timer = metricsService.startChangePasswordProcessingTimer();

        try {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
            LogUtils.setEmail(user.getEmail());

            log.info("Проверяем текущий пароль: userId={}, email={}", user.getId(), user.getEmail());

            // Проверяем текущий пароль
            if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
                LogUtils.setOperationTags(LogUtils.OPERATION_CHANGE_PASSWORD, user.getId().toString(), user.getEmail(), ipAddress, LogUtils.STATUS_FAILED);
                log.warn("Неверный текущий пароль: userId={}, email={}", user.getId(), user.getEmail());
                throw new IncorrectPasswordException("Неверный текущий пароль");
            }

            log.info("Текущий пароль проверен, обновляем пароль: userId={}, email={}", user.getId(), user.getEmail());
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            user.setPasswordChangedAt(LocalDateTime.now());
            userRepository.save(user);

            log.info("Отзываем все refresh токены: userId={}, email={}", user.getId(), user.getEmail());
            // Отзываем все refresh токены пользователя
            refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());

            log.info("Генерируем новые токены: userId={}, email={}", user.getId(), user.getEmail());
            // Генерируем новые токены
            String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getId().toString(), user.getRole().name());
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getId().toString());

            // Сохраняем новый refresh token
            saveRefreshToken(user, newRefreshToken, ipAddress, userAgent);

            LogUtils.setOperationTags(LogUtils.OPERATION_CHANGE_PASSWORD, user.getId().toString(), user.getEmail(), ipAddress, LogUtils.STATUS_SUCCESS);
            log.info("Смена пароля завершена успешно: userId={}, email={}", user.getId(), user.getEmail());

            // Возвращаем новые токены
            return AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getTimeUntilExpiration(newAccessToken))
                    .userId(user.getId().toString())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .role(user.getRole().name())
                    .isVerified(user.getIsVerified())
                    .build();
        } finally {
            LogUtils.clearTags();
            metricsService.stopChangePasswordProcessingTimer(timer);
        }
    }

    /**
     * Сохраняет refresh token в базе данных
     */
    private void saveRefreshToken(User user, String token, String ipAddress, String userAgent) {
        log.debug("Сохраняем refresh token для пользователя: {}", user.getEmail());
        
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        
        refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token сохранен успешно для пользователя: {}", user.getEmail());
    }

    /**
     * Обрабатывает неудачную попытку входа
     */
    private void handleFailedLogin(User user) {
        user.incrementFailedAttempts();
        
        if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.lockAccount(LOCK_TIME_MINUTES);
            log.warn("Аккаунт заблокирован из-за множественных неудачных попыток: {}", user.getEmail());
        }
        
        userRepository.save(user);
    }
}