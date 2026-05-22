package io.github.danieledalia.tma.spring.context;

import io.github.danieledalia.tma.core.model.TelegramMiniAppContext;

import java.util.Optional;

public final class TelegramMiniAppContextHolder {

    private static final ThreadLocal<TelegramMiniAppContext> HOLDER = new ThreadLocal<>();

    private TelegramMiniAppContextHolder() {
    }

    public static void setContext(TelegramMiniAppContext context) {
        HOLDER.set(context);
    }

    public static Optional<TelegramMiniAppContext> getContext() {
        return Optional.ofNullable(HOLDER.get());
    }

    public static TelegramMiniAppContext getRequiredContext() {
        return getContext().orElseThrow(() -> new IllegalStateException("Telegram Mini App context is not available"));
    }

    public static void clear() {
        HOLDER.remove();
    }
}
