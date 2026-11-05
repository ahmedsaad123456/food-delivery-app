package com.example.food_delivery_app.order.assignment.strategy;

import com.example.food_delivery_app.order.assignment.DriverCandidate;
import com.example.food_delivery_app.order.assignment.PickupLocation;
import com.example.food_delivery_app.order.entity.driver.Driver;
import com.example.food_delivery_app.order.entity.driver.DriverStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LeastBusyDriverStrategy implements DriverAssignmentStrategy {

    @Override
    public Optional<Driver> select(PickupLocation pickupLocation, List<DriverCandidate> candidates) {
        return candidates.stream()
                .filter(c -> c.driver().getStatus() == DriverStatus.AVAILABLE)
                .min(Comparator
                        .comparingInt(DriverCandidate::activeOrders)  // fewest active orders first
                        .thenComparing(c -> c.driver().getId()))      // stable tie-breaker
                .map(DriverCandidate::driver);
    }
}
