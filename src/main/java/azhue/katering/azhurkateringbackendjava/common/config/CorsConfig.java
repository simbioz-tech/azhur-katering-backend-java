package azhue.katering.azhurkateringbackendjava.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Конфигурация CORS для кросс-доменных запросов.
 * 
 * <p>Настраивает разрешенные источники, методы и заголовки
 * для взаимодействия с фронтендом.</p>
 * 
 * @version 1.0.0
 */
@Configuration
public class CorsConfig {

    /**
     * Создает конфигурацию CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Разрешенные источники
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:3000",      // React dev server
            "http://localhost:8080",      // Spring Boot dev server
            "https://prod-url" // Production frontend
        ));
        
        // Разрешенные методы
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Разрешенные заголовки
        configuration.setAllowedHeaders(Arrays.asList(
            "Origin", "Content-Type", "Accept", "Authorization", 
            "X-Requested-With", "Cache-Control", "X-File-Name"
        ));
        
        // Разрешенные заголовки для экспозиции
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
        ));
        
        // Разрешить credentials
        configuration.setAllowCredentials(true);
        
        // Время кэширования preflight запросов
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}