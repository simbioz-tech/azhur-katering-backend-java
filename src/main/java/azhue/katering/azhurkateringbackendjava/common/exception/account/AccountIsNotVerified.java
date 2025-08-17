package azhue.katering.azhurkateringbackendjava.common.exception.account;

/**
 * Исключение для неподтвержденного аккаунта.
 * 
 * <p>Выбрасывается при попытке входа в аккаунт,
 * который не прошел email верификацию.</p>
 * 
 * @version 1.0.0
 */
public class AccountIsNotVerified extends RuntimeException {
    
    /**
     * Создает исключение с сообщением об ошибке
     */
    public AccountIsNotVerified(String message) {
        super(message);
    }
}
