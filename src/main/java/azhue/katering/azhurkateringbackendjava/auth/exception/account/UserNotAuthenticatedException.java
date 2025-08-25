package azhue.katering.azhurkateringbackendjava.auth.exception.account;

/**
 * Исключение для неаутентифицированного пользователя.
 * 
 * <p>Выбрасывается когда пользователь не прошел аутентификацию,
 * но пытается получить доступ к защищенным ресурсам.</p>
 * 
 * @version 1.0.0
 */
public class UserNotAuthenticatedException extends RuntimeException {
    
    /**
     * Создает исключение с сообщением об ошибке
     */
    public UserNotAuthenticatedException(String message) {
        super(message);
    }
    
    /**
     * Создает исключение с сообщением и причиной
     */
    public UserNotAuthenticatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
