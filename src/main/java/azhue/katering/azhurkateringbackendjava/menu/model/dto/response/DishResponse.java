package azhue.katering.azhurkateringbackendjava.menu.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для ответа с данными блюда
 *
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishResponse {
    
    private UUID id;

    private String name;

    private String description;

    private BigDecimal price;

    private CategoryResponse category;
    
    private String imageUrl;

    private String thumbnailUrl;

    private Boolean isAvailable;
}
