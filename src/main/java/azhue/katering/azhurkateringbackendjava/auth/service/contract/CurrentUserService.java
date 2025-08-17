package azhue.katering.azhurkateringbackendjava.auth.service.contract;

import azhue.katering.azhurkateringbackendjava.auth.model.entity.User;
import org.springframework.security.core.Authentication;

import java.util.UUID;

/**
 * Интерфейс для работы с текущим пользователем.
 * 
 * <p>Предоставляет методы для получения информации о текущем
 * аутентифицированном пользователе из Spring Security контекста.</p>
 * 
 * @version 1.0.0
 */
public interface CurrentUserService {

    /**
     * Получает текущего пользователя из аутентификации
     */
    User getCurrentUser(Authentication authentication);

    /**
     * Получает ID текущего пользователя
     */
    UUID getCurrentUserId(Authentication authentication);

    /**
     * Проверяет аутентификацию пользователя
     */
    boolean isAuthenticated(Authentication authentication);

    /**
     * Проверяет аутентификацию с выбросом исключения
     */
    void requireAuthentication(Authentication authentication);
}
