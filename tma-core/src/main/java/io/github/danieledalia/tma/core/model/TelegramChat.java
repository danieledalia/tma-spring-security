package io.github.danieledalia.tma.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramChat(
        Long id,
        String type,
        String title,
        String username,
        @JsonProperty("photo_url") String photoUrl
) {
}
