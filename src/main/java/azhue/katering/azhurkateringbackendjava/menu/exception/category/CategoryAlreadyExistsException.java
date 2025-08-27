package azhue.katering.azhurkateringbackendjava.menu.exception.category;

/**
 * Исключение, возникающее когда категория уже существует
 *
 * @version 1.0.0
 */
public class CategoryAlreadyExistsException extends RuntimeException {
    
    public CategoryAlreadyExistsException(String message) {
        super(message);
    }
}
