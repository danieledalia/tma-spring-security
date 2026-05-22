package io.github.danieledalia.tma.core.exception;

public class ExpiredInitDataException extends TelegramMiniAppValidationException {
    public ExpiredInitDataException() {
        super("Telegram Mini App initData is expired");
    }
}
