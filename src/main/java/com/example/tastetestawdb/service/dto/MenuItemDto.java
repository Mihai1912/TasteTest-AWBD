package com.example.tastetestawdb.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class MenuItemDto {
    private String id;
    private String menuId;

    @NotBlank(message = "Numele articolului este obligatoriu")
    @Size(max = 150, message = "Numele poate avea maximum 150 de caractere")
    private String name;

    @NotNull(message = "Pretul este obligatoriu")
    @DecimalMin(value = "0.0", inclusive = true, message = "Pretul nu poate fi negativ")
    private BigDecimal price;

    @Size(max = 500, message = "Descrierea poate avea maximum 500 de caractere")
    private String description;

    public MenuItemDto(String id, String menuId, String name, BigDecimal price, String description) {
        this.id = id;
        this.menuId = menuId;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public MenuItemDto(String name, BigDecimal price, String description) {
        this(null, null, name, price, description);
    }

    public MenuItemDto(){}

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getMenuId() { return menuId; }

    public void setMenuId(String menuId) { this.menuId = menuId; }

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

