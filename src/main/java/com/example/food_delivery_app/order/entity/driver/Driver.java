package com.example.food_delivery_app.order.entity.driver;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor // JPA needs a no-arg constructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // matches GENERATED ALWAYS AS IDENTITY
    private Integer id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 30)
    private String phone;

    @Enumerated(EnumType.STRING) // store "AVAILABLE" as text, NOT 0/1/2 (reordering the enum would corrupt data)
    @Column(nullable = false, length = 20)
    private DriverStatus status = DriverStatus.AVAILABLE;

    // BigDecimal because the column is NUMERIC. A Java double maps to float8 and schema validation would complain.
    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist // runs just before the first INSERT
    void onCreate() { createdAt = Instant.now(); updatedAt = createdAt; }

    @PreUpdate // runs just before every UPDATE
    void onUpdate() { updatedAt = Instant.now(); }
}