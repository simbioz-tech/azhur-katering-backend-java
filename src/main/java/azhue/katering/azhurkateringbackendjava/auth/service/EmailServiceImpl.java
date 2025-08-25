package azhue.katering.azhurkateringbackendjava.auth.service;

import azhue.katering.azhurkateringbackendjava.auth.exception.account.UserNotFoundException;
import azhue.katering.azhurkateringbackendjava.auth.exception.email.VereficationCodeException;
import azhue.katering.azhurkateringbackendjava.auth.exception.email.VerifiedException;
import azhue.katering.azhurkateringbackendjava.auth.model.entity.EmailVerification;
import azhue.katering.azhurkateringbackendjava.auth.model.entity.User;
import azhue.katering.azhurkateringbackendjava.auth.repository.EmailVerificationRepository;
import azhue.katering.azhurkateringbackendjava.auth.repository.UserRepository;
import azhue.katering.azhurkateringbackendjava.auth.service.contract.EmailService;
import azhue.katering.azhurkateringbackendjava.common.service.MetricsService;
import azhue.katering.azhurkateringbackendjava.common.util.LogUtils;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Сервис для работы с email.
 * 
 * <p>Отправляет коды подтверждения, верифицирует email адреса и
 * управляет процессом регистрации пользователей.</p>
 * 
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final MetricsService metricsService;

    private static final int CODE_EXPIRATION_MINUTES = 15;
    
    // Константы для операций логирования
    private static final String OPERATION_SEND_VERIFICATION = "send_verification";
    private static final String OPERATION_VERIFY_EMAIL = "verify_email";

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Генерирует случайный код подтверждения
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6-значный код
        return String.valueOf(code);
    }

    /**
     * Асинхронно отправляет код подтверждения
     */
    @Async
    @Transactional
    public void sendVerificationCodeAsync(User user, String ipAddress) {
        if (user == null) {
            log.error("Cannot send verification code: user is null");
            return;
        }

        LogUtils.setOperationTags(OPERATION_SEND_VERIFICATION, user.getId().toString(), user.getEmail(), ipAddress, LogUtils.STATUS_START);
        log.info("Начинаем отправку кода верификации: userId={}, email={}", user.getId(), user.getEmail());
        Timer.Sample timer = metricsService.startEmailProcessingTimer();
        
        try {
            // Отзываем все предыдущие действительные коды
            log.info("Отзываем предыдущие коды верификации: userId={}, email={}", user.getId(), user.getEmail());
            revokeAllValidCodes(user.getEmail());
            
            String verificationCode = generateVerificationCode();
            log.info("Сгенерирован код верификации: userId={}, email={}", user.getId(), user.getEmail());
            
            log.info("Подготавливаем email: userId={}, email={}, fromEmail={}", user.getId(), user.getEmail(), fromEmail);

            SimpleMailMessage message = getSimpleMailMessage(user, verificationCode);

            log.info("Сохраняем запись верификации: userId={}, email={}", user.getId(), user.getEmail());

            EmailVerification emailVerification = EmailVerification.builder()
                    .user(user)
                    .verificationCode(verificationCode)
                    .expiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES))
                    .ipAddress(ipAddress)
                    .build();

            emailVerificationRepository.save(emailVerification);
            log.info("Запись верификации сохранена: userId={}, email={}", user.getId(), user.getEmail());

            log.info("Отправляем email: userId={}, email={}", user.getId(), user.getEmail());
            mailSender.send(message);

            LogUtils.setOperationTags(OPERATION_SEND_VERIFICATION, user.getId().toString(), user.getEmail(), ipAddress, LogUtils.STATUS_SUCCESS);
            log.info("Код верификации отправлен успешно: userId={}, email={}", user.getId(), user.getEmail());
            
            // Увеличиваем счетчик отправленных email
            metricsService.incrementEmailsSent();
        } finally {
            LogUtils.clearTags();
            metricsService.stopEmailProcessingTimer(timer);
        }
    }

    /**
     * Отправляет код подтверждения по email
     */
    @Override
    public void sendVerificationCode(String email, String ipAddress) {
        log.info("Запрос на отправку кода верификации: email={}, ip={}", email, ipAddress);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Попытка отправки кода несуществующему пользователю: email={}", email);
                    return new UserNotFoundException(email);
                });
        
        log.info("Пользователь найден, отправляем код: userId={}, email={}", user.getId(), user.getEmail());
        sendVerificationCodeAsync(user, ipAddress);
    }

    /**
     * Отзывает все действительные коды верификации
     */
    private void revokeAllValidCodes(String email) {
        emailVerificationRepository.findValidVerificationByUserEmail(email, LocalDateTime.now())
                .ifPresent(verification -> {
                    log.debug("Отзываем предыдущий код верификации: email={}, code={}", email, verification.getVerificationCode());
                    verification.use();
                    emailVerificationRepository.save(verification);
                    log.debug("Код верификации отозван: email={}", email);
                });
    }

    private SimpleMailMessage getSimpleMailMessage(User user, String verificationCode) {
        log.debug("Создаем email сообщение: userId={}, email={}", user.getId(), user.getEmail());
        
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Код подтверждения - Azhur Katering");
        message.setText(String.format(
                """
                        Здравствуйте!
                        
                        Ваш код подтверждения: %s
                        
                        Код действителен в течение 15 минут.
                        Если вы не регистрировались в Azhur Katering, проигнорируйте это письмо.
                        
                        С уважением,
                        Команда Azhur Katering""",
                verificationCode
        ));
        
        log.debug("Email сообщение создано: userId={}, email={}, fromEmail={}", user.getId(), user.getEmail(), fromEmail);
        return message;
    }

    /**
     * Верифицирует email пользователя
     */
    @Transactional
    public void verifyEmail(String email, String code, String ipAddress, String userAgent) {
        LogUtils.setOperationTags(OPERATION_VERIFY_EMAIL, null, email, ipAddress, LogUtils.STATUS_START);
        log.info("Начинаем верификацию email: email={}, code={}", email, code);
        Timer.Sample timer = metricsService.startEmailProcessingTimer();
        
        try {
            log.info("Ищем пользователя: email={}", email);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        LogUtils.setOperationTags(OPERATION_VERIFY_EMAIL, null, email, ipAddress, LogUtils.STATUS_FAILED);
                        log.warn("Попытка верификации несуществующего пользователя: email={}", email);
                        return new UserNotFoundException("Пользователь не найден");
                    });

            LogUtils.setUserId(user.getId().toString());
            log.info("Пользователь найден: userId={}, email={}, isVerified={}", user.getId(), user.getEmail(), user.getIsVerified());

            if (user.getIsVerified()) {
                LogUtils.setOperationTags(OPERATION_VERIFY_EMAIL, user.getId().toString(), user.getEmail(), ipAddress, LogUtils.STATUS_FAILED);
                log.warn("Попытка повторной верификации email: userId={}, email={}", user.getId(), user.getEmail());
                throw new VerifiedException("Email уже верифицирован");
            }

            log.info("Проверяем код верификации: userId={}, email={}", user.getId(), user.getEmail());
            if (!verifyEmailCode(email, code)) {
                LogUtils.setOperationTags(OPERATION_VERIFY_EMAIL, user.getId().toString(), user.getEmail(), ipAddress, LogUtils.STATUS_FAILED);
                log.warn("Неверный код верификации: userId={}, email={}, code={}", user.getId(), user.getEmail(), code);
                throw new VereficationCodeException("Неверный код подтверждения или код истек");
            }

            log.info("Код верификации подтвержден, обновляем пользователя: userId={}, email={}", user.getId(), user.getEmail());
            // Верифицируем email
            user.setIsVerified(true);
            user.setEmailVerifiedAt(LocalDateTime.now());
            userRepository.save(user);

            log.info("Отзываем использованный код: userId={}, email={}", user.getId(), user.getEmail());
            // Удаляем использованный код
            revokeAllValidCodes(user.getEmail());

            LogUtils.setOperationTags(OPERATION_VERIFY_EMAIL, user.getId().toString(), user.getEmail(), ipAddress, LogUtils.STATUS_SUCCESS);
            log.info("Email верифицирован успешно: userId={}, email={}", user.getId(), user.getEmail());
            
            // Увеличиваем счетчик верификаций
            metricsService.incrementEmailVerifications();
            
        } finally {
            LogUtils.clearTags();
            metricsService.stopEmailProcessingTimer(timer);
        }
    }

    /**
     * Проверяет код подтверждения email
     */
    private boolean verifyEmailCode(String email, String code) {
        log.debug("Проверяем код верификации: email={}, code={}", email, code);
        
        boolean isValid = emailVerificationRepository.findValidVerificationByUserEmail(email, LocalDateTime.now())
                .map(verification -> verification.getVerificationCode().equals(code))
                .orElse(false);
        
        log.debug("Результат проверки кода: email={}, code={}, isValid={}", email, code, isValid);
        return isValid;
    }
}
