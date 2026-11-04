package com.example.food_delivery_app.inventory.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Same table, but with @Version. Used only by the optimistic variant.
@Entity
@Table(name = "inventory_lab")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LabItemVersioned {
    @Id
    private Integer id;
    private int stock;

    @Version            // Hibernate adds "AND version = ?" to every UPDATE and bumps it
    private long version;

}