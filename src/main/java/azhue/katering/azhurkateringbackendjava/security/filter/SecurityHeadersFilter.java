package azhue.katering.azhurkateringbackendjava.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Фильтр для добавления заголовков безопасности в HTTP ответы.
 * 
 * <p>Этот фильтр автоматически добавляет различные заголовки безопасности к каждому HTTP ответу,
 * что помогает защитить приложение от различных типов атак. Фильтр использует Spring Security
 * HeaderWriter'ы для добавления стандартных заголовков безопасности.</p>
 * 
 * <p><strong>Добавляемые заголовки:</strong></p>
 * <ul>
 *   <li>X-Content-Type-Options: nosniff - предотвращает MIME sniffing</li>
 *   <li>X-Frame-Options: DENY - защищает от clickjacking</li>
 *   <li>X-XSS-Protection: 1; mode=block - включает XSS защиту браузера</li>
 *   <li>Strict-Transport-Security - принуждает HTTPS</li>
 *   <li>Content-Security-Policy - контролирует ресурсы</li>
 * </ul>
 * 
 * <p><strong>Исключения:</strong> Фильтр не применяется к статическим ресурсам
 * (CSS, JS, изображения) для оптимизации производительности.</p>
 * 
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityHeadersFilter extends OncePerRequestFilter {

    private final List<HeaderWriter> headerWriters;

    /**
     * Добавляет заголовки безопасности к HTTP ответу
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Добавляем все Security Headers
        for (HeaderWriter headerWriter : headerWriters) {
            try {
                headerWriter.writeHeaders(request, response);
            } catch (Exception e) {
                log.warn("Failed to write security header: {}", e.getMessage());
            }
        }

        // Продолжаем цепочку фильтров
        filterChain.doFilter(request, response);
    }

    /**
     * Определяет, нужно ли применять фильтр к запросу
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Не применяем фильтр к статическим ресурсам
        String path = request.getRequestURI();
        return path.startsWith("/static/") || 
               path.startsWith("/css/") || 
               path.startsWith("/js/") || 
               path.startsWith("/images/") ||
               path.startsWith("/favicon.ico");
    }
}
