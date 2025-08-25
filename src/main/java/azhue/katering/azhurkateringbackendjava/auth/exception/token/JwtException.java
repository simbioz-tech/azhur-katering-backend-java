package azhue.katering.azhurkateringbackendjava.auth.exception.token;

/**
 * Исключение для JWT операций.
 * 
 * <p>Выбрасывается при ошибках работы с JWT токенами:
 * генерация, валидация, парсинг и другие операции.</p>
 * 
 * @version 1.0.0
 */
public class JwtException extends RuntimeException {

    /**
     * Создает исключение с сообщением об ошибке
     */
    public JwtException(String message) {
        super(message);
    }

    /**
     * Создает исключение с сообщением и причиной
     */
    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }
}