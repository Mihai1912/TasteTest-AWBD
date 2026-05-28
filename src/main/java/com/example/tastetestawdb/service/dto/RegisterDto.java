package com.example.tastetestawdb.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Component;

@Component
public class RegisterDto {

    @NotBlank(message = "Email-ul este obligatoriu")
    @Email(message = "Email-ul nu este valid")
    private String email;

    @NotBlank(message = "Numele de utilizator este obligatoriu")
    @Size(min = 3, max = 50, message = "Numele de utilizator trebuie sa aiba intre 3 si 50 de caractere")
    private String username;

    @NotBlank(message = "Parola este obligatorie")
    @Size(min = 6, max = 100, message = "Parola trebuie sa aiba minimum 6 caractere")
    private String password;

    public String getEmail() {
        return email;
    }

    public RegisterDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public RegisterDto setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public RegisterDto setPassword(String password) {
        this.password = password;
        return this;
    }
}
