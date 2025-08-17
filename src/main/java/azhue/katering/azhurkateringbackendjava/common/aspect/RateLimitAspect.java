package azhue.katering.azhurkateringbackendjava.common.aspect;

import azhue.katering.azhurkateringbackendjava.common.annotation.RateLimit;
import azhue.katering.azhurkateringbackendjava.common.exception.general.RateLimitExceededException;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Аспект для ограничения частоты запросов.
 * 
 * <p>Перехватывает вызовы методов с аннотацией @RateLimit и проверяет
 * лимиты запросов с помощью Bucket4j.</p>
 * 
 * @version 1.0.0
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    private final ApplicationContext applicationContext;

    /**
     * Проверяет лимит запросов перед выполнением метода
     */
    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String bucketName = rateLimit.value();
        Bucket bucket = applicationContext.getBean(bucketName, Bucket.class);
        
        if (bucket.tryConsume(1)) {
            log.debug("Rate limit check passed for method: {}", joinPoint.getSignature().getName());
            return joinPoint.proceed();
        } else {
            log.warn("Rate limit exceeded for method: {} with bucket: {}", 
                    joinPoint.getSignature().getName(), bucketName);
            throw new RateLimitExceededException(rateLimit.message());
        }
    }
}
