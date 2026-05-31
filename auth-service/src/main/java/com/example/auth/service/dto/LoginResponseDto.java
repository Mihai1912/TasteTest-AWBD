package com.example.auth.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponseDto(
        @JsonProperty("access_token") String token,
        @JsonProperty("token_type") String type,
        @JsonProperty("expires_in") long expire
) {}
