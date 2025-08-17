package azhue.katering.azhurkateringbackendjava.auth.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа аутентификации.
 * 
 * <p>Содержит информацию о пользователе после успешной аутентификации.
 * JWT токены автоматически сохраняются в HTTP-only cookies для безопасности.</p>
 * 
 * @version 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    name = "AuthResponse",
    description = "Ответ на успешную аутентификацию пользователя",
    example = """
    {
        "userId": "123e4567-e89b-12d3-a456-426614174000",
        "email": "ivan@example.com",
        "username": "ivan_petrov",
        "role": "USER",
        "isVerified": true,
        "tokenType": "Bearer",
        "expiresIn": 900
    }
    """
)
public class AuthResponse {

    @Schema(
        description = "JWT токен доступа (сохраняется в HTTP-only cookie) и не передается в ответ.",
        example = "null",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String accessToken;

    @Schema(
        description = "JWT токен обновления (сохраняется в HTTP-only cookie) и не передается в ответ.",
        example = "null",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String refreshToken;

    @Schema(
        description = "Тип токена",
        example = "Bearer",
        defaultValue = "Bearer"
    )
    private String tokenType;

    @Schema(
        description = "Время жизни access токена в секундах",
        example = "900",
        minimum = "0"
    )
    private Long expiresIn;

    @Schema(
        description = "Уникальный идентификатор пользователя",
        example = "123e4567-e89b-12d3-a456-426614174000",
        format = "uuid"
    )
    private String userId;

    @Schema(
        description = "Email адрес пользователя",
        example = "ivan@example.com",
        format = "email"
    )
    private String email;

    @Schema(
        description = "Имя пользователя",
        example = "ivan_petrov"
    )
    private String username;

    @Schema(
        description = "Роль пользователя в системе",
        example = "USER",
        allowableValues = {"USER", "ADMIN"}
    )
    private String role;

    @Schema(
        description = "Статус подтверждения email адреса",
        example = "true"
    )
    private Boolean isVerified;

    @Schema(
        description = "Флаг необходимости верификации email",
        example = "false"
    )
    private Boolean requiresVerification;

    @Schema(
        description = "Сообщение о верификации email",
        example = "Email не верифицирован. Новый код подтверждения отправлен на вашу почту."
    )
    private String verificationMessage;
}