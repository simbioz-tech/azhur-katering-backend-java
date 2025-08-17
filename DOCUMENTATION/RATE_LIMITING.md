# 🛡️ Rate Limiting Guide

## 📋 Обзор

Rate limiting защищает API от злоупотреблений и DDoS атак, ограничивая количество запросов в единицу времени.

## ⚙️ Конфигурация

### **Rate Limiters:**

| Endpoint | Лимит | Время | Bean Name |
|----------|-------|-------|-----------|
| Логин/Регистрация | 5 | 1 минута | `authRateLimiter` |
| Email верификация | 3 | 5 минут | `emailVerificationRateLimiter` |
| Refresh токены | 10 | 1 минута | `refreshTokenRateLimiter` |
| Смена пароля | 3 | 1 час | `passwordChangeRateLimiter` |
| Общие API | 100 | 1 минута | `generalApiRateLimiter` |

## 🔧 Использование

### **Добавление к методу:**
```java
@PostMapping("/login")
@RateLimit("authRateLimiter")
public AuthResponse login(@RequestBody LoginRequest request) {
    // логика метода
}
```

### **Кастомное сообщение:**
```java
@RateLimit(value = "authRateLimiter", 
           message = "Слишком много попыток входа. Попробуйте через минуту.")
public AuthResponse login(@RequestBody LoginRequest request) {
    // логика метода
}
```

## 📊 Ответ при превышении лимита:

```json
{
  "success": false,
  "message": "Слишком много запросов, попробуйте позже.",
  "errorCode": "RATE_LIMIT_EXCEEDED",
  "timestamp": "2025-08-16T22:50:00"
}
```

**HTTP Status:** `429 Too Many Requests`

## 🎯 Защищенные Endpoints:

### **Аутентификация:**
- ✅ `POST /api/v1/auth/login` - 5 попыток/минута
- ✅ `POST /api/v1/auth/register` - 5 попыток/минута
- ✅ `POST /api/v1/auth/refresh` - 10 попыток/минута

### **Email верификация:**
- ✅ `POST /api/v1/auth/send-verification` - 3 попытки/5 минут
- ✅ `POST /api/v1/auth/verify-email` - 3 попытки/5 минут

### **Безопасность:**
- ✅ `POST /api/v1/auth/change-password` - 3 попытки/час

## 🔍 Логирование:

### **Успешные запросы:**
```
DEBUG - Rate limit check passed for method: login
```

### **Превышение лимита:**
```
WARN - Rate limit exceeded for method: login with bucket: authRateLimiter
```

## ⚙️ Настройка лимитов:

### **Изменение в RateLimitConfig:**
```java
@Bean("authRateLimiter")
public Bucket authRateLimiter() {
    // Изменить лимит здесь
    Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
    return Bucket.builder().addLimit(limit).build();
}
```

### **Добавление нового лимитера:**
```java
@Bean("customRateLimiter")
public Bucket customRateLimiter() {
    Bandwidth limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
    return Bucket.builder().addLimit(limit).build();
}
```

## 🚀 Преимущества:

- **Защита от брутфорса** - ограничение попыток входа
- **Предотвращение спама** - ограничение отправки email
- **Защита от DDoS** - общие лимиты на API
- **Экономия ресурсов** - снижение нагрузки на сервер
- **Улучшение UX** - справедливое распределение ресурсов

## 🔧 Техническая реализация:

### **Bucket4j:**
- Используется библиотека Bucket4j
- Token bucket алгоритм
- Поддержка различных стратегий пополнения

### **AOP Aspect:**
- Перехват методов с аннотацией `@RateLimit`
- Автоматическая проверка лимитов
- Логирование превышений

### **Конфигурация:**
- Централизованная настройка в `RateLimitConfig`
- Возможность настройки для разных окружений
- Легкое добавление новых лимитеров