package azhue.katering.azhurkateringbackendjava.auth.service;

import azhue.katering.azhurkateringbackendjava.auth.model.entity.AuthLog;
import azhue.katering.azhurkateringbackendjava.auth.model.entity.User;
import azhue.katering.azhurkateringbackendjava.auth.repository.AuthLogRepository;
import azhue.katering.azhurkateringbackendjava.auth.service.contract.LoggingAuthActions;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Сервис для логирования действий аутентификации.
 * 
 * <p>Сохраняет информацию о попытках входа, регистрации и других
 * действиях пользователей в базу данных для аудита.</p>
 * 
 * @version 1.0.0
 */
@Service
@AllArgsConstructor
@Slf4j
public class LoggingAuthActionsImpl implements LoggingAuthActions {

    private final AuthLogRepository authLogRepository;

    /**
     * Логирует действие аутентификации
     */
    @Override
    public void logAuthAction(User user, AuthLog.AuthAction action, String ipAddress, String userAgent, boolean success, String failureReason) {
        try {
            AuthLog authLog = AuthLog.builder()
                    .user(user)
                    .action(action)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .success(success)
                    .failureReason(failureReason)
                    .build();

            authLogRepository.save(authLog);
        } catch (Exception e) {
            log.error("Ошибка при логировании действия аутентификации: {}", e.getMessage());
        }
    }
}
