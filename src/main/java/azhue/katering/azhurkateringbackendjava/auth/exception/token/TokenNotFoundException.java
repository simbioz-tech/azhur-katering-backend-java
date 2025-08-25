package azhue.katering.azhurkateringbackendjava.auth.exception.token;

/**
 * Исключение для отсутствующего токена.
 * 
 * <p>Выбрасывается когда refresh token не найден
 * в базе данных или был отозван.</p>
 * 
 * @version 1.0.0
 */
public class TokenNotFoundException extends RuntimeException {
    
    /**
     * Создает исключение с сообщением об ошибке
     */
    public TokenNotFoundException(String message) {
        super(message);
    }
}
