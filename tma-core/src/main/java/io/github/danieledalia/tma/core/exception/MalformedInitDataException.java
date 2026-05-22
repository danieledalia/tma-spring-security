package io.github.danieledalia.tma.core.exception;

public class MalformedInitDataException extends TelegramMiniAppValidationException {
    public MalformedInitDataException(String message) {
        super(message);
    }

    public MalformedInitDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
