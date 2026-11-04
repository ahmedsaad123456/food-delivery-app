package com.example.food_delivery_app.inventory.repository;


import com.example.food_delivery_app.inventory.entity.LabItemVersioned;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabItemVersionedRepository extends JpaRepository<LabItemVersioned, Integer> {}