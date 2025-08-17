package azhue.katering.azhurkateringbackendjava.auth.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса смены пароля.
 * 
 * <p>Содержит текущий и новый пароль пользователя.
 * После смены пароля все существующие токены отзываются,
 * и пользователю выдаются новые.</p>
 * 
 * @version 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    name = "ChangePasswordRequest",
    description = "Данные для смены пароля пользователя",
    example = """
    {
        "oldPassword": "OldPass123!",
        "newPassword": "NewSecurePass456!"
    }
    """
)
public class ChangePasswordRequest {

    @NotBlank(message = "Текущий пароль обязателен для заполнения")
    @Size(min = 1, max = 100, message = "Пароль не может быть пустым и не должен превышать 100 символов")
    @Schema(
        description = "Текущий пароль пользователя",
        example = "OldPass123!",
        minLength = 1,
        maxLength = 100
    )
    private String oldPassword;

    @NotBlank(message = "Новый пароль обязателен для заполнения")
    @Size(min = 8, max = 100, message = "Новый пароль должен содержать от 8 до 100 символов")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$",
        message = "Новый пароль должен содержать хотя бы одну букву и одну цифру"
    )
    @Schema(
        description = "Новый пароль пользователя (минимум 8 символов, должен содержать буквы и цифры)",
        example = "NewPass456!",
        minLength = 8,
        maxLength = 100,
        pattern = "^(?=.*[a-zA-Z])(?=.*\\d).+$"
    )
    private String newPassword;
}
