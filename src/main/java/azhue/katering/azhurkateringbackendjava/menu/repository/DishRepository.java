package azhue.katering.azhurkateringbackendjava.menu.repository;

import azhue.katering.azhurkateringbackendjava.menu.model.entity.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с блюдами
 *
 * @version 1.0.0
 */
@Repository
public interface DishRepository extends JpaRepository<Dish, UUID> {
    
    /**
     * Найти блюда по категории
     */
    @Query("SELECT d FROM Dish d " +
            "LEFT JOIN FETCH d.category c " +
            "WHERE (:categoryId IS NULL OR d.category.id = :categoryId)"
    )
    Page<Dish> findByCategoryId(@Param("categoryId") UUID categoryId,
                                Pageable pageable);
    
    /**
     * Найти все доступные блюда
     */
    List<Dish> findByIsAvailableTrue();
    
    /**
     * Поиск блюд по названию (содержит)
     */
    @Query("SELECT d FROM Dish d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Dish> findByNameContainingIgnoreCase(@Param("name") String name);
    
    /**
     * Поиск блюд с пагинацией
     */
    @Query("SELECT d FROM Dish d " +
           "LEFT JOIN FETCH d.category c " +
           "WHERE (:categoryId IS NULL OR d.category.id = :categoryId) " +
           "AND (:isAvailable IS NULL OR d.isAvailable = :isAvailable)")
    Page<Dish> findByFilters(@Param("categoryId") UUID categoryId, 
                            @Param("isAvailable") Boolean isAvailable, 
                            Pageable pageable);
    
    /**
     * Проверить существование блюд в категории
     */
    boolean existsByCategoryId(UUID categoryId);
}
