package com.example.food_delivery_app.users.repository;

import com.example.food_delivery_app.users.entity.Role;
import com.example.food_delivery_app.users.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {
    Optional<RoleEntity> findByName(Role name);

    @Query(value = "select name from roles", nativeQuery = true)
    List<String> findAllNames();
}
