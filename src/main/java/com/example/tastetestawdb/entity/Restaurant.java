package com.example.tastetestawdb.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "restaurants", schema = "project")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    @Column(name = "name")
    private String name;
    @Column(name = "address")
    private String address;
    @Column(name = "phone")
    private String phone;
    @Column(name = "website")
    private String website;
    @Column(name = "schedule")
    private String schedule;
    @Column(name = "owner_id")
    private UUID ownerId;

    public Restaurant(UUID id, String name, String address, String phone, String website, String schedule, UUID ownerId) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.website = website;
        this.schedule = schedule;
        this.ownerId = ownerId;
    }

    public Restaurant() {
    }

    public UUID getId() {
        return id;
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

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }
}
