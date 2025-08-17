# 🛡️ Security Headers Guide

## 📋 Обзор

Security Headers - это HTTP заголовки, которые защищают веб-приложение от различных типов атак.

## 🔧 Реализованные заголовки

### **1. X-Content-Type-Options: nosniff**
```http
X-Content-Type-Options: nosniff
```
- **Зачем:** Предотвращает MIME-sniffing атаки
- **Проблема:** Браузер может "угадывать" тип файла и выполнять вредоносный код
- **Защита:** Принуждает браузер использовать указанный Content-Type

### **2. X-Frame-Options: DENY**
```http
X-Frame-Options: DENY
```
- **Зачем:** Защищает от clickjacking атак
- **Проблема:** Злоумышленник может встроить ваш сайт в iframe и обмануть пользователя
- **Защита:** Запрещает встраивание страницы в iframe

### **3. X-XSS-Protection: 1; mode=block**
```http
X-XSS-Protection: 1; mode=block
```
- **Зачем:** Включает встроенную защиту браузера от XSS
- **Проблема:** Cross-Site Scripting атаки
- **Защита:** Блокирует выполнение подозрительных скриптов

### **4. Strict-Transport-Security**
```http
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
```
- **Зачем:** Принуждает использовать HTTPS
- **Проблема:** Man-in-the-middle атаки
- **Защита:** Браузер будет использовать только HTTPS в течение 1 года

### **5. Referrer-Policy**
```http
Referrer-Policy: strict-origin-when-cross-origin
```
- **Зачем:** Контролирует передачу referrer информации
- **Проблема:** Утечка конфиденциальных данных через referrer
- **Защита:** Ограничивает передачу referrer информации

### **6. Content-Security-Policy**
```http
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'; upgrade-insecure-requests
```
- **Зачем:** Контролирует, какие ресурсы можно загружать
- **Проблема:** XSS, инъекции скриптов
- **Защита:** Ограничивает источники загружаемых ресурсов

### **7. Permissions-Policy**
```http
Permissions-Policy: geolocation=(), microphone=(), camera=(), payment=(), usb=(), magnetometer=(), gyroscope=(), accelerometer=()
```
- **Зачем:** Контролирует доступ к API браузера
- **Проблема:** Несанкционированный доступ к устройствам
- **Защита:** Отключает доступ к чувствительным API

### **8. Cache-Control**
```http
Cache-Control: no-store, no-cache, must-revalidate, max-age=0
```
- **Зачем:** Предотвращает кэширование чувствительных данных
- **Проблема:** Утечка данных через кэш браузера
- **Защита:** Применяется к `/api/v1/**` и `/api/v1/auth/**` endpoints

## 🎯 Применение заголовков

### **Глобальные заголовки:**
- Применяются ко всем запросам
- Исключение: статические ресурсы (`/static/`, `/css/`, `/js/`, `/images/`)

### **Условные заголовки:**
- `Cache-Control` применяется только к API endpoints
- Разные настройки для разных типов ресурсов

## 🔍 Проверка заголовков

### **Через браузер:**
1. Откройте Developer Tools (F12)
2. Перейдите на вкладку Network
3. Выполните запрос к API
4. Проверьте заголовки ответа

### **Через curl:**
```bash
curl -I http://localhost:8080/api/v1/auth/login
```

### **Ожидаемые заголовки:**
```http
HTTP/1.1 200 OK
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
Referrer-Policy: strict-origin-when-cross-origin
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'; upgrade-insecure-requests
Permissions-Policy: geolocation=(), microphone=(), camera=(), payment=(), usb=(), magnetometer=(), gyroscope=(), accelerometer=()
Cache-Control: no-store, no-cache, must-revalidate, max-age=0
```

## 🚀 Преимущества:

- **Защита от XSS** - Content-Security-Policy, X-XSS-Protection
- **Защита от clickjacking** - X-Frame-Options
- **Принуждение HTTPS** - Strict-Transport-Security
- **Контроль кэширования** - Cache-Control
- **Защита от MIME-sniffing** - X-Content-Type-Options
- **Контроль API браузера** - Permissions-Policy
- **Контроль referrer** - Referrer-Policy