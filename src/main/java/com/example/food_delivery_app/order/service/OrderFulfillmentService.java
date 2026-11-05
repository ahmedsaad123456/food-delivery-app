package com.example.food_delivery_app.order.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderFulfillmentService {

//    private static final int DEFAULT_ETA_MINUTES = 45;
//    // eta service is a service that estimates the time it will take for the order to be delivered
//    private final EtaService etaService;
//    private final DriverAssignmentService driverService;
//    private final OrderNotificationService notifications;
//    private final OrderPlanService planService;
//    private final ThreadPoolTaskExecutor executor;
//
//    public OrderFulfillmentService(EtaService etaService,
//                                   DriverAssignmentService driverService,
//                                   OrderNotificationService notifications,
//                                   OrderPlanService planService,
//                                   @Qualifier("orderExecutor") ThreadPoolTaskExecutor executor) {
//        this.etaService = etaService;
//        this.driverService = driverService;
//        this.notifications = notifications;
//        this.planService = planService;
//        this.executor = executor;
//    }
//
//    /** Called AFTER the payment transaction committed. Returns a future so the caller CAN wait or just ignore it. */
//    public CompletableFuture<Void> fulfill(int orderId) {
//
//        // 1) ETA: if it fails we still have a sensible default
         // run the task asynchronously on the executor, and if it fails, return a default value
//        CompletableFuture<Integer> eta = CompletableFuture
//                .supplyAsync(() -> etaService.estimateMinutes(orderId), executor) // run the task asynchronously on the executor and return a CompletableFuture that will complete with the result of the task
//                .exceptionally(ex -> {
//                    log.warn("ETA failed for order {}, using default", orderId, ex);
//                    return DEFAULT_ETA_MINUTES;
//                });
//
//        // 2) Driver: failure is NOT fatal, it just means "no driver yet" (empty)
         // run the task asynchronously on the executor, and if it fails, return an empty Optional
//        CompletableFuture<Optional<Integer>> driver = CompletableFuture
//                .supplyAsync(() -> Optional.of(driverService.assignDriver(orderId)), executor)
//                .exceptionally(ex -> {
//                    log.warn("Driver assignment failed for order {}", orderId, ex);
//                    return Optional.empty();
//                });
//
//        // 3) Notifications: independent of the other two; failure only gets logged
          // run the task asynchronously on the executor, and if it fails, log the error and return null
//        CompletableFuture<Void> notified = CompletableFuture
//                .runAsync(() -> notifications.sendPaid(orderId), executor) // run async does not return a value, so we use runAsync instead of supplyAsync
//                .exceptionally(ex -> {
//                    log.error("Paid notification failed for order {}", orderId, ex);
//                    return null;
//                });
//
//        // 4) Needs BOTH eta and driver: combine them, then save (on the executor, not on whatever thread finished last)
          // eta and driver are independent, but we need both to create a DeliveryPlan.
          // We use thenCombine to combine the results of both futures,
         // and thenAcceptAsync to save the plan on the executor.
         // If either future fails, the combined future will also fail, and we will not save the plan.
//        CompletableFuture<Void> planSaved = eta
//                .thenCombine(driver, (etaMinutes, driverId) -> new DeliveryPlan(etaMinutes, driverId))
//                .thenAcceptAsync(plan -> planService.save(orderId, plan), executor);
//
//        // 5) Done when everything is done
//        return CompletableFuture.allOf(planSaved, notified);
//    }
}

// inside the handler, AFTER the payment transaction committed
//fulfillment.fulfill(orderId)
//        .whenComplete((v, ex) -> { // when complete is called when the future is completed, either normally or exceptionally
//        if (ex != null) log.error("Fulfillment failed for order {}", orderId, ex);
//        });
//   no join(): the webhook returns 200 to Stripe immediately
// if we use Join(), the webhook will wait for the fulfillment to complete, which can take a long time and cause Stripe to retry the webhook.
// We want to return 200 immediately, and let the fulfillment run in the background.