package azhue.katering.azhurkateringbackendjava.common.util.contract;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Интерфейс для работы с HTTP запросами.
 * 
 * <p>Определяет методы для извлечения информации из HTTP запросов:
 * IP адрес клиента, User-Agent и другие данные.</p>
 * 
 * @version 1.0.0
 */
public interface HttpUtils {

    /**
     * Извлекает IP адрес клиента из запроса
     */
    String getClientIpAddress(HttpServletRequest request);

    /**
     * Извлекает User-Agent из запроса
     */
    String getUserAgent(HttpServletRequest request);

    /**
     * Получает информацию о запросе
     */
    RequestInfo getRequestInfo(HttpServletRequest request);

    /**
     * Интерфейс для информации о запросе
     */
    interface RequestInfo {
        /**
         * Возвращает IP адрес клиента
         */
        String ipAddress();
        
        /**
         * Возвращает User-Agent клиента
         */
        String userAgent();
    }
}
