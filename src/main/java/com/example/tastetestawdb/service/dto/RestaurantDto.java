package com.example.tastetestawdb.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class RestaurantDto {
    private UUID id;

    @NotBlank(message = "Numele restaurantului este obligatoriu")
    @Size(max = 150, message = "Numele poate avea maximum 150 de caractere")
    private String name;

    @NotBlank(message = "Adresa este obligatorie")
    private String address;

    @NotBlank(message = "Numarul de telefon este obligatoriu")
    @Size(max = 30, message = "Numarul de telefon poate avea maximum 30 de caractere")
    private String phone;

    private String website;
    private String schedule;

    public RestaurantDto() {
    }

    public RestaurantDto(String name, String address, String phone, String website, String schedule) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.website = website;
        this.schedule = schedule;
    }

    public RestaurantDto(UUID id, String name, String address, String phone, String website, String schedule) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.website = website;
        this.schedule = schedule;
    }

    public UUID getId() {
        return id;
    }

    public RestaurantDto setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getWebsite() {
        return website;
    }

    public String getSchedule() {
        return schedule;
    }

    public RestaurantDto setName(String name) {
        this.name = name;
        return this;
    }

    public RestaurantDto setAddress(String address) {
        this.address = address;
        return this;
    }

    public RestaurantDto setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public RestaurantDto setWebsite(String website) {
        this.website = website;
        return this;
    }

    public RestaurantDto setSchedule(String schedule) {
        this.schedule = schedule;
        return this;
    }
}
