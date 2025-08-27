package azhue.katering.azhurkateringbackendjava.common.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MetricsService {

    private final Counter loginAttemptsCounter;
    private final Counter successfulLoginsCounter;
    private final Counter failedLoginsCounter;
    private final Counter registrationsCounter;
    private final Counter emailVerificationsCounter;
    private final Counter emailsSentCounter;

    
    // Специфичные таймеры для каждой операции
    private final Timer emailProcessingTimer;
    private final Timer loginProcessingTimer;
    private final Timer registerProcessingTimer;
    private final Timer refreshTokenProcessingTimer;
    private final Timer logoutProcessingTimer;
    private final Timer changePasswordProcessingTimer;

    // Метрики для блюд
    private final Counter dishCreateCounter;
    private final Counter dishUpdateCounter;
    private final Counter dishDeleteCounter;
    private final Counter dishReadCounter;
    private final Counter dishAvailabilityToggleCounter;
    private final Counter dishSearchCounter;
    
    // Таймеры для блюд
    private final Timer dishCreateProcessingTimer;
    private final Timer dishUpdateProcessingTimer;
    private final Timer dishDeleteProcessingTimer;
    private final Timer dishReadProcessingTimer;
    private final Timer dishSearchProcessingTimer;
    
    // Метрики для изображений
    private final Counter imageUploadCounter;
    private final Counter imageDeleteCounter;
    private final Counter imageUpdateCounter;
    private final Counter imageProcessingErrorCounter;
    
    // Таймеры для изображений
    private final Timer imageUploadProcessingTimer;
    private final Timer imageDeleteProcessingTimer;
    private final Timer imageUpdateProcessingTimer;


    public MetricsService(MeterRegistry meterRegistry) {
        this.loginAttemptsCounter = Counter.builder("auth.login.attempts")
                .description("Total login attempts")
                .register(meterRegistry);
        
        this.successfulLoginsCounter = Counter.builder("auth.login.successful")
                .description("Successful login attempts")
                .register(meterRegistry);
        
        this.failedLoginsCounter = Counter.builder("auth.login.failed")
                .description("Failed login attempts")
                .register(meterRegistry);
        
        this.registrationsCounter = Counter.builder("auth.registrations")
                .description("Total user registrations")
                .register(meterRegistry);
        
        this.emailVerificationsCounter = Counter.builder("auth.email.verifications")
                .description("Email verification attempts")
                .register(meterRegistry);
        
        this.emailsSentCounter = Counter.builder("email.sent")
                .description("Total emails sent")
                .register(meterRegistry);
        
        this.emailProcessingTimer = Timer.builder("email.processing.time")
                .description("Email processing time")
                .register(meterRegistry);
        
        // Инициализация специфичных таймеров
        this.loginProcessingTimer = Timer.builder("auth.login.processing.time")
                .description("Login processing time")
                .register(meterRegistry);
        
        this.registerProcessingTimer = Timer.builder("auth.register.processing.time")
                .description("Registration processing time")
                .register(meterRegistry);
        
        this.refreshTokenProcessingTimer = Timer.builder("auth.refresh_token.processing.time")
                .description("Refresh token processing time")
                .register(meterRegistry);
        
        this.logoutProcessingTimer = Timer.builder("auth.logout.processing.time")
                .description("Logout processing time")
                .register(meterRegistry);
        
        this.changePasswordProcessingTimer = Timer.builder("auth.change_password.processing.time")
                .description("Change password processing time")
                .register(meterRegistry);
        
        // Инициализация метрик для блюд
        this.dishCreateCounter = Counter.builder("dish.create.total")
                .description("Total dishes created")
                .register(meterRegistry);
        
        this.dishUpdateCounter = Counter.builder("dish.update.total")
                .description("Total dishes updated")
                .register(meterRegistry);
        
        this.dishDeleteCounter = Counter.builder("dish.delete.total")
                .description("Total dishes deleted")
                .register(meterRegistry);
        
        this.dishReadCounter = Counter.builder("dish.read.total")
                .description("Total dish read operations")
                .register(meterRegistry);
        
        this.dishAvailabilityToggleCounter = Counter.builder("dish.availability.toggle.total")
                .description("Total dish availability toggles")
                .register(meterRegistry);
        
        this.dishSearchCounter = Counter.builder("dish.search.total")
                .description("Total dish search operations")
                .register(meterRegistry);
        
        // Инициализация таймеров для блюд
        this.dishCreateProcessingTimer = Timer.builder("dish.create.processing.time")
                .description("Dish creation processing time")
                .register(meterRegistry);
        
        this.dishUpdateProcessingTimer = Timer.builder("dish.update.processing.time")
                .description("Dish update processing time")
                .register(meterRegistry);
        
        this.dishDeleteProcessingTimer = Timer.builder("dish.delete.processing.time")
                .description("Dish deletion processing time")
                .register(meterRegistry);
        
        this.dishReadProcessingTimer = Timer.builder("dish.read.processing.time")
                .description("Dish read processing time")
                .register(meterRegistry);
        
        this.dishSearchProcessingTimer = Timer.builder("dish.search.processing.time")
                .description("Dish search processing time")
                .register(meterRegistry);
        
        // Инициализация метрик для изображений
        this.imageUploadCounter = Counter.builder("image.upload.total")
                .description("Total images uploaded")
                .register(meterRegistry);
        
        this.imageDeleteCounter = Counter.builder("image.delete.total")
                .description("Total images deleted")
                .register(meterRegistry);
        
        this.imageUpdateCounter = Counter.builder("image.update.total")
                .description("Total images updated")
                .register(meterRegistry);
        
        this.imageProcessingErrorCounter = Counter.builder("image.processing.errors")
                .description("Total image processing errors")
                .register(meterRegistry);
        
        // Инициализация таймеров для изображений
        this.imageUploadProcessingTimer = Timer.builder("image.upload.processing.time")
                .description("Image upload processing time")
                .register(meterRegistry);
        
        this.imageDeleteProcessingTimer = Timer.builder("image.delete.processing.time")
                .description("Image deletion processing time")
                .register(meterRegistry);
        
        this.imageUpdateProcessingTimer = Timer.builder("image.update.processing.time")
                .description("Image update processing time")
                .register(meterRegistry);
    }

    public void incrementLoginAttempts() {
        loginAttemptsCounter.increment();
    }

    public void incrementSuccessfulLogins() {
        successfulLoginsCounter.increment();
    }

    public void incrementFailedLogins() {
        failedLoginsCounter.increment();
    }

    public void incrementRegistrations() {
        registrationsCounter.increment();
    }

    public void incrementEmailVerifications() {
        emailVerificationsCounter.increment();
    }
    
    // Методы для специфичных таймеров
    public Timer.Sample startLoginProcessingTimer() {
        return Timer.start();
    }
    
    public void stopLoginProcessingTimer(Timer.Sample sample) {
        sample.stop(loginProcessingTimer);
    }
    
    public Timer.Sample startRegisterProcessingTimer() {
        return Timer.start();
    }
    
    public void stopRegisterProcessingTimer(Timer.Sample sample) {
        sample.stop(registerProcessingTimer);
    }
    
    public Timer.Sample startRefreshTokenProcessingTimer() {
        return Timer.start();
    }
    
    public void stopRefreshTokenProcessingTimer(Timer.Sample sample) {
        sample.stop(refreshTokenProcessingTimer);
    }
    
    public Timer.Sample startLogoutProcessingTimer() {
        return Timer.start();
    }
    
    public void stopLogoutProcessingTimer(Timer.Sample sample) {
        sample.stop(logoutProcessingTimer);
    }
    
    public Timer.Sample startChangePasswordProcessingTimer() {
        return Timer.start();
    }
    
    public void stopChangePasswordProcessingTimer(Timer.Sample sample) {
        sample.stop(changePasswordProcessingTimer);
    }
    
    // Методы для email метрик
    public void incrementEmailsSent() {
        emailsSentCounter.increment();
    }
    
    public Timer.Sample startEmailProcessingTimer() {
        return Timer.start();
    }
    
    public void stopEmailProcessingTimer(Timer.Sample sample) {
        sample.stop(emailProcessingTimer);
    }
    
    // Методы для метрик блюд
    public void incrementDishCreate() {
        dishCreateCounter.increment();
    }
    
    public void incrementDishUpdate() {
        dishUpdateCounter.increment();
    }
    
    public void incrementDishDelete() {
        dishDeleteCounter.increment();
    }
    
    public void incrementDishRead() {
        dishReadCounter.increment();
    }
    
    public void incrementDishAvailabilityToggle() {
        dishAvailabilityToggleCounter.increment();
    }
    
    public void incrementDishSearch() {
        dishSearchCounter.increment();
    }
    
    // Таймеры для блюд
    public Timer.Sample startDishCreateProcessingTimer() {
        return Timer.start();
    }
    
    public void stopDishCreateProcessingTimer(Timer.Sample sample) {
        sample.stop(dishCreateProcessingTimer);
    }
    
    public Timer.Sample startDishUpdateProcessingTimer() {
        return Timer.start();
    }
    
    public void stopDishUpdateProcessingTimer(Timer.Sample sample) {
        sample.stop(dishUpdateProcessingTimer);
    }
    
    public Timer.Sample startDishDeleteProcessingTimer() {
        return Timer.start();
    }
    
    public void stopDishDeleteProcessingTimer(Timer.Sample sample) {
        sample.stop(dishDeleteProcessingTimer);
    }
    
    public Timer.Sample startDishReadProcessingTimer() {
        return Timer.start();
    }
    
    public void stopDishReadProcessingTimer(Timer.Sample sample) {
        sample.stop(dishReadProcessingTimer);
    }
    
    public Timer.Sample startDishSearchProcessingTimer() {
        return Timer.start();
    }
    
    public void stopDishSearchProcessingTimer(Timer.Sample sample) {
        sample.stop(dishSearchProcessingTimer);
    }
    
    // Методы для метрик изображений
    public void incrementImageUpload() {
        imageUploadCounter.increment();
    }
    
    public void incrementImageDelete() {
        imageDeleteCounter.increment();
    }
    
    public void incrementImageUpdate() {
        imageUpdateCounter.increment();
    }
    
    public void incrementImageProcessingError() {
        imageProcessingErrorCounter.increment();
    }
    
    // Таймеры для изображений
    public Timer.Sample startImageUploadProcessingTimer() {
        return Timer.start();
    }
    
    public void stopImageUploadProcessingTimer(Timer.Sample sample) {
        sample.stop(imageUploadProcessingTimer);
    }
    
    public Timer.Sample startImageDeleteProcessingTimer() {
        return Timer.start();
    }
    
    public void stopImageDeleteProcessingTimer(Timer.Sample sample) {
        sample.stop(imageDeleteProcessingTimer);
    }
    
    public Timer.Sample startImageUpdateProcessingTimer() {
        return Timer.start();
    }
    
    public void stopImageUpdateProcessingTimer(Timer.Sample sample) {
        sample.stop(imageUpdateProcessingTimer);
    }
    

}
