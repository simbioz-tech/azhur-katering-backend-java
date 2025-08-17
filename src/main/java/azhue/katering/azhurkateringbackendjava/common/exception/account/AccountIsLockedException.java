package azhue.katering.azhurkateringbackendjava.common.exception.account;

/**
 * Исключение для заблокированного аккаунта.
 * 
 * <p>Выбрасывается при попытке входа в заблокированный аккаунт
 * из-за множественных неудачных попыток аутентификации.</p>
 * 
 * @version 1.0.0
 */
public class AccountIsLockedException extends RuntimeException {

    /**
     * Создает исключение с сообщением об ошибке
     */
    public AccountIsLockedException(String message) {
        super(message);
    }
}
