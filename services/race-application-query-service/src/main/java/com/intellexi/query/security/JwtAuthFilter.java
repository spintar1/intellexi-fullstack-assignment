package com.intellexi.query.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            logger.debug("Processing JWT authentication for {} {}", method, requestURI);
            
            try {
                String secret = Optional.ofNullable(System.getenv("JWT_SECRET")).orElse("dev-shared-secret-please-change-this-is-a-very-long-secret-key-for-jwt-signing-that-is-at-least-256-bits-long");
                Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
                
                String email = claims.getSubject();
                String role = claims.get("role", String.class);
                
                Collection<? extends GrantedAuthority> auths = role == null ? List.of() : List.of(new SimpleGrantedAuthority("ROLE_" + role));
                AbstractAuthenticationToken auth = new AbstractAuthenticationToken(auths) {
                    @Override public Object getCredentials() { return token; }
                    @Override public Object getPrincipal() { return claims.getSubject(); }
                };
                auth.setAuthenticated(true);
                SecurityContextHolder.getContext().setAuthentication(auth);
                
                logger.info("Successfully authenticated user: {} with role: {} for {} {}", email, role != null ? role : "none", method, requestURI);
                
            } catch (Exception e) {
                logger.warn("JWT authentication failed for {} {} - Invalid token: {}", method, requestURI, e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            logger.debug("No Bearer token found for {} {}", method, requestURI);
        }
        
        filterChain.doFilter(request, response);
    }
}


