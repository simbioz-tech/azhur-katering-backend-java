package azhue.katering.azhurkateringbackendjava.common.exception.general;

/**
 * Исключение при превышении лимита запросов.
 * 
 * <p>Выбрасывается когда пользователь превысил допустимое количество
 * запросов в единицу времени для защиты от злоупотреблений.</p>
 * 
 * @version 1.0.0
 */
public class RateLimitExceededException extends RuntimeException {
    
    /**
     * Создает исключение с сообщением об ошибке
     */
    public RateLimitExceededException(String message) {
        super(message);
    }
    
    /**
     * Создает исключение с сообщением и причиной
     */
    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
