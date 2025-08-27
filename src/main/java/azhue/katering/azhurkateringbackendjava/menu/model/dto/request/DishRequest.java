package azhue.katering.azhurkateringbackendjava.menu.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для запроса создания блюда
 *
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishRequest {
    
    @NotBlank(message = "Название блюда обязательно")
    @Size(min = 2, max = 200, message = "Название блюда должно быть от 2 до 200 символов")
    private String name;
    
    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;
    
    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "0.0", inclusive = false, message = "Цена должна быть больше 0")
    private BigDecimal price;
    
    @NotNull(message = "Категория обязательна")
    private UUID categoryId;
    
    private Boolean isAvailable;
}
