package io.github.danieledalia.tma.core.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramInitDataParserTest {

    private final TelegramInitDataParser parser = new TelegramInitDataParser();

    @Test
    void shouldParseRawData() {
        var rawData = parser.parseRawData("auth_date=1710000000&user=%7B%22id%22%3A1%2C%22first_name%22%3A%22Daniele%22%7D&hash=abc");

        assertThat(rawData.get("auth_date")).isEqualTo("1710000000");
        assertThat(rawData.get("user")).contains("Daniele");
        assertThat(rawData.get("hash")).isEqualTo("abc");
    }
}
