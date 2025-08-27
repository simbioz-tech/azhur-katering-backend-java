package azhue.katering.azhurkateringbackendjava.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурация кэширования с Redis
 * 
 * <p>Настраивает Redis кэширование для улучшения производительности API.
 * Использует Redis для распределенного кэширования в продакшене.</p>
 * 
 * @version 1.0.0
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Настройка менеджера кэша с Redis
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        log.info("Инициализация Redis кэш-менеджера");

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15)) // TTL по умолчанию 15 минут
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();
        
        // Специфичные конфигурации для разных кэшей
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Кэш доступных блюд - 15 минут
        cacheConfigurations.put("available-dishes", 
                defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Кэш категорий - 30 минут (менее часто изменяются)
        cacheConfigurations.put("categories", 
                defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Кэш пользовательских сессий - 1 час
        cacheConfigurations.put("user-sessions", 
                defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Кэш изображений - 24 часа (статические данные)
        cacheConfigurations.put("images", 
                defaultConfig.entryTtl(Duration.ofHours(24)));
        
        RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
        
        log.info("Redis кэш-менеджер инициализирован с кэшами: {}", cacheManager.getCacheNames());
        
        return cacheManager;
    }
}
