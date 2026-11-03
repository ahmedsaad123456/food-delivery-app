package com.example.food_delivery_app.order.entity;

import com.example.food_delivery_app.cart.pricing.PriceSummary;
import com.example.food_delivery_app.common.ErrorCode;
import com.example.food_delivery_app.exceptions.DomainException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "orders")                       // "order" is a reserved word in SQL
@Getter
// PROTECTED: JPA needs it, nobody else may use it
// to create an Order, use the OrderService.createOrder() method
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")       private Integer userId;
    @Column(name = "restaurant_id") private Integer restaurantId;

    @Enumerated(EnumType.STRING)            // store "PAID", not its ordinal number
    private OrderStatus status;

    private BigDecimal subtotal;  // subtotal is items total
    @Column(name = "delivery_fee") private BigDecimal deliveryFee;
    private BigDecimal discount;

    @Column(name = "total_amount")
    private BigDecimal total; // subtotal + delivery + tax + fee - discount


    // the items in the order, but do not allow modification of the list from outside the Order class
    // cascade = CascadeType.ALL: when an Order is saved, its items are also saved; when an Order is deleted, its items are also deleted
    // orphanRemoval = true: when an Order deleted, its items also deleted
    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // get the items in the order, but do not allow modification of the list
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);       // callers cannot modify it
    }


    // create a new order with the given userId, restaurantId, order lines, and price summary
    // lines are the items in the order, price is the summary of the order's prices (subtotal, delivery fee, discount, total)
    public static Order create(Integer userId, Integer restaurantId, List<OrderLine> lines, PriceSummary price) {
        if (lines == null || lines.isEmpty()) {
            throw new DomainException(ErrorCode.ORDER_EMPTY);
        }
        Order order = new Order();
        order.userId = userId;
        order.restaurantId = restaurantId;
        order.status = OrderStatus.PENDING;                     // every order starts here
        order.subtotal = price.subtotal().amount();
        order.deliveryFee = price.deliveryFee().amount();
        order.discount = price.discount().amount();
        order.total = price.total().amount();
        lines.forEach(line -> order.items.add(new OrderItem(order, line)));
        return order;
    }

    public void markPaid()       { transitionTo(OrderStatus.PAID); }
    public void startPreparing() { transitionTo(OrderStatus.PREPARING); }
    public void cancel()         { transitionTo(OrderStatus.CANCELLED); }
    public void markOutForDelivery() { transitionTo(OrderStatus.OUT_FOR_DELIVERY); }
    public void markDelivered() { transitionTo(OrderStatus.DELIVERED); }
    private void transitionTo(OrderStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new DomainException(ErrorCode.ORDER_INVALID_TRANSITION, status, next);
        }
        this.status = next;
    }
}