package io.github.danieledalia.tma.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "telegram.miniapp")
public class TelegramMiniAppProperties {

    private boolean enabled = true;
    private String botToken;
    private String headerName = "X-Telegram-Init-Data";
    private Duration maxAuthAge = Duration.ofHours(1);
    private boolean failOnMissingHeader = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public Duration getMaxAuthAge() {
        return maxAuthAge;
    }

    public void setMaxAuthAge(Duration maxAuthAge) {
        this.maxAuthAge = maxAuthAge;
    }

    public boolean isFailOnMissingHeader() {
        return failOnMissingHeader;
    }

    public void setFailOnMissingHeader(boolean failOnMissingHeader) {
        this.failOnMissingHeader = failOnMissingHeader;
    }
}
