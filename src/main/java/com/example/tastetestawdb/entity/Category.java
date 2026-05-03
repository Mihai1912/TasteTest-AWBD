package com.example.tastetestawdb.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "categories", schema = "project")
public class Category {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id")
    private UUID id;
    @Column(name = "name")
    private String name;

    public Category(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public Category() {
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
