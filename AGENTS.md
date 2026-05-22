# Agent Guidelines

## Before you start

1. Read `SPEC.md` to understand the project scope and requirements.
2. Run `mvn -B clean verify` to confirm the build passes before making changes.

## Project goal

Build a small, focused Java/Spring library for Telegram Mini Apps backend security.

The library validates Telegram Mini App `initData` and exposes a verified Telegram context to Spring MVC controllers and, optionally, Spring Security.

## Principles

- **Keep it small.** Do not add features beyond the spec.
- **No unnecessary dependencies.** No Lombok, no WebFlux, no database, no Docker, no Actuator.
- **Preserve module boundaries:**
  - `tma-core` — pure Java, no Spring dependency.
  - `tma-spring-boot-starter` — Spring Boot autoconfigure + Spring Web.
  - `tma-spring-security` — Spring Security integration only.
- **Do not change the public API** unless the spec requires it.
- **Run tests before finishing.** The build must pass: `mvn -B clean verify`.

## Non-goals

Do not implement:

- Telegram Bot API client
- Application user persistence
- Session management
- Payments
- Business roles
- WebFlux support

## Architecture rules

- Keep validation logic in `tma-core`.
- Keep Servlet request lifecycle code in `tma-spring-boot-starter`.
- Always clear ThreadLocal context in `finally`.

## Compatibility target

- Java 17
- Spring Boot 3.5.x
- Spring Security 6.x
- Spring MVC first
