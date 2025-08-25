package azhue.katering.azhurkateringbackendjava.auth.exception.token;

/**
 * Исключение для неправильного типа токена.
 * 
 * <p>Выбрасывается когда используется токен неправильного типа,
 * например, access token вместо refresh token.</p>
 * 
 * @version 1.0.0
 */
public class TypeTokenException extends RuntimeException {

    /**
     * Создает исключение с сообщением об ошибке
     */
    public TypeTokenException(String message) {
        super(message);
    }
}
