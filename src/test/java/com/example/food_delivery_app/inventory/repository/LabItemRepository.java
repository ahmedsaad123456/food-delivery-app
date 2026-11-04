package com.example.food_delivery_app.inventory.repository;


import com.example.food_delivery_app.inventory.entity.LabItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface LabItemRepository extends JpaRepository<LabItem, Integer> {

    // Used ONLY by the naive variant: decrements with NO guard. Relative update (stock - q),
    // so the result is based on the real current value, and can go below zero.
    @Modifying
    @Query("update LabItem i set i.stock = i.stock - :q where i.id = :id")
    int deductUnguarded(@Param("id") Integer id, @Param("q") int q);

    // Item 03: SELECT ... FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from LabItem i where i.id = :id")
    Optional<LabItem> findByIdForUpdate(@Param("id") Integer id);

    // Item 04: check and decrement in ONE statement
    @Modifying
    @Query("update LabItem i set i.stock = i.stock - :q where i.id = :id and i.stock >= :q")
    int deductAtomic(@Param("id") Integer id, @Param("q") int q);
}