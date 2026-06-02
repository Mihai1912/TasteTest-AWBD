package com.example.tastetestawdb.apigateway.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${token.secret}")
    private String jwtSecret;

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
        } catch (JwtException e) {
            logger.error("JWT validation failed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String getEmailFromToken(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (JwtException e) {
            logger.error("Error extracting email from token: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            Object roles = parseClaims(token).get("roles");
            if (roles instanceof List<?>) {
                return (List<String>) roles;
            }
            return Collections.emptyList();
        } catch (JwtException e) {
            logger.error("Error extracting roles from token: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private SecretKey signingKey() {
        // Match auth-service: raw UTF-8 bytes wrapped in SecretKeySpec.
        return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
