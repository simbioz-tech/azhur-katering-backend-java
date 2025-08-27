package azhue.katering.azhurkateringbackendjava.menu.exception.category;

/**
 * Исключение, возникающее когда категория не найдена
 *
 * @version 1.0.0
 */
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String message) {
        super(message);
    }
}
