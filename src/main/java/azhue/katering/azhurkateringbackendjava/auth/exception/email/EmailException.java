package azhue.katering.azhurkateringbackendjava.auth.exception.email;

/**
 * Исключение для email операций.
 * 
 * <p>Выбрасывается при ошибках отправки email:
 * проблемы с SMTP, неверные настройки, недоступность сервера.</p>
 * 
 * @version 1.0.0
 */
public class EmailException extends RuntimeException {

    /**
     * Создает исключение с сообщением об ошибке
     */
    public EmailException(String message) {
        super(message);
    }

    /**
     * Создает исключение с сообщением и причиной
     */
    public EmailException(String message, Throwable cause) {
        super(message, cause);
    }
}