package azhue.katering.azhurkateringbackendjava.common.util;

import azhue.katering.azhurkateringbackendjava.common.util.contract.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Реализация утилит для работы с HTTP запросами.
 * 
 * <p>Предоставляет методы для извлечения IP адреса клиента,
 * User-Agent и другой информации из HTTP запросов.</p>
 * 
 * @version 1.0.0
 */
@Component
public class HttpUtilsImpl implements HttpUtils {

    /**
     * Извлекает IP адрес клиента из запроса
     */
    @Override
    public String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Извлекает User-Agent из запроса
     */
    @Override
    public String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    /**
     * Получает информацию о запросе
     */
    @Override
    public RequestInfo getRequestInfo(HttpServletRequest request) {
        return new RequestInfo(getClientIpAddress(request), getUserAgent(request));
    }

    /**
     * Record для хранения информации о запросе
     */
    public record RequestInfo(String ipAddress, String userAgent) implements HttpUtils.RequestInfo {}
}
