package azhue.katering.azhurkateringbackendjava.menu.service.contract;

import azhue.katering.azhurkateringbackendjava.menu.model.dto.request.CategoryRequest;
import azhue.katering.azhurkateringbackendjava.menu.model.dto.response.CategoryResponse;

import java.util.List;
import java.util.UUID;

/**
 * Интерфейс сервиса для работы с категориями
 *
 * @version 1.0.0
 */
public interface CategoryService {
    
    /**
     * Получить все категории
     */
    List<CategoryResponse> getAllCategories();
    
    /**
     * Получить все активные категории
     */
    List<CategoryResponse> getActiveCategories();

    /**
     * Создать новую категорию
     */
    CategoryResponse createCategory(CategoryRequest request);
    
    /**
     * Обновить категорию
     */
    CategoryResponse updateCategory(UUID id, CategoryRequest request);
    
    /**
     * Удалить категорию
     */
    void deleteCategory(UUID id);
    
    /**
     * Активировать/деактивировать категорию
     */
    void toggleCategoryStatus(UUID id);
}
