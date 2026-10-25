package com.example.food_delivery_app.users.repository;

import com.example.food_delivery_app.users.entity.Permission;
import com.example.food_delivery_app.users.entity.PermissionEntity;
import com.example.food_delivery_app.users.entity.Role;
import com.example.food_delivery_app.users.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Integer> {
    Optional<PermissionEntity> findByName(Permission name);

    @Query(value = "select name from permissions", nativeQuery = true)
    List<String> findAllNames();
}
