package azhue.katering.azhurkateringbackendjava.menu.exception.category;

/**
 * Исключение, возникающее когда нельзя удалить категорию, содержащую блюда
 *
 * @version 1.0.0
 */
public class CategoryHasDishesException extends RuntimeException {

    public CategoryHasDishesException(String message) {
        super(message);
    }
}
