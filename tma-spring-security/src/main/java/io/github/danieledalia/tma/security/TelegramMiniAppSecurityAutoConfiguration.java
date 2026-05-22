package io.github.danieledalia.tma.security;

import io.github.danieledalia.tma.core.validation.TelegramInitDataValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@AutoConfiguration
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnBean(TelegramInitDataValidator.class)
public class TelegramMiniAppSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TelegramMiniAppSecurityFilter.class)
    public TelegramMiniAppSecurityFilter telegramMiniAppSecurityFilter(TelegramInitDataValidator validator) {
        return new TelegramMiniAppSecurityFilter(validator, "X-Telegram-Init-Data", false);
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain telegramSecurityFilterChain(HttpSecurity http, TelegramMiniAppSecurityFilter filter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .build();
    }
}
