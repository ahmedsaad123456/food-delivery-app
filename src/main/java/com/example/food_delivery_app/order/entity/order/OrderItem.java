package com.example.food_delivery_app.order.entity.order;
import com.example.food_delivery_app.common.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;


@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "menu_item_id") private Integer menuItemId;   // reference for reporting only
    @Column(name = "item_name")
    private String name;                                       // snapshot
    @Column(name = "unit_price") private BigDecimal unitPrice; // snapshot
    private int quantity;

    OrderItem(Order order, OrderLine line) {                   // package-private: only Order creates items
        this.order = order;
        this.menuItemId = line.menuItemId();
        this.name = line.name();
        this.unitPrice = line.unitPrice().amount();
        this.quantity = line.quantity();
    }

    public Money lineTotal() {
        return Money.egpMoney(unitPrice).multiply(quantity);
    }
}