package com.example.food_delivery_app.order.mapper;

import com.example.food_delivery_app.common.Money;
import com.example.food_delivery_app.order.dto.order.OrderItemResponse;
import com.example.food_delivery_app.order.dto.order.OrderResponse;
import com.example.food_delivery_app.order.dto.order.OrderSummaryResponse;
import com.example.food_delivery_app.order.entity.order.Order;
import com.example.food_delivery_app.order.entity.order.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderResponse toOrderResponse(Order order);

    @Mapping(target = "lineTotal", expression = "java(item.lineTotal())")
    OrderItemResponse toItemResponse(OrderItem item);

    // MapStruct uses this automatically for every BigDecimal -> Money property
    default Money toMoney(BigDecimal amount) {
        return Money.egpMoney(amount);
    }

    OrderSummaryResponse toOrderSummary(Order order);
}