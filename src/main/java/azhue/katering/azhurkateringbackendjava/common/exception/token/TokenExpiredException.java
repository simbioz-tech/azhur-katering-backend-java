package azhue.katering.azhurkateringbackendjava.common.exception.token;

/**
 * Исключение для истекшего токена.
 * 
 * <p>Выбрасывается когда JWT токен превысил время жизни
 * и больше не может быть использован.</p>
 * 
 * @version 1.0.0
 */
public class TokenExpiredException extends RuntimeException {

    /**
     * Создает исключение с сообщением об ошибке
     */
    public TokenExpiredException(String message) {
        super(message);
    }
}
