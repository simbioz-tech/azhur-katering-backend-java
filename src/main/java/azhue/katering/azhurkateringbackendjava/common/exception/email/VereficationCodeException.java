package azhue.katering.azhurkateringbackendjava.common.exception.email;

/**
 * Исключение для кода верификации.
 * 
 * <p>Выбрасывается когда код верификации неверный,
 * истек или не найден в базе данных.</p>
 * 
 * @version 1.0.0
 */
public class VereficationCodeException extends RuntimeException {
    
    /**
     * Создает исключение с сообщением об ошибке
     */
    public VereficationCodeException(String message) {
        super(message);
    }
}
