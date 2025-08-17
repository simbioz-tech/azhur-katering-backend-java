package azhue.katering.azhurkateringbackendjava.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * Конфигурация асинхронных операций и планировщика задач.
 * 
 * <p>Настраивает пулы потоков для асинхронных операций и планировщик
 * для выполнения задач по расписанию.</p>
 * 
 * @version 1.0.0
 */
@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
public class SchedulingConfig implements AsyncConfigurer {

    /**
     * Создает пул потоков для асинхронных операций
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }

    /**
     * Возвращает обработчик исключений для асинхронных методов
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }

    /**
     * Создает планировщик задач
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        return scheduler;
    }

    /**
     * Обработчик исключений для асинхронных методов
     */
    @Slf4j
    public static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        /**
         * Обрабатывает необработанные исключения в асинхронных методах
         */
        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            log.error("Async method '{}' threw exception: {}", method.getName(), ex.getMessage());
            log.error("Exception details:", ex);
            
            // Логируем параметры метода для отладки
            if (params != null && params.length > 0) {
                log.error("Method parameters:");
                for (int i = 0; i < params.length; i++) {
                    if (params[i] != null) {
                        log.error("  Param {}: {} ({})", i, params[i], params[i].getClass().getSimpleName());
                    } else {
                        log.error("  Param {}: null", i);
                    }
                }
            }
        }
    }
}