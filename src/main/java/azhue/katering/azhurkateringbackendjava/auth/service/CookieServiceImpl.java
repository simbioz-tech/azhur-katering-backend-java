package azhue.katering.azhurkateringbackendjava.auth.service;

import azhue.katering.azhurkateringbackendjava.auth.service.contract.CookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

/**
 * Реализация сервиса для работы с HTTP cookies.
 * 
 * <p>Предоставляет методы для создания, получения и удаления безопасных cookies
 * для хранения JWT токенов. Использует префикс __Host- для максимальной безопасности.
 * Все cookies создаются с флагами HttpOnly и Secure.</p>
 * 
 * @version 1.0.0
 */
@Service
@Slf4j
public class CookieServiceImpl implements CookieService {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "__Host-access-token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "__Host-refresh-token";
    
    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    /**
     * Устанавливает access token в cookie
     */
    @Override
    public void setAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = createSecureCookie(ACCESS_TOKEN_COOKIE_NAME, token, 900); // 15 минут
        response.addCookie(cookie);
        log.debug("Access token cookie установлен");
    }

    /**
     * Устанавливает refresh token в cookie
     */
    @Override
    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = createSecureCookie(REFRESH_TOKEN_COOKIE_NAME, token, 604800); // 7 дней
        response.addCookie(cookie);
        log.debug("Refresh token cookie установлен");
    }

    /**
     * Получает access token из cookie
     */
    @Override
    public String getAccessTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_COOKIE_NAME);
    }

    /**
     * Получает refresh token из cookie
     */
    @Override
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    /**
     * Удаляет access token cookie
     */
    @Override
    public void removeAccessTokenCookie(HttpServletResponse response) {
        Cookie cookie = createExpiredCookie(ACCESS_TOKEN_COOKIE_NAME);
        response.addCookie(cookie);
        log.debug("Access token cookie удален");
    }

    /**
     * Удаляет refresh token cookie
     */
    @Override
    public void removeRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = createExpiredCookie(REFRESH_TOKEN_COOKIE_NAME);
        response.addCookie(cookie);
        log.debug("Refresh token cookie удален");
    }

    /**
     * Удаляет все токены из cookies
     */
    @Override
    public void removeAllTokenCookies(HttpServletResponse response) {
        removeAccessTokenCookie(response);
        removeRefreshTokenCookie(response);
        log.debug("Все токены удалены из cookies");
    }

    /**
     * Создает безопасный cookie с префиксом __Host-
     * 
     * <p>Cookies с префиксом __Host- автоматически получают следующие свойства:
     * - Secure: true (обязательно)
     * - Path: / (обязательно)
     * - Domain: не устанавливается (обязательно)</p>
     */
    private Cookie createSecureCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Обязательно для __Host- cookies
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        
        // Для __Host- cookies нельзя устанавливать domain
        // Это обеспечивает дополнительную безопасность
        
        return cookie;
    }

    /**
     * Создает истекший cookie для удаления
     */
    private Cookie createExpiredCookie(String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Обязательно для __Host- cookies
        cookie.setPath("/");
        cookie.setMaxAge(0);
        
        return cookie;
    }

    /**
     * Получение значения cookie
     */
    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        
        Optional<Cookie> cookie = Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .findFirst();
        
        return cookie.map(Cookie::getValue).orElse(null);
    }
}
