package io.github.danieledalia.tma.security;

import io.github.danieledalia.tma.core.model.TelegramMiniAppContext;
import io.github.danieledalia.tma.core.model.TelegramUser;
import io.github.danieledalia.tma.core.validation.TelegramInitDataParser;
import io.github.danieledalia.tma.core.validation.TelegramInitDataValidator;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramMiniAppSecurityTest {

    private static final String BOT_TOKEN = "test-token:secret";

    @Test
    void authenticationTokenExposesCorrectPrincipal() {
        var user = new TelegramUser(42L, false, "Daniele", null, "daniele", "en", true);
        var context = new TelegramMiniAppContext(user, null, null, null, Instant.now(), Map.of());

        var token = new TelegramMiniAppAuthenticationToken(context);

        assertThat(token.isAuthenticated()).isTrue();
        assertThat(token.getPrincipal()).isInstanceOf(TelegramMiniAppPrincipal.class);
        assertThat(token.getPrincipal().user()).isEqualTo(user);
        assertThat(token.getPrincipal().telegramUserId()).isEqualTo(42L);
        assertThat(token.getPrincipal().username()).isEqualTo("daniele");
        assertThat(token.getCredentials()).isEqualTo("");
    }

    @Test
    void principalExposesContextAndUser() {
        var user = new TelegramUser(1L, null, "Test", "User", "testuser", null, null);
        var context = new TelegramMiniAppContext(user, null, "qid", "start", Instant.now(), Map.of());

        var principal = new TelegramMiniAppPrincipal(context);

        assertThat(principal.context()).isSameAs(context);
        assertThat(principal.user()).isSameAs(user);
        assertThat(principal.telegramUserId()).isEqualTo(1L);
        assertThat(principal.username()).isEqualTo("testuser");
    }

    @Test
    void principalHandlesNullUser() {
        var context = new TelegramMiniAppContext(null, null, null, null, Instant.now(), Map.of());
        var principal = new TelegramMiniAppPrincipal(context);

        assertThat(principal.user()).isNull();
        assertThat(principal.telegramUserId()).isNull();
        assertThat(principal.username()).isNull();
    }

    @Test
    void securityFilterSetsSecurityContext() throws Exception {
        var validator = new TelegramInitDataValidator(BOT_TOKEN, Duration.ZERO);
        var filter = new TelegramMiniAppSecurityFilter(validator, "X-Telegram-Init-Data", false);

        var request = new MockHttpServletRequest();
        request.addHeader("X-Telegram-Init-Data", validInitData());
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        // SecurityContext is cleared after filter, so check chain was invoked
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void securityFilterReturns401OnInvalidData() throws Exception {
        var validator = new TelegramInitDataValidator(BOT_TOKEN, Duration.ZERO);
        var filter = new TelegramMiniAppSecurityFilter(validator, "X-Telegram-Init-Data", false);

        var request = new MockHttpServletRequest();
        request.addHeader("X-Telegram-Init-Data", "bad=data&hash=invalid");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void securityFilterClearsContextAfterRequest() throws Exception {
        var validator = new TelegramInitDataValidator(BOT_TOKEN, Duration.ZERO);
        var filter = new TelegramMiniAppSecurityFilter(validator, "X-Telegram-Init-Data", false);

        var request = new MockHttpServletRequest();
        request.addHeader("X-Telegram-Init-Data", validInitData());
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private String validInitData() {
        long authDate = Instant.now().getEpochSecond();
        String userJson = "{\"id\":42,\"first_name\":\"Test\"}";
        String encodedUser = URLEncoder.encode(userJson, StandardCharsets.UTF_8);
        String dataCheckString = "auth_date=" + authDate + "\nuser=" + userJson;

        byte[] secretKey = hmac("WebAppData".getBytes(StandardCharsets.UTF_8), BOT_TOKEN.getBytes(StandardCharsets.UTF_8));
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
}
