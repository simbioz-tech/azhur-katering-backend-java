package azhue.katering.azhurkateringbackendjava.security.filter;

import azhue.katering.azhurkateringbackendjava.auth.service.UserDetailsServiceImpl;
import azhue.katering.azhurkateringbackendjava.common.service.contract.CookieService;
import azhue.katering.azhurkateringbackendjava.security.jwt.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр для аутентификации пользователей на основе JWT токенов.
 * 
 * <p>Этот фильтр перехватывает все HTTP запросы и проверяет наличие валидного JWT токена.
 * Токен может быть получен из HTTP-only cookie или из заголовка Authorization.
 * При успешной валидации токена пользователь аутентифицируется в Spring Security контексте.</p>
 * 
 * <p><strong>Приоритет источников токена:</strong></p>
 * <ol>
 *   <li>HTTP-only cookie (основной способ)</li>
 *   <li>Authorization header (для обратной совместимости)</li>
 * </ol>
 * 
 * <p><strong>Безопасность:</strong></p>
 * <ul>
 *   <li>Использует HTTP-only cookies для предотвращения XSS атак</li>
 *   <li>Проверяет тип токена (только access token)</li>
 *   <li>Валидирует токен перед установкой аутентификации</li>
 *   <li>Логирует ошибки без раскрытия чувствительной информации</li>
 * </ul>
 * 
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final CookieService cookieService;

    /**
     * Обрабатывает HTTP запрос для аутентификации пользователя.
     * 
     * <p>Метод выполняет следующие шаги:</p>
     * <ol>
     *   <li>Извлекает JWT токен из запроса</li>
     *   <li>Проверяет, что токен является access token</li>
     *   <li>Извлекает email пользователя из токена</li>
     *   <li>Загружает детали пользователя из базы данных</li>
     *   <li>Валидирует токен</li>
     *   <li>Устанавливает аутентификацию в Security контексте</li>
     * </ol>
     * 
     * <p>Если на любом этапе происходит ошибка, она логируется, но не прерывает
     * выполнение фильтра. Это обеспечивает стабильность работы приложения.</p>
     * 
     * @param request HTTP запрос
     * @param response HTTP ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException если произошла ошибка сервлета
     * @throws IOException если произошла ошибка ввода/вывода
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String jwt = extractJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && jwtUtil.isAccessToken(jwt)) {
                String email = jwtUtil.extractEmail(jwt);
                
                if (StringUtils.hasText(email) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    
                    if (jwtUtil.validateToken(jwt, email)) {
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails, 
                                null, 
                                userDetails.getAuthorities()
                            );
                        
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        log.debug("Аутентификация успешна для пользователя: {}", email);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке JWT токена: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Извлекает JWT токен из HTTP запроса.
     * 
     * <p>Метод проверяет следующие источники токена в порядке приоритета:</p>
     * <ol>
     *   <li><strong>HTTP-only cookie:</strong> Основной и безопасный способ передачи токена</li>
     *   <li><strong>Authorization header:</strong> Fallback для обратной совместимости</li>
     * </ol>
     * 
     * <p><strong>Формат Authorization header:</strong> {@code Bearer <token>}</p>
     * 
     * @param request HTTP запрос
     * @return JWT токен или {@code null}, если токен не найден
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        // Сначала пытаемся получить из cookie
        String jwt = cookieService.getAccessTokenFromCookie(request);
        
        if (StringUtils.hasText(jwt)) {
            return jwt;
        }
        
        // Fallback: пытаемся получить из Authorization header (для обратной совместимости)
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
}