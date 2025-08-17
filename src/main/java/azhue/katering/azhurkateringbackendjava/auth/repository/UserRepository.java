package azhue.katering.azhurkateringbackendjava.auth.repository;

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
 * Репозиторий для работы с пользователями
 *
 * @version 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Поиск пользователя по email
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверка существования пользователя по email
     */
    boolean existsByEmail(String email);
    
    /**
     * Проверка существования пользователя по username
     */
    boolean existsByUsername(String username);

    /**
     * Поиск пользователей по статусу верификации
     */
    List<User> findByIsVerified(Boolean isVerified);
    
    /**
     * Поиск заблокированных пользователей
     */
    @Query("SELECT u FROM User u WHERE u.lockTime IS NOT NULL AND u.lockTime > :now")
    List<User> findLockedUsers(@Param("now") LocalDateTime now);
    
    /**
     * Обновление неудачных попыток входа
     */
    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = :failedAttempts WHERE u.id = :userId")
    void updateFailedAttempts(@Param("userId") UUID userId, @Param("failedAttempts") Integer failedAttempts);
    
    /**
     * Обновление времени блокировки
     */
    @Modifying
    @Query("UPDATE User u SET u.lockTime = :lockTime WHERE u.id = :userId")
    void updateLockTime(@Param("userId") UUID userId, @Param("lockTime") LocalDateTime lockTime);
    
    /**
     * Обновление времени последнего входа
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") UUID userId, @Param("lastLogin") LocalDateTime lastLogin);
    
    /**
     * Обновление статуса блокировки аккаунта
     */
    @Modifying
    @Query("UPDATE User u SET u.isAccountNonLocked = :isAccountNonLocked WHERE u.id = :userId")
    void updateAccountLockStatus(@Param("userId") UUID userId, @Param("isAccountNonLocked") Boolean isAccountNonLocked);

    /**
     * Обновление пароля
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :passwordHash, u.passwordChangedAt = :passwordChangedAt WHERE u.id = :userId")
    void updatePassword(@Param("userId") UUID userId, @Param("passwordHash") String passwordHash, @Param("passwordChangedAt") LocalDateTime passwordChangedAt);
    
    /**
     * Поиск пользователей, зарегистрированных в определенный период
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersRegisteredBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Подсчет активных пользователей
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();
}