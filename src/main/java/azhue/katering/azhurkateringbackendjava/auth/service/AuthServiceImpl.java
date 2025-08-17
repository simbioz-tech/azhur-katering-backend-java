package azhue.katering.azhurkateringbackendjava.auth.service;

import azhue.katering.azhurkateringbackendjava.auth.model.dto.request.LoginRequest;
import azhue.katering.azhurkateringbackendjava.auth.model.dto.request.RefreshTokenRequest;
import azhue.katering.azhurkateringbackendjava.auth.model.dto.request.RegisterRequest;
import azhue.katering.azhurkateringbackendjava.auth.model.dto.response.AuthResponse;
import azhue.katering.azhurkateringbackendjava.auth.model.entity.AuthLog;
import azhue.katering.azhurkateringbackendjava.auth.model.entity.RefreshToken;
import azhue.katering.azhurkateringbackendjava.auth.model.entity.User;
import azhue.katering.azhurkateringbackendjava.auth.repository.RefreshTokenRepository;
import azhue.katering.azhurkateringbackendjava.auth.repository.UserRepository;
import azhue.katering.azhurkateringbackendjava.auth.service.contract.AuthService;
import azhue.katering.azhurkateringbackendjava.auth.service.contract.EmailService;
import azhue.katering.azhurkateringbackendjava.auth.service.contract.LoggingAuthActions;
import azhue.katering.azhurkateringbackendjava.common.exception.account.AccountIsLockedException;
import azhue.katering.azhurkateringbackendjava.common.exception.account.IncorrectPasswordException;
import azhue.katering.azhurkateringbackendjava.common.exception.account.UserNotFoundException;
import azhue.katering.azhurkateringbackendjava.common.exception.token.TokenExpiredException;
import azhue.katering.azhurkateringbackendjava.common.exception.token.TokenNotFoundException;
import azhue.katering.azhurkateringbackendjava.common.exception.token.TokenNotValidException;
import azhue.katering.azhurkateringbackendjava.common.exception.token.TypeTokenException;
import azhue.katering.azhurkateringbackendjava.security.jwt.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
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
    private final LoggingAuthActions loggingAuthActions;
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_TIME_MINUTES = 30;

    /**
     * Регистрирует нового пользователя
     */
    @Override
    @Transactional
    public void register(RegisterRequest request, String ipAddress, String userAgent) {
        // Проверяем, существует ли пользователь
        if (userRepository.existsByEmail(request.getEmail())) {
            loggingAuthActions.logAuthAction(null, AuthLog.AuthAction.REGISTRATION_FAILED, ipAddress, userAgent, false, "Email уже существует");
            throw new UserNotFoundException("Пользователь с таким email уже существует");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            loggingAuthActions.logAuthAction(null, AuthLog.AuthAction.REGISTRATION_FAILED, ipAddress, userAgent, false, "Username уже существует");
            throw new RuntimeException("Пользователь с таким username уже существует");
        }

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
        log.info("User saved successfully with ID: {}", user.getId());

        log.info("Calling email service for user: {}", user.getEmail());
        emailService.sendVerificationCodeAsync(user, ipAddress);
        log.info("Email service called successfully for user: {}", user.getEmail());

        loggingAuthActions.logAuthAction(user, AuthLog.AuthAction.REGISTRATION, ipAddress, userAgent, true, null);
        log.info("Пользователь зарегистрирован и ему отправлен код на почту {}", user.getEmail());
    }

    /**
     * Выполняет вход пользователя
     */
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    loggingAuthActions.logAuthAction(null, AuthLog.AuthAction.LOGIN_FAILED, ipAddress, userAgent, false, "Пользователь не найден");
                    return new BadCredentialsException("Неверный email или пароль");
                });

        // Проверяем, заблокирован ли аккаунт
        if (user.isAccountLocked()) {
            loggingAuthActions.logAuthAction(user, AuthLog.AuthAction.LOGIN_FAILED, ipAddress, userAgent, false, "Аккаунт заблокирован");
            throw new AccountIsLockedException("Аккаунт заблокирован. Попробуйте позже.");
        }

        // Проверяем пароль
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            loggingAuthActions.logAuthAction(user, AuthLog.AuthAction.LOGIN_FAILED, ipAddress, userAgent, false, "Неверный пароль");
            throw new IncorrectPasswordException("Неверный пароль");
        }

        // Проверяем, верифицирован ли email
        if (!user.getIsVerified()) {
            log.info("Попытка входа неверифицированного пользователя: {}. Отправляем новый код подтверждения", user.getEmail());
            
            // Отправляем новый код подтверждения
            emailService.sendVerificationCodeAsync(user, ipAddress);
            log.info("Новый код подтверждения отправлен для пользователя: {}", user.getEmail());
            
            loggingAuthActions.logAuthAction(user, AuthLog.AuthAction.LOGIN_FAILED, ipAddress, userAgent, false, "Email не верифицирован, отправлен новый код");
            
            // Возвращаем специальный ответ вместо исключения
            return AuthResponse.builder()
                    .requiresVerification(true)
                    .verificationMessage("Email не верифицирован. Новый код подтверждения отправлен на вашу почту.")
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .isVerified(false)
                    .build();
        }

        // Сбрасываем счетчик неудачных попыток
        user.resetFailedAttempts();
        userRepository.save(user);

        // Генерируем токены
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getId().toString(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getId().toString());

        // Сохраняем refresh token
        saveRefreshToken(user, refreshToken, ipAddress, userAgent);

        loggingAuthActions.logAuthAction(user, AuthLog.AuthAction.LOGIN, ipAddress, userAgent, true, null);
        log.info("Пользователь вошел в систему: {}", user.getEmail());

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
    }

    /**
     * Обновляет access token используя refresh token из cookie
     */
    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken, String ipAddress, String userAgent) {
        try {
            // Проверяем, что это refresh token
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                throw new TypeTokenException("Неверный тип токена");
            }

            String email = jwtUtil.extractEmail(refreshToken);

            if (jwtUtil.isTokenExpired(refreshToken)) {
                throw new TokenExpiredException("Refresh token истек");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

            // Проверяем, существует ли refresh token в БД
            RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                    .orElseThrow(() -> new TokenNotFoundException("Refresh token не найден"));

            if (!storedToken.isValid()) {
                throw new TokenNotValidException("Refresh token отозван или истек");
            }

            // Генерируем новые токены
            String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getId().toString(), user.getRole().name());
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getId().toString());

            // Отзываем старый refresh token
            storedToken.revoke();
            refreshTokenRepository.save(storedToken);

            // Сохраняем новый refresh token
            saveRefreshToken(user, newRefreshToken, ipAddress, userAgent);

            loggingAuthActions.logAuthAction(user, AuthLog.AuthAction.LOGIN, ipAddress, userAgent, true, "Token refreshed");

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

        } catch (JwtException e) {
            loggingAuthActions.logAuthAction(null, AuthLog.AuthAction.LOGIN_FAILED, ipAddress, userAgent, false, "Неверный refresh token");
            throw new RuntimeException("Неверный refresh token");
        }
    }

    /**
     * Выполняет выход пользователя
     */
    @Override
    @Transactional
    public void logout(String refreshToken, String ipAddress, String userAgent) {
        try {
            String email = jwtUtil.extractEmail(refreshToken);
            User user = userRepository.findByEmail(email).orElse(null);

            // Отзываем refresh token
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(token -> {
                        token.revoke();
                        refreshTokenRepository.save(token);
                    });

            loggingAuthActions.logAuthAction(user, AuthLog.AuthAction.LOGOUT, ipAddress, userAgent, true, null);
            log.info("Пользователь вышел из системы: {}", email);
        } catch (Exception e) {
            log.warn("Ошибка при выходе пользователя: {}", e.getMessage());
        }
    }

    /**
     * Изменяет пароль пользователя
     */
    @Override
    @Transactional
    public AuthResponse changePassword(UUID userId, String oldPassword, String newPassword, String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        // Проверяем текущий пароль
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            loggingAuthActions.logAuthAction(user, AuthLog.AuthAction.PASSWORD_CHANGED, ipAddress, userAgent, false, "Неверный текущий пароль");
            throw new IncorrectPasswordException("Неверный текущий пароль");
        }

        // Обновляем пароль
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        // Отзываем все refresh токены пользователя
        refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now());

        // Генерируем новые токены
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getId().toString(), user.getRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getId().toString());

        // Сохраняем новый refresh token
        saveRefreshToken(user, newRefreshToken, ipAddress, userAgent);

        loggingAuthActions.logAuthAction(user, AuthLog.AuthAction.PASSWORD_CHANGED, ipAddress, userAgent, true, null);
        log.info("Пароль изменен для пользователя: {}", user.getEmail());

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
    }

    /**
     * Сохраняет refresh token в базе данных
     */
    private void saveRefreshToken(User user, String token, String ipAddress, String userAgent) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        
        refreshTokenRepository.save(refreshToken);
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