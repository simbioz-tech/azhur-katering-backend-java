package azhue.katering.azhurkateringbackendjava.auth.service.contract;

import azhue.katering.azhurkateringbackendjava.auth.model.entity.AuthLog;
import azhue.katering.azhurkateringbackendjava.auth.model.entity.User;

/**
 * Интерфейс для логирования действий аутентификации.
 * 
 * <p>Определяет метод для записи информации о попытках входа,
 * регистрации и других действиях пользователей.</p>
 * 
 * @version 1.0.0
 */
public interface LoggingAuthActions {
    
    /**
     * Логирует действие аутентификации
     */
    void logAuthAction(User user, AuthLog.AuthAction action, String ipAddress, String userAgent, boolean success, String failureReason);
}
