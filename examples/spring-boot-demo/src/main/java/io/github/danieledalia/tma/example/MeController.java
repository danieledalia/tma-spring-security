package io.github.danieledalia.tma.example;

import io.github.danieledalia.tma.core.model.TelegramMiniAppContext;
import io.github.danieledalia.tma.core.model.TelegramUser;
import io.github.danieledalia.tma.spring.annotation.TelegramMiniApp;
import io.github.danieledalia.tma.spring.annotation.TelegramMiniAppUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MeController {

    @GetMapping("/me")
    public TelegramUser me(@TelegramMiniAppUser TelegramUser user) {
        return user;
    }

    @GetMapping("/context")
    public TelegramMiniAppContext context(@TelegramMiniApp TelegramMiniAppContext context) {
        return context;
    }
}
