package com.example.food_delivery_app.payment.service;

import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.common.ErrorCode;
import com.example.food_delivery_app.exceptions.AppException;
import com.example.food_delivery_app.exceptions.ResourceNotFoundException;
import com.example.food_delivery_app.order.entity.Order;
import com.example.food_delivery_app.order.entity.OrderItem;
import com.example.food_delivery_app.order.entity.OrderStatus;
import com.example.food_delivery_app.order.repository.OrderRepository;
import com.example.food_delivery_app.payment.gateway.CheckoutRequest;
import com.example.food_delivery_app.payment.dto.CheckoutResponse;
import com.example.food_delivery_app.payment.entity.Payment;
import com.example.food_delivery_app.payment.entity.PaymentStatus;
import com.example.food_delivery_app.payment.gateway.CheckoutLine;
import com.example.food_delivery_app.payment.gateway.CheckoutSession;
import com.example.food_delivery_app.payment.gateway.CheckoutSessionCreator;
import com.example.food_delivery_app.payment.properties.PaymentProperties;
import com.example.food_delivery_app.payment.repoistory.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CheckoutSessionCreator checkoutSessionCreator;   // ISP: this service sees ONLY this port
    private final PaymentProperties props; // properties for payment service, e.g. reuse window
    private final TransactionTemplate tx; // use it for short transactions, not @Transactional on the whole method to avoid keeping a DB transaction open while waiting for Stripe

    // Deliberately NOT @Transactional: no DB transaction may stay open while we wait for Stripe.
    // We use a short read transaction to load the order and prepare the request, then call Stripe, then a short write transaction to save the payment attempt.

    public CheckoutResponse createCheckout(Integer orderId) {

        // 1) Short read transaction: load the order, build the request from its snapshot
        // we use a short transaction here to get order items as it is lazy loaded, and to ensure we see a consistent snapshot of the order (no concurrent updates)
        PreparedCheckout prepared = tx.execute(status -> prepare(orderId));

        // 2) Same page again if the previous attempt is still open (double click, page refresh)
        Instant cutoff = Instant.now().minus(props.reuseWindow()); // reuse window: if the last attempt is younger than this, we can reuse it

        // find the first payment by order id and status PENDING and created after cutoff time, ordered by createdAt descending
        Optional<Payment> open = paymentRepository
                .findFirstByOrderIdAndStatusAndCreatedAtAfterOrderByCreatedAtDesc(orderId, PaymentStatus.PENDING, cutoff);

        // If we found an open payment, return its checkout URL to the client
        if (open.isPresent()) {
            return new CheckoutResponse(open.get().getCheckoutUrl());
        }


        // 3) The Stripe call, outside any transaction
        CheckoutSession session = checkoutSessionCreator.create(prepared.request());

        // 4) Short write transaction (the repository's own)
        return save(prepared, session);
    }


    // Prepare the checkout request from the order snapshot, and validate that the order is still payable.
    private PreparedCheckout prepare(Integer orderId) {
        // Load the order from the database, or throw an exception if not found
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.ORDER_ID, orderId));

        // Check that the order is still payable (status is PENDING), otherwise throw an exception
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(
                    ErrorCode.PAYMENT_ORDER_NOT_PAYABLE,
                    orderId,
                    order.getStatus());
        }

        // Prepare the checkout lines from the order items and fees
        List<CheckoutLine> lines = prepareLines(order);

        // Count the number of previous payment attempts for this order, and create an idempotency key for this attempt
        long attempt = paymentRepository.countByOrderId(orderId) + 1;

        // The idempotency key is a unique string that identifies this checkout attempt.
        // It is used to ensure that if the client retries the request (e.g., due to network issues),
        // Stripe will recognize it as the same attempt and not create a duplicate payment.
        String idempotencyKey =
                "checkout-order-" + orderId + "-attempt-" + attempt;

        // Create the checkout request with the order id, currency, lines, discount, success and cancel URLs, and idempotency key
        CheckoutRequest request = new CheckoutRequest(
                orderId,
                props.currency(),
                lines,
                order.getDiscount(),
                props.successUrl(),
                props.cancelUrl(),
                idempotencyKey);

        return new PreparedCheckout(request, order.getTotal());
    }


    // Prepare the checkout lines from the order items and fees. Each item becomes a line, and if there are any fees (delivery, taxes), they become a separate line.
    private List<CheckoutLine> prepareLines(Order order) {
        List<CheckoutLine> lines = new ArrayList<>();
        BigDecimal itemsTotal = BigDecimal.ZERO;


        // For each item in the order, create a checkout line and add its total to itemsTotal
        // getItems() is a lazy-loaded collection, so we need to be in a transaction to access it. We are in a short read transaction here, so it's safe.
        for (OrderItem item : order.getItems()) {
            lines.add(new CheckoutLine(
                    item.getName(),
                    item.getUnitPrice(),
                    item.getQuantity()));

            itemsTotal = itemsTotal.add(
                    item.getUnitPrice().multiply(
                            BigDecimal.valueOf(item.getQuantity())));
        }

        // Calculate the fees as the difference between the order total (including discount) and the items total.
        // If fees are negative, throw an exception. If fees are positive, add a checkout line for them.
        BigDecimal fees = order.getTotal()
                .add(order.getDiscount())
                .subtract(itemsTotal);

        // If fees are negative, it means the order total is lower than the sum of its items, which is a bug in pricing. Throw an exception in that case.
        if (fees.signum() < 0) {
            throw new IllegalStateException(
                    "Order " + order.getId()
                            + " total is lower than its items: bug in pricing");
        }

        // If fees are positive, add a checkout line for them. The line is called "Delivery, taxes and fees", has the amount of fees, and quantity 1.
        if (fees.signum() > 0) {
            lines.add(new CheckoutLine(
                    "Delivery, taxes and fees",
                    fees,
                    1));
        }

        return lines;
    }


    // Save the payment attempt to the database.
    // If another concurrent request already saved a payment with the same idempotency key, return that one instead of throwing an error.
    // No transaction here because the repository's own transaction is enough
    // As we want to call another query in case of a DataIntegrityViolationException (unique constraint violation).
    private CheckoutResponse save(PreparedCheckout prepared, CheckoutSession session) {
        try {
            Payment payment = paymentRepository.save(
                    Payment.pending(prepared.request().orderId(), prepared.total(), props.currency().toUpperCase(), session));
            return new CheckoutResponse(payment.getCheckoutUrl());
        } catch (DataIntegrityViolationException e) {
            // Two concurrent requests used the same idempotency key, so Stripe gave both the SAME session.
            // The second insert hits UNIQUE(stripe_session_id): just return the row the first one saved.
            return paymentRepository.findByStripeSessionId(session.sessionId())
                    .map(p -> new CheckoutResponse(p.getCheckoutUrl()))
                    .orElseThrow(() -> e);
        }
    }


    // A simple record to hold the prepared checkout request and the order total, so we can pass them together between methods.
    private record PreparedCheckout(CheckoutRequest request, BigDecimal total) {}
}