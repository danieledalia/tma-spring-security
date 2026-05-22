package io.github.danieledalia.tma.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramUser(
        Long id,
        @JsonProperty("is_bot") Boolean bot,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String username,
        @JsonProperty("language_code") String languageCode,
        @JsonProperty("is_premium") Boolean premium
) {
}
