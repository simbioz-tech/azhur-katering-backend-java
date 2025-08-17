package azhue.katering.azhurkateringbackendjava.common.service;

import azhue.katering.azhurkateringbackendjava.auth.repository.EmailVerificationRepository;
import azhue.katering.azhurkateringbackendjava.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Сервис для очистки устаревших данных.
 * 
 * <p>Выполняет периодическую очистку истекших токенов и верификаций
 * для поддержания производительности базы данных.</p>
 * 
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    /**
     * Очищает истекшие refresh токены
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        log.info("Starting cleanup of expired refresh tokens");
        try {
            LocalDateTime now = LocalDateTime.now();
            refreshTokenRepository.deleteExpiredTokens(now);
            log.info("Cleanup of expired refresh tokens completed");
        } catch (Exception e) {
            log.error("Error during cleanup of expired refresh tokens", e);
        }
    }

    /**
     * Очищает истекшие email верификации
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredEmailVerifications() {
        log.info("Starting cleanup of expired email verifications");
        try {
            LocalDateTime now = LocalDateTime.now();
            emailVerificationRepository.deleteExpiredVerifications(now);
            log.info("Cleanup of expired email verifications completed");
        } catch (Exception e) {
            log.error("Error during cleanup of expired email verifications", e);
        }
    }

    /**
     * Очищает использованные email верификации
     */
    @Scheduled(cron = "0 0 4 ? * SUN")
    @Transactional
    public void cleanupUsedEmailVerifications() {
        log.info("Starting cleanup of used email verifications");
        try {
            emailVerificationRepository.deleteUsedVerifications();
            log.info("Cleanup of used email verifications completed");
        } catch (Exception e) {
            log.error("Error during cleanup of used email verifications", e);
        }
    }
}