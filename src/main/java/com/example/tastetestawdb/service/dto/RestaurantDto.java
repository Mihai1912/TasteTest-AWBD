package com.example.tastetestawdb.service.dto;

import java.util.UUID;

public class RestaurantDto {
    private String name;
    private String address;
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
