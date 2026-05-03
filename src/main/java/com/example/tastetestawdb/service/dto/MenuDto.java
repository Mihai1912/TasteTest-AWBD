package com.example.tastetestawdb.service.dto;

import java.util.UUID;

public class MenuDto {
    private String name;
    private UUID id;

    public MenuDto(String name, UUID id) {
        this.name = name;
        this.id = id;
    }

    public MenuDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
