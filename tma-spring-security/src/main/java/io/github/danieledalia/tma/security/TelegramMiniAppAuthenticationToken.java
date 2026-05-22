package io.github.danieledalia.tma.security;

import io.github.danieledalia.tma.core.model.TelegramMiniAppContext;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public class TelegramMiniAppAuthenticationToken extends AbstractAuthenticationToken {

    private final TelegramMiniAppPrincipal principal;

    public TelegramMiniAppAuthenticationToken(TelegramMiniAppContext context) {
        this(context, List.of());
    }

    public TelegramMiniAppAuthenticationToken(
            TelegramMiniAppContext context,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.principal = new TelegramMiniAppPrincipal(context);
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public TelegramMiniAppPrincipal getPrincipal() {
        return principal;
    }
}
