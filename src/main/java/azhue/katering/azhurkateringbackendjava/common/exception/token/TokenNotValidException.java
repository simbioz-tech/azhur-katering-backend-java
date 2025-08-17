package azhue.katering.azhurkateringbackendjava.common.exception.token;

/**
 * Исключение для невалидного токена.
 * 
 * <p>Выбрасывается когда токен не прошел валидацию
 * или был скомпрометирован.</p>
 * 
 * @version 1.0.0
 */
public class TokenNotValidException extends RuntimeException {
    
    /**
     * Создает исключение с сообщением об ошибке
     */
    public TokenNotValidException(String message) {
        super(message);
    }
}
