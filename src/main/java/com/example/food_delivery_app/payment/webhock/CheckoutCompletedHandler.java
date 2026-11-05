package com.example.food_delivery_app.payment.webhock;

import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.order.service.OrderStatusService;
import com.example.food_delivery_app.restaurant.service.MenuItemService;
import com.example.food_delivery_app.order.entity.order.Order;
import com.example.food_delivery_app.order.entity.order.OrderStatus;
import com.example.food_delivery_app.order.repository.OrderRepository;
import com.example.food_delivery_app.payment.entity.Payment;
import com.example.food_delivery_app.payment.entity.RefundReason;
import com.example.food_delivery_app.payment.event.PaymentSucceededEvent;
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

    @Override public PaymentEventType type() { return PaymentEventType.CHECKOUT_COMPLETED; }

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
        orderStatusService.orderStatusTransition(order, Order::markPaid , Constants.ORDER_PAID);                                      // dirty checking saves it, no save() call
        events.publishEvent(new PaymentSucceededEvent(order.getId(), order.getUserId()));
    }

    private void refund(Payment payment, Integer userId, RefundReason reason) {
        refundService.refundFully(payment, reason);            // Stripe call. If it fails, the webhook returns 500 and Stripe retries
        if (userId != null) {
            events.publishEvent(new RefundRequestedEvent(payment.getOrderId(), userId,
                    payment.getAmount(), payment.getCurrency(), reason));   // email goes out AFTER_COMMIT
        }
    }
}