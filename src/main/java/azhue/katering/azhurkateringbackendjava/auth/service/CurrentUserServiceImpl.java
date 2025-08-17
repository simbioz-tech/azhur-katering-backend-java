package azhue.katering.azhurkateringbackendjava.auth.service;

import azhue.katering.azhurkateringbackendjava.auth.model.entity.User;
import azhue.katering.azhurkateringbackendjava.auth.service.contract.CurrentUserService;
import azhue.katering.azhurkateringbackendjava.common.exception.account.UserNotAuthenticatedException;
import azhue.katering.azhurkateringbackendjava.common.exception.account.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Сервис для работы с текущим пользователем.
 * 
 * <p>Предоставляет методы для получения информации о текущем
 * аутентифицированном пользователе из Spring Security контекста.</p>
 * 
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurrentUserServiceImpl implements CurrentUserService {

    /**
     * Получает текущего пользователя из аутентификации
     */
    @Override
    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotAuthenticatedException("Пользователь не аутентифицирован");
        }

        if (authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }

        throw new UserNotFoundException("Не удалось получить информацию о пользователе");
    }

    /**
     * Получает ID текущего пользователя
     */
    @Override
    public UUID getCurrentUserId(Authentication authentication) {
        return getCurrentUser(authentication).getId();
    }

    /**
     * Проверяет аутентификацию пользователя
     */
    @Override
    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Проверяет аутентификацию с выбросом исключения
     */
    @Override
    public void requireAuthentication(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            throw new UserNotAuthenticatedException("Пользователь не аутентифицирован");
        }
    }
}
