package com.example.food_delivery_app.order.mapper;

import com.example.food_delivery_app.common.Money;
import com.example.food_delivery_app.order.dto.OrderItemResponse;
import com.example.food_delivery_app.order.dto.OrderResponse;
import com.example.food_delivery_app.order.dto.OrderSummaryResponse;
import com.example.food_delivery_app.order.entity.Order;
import com.example.food_delivery_app.order.entity.OrderItem;
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