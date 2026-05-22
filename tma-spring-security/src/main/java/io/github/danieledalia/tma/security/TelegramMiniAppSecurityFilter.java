package io.github.danieledalia.tma.security;

import io.github.danieledalia.tma.core.exception.TelegramMiniAppValidationException;
import io.github.danieledalia.tma.core.model.TelegramMiniAppContext;
import io.github.danieledalia.tma.core.validation.TelegramInitDataValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TelegramMiniAppSecurityFilter extends OncePerRequestFilter {

    private final TelegramInitDataValidator validator;
    private final String headerName;
    private final boolean failOnMissingHeader;

    public TelegramMiniAppSecurityFilter(TelegramInitDataValidator validator, String headerName, boolean failOnMissingHeader) {
        this.validator = validator;
        this.headerName = headerName;
        this.failOnMissingHeader = failOnMissingHeader;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String initData = request.getHeader(headerName);

        if (initData == null || initData.isBlank()) {
            if (failOnMissingHeader) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing Telegram Mini App initData header");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        try {
            TelegramMiniAppContext context = validator.validate(initData);
            var token = new TelegramMiniAppAuthenticationToken(context);
            SecurityContextHolder.getContext().setAuthentication(token);
            filterChain.doFilter(request, response);
        } catch (TelegramMiniAppValidationException e) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
