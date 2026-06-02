package com.example.agent.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "message must not be blank")
        @Size(max = 4000, message = "message too long")
        String message
) {}
