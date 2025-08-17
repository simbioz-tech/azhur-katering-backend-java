package azhue.katering.azhurkateringbackendjava.auth.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса обновления токена.
 * 
 * <p>Содержит refresh токен для получения новой пары токенов доступа.
 * Refresh токен обычно извлекается из HTTP-only cookie.</p>
 * 
 * @version 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    name = "RefreshTokenRequest",
    description = "Запрос на обновление токена доступа",
    example = """
    {
        "refreshToken": "refresh_token",
    }
    """
)
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh токен обязателен для заполнения")
    @Size(max = 1000, message = "Refresh токен не может превышать 1000 символов")
    @Schema(
        description = "JWT refresh токен для обновления access токена",
        example = "refresh_token",
        maxLength = 1000
    )
    private String refreshToken;
}