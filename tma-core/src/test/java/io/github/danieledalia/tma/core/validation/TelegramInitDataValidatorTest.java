package io.github.danieledalia.tma.core.validation;

import io.github.danieledalia.tma.core.exception.ExpiredInitDataException;
import io.github.danieledalia.tma.core.exception.InvalidInitDataSignatureException;
import io.github.danieledalia.tma.core.exception.MalformedInitDataException;
import io.github.danieledalia.tma.core.model.TelegramMiniAppContext;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelegramInitDataValidatorTest {

    private static final String BOT_TOKEN = "7313957691:AAFnoCFXx8OjABCDEFGHIJKLMNOPQRSTUVW";
    private static final long AUTH_DATE_EPOCH = 1710000000L;
    private static final Instant FIXED_NOW = Instant.ofEpochSecond(AUTH_DATE_EPOCH + 60);
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);

    private String buildInitData(String botToken, long authDate, String userJson) {
        String encodedUser = URLEncoder.encode(userJson, StandardCharsets.UTF_8);
        String dataCheckString = "auth_date=" + authDate + "\nuser=" + userJson;

        byte[] secretKey = hmac("WebAppData".getBytes(StandardCharsets.UTF_8), botToken.getBytes(StandardCharsets.UTF_8));
        String hash = HexFormat.of().formatHex(hmac(secretKey, dataCheckString.getBytes(StandardCharsets.UTF_8)));

        return "auth_date=" + authDate + "&user=" + encodedUser + "&hash=" + hash;
    }

    private byte[] hmac(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void validInitData() {
        String userJson = "{\"id\":123,\"first_name\":\"Daniele\"}";
        String initData = buildInitData(BOT_TOKEN, AUTH_DATE_EPOCH, userJson);

        var validator = new TelegramInitDataValidator(BOT_TOKEN, Duration.ofHours(1), FIXED_CLOCK, new TelegramInitDataParser());
        TelegramMiniAppContext ctx = validator.validate(initData);

        assertThat(ctx.user().id()).isEqualTo(123L);
        assertThat(ctx.user().firstName()).isEqualTo("Daniele");
        assertThat(ctx.authDate()).isEqualTo(Instant.ofEpochSecond(AUTH_DATE_EPOCH));
    }

    @Test
    void invalidHash() {
        String initData = "auth_date=" + AUTH_DATE_EPOCH + "&user=%7B%22id%22%3A1%7D&hash=deadbeef";

        var validator = new TelegramInitDataValidator(BOT_TOKEN, Duration.ofHours(1), FIXED_CLOCK, new TelegramInitDataParser());

        assertThatThrownBy(() -> validator.validate(initData))
                .isInstanceOf(InvalidInitDataSignatureException.class);
    }

    @Test
    void missingHash() {
        String initData = "auth_date=" + AUTH_DATE_EPOCH + "&user=%7B%22id%22%3A1%7D";

        var validator = new TelegramInitDataValidator(BOT_TOKEN, Duration.ofHours(1), FIXED_CLOCK, new TelegramInitDataParser());

        assertThatThrownBy(() -> validator.validate(initData))
                .isInstanceOf(MalformedInitDataException.class)
                .hasMessageContaining("hash");
    }

    @Test
    void missingAuthDate() {
        // Build initData without auth_date but with valid hash for that data
        String userJson = "{\"id\":1}";
        String encodedUser = URLEncoder.encode(userJson, StandardCharsets.UTF_8);
        String dataCheckString = "user=" + userJson;

        byte[] secretKey = hmac("WebAppData".getBytes(StandardCharsets.UTF_8), BOT_TOKEN.getBytes(StandardCharsets.UTF_8));
        String hash = HexFormat.of().formatHex(hmac(secretKey, dataCheckString.getBytes(StandardCharsets.UTF_8)));

        String initData = "user=" + encodedUser + "&hash=" + hash;

        var validator = new TelegramInitDataValidator(BOT_TOKEN, Duration.ofHours(1), FIXED_CLOCK, new TelegramInitDataParser());

        assertThatThrownBy(() -> validator.validate(initData))
                .isInstanceOf(MalformedInitDataException.class)
                .hasMessageContaining("auth_date");
    }

    @Test
    void expiredAuthDate() {
        String userJson = "{\"id\":123,\"first_name\":\"Daniele\"}";
        String initData = buildInitData(BOT_TOKEN, AUTH_DATE_EPOCH, userJson);

        // Clock is 2 hours after auth_date, maxAuthAge is 1 hour
        Clock expiredClock = Clock.fixed(Instant.ofEpochSecond(AUTH_DATE_EPOCH + 7200), ZoneOffset.UTC);
        var validator = new TelegramInitDataValidator(BOT_TOKEN, Duration.ofHours(1), expiredClock, new TelegramInitDataParser());

        assertThatThrownBy(() -> validator.validate(initData))
                .isInstanceOf(ExpiredInitDataException.class);
    }

    @Test
    void maxAuthAgeNullDisablesExpiry() {
        String userJson = "{\"id\":123,\"first_name\":\"Daniele\"}";
        String initData = buildInitData(BOT_TOKEN, AUTH_DATE_EPOCH, userJson);

        Clock farFuture = Clock.fixed(Instant.ofEpochSecond(AUTH_DATE_EPOCH + 999999), ZoneOffset.UTC);
        var validator = new TelegramInitDataValidator(BOT_TOKEN, null, farFuture, new TelegramInitDataParser());

        TelegramMiniAppContext ctx = validator.validate(initData);
        assertThat(ctx.user().id()).isEqualTo(123L);
    }

    @Test
    void maxAuthAgeZeroDisablesExpiry() {
        String userJson = "{\"id\":123,\"first_name\":\"Daniele\"}";
        String initData = buildInitData(BOT_TOKEN, AUTH_DATE_EPOCH, userJson);

        Clock farFuture = Clock.fixed(Instant.ofEpochSecond(AUTH_DATE_EPOCH + 999999), ZoneOffset.UTC);
        var validator = new TelegramInitDataValidator(BOT_TOKEN, Duration.ZERO, farFuture, new TelegramInitDataParser());

        TelegramMiniAppContext ctx = validator.validate(initData);
        assertThat(ctx.user().id()).isEqualTo(123L);
    }
}
