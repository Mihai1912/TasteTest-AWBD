package com.example.user.config;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.boot.context.properties.ConfigurationProperties;

@RefreshScope
@ConfigurationProperties(prefix = "token")
public class TokenProperties {

    private long ttl;
    private String secret;

    public long getTtl() { return ttl; }
    public void setTtl(long ttl) { this.ttl = ttl; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
}
