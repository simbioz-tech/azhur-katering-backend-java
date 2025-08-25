package azhue.katering.azhurkateringbackendjava.auth.exception.account;

public class UsernameExistsException extends RuntimeException {
    public UsernameExistsException(String message) {
        super(message);
    }
}
