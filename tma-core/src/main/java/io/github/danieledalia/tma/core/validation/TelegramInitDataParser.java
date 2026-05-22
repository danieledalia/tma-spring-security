package io.github.danieledalia.tma.core.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.danieledalia.tma.core.exception.MalformedInitDataException;
import io.github.danieledalia.tma.core.model.TelegramChat;
import io.github.danieledalia.tma.core.model.TelegramMiniAppContext;
import io.github.danieledalia.tma.core.model.TelegramUser;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class TelegramInitDataParser {

    private final ObjectMapper objectMapper;

    public TelegramInitDataParser() {
        this(new ObjectMapper());
    }

    public TelegramInitDataParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, String> parseRawData(String initData) {
        if (initData == null || initData.isBlank()) {
            throw new MalformedInitDataException("Telegram Mini App initData is blank");
        }

        Map<String, String> result = new LinkedHashMap<>();
        String[] pairs = initData.split("&");

        for (String pair : pairs) {
            if (pair.isBlank()) {
                continue;
            }

            int separatorIndex = pair.indexOf('=');
            if (separatorIndex <= 0) {
                throw new MalformedInitDataException("Invalid initData pair: " + pair);
            }

            String key = decode(pair.substring(0, separatorIndex));
            String value = decode(pair.substring(separatorIndex + 1));
            result.put(key, value);
        }

        return Collections.unmodifiableMap(result);
    }

    public TelegramMiniAppContext parseContext(Map<String, String> rawData) {
        try {
            TelegramUser user = parseJson(rawData.get("user"), TelegramUser.class);
            TelegramChat chat = parseJson(rawData.get("chat"), TelegramChat.class);
            Instant authDate = parseAuthDate(rawData.get("auth_date"));

            return new TelegramMiniAppContext(
                    user,
                    chat,
                    rawData.get("query_id"),
                    rawData.get("start_param"),
                    authDate,
                    rawData
            );
        } catch (MalformedInitDataException e) {
            throw e;
        } catch (Exception e) {
            throw new MalformedInitDataException("Cannot parse Telegram Mini App initData", e);
        }
    }

    private Instant parseAuthDate(String value) {
        if (value == null || value.isBlank()) {
            throw new MalformedInitDataException("Missing auth_date in Telegram Mini App initData");
        }
        try {
            return Instant.ofEpochSecond(Long.parseLong(value));
        } catch (NumberFormatException e) {
            throw new MalformedInitDataException("Invalid auth_date in Telegram Mini App initData", e);
        }
    }

    private <T> T parseJson(String value, Class<T> type) throws Exception {
        if (value == null || value.isBlank()) {
            return null;
        }
        return objectMapper.readValue(value, type);
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
