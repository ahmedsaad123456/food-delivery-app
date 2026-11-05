package com.example.food_delivery_app.order.assignment.strategy;

import com.example.food_delivery_app.order.assignment.DriverCandidate;
import com.example.food_delivery_app.order.assignment.PickupLocation;
import com.example.food_delivery_app.order.entity.driver.Driver;

import java.util.List;
import java.util.Optional;

public interface DriverAssignmentStrategy {

    /**
     * Chooses the driver who should deliver an order.
     * CONTRACT (every implementation MUST respect all of it, see DriverAssignmentStrategyContractTest):
     * CONTRACT for LSP (Liskov Substitution Principle): any implementation of this interface must be substitutable for any other implementation.
     * 1. Never returns null. "No driver" is expressed as Optional.empty().
     * 2. Only returns a driver whose status is AVAILABLE.
     * 3. The returned driver is one of the given candidates (never invented).
     * 4. An empty list, or a list without available drivers, returns Optional.empty() and never throws.
     * 5. If at least one AVAILABLE candidate exists, the result is present, even if that driver has no location.
     * 6. Does not modify the given list.
     *
     */
    Optional<Driver> select(PickupLocation pickupLocation, List<DriverCandidate> candidates);
}
