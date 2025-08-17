package azhue.katering.azhurkateringbackendjava.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
  * Конфигурация ограничения скорости запросов.
 * 
 * <p>Создает различные rate limiter'ы для защиты от злоупотреблений
 * и DDoS атак.</p>
 * 
 * @version 1.0.0
 */
@Configuration
public class RateLimitConfig {

    /**
     * Rate limiter для аутентификации (логин/регистрация)
     * 5 попыток в минуту
     */
    @Bean("authRateLimiter")
    public Bucket authRateLimiter() {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Rate limiter для email верификации
     * 3 попытки в 5 минут
     */
    @Bean("emailVerificationRateLimiter")
    public Bucket emailVerificationRateLimiter() {
        Bandwidth limit = Bandwidth.classic(3, Refill.greedy(3, Duration.ofMinutes(5)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Rate limiter для refresh токенов
     * 10 попыток в минуту
     */
    @Bean("refreshTokenRateLimiter")
    public Bucket refreshTokenRateLimiter() {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Rate limiter для общих API endpoints
     * 100 запросов в минуту
     */
    @Bean("generalApiRateLimiter")
    public Bucket generalApiRateLimiter() {
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Rate limiter для смены пароля
     * 3 попытки в час
     */
    @Bean("passwordChangeRateLimiter")
    public Bucket passwordChangeRateLimiter() {
        Bandwidth limit = Bandwidth.classic(3, Refill.greedy(3, Duration.ofHours(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
