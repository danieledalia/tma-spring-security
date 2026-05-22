# Changelog

## 0.1.0 (2026-05-22)

### Added

- `tma-core`: Parse raw `initData` query string and validate HMAC-SHA256 signature using bot token.
- `tma-core`: Validate `auth_date` expiry with configurable `maxAuthAge`.
- `tma-core`: Parse `user`, `chat`, `query_id`, and `start_param` fields.
- `tma-spring-boot-starter`: Auto-configuration with `telegram.miniapp.*` properties.
- `tma-spring-boot-starter`: Servlet filter reads and validates `X-Telegram-Init-Data` header.
- `tma-spring-boot-starter`: `@TelegramMiniAppUser` argument resolver for controllers.
- `tma-spring-boot-starter`: `@TelegramMiniApp` argument resolver for full context.
- `tma-spring-boot-starter`: `TelegramMiniAppContextHolder` for programmatic access.
- `tma-spring-security`: `TelegramMiniAppAuthenticationToken` and `TelegramMiniAppPrincipal`.
- `tma-spring-security`: `TelegramMiniAppSecurityFilter` sets `SecurityContext` automatically.
- `tma-spring-security`: Auto-configured `SecurityFilterChain` with CSRF disabled.
