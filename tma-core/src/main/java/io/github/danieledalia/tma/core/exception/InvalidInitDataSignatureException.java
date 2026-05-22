package io.github.danieledalia.tma.core.exception;

public class InvalidInitDataSignatureException extends TelegramMiniAppValidationException {
    public InvalidInitDataSignatureException() {
        super("Telegram Mini App initData signature is invalid");
    }
}
