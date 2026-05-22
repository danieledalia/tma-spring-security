package io.github.danieledalia.tma.spring;

import io.github.danieledalia.tma.core.model.TelegramMiniAppContext;
import io.github.danieledalia.tma.core.model.TelegramUser;
import io.github.danieledalia.tma.spring.annotation.TelegramMiniApp;
import io.github.danieledalia.tma.spring.annotation.TelegramMiniAppUser;
import io.github.danieledalia.tma.spring.context.TelegramMiniAppContextHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        classes = TelegramMiniAppFilterTest.TestApp.class,
        properties = {
                "telegram.miniapp.bot-token=test-bot-token:secret",
                "telegram.miniapp.max-auth-age=0"
        }
)
@AutoConfigureMockMvc
class TelegramMiniAppFilterTest {

    private static final String BOT_TOKEN = "test-bot-token:secret";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void validHeaderInjectsUser() throws Exception {
        mockMvc.perform(get("/test/user").header("X-Telegram-Init-Data", validInitData()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.first_name").value("Test"));
    }

    @Test
    void validHeaderInjectsContext() throws Exception {
        mockMvc.perform(get("/test/context").header("X-Telegram-Init-Data", validInitData()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(42));
    }

    @Test
    void missingHeaderWithoutFailOnMissing() throws Exception {
        mockMvc.perform(get("/test/open"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void invalidHeaderReturns401() throws Exception {
        mockMvc.perform(get("/test/user").header("X-Telegram-Init-Data", "invalid=data&hash=bad"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void contextHolderClearedAfterRequest() throws Exception {
        mockMvc.perform(get("/test/user").header("X-Telegram-Init-Data", validInitData()))
                .andExpect(status().isOk());

        assertThat(TelegramMiniAppContextHolder.getContext()).isEmpty();
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

    @SpringBootApplication
    static class TestApp {

        @Controller
        @ResponseBody
        static class TestController {

            @GetMapping("/test/user")
            TelegramUser user(@TelegramMiniAppUser TelegramUser user) {
                return user;
            }

            @GetMapping("/test/context")
            TelegramMiniAppContext context(@TelegramMiniApp TelegramMiniAppContext ctx) {
                return ctx;
            }

            @GetMapping("/test/open")
            String open() {
                return "ok";
            }
        }
    }
}
