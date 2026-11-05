package com.example.food_delivery_app.order.assignment.strategy;


import com.example.food_delivery_app.order.assignment.PickupLocation;
import com.example.food_delivery_app.order.assignment.DriverCandidate;
import com.example.food_delivery_app.order.entity.driver.Driver;
import com.example.food_delivery_app.order.entity.driver.DriverStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class NearestDriverStrategy implements DriverAssignmentStrategy {

    @Override
    public Optional<Driver> select(PickupLocation pickupLocation, List<DriverCandidate> candidates) {
        return candidates.stream()
                .filter(c -> c.driver().getStatus() == DriverStatus.AVAILABLE)          // contract 2 (and 4)
                .min(Comparator
                        .comparingDouble((DriverCandidate c) -> distanceKm(pickupLocation, c.driver()))
                        .thenComparing(c -> c.driver().getId()))                         // tie-breaker: lowest id, so the result is stable
                .map(DriverCandidate::driver);                                           // min() gives Optional: empty when the list is empty (contract 1, 4)
    }

    /** Great-circle distance in km (haversine). A driver with no location is treated as infinitely far: ranked LAST but still selectable (contract 5). */
    static double distanceKm(PickupLocation p, Driver d) {
        if (d.getLatitude() == null || d.getLongitude() == null) {
            return Double.POSITIVE_INFINITY;
        }
        double lat = d.getLatitude().doubleValue();
        double lon = d.getLongitude().doubleValue();
        double dLat = Math.toRadians(lat - p.latitude());
        double dLon = Math.toRadians(lon - p.longitude());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(p.latitude())) * Math.cos(Math.toRadians(lat))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371.0 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)); // 6371 = Earth radius in km
    }
}