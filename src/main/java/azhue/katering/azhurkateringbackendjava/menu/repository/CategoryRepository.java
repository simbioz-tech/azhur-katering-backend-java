package azhue.katering.azhurkateringbackendjava.menu.repository;

import azhue.katering.azhurkateringbackendjava.menu.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с категориями
 *
 * @version 1.0.0
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    
    /**
     * Найти категорию по названию
     */
    Optional<Category> findByName(String name);
    
    /**
     * Найти все активные категории
     */
    List<Category> findByIsActiveTrue();
    
    /**
     * Проверить существование категории по названию
     */
    boolean existsByName(String name);
    
    /**
     * Проверить существование категории по названию, исключая указанную
     */
    boolean existsByNameAndIdNot(String name, UUID id);
    
    /**
     * Найти категории с блюдами (для меню)
     */
    @Query("SELECT DISTINCT c FROM Category c " +
           "LEFT JOIN FETCH c.dishes d " +
           "WHERE c.isActive = true " +
           "ORDER BY c.name")
    List<Category> findActiveCategoriesWithDishes();
}
