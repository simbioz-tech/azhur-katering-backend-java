package azhue.katering.azhurkateringbackendjava.auth.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса регистрации нового пользователя.
 * 
 * <p>Содержит данные, необходимые для создания нового аккаунта в системе.
 * Все поля проходят валидацию на стороне сервера.</p>
 * 
 * @version 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    name = "RegisterRequest",
    description = "Данные для регистрации нового пользователя",
    example = """
    {
        "username": "ivan_petrov",
        "email": "ivan@example.com",
        "password": "Pass123!"
    }
    """
)
public class RegisterRequest {

    @NotBlank(message = "Имя пользователя обязательно для заполнения")
    @Size(min = 3, max = 30, message = "Имя пользователя должно содержать от 3 до 30 символов")
    @Pattern(
        regexp = "^[^\\s].*[^\\s]$",
        message = "Имя пользователя не может начинаться или заканчиваться пробелом"
    )
    @Pattern(
        regexp = "^[a-zA-Zа-яА-Я0-9\\-_\\s]+$",
        message = "Имя пользователя может содержать только буквы, цифры, тире, подчеркивания и пробелы"
    )
    @Schema(
        description = "Уникальное имя пользователя",
        example = "ivan_petrov",
        minLength = 3,
        maxLength = 30,
        pattern = "^[a-zA-Zа-яА-Я0-9\\-_\\s]+$"
    )
    private String username;

    @NotBlank(message = "Email обязателен для заполнения")
    @Email(message = "Некорректный формат email адреса")
    @Size(max = 255, message = "Email не может превышать 255 символов")
    @Schema(
        description = "Email адрес пользователя (используется для входа в систему)",
        example = "ivan@example.com",
        maxLength = 255,
        format = "email"
    )
    private String email;

    @NotBlank(message = "Пароль обязателен для заполнения")
    @Size(min = 8, max = 100, message = "Пароль должен содержать от 8 до 100 символов")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$",
        message = "Пароль должен содержать хотя бы одну букву и одну цифру"
    )
    @Schema(
        description = "Пароль пользователя (минимум 8 символов, должен содержать буквы и цифры)",
        example = "SecurePass123!",
        minLength = 8,
        maxLength = 100,
        pattern = "^(?=.*[a-zA-Z])(?=.*\\d).+$"
    )
    private String password;
}