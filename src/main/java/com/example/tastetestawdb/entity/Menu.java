package com.example.tastetestawdb.entity;

import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "menus", schema = "project")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    @Column(name = "name")
    private String name;
    @Column(name = "restaurant_id")
    private UUID restaurantId;

    // @ManyToOne: mai multe meniuri apartin unui restaurant (read-only peste restaurant_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", insertable = false, updatable = false)
    private Restaurant restaurant;

    // @OneToMany: un meniu are mai multe articole (inversul lui MenuItem.menu)
    @OneToMany(mappedBy = "menu", fetch = FetchType.LAZY)
    private List<MenuItem> menuItems;

    public Menu(UUID id, String name, UUID restaurantId) {
        this.id = id;
        this.name = name;
        this.restaurantId = restaurantId;
    }

    public Menu() {
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRestaurantId(UUID restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }
}
