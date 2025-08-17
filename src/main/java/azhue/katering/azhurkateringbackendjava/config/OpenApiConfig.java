package azhue.katering.azhurkateringbackendjava.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Конфигурация OpenAPI для Swagger UI.
 * 
 * <p>Настраивает документацию API, включая:</p>
 * <ul>
 *   <li>Информацию о проекте и API</li>
 *   <li>Схемы безопасности (JWT)</li>
 *   <li>Серверы для разных окружений</li>
 *   <li>Контактную информацию</li>
 * </ul>
 * 
 * <p>Документация доступна по адресу: http://localhost:8080/swagger-ui.html</p>
 * 
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Настраивает OpenAPI документацию.
     * 
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .components(components())
                .addSecurityItem(new SecurityRequirement().addList("JWT"));
    }

    /**
     * Создает информацию о API.
     */
    private Info apiInfo() {
        return new Info()
                .title("Azhur Katering API")
                .description("""
                    REST API для системы кейтеринга Azhur.
                    
                    ## Основные возможности:
                    - **Аутентификация и авторизация** - регистрация, вход, управление токенами
                    - **Управление пользователями** - профили, роли, верификация email
                    - **Безопасность** - JWT токены, rate limiting, валидация данных
                    
                    ## Аутентификация:
                    API использует JWT токены для аутентификации. Токены автоматически
                    сохраняются в HTTP-only cookies для безопасности.
                    
                    ## Rate Limiting:
                    Все эндпоинты защищены от злоупотреблений с помощью rate limiting.
                    
                    ## Валидация:
                    Все входные данные проходят строгую валидацию на стороне сервера.
                    """)
                .version("1.0.0")
                .contact(contact());
    }

    /**
     * Создает контактную информацию.
     */
    private Contact contact() {
        return new Contact()
                .name("Azhur Katering Team")
                .email("email")
                .url("https://prod-url");
    }

    /**
     * Настраивает серверы для разных окружений.
     */
    private List<Server> servers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("Локальный сервер разработки"),
                new Server()
                        .url("https://prod-url")
                        .description("Продакшн сервер")
        );
    }

    /**
     * Настраивает компоненты OpenAPI.
     */
    private Components components() {
        return new Components()
                .addSecuritySchemes("JWT", jwtSecurityScheme());
    }

    /**
     * Создает схему безопасности для JWT токенов.
     */
    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                    JWT токен для аутентификации.
                    
                    Токены автоматически сохраняются в HTTP-only cookies.
                    Для API тестирования можно использовать заголовок Authorization:
                    `Authorization: Bearer <your-jwt-token>`
                    """);
    }
}