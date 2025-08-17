# JWT Аутентификация - Полное руководство

## Обзор системы

Реализована полноценная система JWT аутентификации с высокой безопасностью, включающая:

- ✅ Регистрация с подтверждением email
- ✅ Вход с защитой от брутфорса
- ✅ JWT токены (access + refresh)
- ✅ Автоматическое обновление токенов
- ✅ Роли пользователей (USER, ADMIN)
- ✅ Блокировка аккаунтов при множественных неудачных попытках
- ✅ CORS настройки для фронтенда
- ✅ Валидация данных
- ✅ Логирование безопасности
- ✅ HTTP-only cookies для токенов
- ✅ Rate limiting

## API Endpoints

### 1. Регистрация

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "ivan_petrov",
  "email": "ivan@example.com",
  "password": "SecurePass123!"
}
```

### 2. Отправка кода подтверждения

```http
POST /api/v1/auth/send-verification
Content-Type: application/json

{
  "email": "ivan@example.com"
}
```

### 3. Верификация email

```http
POST /api/v1/auth/verify-email
Content-Type: application/json

{
  "email": "ivan@example.com",
  "code": "123456"
}
```

### 4. Вход

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "ivan@example.com",
  "password": "SecurePass123!"
}
```

### 5. Обновление токена

```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "your-refresh-token"
}
```

### 6. Смена пароля

```http
POST /api/v1/auth/change-password
Authorization: Bearer your-access-token
Content-Type: application/json

{
  "oldPassword": "SecurePass123!",
  "newPassword": "NewSecurePass456!"
}
```

### 7. Выход

```http
POST /api/v1/auth/logout
Authorization: Bearer your-access-token
```

## Архитектура безопасности

### JWT Токены

**Access Token:**
- Алгоритм: HS512
- Время жизни: 15 минут
- Содержит: email, userId, role

**Refresh Token:**
- Алгоритм: HS512
- Время жизни: 7 дней
- Содержит: email, userId

### Хранение токенов

Токены сохраняются в HTTP-only cookies:
- `access-token`: JWT access token
- `refresh-token`: JWT refresh token

Настройки cookies:
- `HttpOnly`: true (недоступны для JavaScript)
- `Secure`: true (только HTTPS)
- `SameSite`: Strict
- `Path`: /

### Rate Limiting

Защита от злоупотреблений:
- **Аутентификация**: 5 попыток в минуту
- **Email верификация**: 3 попытки в 5 минут
- **Refresh токены**: 10 попыток в минуту
- **Смена пароля**: 3 попытки в час
- **Общие API**: 100 запросов в минуту

### Блокировка аккаунтов

- Максимум 5 неудачных попыток входа
- Блокировка на 30 минут
- Автоматическая разблокировка

## Процесс аутентификации

### 1. Регистрация
1. Пользователь отправляет данные регистрации
2. Система создает пользователя с `isVerified = false`
3. Отправляется код подтверждения на email
4. Возвращается успешный ответ

### 2. Верификация email
1. Пользователь вводит код из email
2. Система проверяет код и время жизни
3. Устанавливается `isVerified = true`
4. Пользователь может входить в систему

### 3. Вход в систему
1. Проверяется существование пользователя
2. Проверяется блокировка аккаунта
3. Проверяется пароль
4. Если email не верифицирован:
   - Отправляется новый код
   - Возвращается `requiresVerification = true`
5. Если все проверки пройдены:
   - Генерируются токены
   - Токены сохраняются в cookies
   - Возвращается информация о пользователе

### 4. Обновление токенов
1. Проверяется refresh token
2. Проверяется время жизни
3. Проверяется существование в БД
4. Генерируются новые токены
5. Старый refresh token отзывается

### 5. Смена пароля
1. Проверяется текущий пароль
2. Обновляется пароль в БД
3. Отзываются все refresh токены
4. Генерируются новые токены

## Безопасность

### Заголовки безопасности
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security: max-age=31536000; includeSubDomains; preload`
- `Content-Security-Policy`
- `Permissions-Policy`

### Валидация данных
- Все входные данные валидируются
- Используется Bean Validation

### Логирование
- Все действия аутентификации логируются
- Сохраняется IP адрес и User-Agent
- Отслеживаются неудачные попытки

## Обработка ошибок

### Стандартный формат ошибок
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
- `TOKEN_NOT_VALID`: Токен недействителен
- `TOKEN_NOT_FOUND`: Токен не найден
- `TYPE_TOKEN_EXCEPTION`: Неверный тип токена
- `RATE_LIMIT_EXCEEDED`: Превышен лимит запросов
- `VALIDATION_ERROR`: Ошибка валидации
- `EMAIL_EXCEPTION`: Ошибка отправки email
- `VEREFICATION_CODE_EXCEPTION`: Неверный код подтверждения
- `VERIFIED_EXCEPTION`: Email уже верифицирован

## Мониторинг


### Swagger документация
```http
GET /swagger-ui/index.html
```

### Продакшн настройки
- Измените `app.cookie.secure` на `true`
- Настройте HTTPS
- Измените `app.cookie.domain` на прод домен
- Настройте CORS для продакшн доменов