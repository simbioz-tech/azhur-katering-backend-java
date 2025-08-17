package azhue.katering.azhurkateringbackendjava.auth.service.contract;

import azhue.katering.azhurkateringbackendjava.auth.model.entity.User;

/**
 * Интерфейс для работы с email.
 * 
 * <p>Определяет операции для отправки кодов подтверждения
 * и верификации email адресов пользователей.</p>
 * 
 * @version 1.0.0
 */
public interface EmailService {

    /**
     * Асинхронно отправляет код подтверждения
     */
    void sendVerificationCodeAsync(User user, String ipAddress);

    /**
     * Верифицирует email пользователя
     */
    void verifyEmail(String email, String code, String ipAddress, String userAgent);

    /**
     * Отправляет код подтверждения по email
     */
    void sendVerificationCode(String email, String ipAddress);
}
