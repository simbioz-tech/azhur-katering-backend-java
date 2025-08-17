package azhue.katering.azhurkateringbackendjava.auth.repository;

import azhue.katering.azhurkateringbackendjava.auth.model.entity.RefreshToken;
import azhue.katering.azhurkateringbackendjava.auth.model.entity.User;
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
 * Репозиторий для работы с refresh токенами
 *
 * @version 1.0.0
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    
    /**
     * Поиск токена по значению
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Поиск действительных токенов пользователя
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.isRevoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);
    
    /**
     * Поиск истекших токенов
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < :now")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Поиск отозванных токенов
     */
    List<RefreshToken> findByIsRevokedTrue();
    
    /**
     * Отзыв всех токенов пользователя
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true, rt.revokedAt = :revokedAt WHERE rt.user = :user AND rt.isRevoked = false")
    void revokeAllUserTokens(@Param("user") User user, @Param("revokedAt") LocalDateTime revokedAt);

    /**
     * Удаление истекших токенов
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}