package com.example.auth.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Component;

@Component
public class LoginDto {
    @NotBlank(message = "Email-ul este obligatoriu")
    @Email(message = "Email-ul nu este valid")
    private String email;

    @NotBlank(message = "Parola este obligatorie")
    private String password;

    public String getEmail() {
        return email;
    }

    public LoginDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public LoginDto setPassword(String password) {
        this.password = password;
        return this;
    }
}
