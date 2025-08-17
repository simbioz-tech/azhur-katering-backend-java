package azhue.katering.azhurkateringbackendjava.common.exception.email;

/**
 * Исключение для уже верифицированного аккаунта.
 * 
 * <p>Выбрасывается при попытке повторной верификации
 * уже подтвержденного email адреса.</p>
 * 
 * @version 1.0.0
 */
public class VerifiedException extends RuntimeException {
    
    /**
     * Создает исключение с сообщением об ошибке
     */
    public VerifiedException(String message) {
        super(message);
    }
}
