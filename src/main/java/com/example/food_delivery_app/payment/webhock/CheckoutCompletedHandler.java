package com.example.food_delivery_app.payment.webhock;

import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.order.service.OrderStatusService;
import com.example.food_delivery_app.payment.outbox.OutboxEventTypes;
import com.example.food_delivery_app.payment.service.OutboxService;
import com.example.food_delivery_app.payment.outbox.PaymentSucceededPayload;
import com.example.food_delivery_app.restaurant.service.MenuItemService;
import com.example.food_delivery_app.order.entity.order.Order;
import com.example.food_delivery_app.order.entity.order.OrderStatus;
import com.example.food_delivery_app.order.repository.OrderRepository;
import com.example.food_delivery_app.payment.entity.Payment;
import com.example.food_delivery_app.payment.entity.RefundReason;
import com.example.food_delivery_app.payment.event.RefundRequestedEvent;
import com.example.food_delivery_app.payment.gateway.PaymentEvent;
import com.example.food_delivery_app.payment.gateway.PaymentEventType;
import com.example.food_delivery_app.payment.repoistory.PaymentRepository;
import com.example.food_delivery_app.payment.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Slf4j
public class CheckoutCompletedHandler implements WebhookHandler {

    private final PaymentRepository payments;
    private final OrderRepository orders;
    private final MenuItemService menuItemService;
    private final RefundService refundService;
    private final ApplicationEventPublisher events;
    private final OrderStatusService orderStatusService;
    private final OutboxService outbox;

    @Override public PaymentEventType type() { return PaymentEventType.CHECKOUT_COMPLETED; }


    // Apply the outbox pattern: the event is written INSIDE the same transaction as the order update,
    // so that if the transaction rolls back, the event is not written either.
    // if the transaction failed and rolled back, the webhook returns 500 and Stripe retries the webhook later.
    // so we will not lose the event, and the order will eventually become PAID.
    @Override
    public void handleEvent(PaymentEvent event) {
        Payment payment = payments.findByStripeSessionId(event.sessionId()).orElse(null);
        if (payment == null) {
            log.warn("Unknown checkout session {}: ignored", event.sessionId());   // retrying can never fix this
            return;
        }
        if (!payment.markSucceeded(event.paymentReference())) {
            log.info("Payment {} is already {}: ignored", payment.getId(), payment.getStatus());
            return;
        }

        Order order = orders.findById(payment.getOrderId()).orElse(null);

        // Case A: money arrived but the order cannot become PAID.
        // Usually a DOUBLE PAYMENT (old session + new session, the order is already PAID), or the order was cancelled.
        if (order == null || order.getStatus() != OrderStatus.PENDING) {
            log.warn("Payment {} succeeded but order {} is not payable: refunding", payment.getId(), payment.getOrderId());
            refund(payment, order == null ? null : order.getUserId(), RefundReason.ORDER_NOT_PAYABLE);
            return;
        }

        // Case B: paid, but an item ran out in the meantime (someone bought the last one)
        if (!menuItemService.tryDeductItemsFromStock(order)) {
            log.warn("Order {} went out of stock after payment: cancelling and refunding", order.getId());
            order.cancel();                                    // PENDING -> CANCELLED (the user sees it in GET /orders/{id})
            refund(payment, order.getUserId(), RefundReason.OUT_OF_STOCK);
            return;
        }

        // Case C: the normal path
        orderStatusService.orderStatusTransition(order, Order::markPaid , Constants.ORDER_PAID);  // dirty checking saves it, no save() call

        Integer orderId = order.getId();
        // the event is written INSIDE the same transaction as the order update
        var payload = new PaymentSucceededPayload( orderId,  order.getUserId());
        outbox.save(String.valueOf(orderId), OutboxEventTypes.PAYMENT_SUCCEEDED, payload);
        outbox.save(String.valueOf(orderId), OutboxEventTypes.PAYMENT_EMAIL_REQUESTED, payload);
    }

    private void refund(Payment payment, Integer userId, RefundReason reason) {
        refundService.refundFully(payment, reason);            // Stripe call. If it fails, the webhook returns 500 and Stripe retries
        if (userId != null) {
            events.publishEvent(new RefundRequestedEvent(payment.getOrderId(), userId,
                    payment.getAmount(), payment.getCurrency(), reason));   // email goes out AFTER_COMMIT
        }
    }
}