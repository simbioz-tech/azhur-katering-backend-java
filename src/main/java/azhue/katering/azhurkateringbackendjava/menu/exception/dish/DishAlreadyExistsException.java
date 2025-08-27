package azhue.katering.azhurkateringbackendjava.menu.exception.dish;

public class DishAlreadyExistsException extends RuntimeException {
    public DishAlreadyExistsException(String message) {
        super(message);
    }
}
