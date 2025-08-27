package azhue.katering.azhurkateringbackendjava.menu.service.contract;

import azhue.katering.azhurkateringbackendjava.menu.model.dto.request.DishRequest;
import azhue.katering.azhurkateringbackendjava.menu.model.dto.response.DishResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Интерфейс сервиса для работы с блюдами
 *
 * @version 1.0.0
 */
public interface DishService {
    
    /**
     * Получить все блюда
     */
    Page<DishResponse> getAllDishes(Pageable pageable);
    
    /**
     * Получить все доступные блюда
     */
    List<DishResponse> getAvailableDishes();
    
    /**
     * Получить блюдо по ID
     */
    DishResponse getDishById(UUID id);
    
    /**
     * Создать новое блюдо
     */
    DishResponse createDish(DishRequest request, MultipartFile file) throws IOException;
    
    /**
     * Обновить блюдо
     */
    DishResponse updateDish(UUID id, DishRequest request, MultipartFile file) throws IOException;
    
    /**
     * Удалить блюдо
     */
    void deleteDish(UUID id);
    
    /**
     * Изменить статус доступности блюда
     */
    DishResponse toggleDishAvailability(UUID id);

    /**
     * Получить блюда по категории
     */
    Page<DishResponse> getDishesByCategory(UUID categoryId, Pageable pageable);

    /**
     * Поиск блюд по названию
     */
    List<DishResponse> searchDishesByName(String name);

    /**
     * Поиск блюд с фильтрами и пагинацией
     */
    Page<DishResponse> searchDishes(UUID categoryId, Boolean isAvailable, Pageable pageable);
}
