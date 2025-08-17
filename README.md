# 🍽️ Azhur Katering Backend

Современный REST API для системы управления кейтерингом, построенный на Spring Boot с использованием лучших практик разработки и безопасности.

## 🚀 Особенности

- **🔐 Безопасная аутентификация** - JWT токены с HTTP-only cookies (префикс `__Host-`)
- **📧 Email верификация** - Подтверждение email адресов через Yandex SMTP
- **🛡️ Защита от атак** - Rate limiting, блокировка аккаунтов, security headers
- **🗄️ Миграции БД** - Flyway для управления схемой
- **📝 API документация** - OpenAPI 3.0

## 🛠️ Технологический стек

### Backend
- **Java 21** - Современная версия Java
- **Spring Boot 3.5.4** - Основной фреймворк
- **Spring Security 6.x** - Безопасность
- **Spring Data JPA** - Работа с БД
- **PostgreSQL 15** - Основная БД
- **Flyway** - Миграции БД
- **JWT** - Аутентификация
- **Bucket4j** - Rate limiting

### Инфраструктура
- **Docker & Docker Compose** - Контейнеризация

## 📋 Требования

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 15+

## 🚀 Быстрый старт

### 1. Клонирование репозитория
```bash
git clone https://github.com/your-org/azhur-katering-backend.git
cd azhur-katering-backend
```

### 2. Настройка конфигурации

Создайте .env для хранения чувствительных данных

### 3. Запуск с Docker Compose
```bash
# Запуск всех сервисов
docker-compose up -d

# Проверка статуса
docker-compose ps
```

### 4. Запуск приложения
```bash
# Development режим
mvn spring-boot:run -Dspring.profiles.active=dev

# Production режим
mvn spring-boot:run -Dspring.profiles.active=prod
```

## 📁 Структура проекта

```
src/main/java/azhue/katering/azhurkateringbackendjava/
├── AzhurKateringBackendJavaApplication.java    # Главный класс
├── auth/                                       # Аутентификация
│   ├── controller/                            # REST контроллеры
│   ├── model/                                 # Сущности и DTO
│   │   ├── dto/                              # Data Transfer Objects
│   │   │   ├── request/                      # Запросы
│   │   │   └── response/                     # Ответы
│   │   └── entity/                           # JPA сущности
│   ├── repository/                            # Репозитории
│   └── service/                               # Бизнес-логика
│       ├── contract/                         # Интерфейсы сервисов
│       └── impl/                             # Реализации сервисов
├── common/                                    # Общие компоненты
│   ├── dto/                                   # Общие DTO
│   ├── entity/                                # Базовые сущности
│   ├── exception/                             # Исключения
│   ├── service/                               # Общие сервисы
│   ├── util/                                  # Утилиты
│   ├── aspect/                                # AOP аспекты
│   └── annotation/                            # Кастомные аннотации
├── config/                                    # Конфигурации
│   ├── SecurityConfig.java                    # Spring Security
│   ├── SecurityHeadersConfig.java             # Security Headers
│   ├── OpenApiConfig.java                     # OpenAPI
│   ├── CorsConfig.java                        # CORS
│   ├── RateLimitConfig.java                   # Rate Limiting
│   └── SchedulingConfig.java                  # Асинхронные операции
└── security/                                  # Безопасность
    ├── config/                                # Конфигурации безопасности
    ├── filter/                                # JWT фильтры
    └── jwt/                                   # JWT утилиты
```

## 🔐 Аутентификация

### Регистрация
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "user123",
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

### Вход
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

### Верификация email
```http
POST /api/v1/auth/verify-email
Content-Type: application/json

{
  "email": "user@example.com",
  "code": "123456"
}
```

### Обновление токена
```http
POST /api/v1/auth/refresh
# Refresh token автоматически читается из cookie __Host-refresh-token
```

## 📊 API Endpoints

### Аутентификация
- `POST /api/v1/auth/register` - Регистрация
- `POST /api/v1/auth/login` - Вход
- `POST /api/v1/auth/refresh` - Обновление токена (только из cookie)
- `POST /api/v1/auth/logout` - Выход
- `POST /api/v1/auth/verify-email` - Верификация email
- `POST /api/v1/auth/send-verification` - Отправка кода верификации
- `POST /api/v1/auth/change-password` - Смена пароля

## 🗄️ База данных

### Миграции
```bash
# Просмотр статуса миграций
mvn flyway:info

# Применение миграций
mvn flyway:migrate

# Очистка БД (только development!)
mvn flyway:clean
```

### Основные таблицы
- `users` - Пользователи
- `refresh_tokens` - Refresh токены
- `auth_logs` - Логи аутентификации
- `email_verifications` - Верификация email

## 🔧 Конфигурация

### Профили
- `dev` - Development окружение
- `prod` - Production окружение

### Переменные окружения
```bash
# База данных
DATABASE_URL=jdbc:postgresql://localhost:5432/azhur_katering
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=password

# JWT
JWT_SECRET=your-jwt-secret
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# Email
YANDEX_USERNAME=your-email@yandex.ru
SMTP_PASSWORD=your-smtp-password
SPRING_MAIL_HOST=smtp.yandex.ru
SPRING_MAIL_PORT=465
```

## 🐳 Docker

### Запуск всех сервисов
```bash
docker-compose up -d
```

### Доступные сервисы
- **Приложение**: http://localhost:8080
- **PostgreSQL**: localhost:5432

## 🔒 Безопасность

### JWT
- Алгоритм: HS512
- Время жизни access токена: 15 минут
- Время жизни refresh токена: 7 дней
- Хранение: HTTP-only cookies с префиксом `__Host-`

### Rate Limiting
- Аутентификация: 5 попыток в минуту
- Email верификация: 3 попытки в 5 минут
- Refresh токены: 10 попыток в минуту
- Смена пароля: 3 попытки в час
- Общие API: 100 запросов в минуту

### Security Headers
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- Strict-Transport-Security
- Content-Security-Policy-Report-Only
- Permissions-Policy

### Блокировка аккаунтов
- Максимум неудачных попыток: 5
- Время блокировки: 30 минут

## 📝 Логирование

### Уровни логирования
- `DEBUG` - Детальная отладочная информация
- `INFO` - Общая информация
- `WARN` - Предупреждения
- `ERROR` - Ошибки

## 📚 Документация

Подробная документация доступна в папке [DOCUMENTATION/](DOCUMENTATION/):

- [📡 API Examples](DOCUMENTATION/API_EXAMPLES.md) - Примеры запросов к API
- [🔐 JWT Authentication Guide](DOCUMENTATION/JWT_AUTHENTICATION.md) - Руководство по аутентификации
- [🛡️ Rate Limiting Guide](DOCUMENTATION/RATE_LIMITING.md) - Настройка ограничений запросов
- [🛡️ Security Headers Guide](DOCUMENTATION/SECURITY_HEADERS.md) - Заголовки безопасности
- [📧 Yandex Email Setup](DOCUMENTATION/YANDEX_EMAIL.md) - Настройка email через Yandex
- [🗄️ Database Migration Guide](DOCUMENTATION/DATABASE_MIGRATION.md) - Работа с миграциями БД