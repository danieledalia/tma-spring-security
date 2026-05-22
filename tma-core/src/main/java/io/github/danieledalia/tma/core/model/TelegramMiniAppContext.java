package io.github.danieledalia.tma.core.model;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public record TelegramMiniAppContext(
        TelegramUser user,
        TelegramChat chat,
        String queryId,
        String startParam,
        Instant authDate,
        Map<String, String> rawData
) {
    public Optional<TelegramUser> userOptional() {
        return Optional.ofNullable(user);
    }

    public Optional<TelegramChat> chatOptional() {
        return Optional.ofNullable(chat);
    }
}
