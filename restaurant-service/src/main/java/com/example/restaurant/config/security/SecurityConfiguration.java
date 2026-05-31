package com.example.restaurant.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableConfigurationProperties(com.example.restaurant.config.TokenProperties.class)
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Value("${vars.security.enable:true}")
    private boolean securityEnabled;

    @Autowired
    private JwtAuthEntryPoint authEntryPoint;

    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        if (securityEnabled) {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                    .anyRequest().authenticated())
                .exceptionHandling((exception)-> exception.authenticationEntryPoint(authEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> {});
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        }
        return http.build();
    }

    
}
