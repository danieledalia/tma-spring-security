package io.github.danieledalia.tma.core.validation;

import io.github.danieledalia.tma.core.exception.ExpiredInitDataException;
import io.github.danieledalia.tma.core.exception.InvalidInitDataSignatureException;
import io.github.danieledalia.tma.core.exception.MalformedInitDataException;
import io.github.danieledalia.tma.core.model.TelegramMiniAppContext;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;

public class TelegramInitDataValidator {

    private static final String HASH_FIELD = "hash";
    private static final String WEB_APP_DATA_KEY = "WebAppData";
    private static final HexFormat HEX = HexFormat.of();

    private final String botToken;
    private final Duration maxAuthAge;
    private final Clock clock;
    private final TelegramInitDataParser parser;

    public TelegramInitDataValidator(String botToken, Duration maxAuthAge) {
        this(botToken, maxAuthAge, Clock.systemUTC(), new TelegramInitDataParser());
    }

    public TelegramInitDataValidator(
            String botToken,
            Duration maxAuthAge,
            Clock clock,
            TelegramInitDataParser parser
    ) {
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalArgumentException("Telegram bot token must not be blank");
        }
        this.botToken = botToken;
        this.maxAuthAge = maxAuthAge;
        this.clock = clock;
        this.parser = parser;
    }

    public TelegramMiniAppContext validate(String initData) {
        Map<String, String> rawData = parser.parseRawData(initData);
        validateSignature(rawData);
        TelegramMiniAppContext context = parser.parseContext(rawData);
        validateAuthDate(context.authDate());
        return context;
    }

    private void validateSignature(Map<String, String> rawData) {
        String receivedHash = rawData.get(HASH_FIELD);
        if (receivedHash == null || receivedHash.isBlank()) {
            throw new MalformedInitDataException("Missing hash in Telegram Mini App initData");
        }

        String dataCheckString = rawData.entrySet().stream()
                .filter(entry -> !HASH_FIELD.equals(entry.getKey()))
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\n"));

        byte[] secretKey = hmacSha256(WEB_APP_DATA_KEY.getBytes(StandardCharsets.UTF_8), botToken);
        String calculatedHash = HEX.formatHex(hmacSha256(secretKey, dataCheckString));

        if (!MessageDigest.isEqual(
                calculatedHash.getBytes(StandardCharsets.UTF_8),
                receivedHash.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new InvalidInitDataSignatureException();
        }
    }

    private void validateAuthDate(Instant authDate) {
        if (maxAuthAge == null || maxAuthAge.isZero() || maxAuthAge.isNegative()) {
            return;
        }
        Instant now = Instant.now(clock);
        if (authDate.plus(maxAuthAge).isBefore(now)) {
            throw new ExpiredInitDataException();
        }
    }

    private byte[] hmacSha256(byte[] key, String data) {
        return hmacSha256(key, data.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] hmacSha256(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot calculate HMAC-SHA256", e);
        }
    }
}
