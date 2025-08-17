package azhue.katering.azhurkateringbackendjava.auth.service;

import azhue.katering.azhurkateringbackendjava.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Сервис для загрузки пользователей в Spring Security.
 * 
 * <p>Реализует интерфейс UserDetailsService для интеграции с Spring Security.
 * Загружает пользователей из базы данных по email адресу.</p>
 * 
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Загружает пользователя по email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));
    }
}
