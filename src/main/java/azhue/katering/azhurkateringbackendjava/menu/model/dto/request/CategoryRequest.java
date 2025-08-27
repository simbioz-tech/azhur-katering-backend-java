package azhue.katering.azhurkateringbackendjava.menu.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса создания категории
 *
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    
    @NotBlank(message = "Название категории обязательно")
    @Size(min = 2, max = 100, message = "Название категории должно быть от 2 до 100 символов")
    private String name;

    private Boolean isActive;
}
