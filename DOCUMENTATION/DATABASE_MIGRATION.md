# 🗄️ Database Migration Guide

## 📋 Обзор

Руководство по работе с миграциями базы данных в проекте Azhur Katering Backend.

## 📊 Структура базы данных

### **Таблицы:**

| Таблица               | Описание                          |
|-----------------------|-----------------------------------|
| `users`               | Пользователи системы              |
| `refresh_tokens`      | Refresh токены для аутентификации |
| `email_verifications` | Коды подтверждения email          |

### **Миграции:**

| Версия                                    | Описание                           |
|-------------------------------------------|------------------------------------|
| `V1__Create_users_table.sql`              | Создание таблицы пользователей     |
| `V2__Create_refresh_tokens_table.sql`     | Создание таблицы refresh токенов   |
| `V3__Create_email_verification_table.sql` | Создание таблицы верификации email |

## 🔧 Конфигурация

### **application-dev.yml:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/azhur_katering
    username: postgres
    password: your_password
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

### **application-prod.yml:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

## 📝 Создание новых миграций

### **Автоматическое создание:**
```bash
# Flyway создаст миграцию на основе изменений в сущностях
mvn flyway:migrate
```

### **Ручное создание:**
1. Создайте файл в `src/main/resources/db/migration/`
2. Именование: `V{версия}__{описание}.sql`
3. Пример: `V5__Add_user_profile_table.sql`

### **Пример миграции:**
```sql
-- V5__Add_user_profile_table.sql
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
```

## 🔄 Команды Flyway

### **Применение миграций:**
```bash
mvn flyway:migrate
```

### **Проверка статуса:**
```bash
mvn flyway:info
```

### **Очистка базы данных:**
```bash
mvn flyway:clean
```

### **Базовый сброс:**
```bash
mvn flyway:baseline
```

### **Валидация:**
```bash
mvn flyway:validate
```

### **Ошибка: "Connection refused"**
- Проверьте, что PostgreSQL запущен
- Проверьте настройки подключения
- Проверьте права доступа пользователя