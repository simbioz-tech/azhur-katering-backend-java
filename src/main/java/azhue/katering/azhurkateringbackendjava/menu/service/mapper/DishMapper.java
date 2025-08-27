package azhue.katering.azhurkateringbackendjava.menu.service.mapper;

import azhue.katering.azhurkateringbackendjava.menu.model.dto.response.DishResponse;
import azhue.katering.azhurkateringbackendjava.menu.model.entity.Dish;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования между Dish и DishResponse
 * Оптимизирован для работы с S3 изображениями
 *
 * @version 1.0.0
 */
@Component
public class DishMapper {
    
    private final CategoryMapper categoryMapper;

    public DishMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }
    
    /**
     * Преобразовать Dish в DishResponse
     */
    public DishResponse toResponse(Dish dish) {
        if (dish == null) {
            return null;
        }
        
        // Используем прямые S3 URLs
        String imageUrl = dish.getImageUrl();

        return DishResponse.builder()
                .id(dish.getId())
                .name(dish.getName())
                .description(dish.getDescription())
                .price(dish.getPrice())
                .category(categoryMapper.toResponse(dish.getCategory()))
                .imageUrl(imageUrl)
                .thumbnailUrl(dish.getThumbnailUrl())
                .isAvailable(dish.getIsAvailable())
                .build();
    }
    
    /**
     * Преобразовать список Dish в список DishResponse
     */
    public List<DishResponse> toResponseList(List<Dish> dishes) {
        if (dishes == null) {
            return List.of();
        }
        
        return dishes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
