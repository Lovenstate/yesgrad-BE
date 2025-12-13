package com.yesgrad.service.config;

import com.yesgrad.service.service.JwtAuthenticationToken;
import com.yesgrad.service.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@org.springframework.core.annotation.Order(-100)
public class JwtAuthenticationWebFilter implements WebFilter {
    
    private final JwtService jwtService;
    private static final String AUTH_COOKIE_NAME = "auth_token";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.info("JWT Filter executing for: {}", exchange.getRequest().getPath());
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(AUTH_COOKIE_NAME);
        log.info("Cookie found: {}", cookie != null);
        
        if (cookie != null && jwtService.isTokenValid(cookie.getValue())) {
            String token = cookie.getValue();
            Long userId = jwtService.extractClaims(token).get("userId", Long.class);
            String role = jwtService.extractClaims(token).get("role", String.class);
            
            if (userId != null && role != null) {
                Authentication authentication = new JwtAuthenticationToken(
                    userId,
                    token,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                
                return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            }
        }
        
        return chain.filter(exchange);
    }
}
