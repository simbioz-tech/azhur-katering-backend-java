# 📡 API Examples

Примеры запросов к API Azhur Katering Backend.

## 🔐 Аутентификация

### Регистрация пользователя
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

**Ответ:**
```json
{
  "success": true,
  "message": "Пользователь зарегистрирован и ему отправлен код на почту john@example.com",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john@example.com",
    "username": "john_doe",
    "role": "USER",
    "isVerified": false
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Отправка кода подтверждения
```bash
curl -X POST http://localhost:8080/api/v1/auth/send-verification \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com"
  }'
```

### Верификация email
```bash
curl -X POST http://localhost:8080/api/v1/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "code": "123456"
  }'
```

### Вход в систему
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

**Ответ (успешный вход):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john@example.com",
    "username": "john_doe",
    "role": "USER",
    "isVerified": true,
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Ответ (требуется верификация):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "email": "john@example.com",
    "username": "john_doe",
    "isVerified": false,
    "requiresVerification": true,
    "verificationMessage": "Email не верифицирован. Новый код подтверждения отправлен на вашу почту."
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Обновление токена
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
  }'
```

**Ответ:**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john@example.com",
    "username": "john_doe",
    "role": "USER",
    "isVerified": true,
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Смена пароля
```bash
curl -X POST http://localhost:8080/api/v1/auth/change-password \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "SecurePass123!",
    "newPassword": "NewSecurePass456!"
  }'
```

**Ответ:**
```json
{
  "success": true,
  "message": "Password changed successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john@example.com",
    "username": "john_doe",
    "role": "USER",
    "isVerified": true,
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Выход из системы
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  -H "Content-Type: application/json"
```

**Ответ:**
```json
{
  "success": true,
  "message": "Logout successful",
  "timestamp": "2024-01-15T10:30:00"
}
```

## 🔒 Безопасность

### Rate Limiting
Все эндпоинты защищены от злоупотреблений:

- **Аутентификация**: 5 попыток в минуту
- **Email верификация**: 3 попытки в 5 минут
- **Refresh токены**: 10 попыток в минуту
- **Смена пароля**: 3 попытки в час
- **Общие API**: 100 запросов в минуту

### Заголовки безопасности
API автоматически добавляет заголовки безопасности:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security: max-age=31536000; includeSubDomains; preload`
- `Content-Security-Policy`
- `Permissions-Policy`

## 🍪 Cookies

Токены автоматически сохраняются в HTTP-only cookies:
- `access-token`: JWT access token
- `refresh-token`: JWT refresh token

Cookies настроены с флагами безопасности:
- `HttpOnly`: true
- `Secure`: true (в продакшене)
- `SameSite`: Strict

## 📝 Обработка ошибок

Все ошибки возвращаются в стандартном формате:

```json
{
  "success": false,
  "message": "Описание ошибки",
  "errorCode": "ERROR_CODE",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Коды ошибок
- `USER_NOT_FOUND`: Пользователь не найден
- `INVALID_PASSWORD`: Неверный пароль
- `ACCOUNT_LOCKED`: Аккаунт заблокирован
- `EMAIL_NOT_VERIFIED`: Email не верифицирован
- `TOKEN_EXPIRED`: Токен истек
- `RATE_LIMIT_EXCEEDED`: Превышен лимит запросов
- `VALIDATION_ERROR`: Ошибка валидации