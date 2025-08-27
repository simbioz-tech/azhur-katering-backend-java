package azhue.katering.azhurkateringbackendjava.menu.exception.dish;

/**
 * Исключение, возникающее когда блюдо не найдено
 *
 * @version 1.0.0
 */
public class DishNotFoundException extends RuntimeException {
    
    public DishNotFoundException(String message) {
        super(message);
    }
}
