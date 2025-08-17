package azhue.katering.azhurkateringbackendjava.common.exception.account;

/**
 * Исключение для неправильного пароля.
 * 
 * <p>Выбрасывается при попытке ввода неправильного пароля.</p>
 * 
 * @version 1.0.0
 */
public class IncorrectPasswordException extends RuntimeException {
    
    /**
     * Создает исключение с сообщением об ошибке
     */
    public IncorrectPasswordException(String message) {
        super(message);
    }
}
