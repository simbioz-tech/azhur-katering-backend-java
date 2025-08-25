package azhue.katering.azhurkateringbackendjava.common.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * Утилита для структурированного логирования с тегами операций.
 * 
 * <p>Позволяет добавлять теги операций в логи для удобного поиска в Kibana.</p>
 * 
 * @version 1.0.0
 */
@Slf4j
public class LogUtils {

    public static final String OPERATION_TAG = "operation";
    public static final String USER_ID_TAG = "userId";
    public static final String EMAIL_TAG = "email";
    public static final String IP_TAG = "ip";
    public static final String STATUS_TAG = "status";

    // Константы для операций
    public static final String OPERATION_REGISTER = "register";
    public static final String OPERATION_LOGIN = "login";
    public static final String OPERATION_LOGOUT = "logout";
    public static final String OPERATION_REFRESH_TOKEN = "refresh_token";
    public static final String OPERATION_CHANGE_PASSWORD = "change_password";
    public static final String OPERATION_EMAIL_VERIFICATION = "email_verification";

    // Константы для статусов
    public static final String STATUS_START = "start";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_WARNING = "warning";

    /**
     * Устанавливает тег операции в MDC
     */
    public static void setOperation(String operation) {
        MDC.put(OPERATION_TAG, operation);
    }

    /**
     * Устанавливает userId в MDC
     */
    public static void setUserId(String userId) {
        if (userId != null) {
            MDC.put(USER_ID_TAG, userId);
        }
    }

    /**
     * Устанавливает email в MDC
     */
    public static void setEmail(String email) {
        if (email != null) {
            MDC.put(EMAIL_TAG, email);
        }
    }

    /**
     * Устанавливает IP адрес в MDC
     */
    public static void setIp(String ip) {
        if (ip != null) {
            MDC.put(IP_TAG, ip);
        }
    }

    /**
     * Устанавливает статус операции в MDC
     */
    public static void setStatus(String status) {
        MDC.put(STATUS_TAG, status);
    }

    /**
     * Очищает все теги из MDC
     */
    public static void clearTags() {
        MDC.remove(OPERATION_TAG);
        MDC.remove(USER_ID_TAG);
        MDC.remove(EMAIL_TAG);
        MDC.remove(IP_TAG);
        MDC.remove(STATUS_TAG);
    }

    /**
     * Устанавливает все теги для операции
     */
    public static void setOperationTags(String operation, String userId, String email, String ip, String status) {
        setOperation(operation);
        setUserId(userId);
        setEmail(email);
        setIp(ip);
        setStatus(status);
    }
}

