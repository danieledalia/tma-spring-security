# tma-spring-security SPEC

## Purpose

`tma-spring-security` provides backend security support for Telegram Mini Apps in Java/Spring applications.

The library validates raw Telegram Mini App `initData` using the bot token and exposes a verified context for request handling.

## Main user story

As a Spring Boot backend developer, I want to add one dependency and configure my Telegram bot token, so that requests from my Telegram Mini App are validated automatically and my controllers can access the verified Telegram user.

## Required MVP features

### Core

- Parse raw `initData` query string.
- Validate required `hash` field.
- Calculate Telegram Mini App data-check-string.
- Validate HMAC-SHA256 signature using the bot token.
- Parse `auth_date`.
- Reject expired initData when `maxAuthAge` is configured.
- Parse `user`, `chat`, `query_id`, and `start_param` where present.

### Spring Boot Starter

- Auto-configure validator from properties.
- Read raw initData from configurable HTTP header.
- Validate each request when the header is present.
- Store validated context in a request-bound holder.
- Clear context after request completion.
- Support `@TelegramMiniAppUser` controller argument.
- Support `@TelegramMiniApp` controller argument.

### Spring Security

- Provide a `TelegramMiniAppAuthenticationToken`.
- Provide a `TelegramMiniAppPrincipal`.

## Configuration

```yaml
telegram:
  miniapp:
    enabled: true
    bot-token: ${TELEGRAM_BOT_TOKEN}
    header-name: X-Telegram-Init-Data
    max-auth-age: 1h
    fail-on-missing-header: false
```
