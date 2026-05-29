package com.example.tastetestawdb.entity;

import jakarta.persistence.*;

import java.util.List;
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

    // ---------------------------------------------------------------------
    // Relatii JPA (adaugate aditiv, read-only, peste coloanele FK existente).
    // Campurile UUID de mai sus raman sursa de adevar pentru scriere, astfel
    // incat API-ul si logica existenta nu se modifica.
    // ---------------------------------------------------------------------

    // @OneToMany: un restaurant are mai multe meniuri (inversul lui Menu.restaurant)
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY)
    private List<Menu> menus;

    // @OneToMany: un restaurant are mai multe recenzii (inversul lui Review.restaurant)
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY)
    private List<Review> reviews;

    // @ManyToMany: un restaurant poate avea mai multe categorii, iar o categorie
    // poate apartine mai multor restaurante (tabela de jonctiune restaurant_categories)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "restaurant_categories", schema = "project",
            joinColumns = @JoinColumn(name = "restaurant_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categories;

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

    public List<Menu> getMenus() {
        return menus;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}
