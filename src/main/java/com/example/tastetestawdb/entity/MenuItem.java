package com.example.tastetestawdb.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "menu_items", schema = "project")
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    @Column(name = "name")
    private String name;
    @Column(name = "price")
    private BigDecimal price;
    @Column(name = "description")
    private String description;
    @Column(name = "menu_id")
    private UUID menuId;

    // @ManyToOne: mai multe articole apartin unui meniu (read-only peste menu_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", insertable = false, updatable = false)
    private Menu menu;

    public MenuItem(UUID id, String name, BigDecimal price, String description, UUID menuId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.menuId = menuId;
    }

    public MenuItem() {
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public UUID getMenuId() {
        return menuId;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public void setMenuId(UUID menuId) {
        this.menuId = menuId;
    }

    public Menu getMenu() {
        return menu;
    }
}
