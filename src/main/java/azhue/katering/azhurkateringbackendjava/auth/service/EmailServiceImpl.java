package azhue.katering.azhurkateringbackendjava.auth.service;

import azhue.katering.azhurkateringbackendjava.auth.model.entity.AuthLog;
import azhue.katering.azhurkateringbackendjava.auth.model.entity.EmailVerification;
import azhue.katering.azhurkateringbackendjava.auth.model.entity.User;
import azhue.katering.azhurkateringbackendjava.auth.repository.EmailVerificationRepository;
import azhue.katering.azhurkateringbackendjava.auth.repository.UserRepository;
import azhue.katering.azhurkateringbackendjava.auth.service.contract.EmailService;
import azhue.katering.azhurkateringbackendjava.auth.service.contract.LoggingAuthActions;
import azhue.katering.azhurkateringbackendjava.common.exception.account.UserNotFoundException;
import azhue.katering.azhurkateringbackendjava.common.exception.email.VereficationCodeException;
import azhue.katering.azhurkateringbackendjava.common.exception.email.VerifiedException;
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
    private final LoggingAuthActions loggingAuthActions;

    private static final int CODE_EXPIRATION_MINUTES = 15;

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

        try {
            log.info("Starting async email verification for user: {}", user.getEmail());
            
            // Отзываем все предыдущие действительные коды
            revokeAllValidCodes(user.getEmail());
            
            String verificationCode = generateVerificationCode();
            
            log.info("Preparing to send email to: {} (async), From email: {}", user.getEmail(), fromEmail);

            SimpleMailMessage message = getSimpleMailMessage(user, verificationCode);

            log.info("Sending email (async)...");

            EmailVerification emailVerification = EmailVerification.builder()
                    .user(user)
                    .verificationCode(verificationCode)
                    .expiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES))
                    .ipAddress(ipAddress)
                    .build();

            emailVerificationRepository.save(emailVerification);
            log.info("Email verification record saved for user: {}", user.getEmail());

            mailSender.send(message);

            log.info("Verification code sent successfully to: {} (async)", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to send verification code to: {} (async)", user.getEmail(), e);
            log.error("Error type: {}, Message: {}", e.getClass().getSimpleName(), e.getMessage());
            
            // Логируем детали для отладки
            if (e.getCause() != null) {
                log.error("Root cause: {}", e.getCause().getMessage());
            }
        }
    }

    /**
     * Отправляет код подтверждения по email
     */
    @Override
    public void sendVerificationCode(String email, String ipAddress) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        
        sendVerificationCodeAsync(user, ipAddress);
    }

    /**
     * Отзывает все действительные коды верификации
     */
    private void revokeAllValidCodes(String email) {
        emailVerificationRepository.findValidVerificationByUserEmail(email, LocalDateTime.now())
                .ifPresent(verification -> {
                    verification.use();
                    emailVerificationRepository.save(verification);
                });
    }

    private SimpleMailMessage getSimpleMailMessage(User user, String verificationCode) {
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
        return message;
    }

    /**
     * Верифицирует email пользователя
     */
    @Transactional
    public void verifyEmail(String email, String code, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (user.getIsVerified()) {
            throw new VerifiedException("Email уже верифицирован");
        }

        if (!verifyEmailCode(email, code)) {
            loggingAuthActions.logAuthAction(user, AuthLog.AuthAction.EMAIL_VERIFICATION, ipAddress, userAgent, false, "Неверный код");
            throw new VereficationCodeException("Неверный код подтверждения или код истек");
        }

        // Верифицируем email
        user.setIsVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        // Удаляем использованный код
        revokeAllValidCodes(user.getEmail());

        loggingAuthActions.logAuthAction(user, AuthLog.AuthAction.EMAIL_VERIFICATION, ipAddress, userAgent, true, null);
        log.info("Email верифицирован: {}", email);
    }

    /**
     * Проверяет код подтверждения email
     */
    private boolean verifyEmailCode(String email, String code) {
        return emailVerificationRepository.findValidVerificationByUserEmail(email, LocalDateTime.now())
                .map(verification -> verification.getVerificationCode().equals(code))
                .orElse(false);
    }
}
