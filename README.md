# tma-spring-security

[![CI](https://github.com/danieledalia/tma-spring-security/actions/workflows/ci.yml/badge.svg)](https://github.com/danieledalia/tma-spring-security/actions/workflows/ci.yml)
![Java 17+](https://img.shields.io/badge/Java-17%2B-blue)
![Spring Boot 3.5.x](https://img.shields.io/badge/Spring%20Boot-3.5.x-green)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> **Pre-release** — API may change before `0.1.0`. Not yet published to Maven Central.

Validate Telegram Mini App `initData` in your Spring Boot backend.  
One dependency, one config line, done.

---

## Quick Start

### Install locally

```bash
mvn clean install
```

### 1. Add dependency

```xml
<dependency>
    <groupId>io.github.danieledalia</groupId>
    <artifactId>tma-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

### 2. Configure bot token

```yaml
telegram:
  miniapp:
    bot-token: ${TELEGRAM_BOT_TOKEN}
```

### 3. Use in controller

```java
@GetMapping("/me")
public TelegramUser me(@TelegramMiniAppUser TelegramUser user) {
    return user;
}
```

That's it. Requests without a valid Telegram signature get a `401`.

---

## Frontend

Send `Telegram.WebApp.initData` in a header with every request:

```javascript
const response = await fetch('https://your-backend.com/api/me', {
  headers: {
    'X-Telegram-Init-Data': window.Telegram.WebApp.initData
  }
});

const user = await response.json();
// { "id": 123456, "first_name": "Daniele", "username": "daniele", ... }
```

---

## Spring Security integration

Add the security module for `@AuthenticationPrincipal` support:

```xml
<dependency>
    <groupId>io.github.danieledalia</groupId>
    <artifactId>tma-spring-security</artifactId>
    <version>0.1.0</version>
</dependency>
```

Then use `@AuthenticationPrincipal` in your controllers:

```java
@GetMapping("/me")
public String me(@AuthenticationPrincipal TelegramMiniAppPrincipal principal) {
    return "Hello, " + principal.user().firstName();
}
```

The security module auto-configures a `SecurityFilterChain` that validates the `X-Telegram-Init-Data` header and sets the `SecurityContext`. CSRF is disabled by default (Mini Apps use header-based auth).

---

## How it works

```
┌─────────────────┐       X-Telegram-Init-Data       ┌─────────────────────┐
│  Telegram Mini  │ ─────────────────────────────────▶│   Spring Boot App   │
│      App        │                                   │                     │
└─────────────────┘                                   │  1. Read header     │
                                                      │  2. Verify HMAC     │
                                                      │  3. Check auth_date │
                                                      │  4. Inject user ✓   │
                                                      └─────────────────────┘
```

---

## Configuration

| Property                              | Default                | Description                        |
| ------------------------------------- | ---------------------- | ---------------------------------- |
| `telegram.miniapp.bot-token`          | —                      | Your Telegram bot token (required) |
| `telegram.miniapp.header-name`        | `X-Telegram-Init-Data` | HTTP header to read initData from  |
| `telegram.miniapp.max-auth-age`       | `1h`                   | Reject initData older than this    |
| `telegram.miniapp.fail-on-missing-header` | `false`            | Return 401 if header is absent     |
| `telegram.miniapp.enabled`            | `true`                 | Disable the filter entirely        |

---

## Modules

| Module                    | Purpose                                                      |
| ------------------------- | ------------------------------------------------------------ |
| `tma-core`                | Pure Java parser & HMAC-SHA256 validator (no Spring)         |
| `tma-spring-boot-starter` | Auto-configuration, servlet filter, `@TelegramMiniAppUser`  |
| `tma-spring-security`     | Spring Security filter, `@AuthenticationPrincipal` support   |

---

## Advanced usage

Access the full context (user, chat, query_id, start_param):

```java
@GetMapping("/context")
public TelegramMiniAppContext context(@TelegramMiniApp TelegramMiniAppContext ctx) {
    return ctx;
}
```

Or read it anywhere via the holder:

```java
TelegramMiniAppContext ctx = TelegramMiniAppContextHolder.getRequiredContext();
```

---

## Scope

This library verifies Telegram identity. It does **not** manage application users, sessions, roles, or database mappings.

## License

MIT
