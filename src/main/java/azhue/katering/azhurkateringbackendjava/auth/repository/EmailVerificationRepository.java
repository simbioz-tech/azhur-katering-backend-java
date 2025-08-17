package azhue.katering.azhurkateringbackendjava.auth.repository;

import azhue.katering.azhurkateringbackendjava.auth.model.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с верификацией email
 *
 * @version 1.0.0
 */
@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {

    /**
     * Поиск действительной верификации по email
     */
    @Query("SELECT ev FROM EmailVerification ev " +
            "JOIN ev.user u " +
            "WHERE u.email = :email " +
            "AND ev.isUsed = false " +
            "AND ev.expiresAt > :now " +
            "ORDER BY ev.createdAt DESC")
    Optional<EmailVerification> findValidVerificationByUserEmail(
            @Param("email") String email,
            @Param("now") LocalDateTime now
    );
    
    /**
     * Поиск истекших верификаций
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.expiresAt < :now AND ev.isUsed = false")
    List<EmailVerification> findExpiredVerifications(@Param("now") LocalDateTime now);
    
    /**
     * Поиск использованных верификаций
     */
    List<EmailVerification> findByIsUsedTrue();

    /**
     * Удаление истекших верификаций
     */
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.expiresAt < :now")
    void deleteExpiredVerifications(@Param("now") LocalDateTime now);
    
    /**
     * Удаление использованных верификаций
     */
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.isUsed = true")
    void deleteUsedVerifications();
}