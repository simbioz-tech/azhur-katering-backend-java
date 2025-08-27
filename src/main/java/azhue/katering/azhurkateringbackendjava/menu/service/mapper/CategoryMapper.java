package azhue.katering.azhurkateringbackendjava.menu.service.mapper;

import azhue.katering.azhurkateringbackendjava.menu.model.dto.response.CategoryResponse;
import azhue.katering.azhurkateringbackendjava.menu.model.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования между Category и CategoryResponse
 *
 * @version 1.0.0
 */
@Component
public class CategoryMapper {
    
    /**
     * Преобразовать Category в CategoryResponse
     */
    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }
        
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .isActive(category.getIsActive())
                .build();
    }
    
    /**
     * Преобразовать список Category в список CategoryResponse
     */
    public List<CategoryResponse> toResponseList(List<Category> categories) {
        if (categories == null) {
            return List.of();
        }
        
        return categories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
