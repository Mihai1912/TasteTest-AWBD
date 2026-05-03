package com.example.tastetestawdb.repository;

import com.example.tastetestawdb.entity.Category;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CategoryRepository extends CrudRepository<Category, UUID> {
}
