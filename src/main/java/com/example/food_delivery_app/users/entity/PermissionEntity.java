package com.example.food_delivery_app.users.entity;

import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "permissions")
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)                       // stores the NAME ("MENU_CREATE"), never the ordinal
    @Column(nullable = false, unique = true, length = 80)
    private Permission name;
}