package com.example.food_delivery_app.order.service;

import com.example.food_delivery_app.common.ErrorCode;
import com.example.food_delivery_app.exceptions.AppException;
import com.example.food_delivery_app.order.assignment.DriverCandidate;
import com.example.food_delivery_app.order.assignment.PickupLocation;
import com.example.food_delivery_app.order.assignment.strategy.DriverAssignmentStrategy;
import com.example.food_delivery_app.order.dto.driver.AssignDriverRequest;
import com.example.food_delivery_app.order.dto.driver.DriverResponse;
import com.example.food_delivery_app.order.entity.driver.DriverLoad;
import com.example.food_delivery_app.order.entity.driver.Driver;
import com.example.food_delivery_app.order.entity.driver.DriverStatus;
import com.example.food_delivery_app.order.mapper.DriverMapper;
import com.example.food_delivery_app.order.repository.OrderRepository;
import com.example.food_delivery_app.order.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Picks a driver for an order and books him safely.
 * Two separate jobs, on purpose:
 *   1) CHOOSING a driver  -> delegated to the strategy (nearest, least busy, ...).
 *   2) BOOKING the driver -> done here, atomically, so two orders can never get the same driver.
 */
@Service
@RequiredArgsConstructor
public class DriverAssignmentService {

    // How many times we retry if somebody else grabs "our" driver before we do.
    private static final int MAX_ATTEMPTS = 3;

    private final DriverRepository drivers;
    private final OrderRepository orders;
    private final DriverAssignmentStrategy strategy; // the one bean chosen by configuration
    private final DriverMapper driverMapper;
    private final TransactionTemplate tx;


    public DriverResponse assignDriverToOrder(Integer orderId, AssignDriverRequest request) {
        PickupLocation pickup = new PickupLocation(request.latitude(), request.longitude());

        // The whole assignment process runs in a single transaction, so either the driver is booked AND the order is updated, or nothing changes.
        Optional<Driver> driver = tx.execute(status -> tryAssignDriver(orderId, pickup));

        return driver
                .map(driverMapper::toDriverResponse)
                .orElseThrow(() -> new AppException(ErrorCode.NO_DRIVER_AVAILABLE, orderId));
    }

    // Everything below runs in ONE database transaction:
    // either the driver is booked AND the order is updated, or nothing changes.
    public Optional<Driver> tryAssignDriver(Integer orderId, PickupLocation pickup) {
        if (!orders.existsById(orderId)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND, orderId); // use your real error code
        }
        // Retry loop: each attempt reloads the available drivers, so a driver
        // taken by someone else in the meantime is no longer in the list.
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

            // Step 1: load all drivers that are currently AVAILABLE.
            List<Driver> available = drivers.findByStatus(DriverStatus.AVAILABLE);
            if (available.isEmpty()) return Optional.empty(); // nobody to assign, stop

            // Step 2: ask the strategy to choose one.
            // We pass candidates (driver + how many active orders he has).
            Optional<Driver> chosen = strategy.select(pickup, getDriversWithAssignedOrders(available)); // trusts ONLY the contract
            if (chosen.isEmpty()) return Optional.empty(); // strategy found no suitable driver

            // Step 3: book him atomically.
            // The strategy only SUGGESTS. Booking must be atomic: another request may have taken him meanwhile.
            // This is a single conditional UPDATE, roughly:
            //   UPDATE driver SET status = 'BUSY' WHERE id = ? AND status = 'AVAILABLE'
            // It returns how many rows it changed: 1 = we won, 0 = somebody else was faster.
            int booked = drivers.changeStatusIfCurrently(chosen.get().getId(), DriverStatus.AVAILABLE, DriverStatus.BUSY);
            if (booked == 1) {
                // We own this driver now: link him to the order and return him.
                int updated = orders.assignDriver(orderId, chosen.get().getId());
                if (updated != 1) {
                    // the order already has a driver (or vanished): throwing rolls back the BUSY booking above
                    throw new AppException(ErrorCode.ORDER_HAS_ALREADY_DRIVER , orderId);
                }
                return chosen;
            }
            // booked == 0: somebody else won this driver, reload the list and choose again
        }

        // All attempts lost the race (very unlikely): give up, the caller decides what to do.
        return Optional.empty();
    }

    // Wraps each available driver with his current load (number of active orders),
    // because the strategy may want to prefer less busy drivers.
    private List<DriverCandidate> getDriversWithAssignedOrders(List<Driver> available) {
        // Collect the ids, so we can ask the database for all loads in ONE query (no N+1 problem).
        List<Integer> ids = available.stream().map(Driver::getId).toList();

        // Query result looks like: driverId -> activeOrders. Turn it into a Map for fast lookup.
        // (int) cast: the query returns a long count, the candidate wants an int.
        Map<Integer, Integer> load = orders.countActiveOrdersByDriver(ids).stream()
                .collect(Collectors.toMap(DriverLoad::getDriverId, l -> (int) l.getActiveOrders()));

        return available.stream()
                .map(d -> new DriverCandidate(d, load.getOrDefault(d.getId(), 0))) // drivers with no orders are missing from the GROUP BY result
                .toList();
    }
}