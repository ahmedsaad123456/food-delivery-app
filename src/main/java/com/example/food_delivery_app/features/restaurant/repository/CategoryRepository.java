package com.example.food_delivery_app.features.restaurant.repository;


import com.example.food_delivery_app.features.restaurant.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
        }