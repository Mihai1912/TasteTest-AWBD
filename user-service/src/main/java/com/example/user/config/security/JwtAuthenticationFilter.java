package com.example.user.config.security;

import com.example.user.config.TokenProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final TokenProperties tokenProperties;

    public JwtAuthenticationFilter(TokenProperties tokenProperties) {
        this.tokenProperties = tokenProperties;
    }

    private SecretKey signingKey() {
        return new SecretKeySpec(tokenProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA512");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getJWTFromRequest(request);
        logger.info("[JWT-FILTER] uri={} method={} bearer-present={}",
                request.getRequestURI(), request.getMethod(), token != null);
        if (StringUtils.hasText(token)) {
            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(signingKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String email = claims.getSubject();
                List<String> rawRoles = extractRoles(claims);
                List<SimpleGrantedAuthority> authorities = rawRoles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.info("[JWT-FILTER] OK email={} roles={} authorities={}",
                        email, rawRoles, authorities);
            } catch (Exception ex) {
                logger.warn("[JWT-FILTER] validation FAILED: {} - {}",
                        ex.getClass().getSimpleName(), ex.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return Collections.emptyList();
    }

    private String getJWTFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
