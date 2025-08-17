package azhue.katering.azhurkateringbackendjava.auth.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса входа в систему.
 * 
 * <p>Содержит учетные данные пользователя для аутентификации.
 * При успешной аутентификации система создает JWT токены и
 * сохраняет их в HTTP-only cookies.</p>
 * 
 * @version 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    name = "LoginRequest",
    description = "Учетные данные для входа в систему",
    example = """
    {
        "email": "ivan@example.com",
        "password": "Pass123!"
    }
    """
)
public class LoginRequest {

    @NotBlank(message = "Email обязателен для заполнения")
    @Email(message = "Некорректный формат email адреса")
    @Size(max = 255, message = "Email не может превышать 255 символов")
    @Schema(
        description = "Email адрес пользователя",
        example = "ivan@example.com",
        maxLength = 255,
        format = "email"
    )
    private String email;

    @NotBlank(message = "Пароль обязателен для заполнения")
    @Size(min = 1, max = 100, message = "Пароль не может быть пустым и не должен превышать 100 символов")
    @Schema(
        description = "Пароль пользователя",
        example = "Pass123!",
        minLength = 1,
        maxLength = 100
    )
    private String password;
}