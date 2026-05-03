package com.example.tastetestawdb.service.dto;

import java.math.BigDecimal;

public class MenuItemDto {
    private String name;
    private BigDecimal price;
    private String description;

    public MenuItemDto(String name, BigDecimal price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public MenuItemDto(){}

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

