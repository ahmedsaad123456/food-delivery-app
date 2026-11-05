package com.example.food_delivery_app.order.repository;

import com.example.food_delivery_app.order.entity.driver.Driver;
import com.example.food_delivery_app.order.entity.driver.DriverStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Integer> {

    boolean existsByPhone(String phone);

    List<Driver> findByStatus(DriverStatus status);

    // Atomic "change status only if it is currently X". Same idea as the stock deduction:
    // the check and the change are ONE statement, so two requests can never both win.
    @Modifying
    @Query("update Driver d set d.status = :newStatus where d.id = :id and d.status = :expected")
    int changeStatusIfCurrently(@Param("id") Integer id,
                                @Param("expected") DriverStatus expected,
                                @Param("newStatus") DriverStatus newStatus);
}