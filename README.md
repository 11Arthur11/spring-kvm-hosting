## 🔐 Authentication API

| Method | Endpoint | Request Body (JSON) | Description |
|:--------|:----------|:--------------------|:-------------|
| `POST` | `/api/v1/auth/register` | `{ "email": "string", "fullName": "string", "rawPassword": "string", "rawPasswordConfirm": "string" }` | ثبت‌نام کاربر جدید در سیستم |
| `POST` | `/api/v1/auth/logout` | — | خروج کاربر از سیستم و باطل‌کردن توکن فعال |
| `POST` | `/api/v1/auth/login` | `{ "email": "string", "password": "string", "rememberMe": boolean }` | ورود کاربر با ایمیل و رمز عبور (در صورت فعال بودن، rememberMe توکن بلندمدت ایجاد می‌کند) |
| `POST` | `/api/v1/auth/2fa-verify` | `"string"` | تأیید کد احراز هویت دو مرحله‌ای (2FA) برای ورود ایمن |
