package com.example.user.config.security;

import com.example.user.config.TokenProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
public class JwtGenerator {

    private final TokenProperties tokenProperties;

    public JwtGenerator(TokenProperties tokenProperties) {
        this.tokenProperties = tokenProperties;
    }

    private SecretKey signingKey() {
        return new SecretKeySpec(tokenProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA512");
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT was expired or incorrect");
        }
    }
}
