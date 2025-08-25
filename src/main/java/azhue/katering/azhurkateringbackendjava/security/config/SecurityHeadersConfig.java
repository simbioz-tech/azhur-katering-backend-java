package azhue.katering.azhurkateringbackendjava.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Конфигурация заголовков безопасности.
 * 
 * <p>Создает заголовки для защиты от различных типов атак:
 * XSS, clickjacking, MIME sniffing и других.</p>
 * 
 * @version 1.0.0
 */
@Configuration
public class SecurityHeadersConfig {

    /**
     * Заголовок X-Content-Type-Options для защиты от MIME sniffing
     */
    @Bean
    public HeaderWriter xContentTypeOptionsHeaderWriter() {
        return new StaticHeadersWriter("X-Content-Type-Options", "nosniff");
    }

    /**
     * Заголовок X-Frame-Options для защиты от clickjacking
     */
    @Bean
    public HeaderWriter xFrameOptionsHeaderWriter() {
        return new StaticHeadersWriter("X-Frame-Options", "DENY");
    }

    /**
     * Заголовок HSTS для принуждения HTTPS
     */
    @Bean
    public HeaderWriter hstsHeaderWriter() {
        return new StaticHeadersWriter("Strict-Transport-Security", 
                "max-age=31536000; includeSubDomains; preload");
    }

    /**
     * Заголовок Referrer-Policy для контроля referrer информации
     */
    @Bean
    public HeaderWriter referrerPolicyHeaderWriter() {
        return new StaticHeadersWriter("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    /**
     * Заголовок Content-Security-Policy в режиме Report-Only
     * 
     * <p>В режиме Report-Only CSP не блокирует ресурсы, а только отправляет отчеты
     * о нарушениях. Это позволяет безопасно тестировать политику перед
     * включением в боевом режиме.</p>
     */
    @Bean
    public HeaderWriter contentSecurityPolicyHeaderWriter() {
        String csp = "default-src 'self'; " +
                "script-src 'self'; " +
                "style-src 'self'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self' data:; " +
                "connect-src 'self'; " +
                "frame-ancestors 'none'; " +
                "base-uri 'self'; " +
                "form-action 'self'";
        
        return new StaticHeadersWriter("Content-Security-Policy-Report-Only", csp);
    }

    /**
     * Заголовок Permissions-Policy для контроля API браузера
     */
    @Bean
    public HeaderWriter permissionsPolicyHeaderWriter() {
        String permissionsPolicy = "geolocation=(), " +
                "microphone=(), " +
                "camera=(), " +
                "payment=(), " +
                "usb=(), " +
                "magnetometer=(), " +
                "gyroscope=(), " +
                "accelerometer=()";
        
        return new StaticHeadersWriter("Permissions-Policy", permissionsPolicy);
    }

    /**
     * Заголовок Cache-Control для API endpoints
     */
    @Bean
    public HeaderWriter apiCacheControlHeaderWriter() {
        return new DelegatingRequestMatcherHeaderWriter(
                (RequestMatcher) request -> request.getRequestURI().startsWith("/api/v1/"),
                new StaticHeadersWriter("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
        );
    }

    /**
     * Заголовок Cache-Control для аутентификации
     */
    @Bean
    public HeaderWriter authCacheControlHeaderWriter() {
        return new DelegatingRequestMatcherHeaderWriter(
                (RequestMatcher) request -> request.getRequestURI().startsWith("/api/v1/auth/"),
                new StaticHeadersWriter("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
        );
    }
}
