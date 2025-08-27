package azhue.katering.azhurkateringbackendjava.menu.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO для ответа с категорией и её блюдами
 *
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryWithDishesResponse {
    
    private UUID id;

    private String name;

    private Boolean isActive;

    private List<DishResponse> dishes;

    private Integer dishCount;
}
