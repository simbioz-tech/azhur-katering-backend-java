package azhue.katering.azhurkateringbackendjava.auth.repository;

import azhue.katering.azhurkateringbackendjava.auth.model.entity.AuthLog;
import azhue.katering.azhurkateringbackendjava.auth.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с логами аутентификации
 *
 * @version 1.0.0
 */
@Repository
public interface AuthLogRepository extends JpaRepository<AuthLog, UUID> {

    /**
     * Поиск логов пользователя
     */
    List<AuthLog> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Поиск логов пользователя с пагинацией
     */
    Page<AuthLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Поиск логов по действию
     */
    List<AuthLog> findByAction(AuthLog.AuthAction action);

    /**
     * Поиск логов по успешности
     */
    List<AuthLog> findBySuccess(Boolean success);

    /**
     * Поиск логов по IP адресу
     */
    List<AuthLog> findByIpAddress(String ipAddress);

    /**
     * Поиск логов в определенный период
     */
    @Query("SELECT al FROM AuthLog al WHERE al.createdAt BETWEEN :startDate AND :endDate")
    List<AuthLog> findLogsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Поиск логов пользователя в определенный период
     */
    @Query("SELECT al FROM AuthLog al WHERE al.user = :user AND al.createdAt BETWEEN :startDate AND :endDate")
    List<AuthLog> findUserLogsBetweenDates(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Поиск последних логов пользователя
     */
    @Query("SELECT al FROM AuthLog al WHERE al.user = :user ORDER BY al.createdAt DESC")
    Page<AuthLog> findRecentUserLogs(@Param("user") User user, Pageable pageable);
}