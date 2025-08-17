package azhue.katering.azhurkateringbackendjava.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для ограничения частоты запросов.
 * 
 * <p>Применяется к методам контроллеров для защиты от злоупотреблений.
 * Используется вместе с RateLimitAspect для проверки лимитов.</p>
 * 
 * @version 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * Имя rate limiter bean'а
     */
    String value();
    
    /**
     * Сообщение об ошибке при превышении лимита
     */
    String message() default "Слишком много запросов, попробуйте позже.";
}
