package azhue.katering.azhurkateringbackendjava.auth.service.contract;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Интерфейс для работы с HTTP cookies.
 * 
 * <p>Предоставляет методы для управления JWT токенами в cookies:
 * установка, получение и удаление access и refresh токенов.</p>
 * 
 * @version 1.0.0
 */
public interface CookieService {

    /**
     * Устанавливает access token в cookie
     */
    void setAccessTokenCookie(HttpServletResponse response, String token);

    /**
     * Устанавливает refresh token в cookie
     */
    void setRefreshTokenCookie(HttpServletResponse response, String token);

    /**
     * Получает access token из cookie
     */
    String getAccessTokenFromCookie(HttpServletRequest request);

    /**
     * Получает refresh token из cookie
     */
    String getRefreshTokenFromCookie(HttpServletRequest request);

    /**
     * Удаляет access token cookie
     */
    void removeAccessTokenCookie(HttpServletResponse response);

    /**
     * Удаляет refresh token cookie
     */
    void removeRefreshTokenCookie(HttpServletResponse response);

    /**
     * Удаляет все токены из cookies
     */
    void removeAllTokenCookies(HttpServletResponse response);
}
