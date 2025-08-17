package azhue.katering.azhurkateringbackendjava.auth.model.entity;

import azhue.katering.azhurkateringbackendjava.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

/**
 * Сущность логов аутентификации
 *
 * @version 1.0.0
 */
@Entity
@Table(name = "auth_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthLog extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuthAction action;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "success", nullable = false)
    @Builder.Default
    private Boolean success = true;
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    /**
     * Действия аутентификации
     */
    public enum AuthAction {
        LOGIN,
        LOGOUT,
        LOGIN_FAILED,
        EMAIL_VERIFICATION,
        ACCOUNT_LOCKED,
        ACCOUNT_UNLOCKED,
        PASSWORD_CHANGED,
        PROFILE_UPDATED,
        REGISTRATION,
        REGISTRATION_FAILED
    }

    @Override
    public final boolean equals(Object object) {
        if (this == object) return true;
        if (object == null) return false;
        Class<?> oEffectiveClass = object instanceof HibernateProxy ? ((HibernateProxy) object).getHibernateLazyInitializer().getPersistentClass() : object.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        AuthLog authLog = (AuthLog) object;
        return getId() != null && Objects.equals(getId(), authLog.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}