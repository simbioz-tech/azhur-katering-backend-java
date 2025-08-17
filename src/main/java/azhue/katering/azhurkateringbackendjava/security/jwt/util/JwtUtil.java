package azhue.katering.azhurkateringbackendjava.security.jwt.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Утилита для работы с JWT токенами.
 * 
 * <p>Предоставляет методы для создания, валидации и извлечения данных из JWT токенов.
 * Поддерживает два типа токенов: access token (короткий срок) и refresh token (долгий срок).</p>
 * 
 * @version 1.0.0
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * Создает секретный ключ для подписи токенов
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Генерирует access token для пользователя
     */
    public String generateAccessToken(String email, String userId, String role) {
        return generateToken(email, userId, role, expiration);
    }

    /**
     * Генерирует refresh token для пользователя
     */
    public String generateRefreshToken(String email, String userId) {
        return generateToken(email, userId, null, refreshExpiration);
    }

    /**
     * Создает JWT токен с указанными параметрами
     */
    private String generateToken(String email, String userId, String role, long expiration) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(expiration, ChronoUnit.MILLIS);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        if (role != null) {
            claims.put("role", role);
            claims.put("type", "access");
        } else {
            claims.put("type", "refresh");
        }
        
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .issuer("azhur-katering")
                .audience().add("azhur-katering-frontend").and()
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Извлекает email из токена
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Извлекает userId из токена
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    /**
     * Извлекает роль из токена
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Извлекает дату истечения токена
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлекает произвольный claim из токена
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Извлекает все claims из токена
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Проверяет, истек ли токен
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.warn("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Валидирует токен для указанного email
     */
    public Boolean validateToken(String token, String email) {
        try {
            final String extractedEmail = extractEmail(token);
            return (email.equals(extractedEmail) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет, является ли токен access token
     */
    public Boolean isAccessToken(String token) {
        try {
            String type = extractClaim(token, claims -> claims.get("type", String.class));
            return "access".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Проверяет, является ли токен refresh token
     */
    public Boolean isRefreshToken(String token) {
        try {
            String type = extractClaim(token, claims -> claims.get("type", String.class));
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Возвращает время до истечения токена в секундах
     */
    public Long getTimeUntilExpiration(String token) {
        try {
            Date expiration = extractExpiration(token);
            return (expiration.getTime() - System.currentTimeMillis()) / 1000;
        } catch (Exception e) {
            return 0L;
        }
    }
}