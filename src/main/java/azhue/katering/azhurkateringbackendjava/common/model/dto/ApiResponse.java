package azhue.katering.azhurkateringbackendjava.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Стандартизированный ответ API.
 * 
 * <p>Используется для всех эндпоинтов для обеспечения единообразного
 * формата ответов с информацией об успешности операции.</p>
 * 
 * @param <T> тип данных в ответе
 * @version 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    name = "ApiResponse",
    description = "Стандартизированный ответ API",
    example = """
    {
        "success": true,
        "message": "Операция выполнена успешно",
        "data": {
            "id": "123e4567-e89b-12d3-a456-426614174000",
            "name": "Пример данных"
        },
        "errorCode": null,
        "timestamp": "2025-08-16T21:00:41"
    }
    """
)
public class ApiResponse<T> {

    /**
     * Флаг успешности операции
     */
    @Schema(
        description = "Флаг успешности операции",
        example = "true"
    )
    private Boolean success;

    /**
     * Сообщение о результате операции
     */
    @Schema(
        description = "Сообщение о результате операции",
        example = "Операция выполнена успешно"
    )
    private String message;

    /**
     * Данные ответа
     */
    @Schema(
        description = "Данные ответа",
        example = """
        {
            "id": "123e4567-e89b-12d3-a456-426614174000",
            "name": "Пример данных"
        }
        """
    )
    private T data;

    /**
     * Код ошибки
     */
    @Schema(
        description = "Код ошибки (null в случае успеха)",
        example = "VALIDATION_ERROR",
        nullable = true
    )
    private String errorCode;

    /**
     * Временная метка ответа
     */
    @Schema(
        description = "Временная метка ответа",
        example = "2025-08-16T21:00:41",
        format = "date-time"
    )
    private LocalDateTime timestamp;

    /**
     * Создает успешный ответ с данными
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Создает успешный ответ без данных
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Создает ответ с ошибкой
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Создает ответ с ошибкой без кода
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}