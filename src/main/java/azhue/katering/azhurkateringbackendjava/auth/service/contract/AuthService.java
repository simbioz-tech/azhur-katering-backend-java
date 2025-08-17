package azhue.katering.azhurkateringbackendjava.auth.service.contract;

import azhue.katering.azhurkateringbackendjava.auth.model.dto.request.LoginRequest;
import azhue.katering.azhurkateringbackendjava.auth.model.dto.request.RegisterRequest;
import azhue.katering.azhurkateringbackendjava.auth.model.dto.response.AuthResponse;

import java.util.UUID;

/**
 * Интерфейс сервиса аутентификации.
 * 
 * <p>Определяет основные операции для работы с пользователями:
 * регистрация, вход, выход, обновление токенов и смена пароля.</p>
 * 
 * @version 1.0.0
 */
public interface AuthService {
    
    /**
     * Регистрирует нового пользователя
     */
    void register(RegisterRequest request, String ipAddress, String userAgent);
    
    /**
     * Выполняет вход пользователя
     */
    AuthResponse login(LoginRequest request, String ipAddress, String userAgent);
    
    /**
     * Обновляет access token используя refresh token из cookie
     */
    AuthResponse refreshToken(String refreshToken, String ipAddress, String userAgent);
    
    /**
     * Выполняет выход пользователя
     */
    void logout(String refreshToken, String ipAddress, String userAgent);
    
    /**
     * Изменяет пароль пользователя
     */
    AuthResponse changePassword(UUID userId, String oldPassword, String newPassword, String ipAddress, String userAgent);
}
