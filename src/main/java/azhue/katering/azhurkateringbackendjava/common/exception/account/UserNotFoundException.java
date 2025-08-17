package azhue.katering.azhurkateringbackendjava.common.exception.account;

/**
 * Исключение когда пользователь не найден.
 * 
 * <p>Выбрасывается при попытке найти пользователя по email или ID,
 * который не существует в системе.</p>
 * 
 * @version 1.0.0
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Создает исключение с сообщением об ошибке
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * Создает исключение с сообщением и причиной
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}