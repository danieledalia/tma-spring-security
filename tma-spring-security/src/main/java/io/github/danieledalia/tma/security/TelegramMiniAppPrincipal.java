package io.github.danieledalia.tma.security;

import io.github.danieledalia.tma.core.model.TelegramMiniAppContext;
import io.github.danieledalia.tma.core.model.TelegramUser;

public record TelegramMiniAppPrincipal(TelegramMiniAppContext context) {

    public TelegramUser user() {
        return context.user();
    }

    public Long telegramUserId() {
        return context.user() == null ? null : context.user().id();
    }

    public String username() {
        return context.user() == null ? null : context.user().username();
    }
}
