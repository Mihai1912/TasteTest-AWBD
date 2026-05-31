package com.example.restaurant.service.dto;

import java.util.UUID;

public class RestaurantDto {
    private UUID id;
    private String name;
    private String address;
    private String phone;
    private String website;
    private String schedule;

    public RestaurantDto() {}

    public RestaurantDto(UUID id, String name, String address, String phone, String website, String schedule) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.website = website;
        this.schedule = schedule;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
}
