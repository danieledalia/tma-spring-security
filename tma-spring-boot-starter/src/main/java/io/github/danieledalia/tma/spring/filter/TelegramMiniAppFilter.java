package io.github.danieledalia.tma.spring.filter;

import io.github.danieledalia.tma.core.exception.TelegramMiniAppValidationException;
import io.github.danieledalia.tma.core.model.TelegramMiniAppContext;
import io.github.danieledalia.tma.core.validation.TelegramInitDataValidator;
import io.github.danieledalia.tma.spring.config.TelegramMiniAppProperties;
import io.github.danieledalia.tma.spring.context.TelegramMiniAppContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TelegramMiniAppFilter extends OncePerRequestFilter {

    private final TelegramInitDataValidator validator;
    private final TelegramMiniAppProperties properties;

    public TelegramMiniAppFilter(TelegramInitDataValidator validator, TelegramMiniAppProperties properties) {
        this.validator = validator;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String initData = request.getHeader(properties.getHeaderName());

        if (initData == null || initData.isBlank()) {
            if (properties.isFailOnMissingHeader()) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing Telegram Mini App initData header");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        try {
            TelegramMiniAppContext context = validator.validate(initData);
            TelegramMiniAppContextHolder.setContext(context);
            filterChain.doFilter(request, response);
        } catch (TelegramMiniAppValidationException e) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
        } finally {
            TelegramMiniAppContextHolder.clear();
        }
    }
}
