package azhue.katering.azhurkateringbackendjava.auth.model.entity;

import azhue.katering.azhurkateringbackendjava.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Сущность пользователя
 *
 * @version 1.0.0
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity implements UserDetails {
    
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.USER;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;
    
    @Column(name = "is_account_non_locked", nullable = false)
    @Builder.Default
    private Boolean isAccountNonLocked = true;
    
    @Column(name = "failed_attempts")
    @Builder.Default
    private Integer failedAttempts = 0;
    
    @Column(name = "lock_time")
    private LocalDateTime lockTime;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /**
     *  Установка времени верификации email
     */
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    /**
     *  Установка времени изменения пароля
     */
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public String getPassword() {
        return passwordHash;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return isActive && isVerified;
    }
    
    /**
     * Роли пользователей
     */
    public enum Role {
        USER, ADMIN
    }
    
    /**
     * Проверка, заблокирован ли аккаунт
     */
    public boolean isAccountLocked() {
        return lockTime != null && LocalDateTime.now().isBefore(lockTime);
    }
    
    /**
     * Блокировка аккаунта
     */
    public void lockAccount(int minutes) {
        this.isAccountNonLocked = false;
        this.lockTime = LocalDateTime.now().plusMinutes(minutes);
    }
    
    /**
     * Разблокировка аккаунта
     */
    public void unlockAccount() {
        this.isAccountNonLocked = true;
        this.lockTime = null;
        this.failedAttempts = 0;
    }
    
    /**
     * Увеличение счетчика неудачных попыток
     */
    public void incrementFailedAttempts() {
        this.failedAttempts++;
    }
    
    /**
     * Сброс счетчика неудачных попыток
     */
    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.lastLogin = LocalDateTime.now();
    }

    @Override
    public final boolean equals(Object object) {
        if (this == object) return true;
        if (object == null) return false;
        Class<?> oEffectiveClass = object instanceof HibernateProxy ? ((HibernateProxy) object).getHibernateLazyInitializer().getPersistentClass() : object.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        User user = (User) object;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}