package com.example.auth.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import com.example.auth.config.TokenProperties;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtGenerator {

    private final TokenProperties tokenProperties;

    public JwtGenerator(TokenProperties tokenProperties) {
        this.tokenProperties = tokenProperties;
    }

    private SecretKey signingKey() {
        // Use raw UTF-8 bytes of the configured secret. SecretKeySpec accepts any length
        // (Keys.hmacShaKeyFor would reject < 64 bytes for HS512).
        return new SecretKeySpec(tokenProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA512");
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + tokenProperties.getTtl());
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setIssuer("http://localhost:8090")
                .setExpiration(expireDate)
                .signWith(signingKey(), SignatureAlgorithm.HS512)
                .compact();
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
