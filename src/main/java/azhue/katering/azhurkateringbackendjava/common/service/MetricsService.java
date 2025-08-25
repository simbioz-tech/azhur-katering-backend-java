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
    private final Counter emailErrorsCounter;
    private final Timer authProcessingTimer;
    private final Timer emailSendingTimer;
    private final Timer emailProcessingTimer;
    
    // Специфичные таймеры для каждой операции
    private final Timer loginProcessingTimer;
    private final Timer registerProcessingTimer;
    private final Timer refreshTokenProcessingTimer;
    private final Timer logoutProcessingTimer;
    private final Timer changePasswordProcessingTimer;

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
        
        this.emailErrorsCounter = Counter.builder("email.errors")
                .description("Email sending errors")
                .register(meterRegistry);
        
        this.authProcessingTimer = Timer.builder("auth.processing.time")
                .description("Authentication processing time")
                .register(meterRegistry);
        
        this.emailSendingTimer = Timer.builder("email.sending.time")
                .description("Email sending time")
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

    public Timer.Sample startAuthProcessingTimer() {
        return Timer.start();
    }

    public void stopAuthProcessingTimer(Timer.Sample sample) {
        sample.stop(authProcessingTimer);
    }

    public Timer.Sample startEmailSendingTimer() {
        return Timer.start();
    }

    public void stopEmailSendingTimer(Timer.Sample sample) {
        sample.stop(emailSendingTimer);
    }

    public void recordAuthProcessingTime(long timeInMs) {
        authProcessingTimer.record(timeInMs, TimeUnit.MILLISECONDS);
    }

    public void recordEmailSendingTime(long timeInMs) {
        emailSendingTimer.record(timeInMs, TimeUnit.MILLISECONDS);
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
    
    public void incrementEmailErrors() {
        emailErrorsCounter.increment();
    }
    
    public Timer.Sample startEmailProcessingTimer() {
        return Timer.start();
    }
    
    public void stopEmailProcessingTimer(Timer.Sample sample) {
        sample.stop(emailProcessingTimer);
    }
}
