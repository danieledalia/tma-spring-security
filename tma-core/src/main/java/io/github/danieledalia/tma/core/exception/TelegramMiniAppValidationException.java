package io.github.danieledalia.tma.core.exception;

public class TelegramMiniAppValidationException extends RuntimeException {
    public TelegramMiniAppValidationException(String message) {
        super(message);
    }

    public TelegramMiniAppValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
