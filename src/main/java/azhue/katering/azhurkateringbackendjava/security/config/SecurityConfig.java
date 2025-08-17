package azhue.katering.azhurkateringbackendjava.security.config;

import azhue.katering.azhurkateringbackendjava.security.filter.JwtAuthenticationFilter;
import azhue.katering.azhurkateringbackendjava.security.filter.SecurityHeadersFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Конфигурация безопасности приложения.
 * 
 * <p>Настраивает Spring Security для работы с JWT аутентификацией,
 * определяет доступные endpoints и настраивает фильтры безопасности.</p>
 * 
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final SecurityHeadersFilter securityHeadersFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * Настраивает цепочку фильтров безопасности
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .authorizeHttpRequests(auth -> auth
                // Публичные endpoints
                .requestMatchers(
                        "/api/v1/auth/login",
                        "/api/v1/auth/register",
                        "/api/v1/auth/send-verification",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/verify-email",
                        "/actuator/health",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/webjars/**",
                        "/swagger-resources/**"
                ).permitAll()
                // Endpoints для аутентифицированных пользователей
                .requestMatchers("/api/v1/auth/me", "/api/v1/auth/logout").authenticated()
                // Endpoints для админов
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Все остальные запросы требуют аутентификации
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


}