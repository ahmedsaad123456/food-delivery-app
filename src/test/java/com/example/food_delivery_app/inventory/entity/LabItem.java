package com.example.food_delivery_app.inventory.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Plain entity: no @Version. Used by the naive, pessimistic and atomic variants.
@Entity
@Table(name = "inventory_lab")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabItem {
    @Id
    private Integer id;
    private int stock;


}