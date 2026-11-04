package com.example.food_delivery_app.order.service;

import com.example.food_delivery_app.cart.pricing.PriceSummary;
import com.example.food_delivery_app.cart.pricing.PricingService;
import com.example.food_delivery_app.cart.repository.CartRepository;
import com.example.food_delivery_app.common.Constants;
import com.example.food_delivery_app.common.ErrorCode;
import com.example.food_delivery_app.common.Money;
import com.example.food_delivery_app.common.PageResponse;
import com.example.food_delivery_app.exceptions.AppException;
import com.example.food_delivery_app.exceptions.ResourceNotFoundException;
import com.example.food_delivery_app.menu.entity.MenuItem;
import com.example.food_delivery_app.menu.repository.MenuItemRepository;
import com.example.food_delivery_app.menu.service.MenuItemService;
import com.example.food_delivery_app.order.dto.OrderResponse;
import com.example.food_delivery_app.order.dto.OrderSummaryResponse;
import com.example.food_delivery_app.order.dto.PlaceOrderRequest;
import com.example.food_delivery_app.order.entity.Order;
import com.example.food_delivery_app.order.entity.OrderLine;
import com.example.food_delivery_app.order.entity.OrderStatus;
import com.example.food_delivery_app.order.mapper.OrderMapper;
import com.example.food_delivery_app.order.repository.OrderRepository;
import com.example.food_delivery_app.order.specification.OrderSpecs;
import com.example.food_delivery_app.order.events.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final PricingService pricingService;
    private final MenuItemService menuItemService;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher events;
    private static final Set<String> SORTABLE = Set.of("createdAt", "total", "status", "id");


    @Transactional
    public OrderResponse placeOrder(Integer userId, PlaceOrderRequest request) {

        // read the cart (Redis)
        Map<Integer, Integer> cart = cartRepository.findAll(userId);
        if (cart.isEmpty()) {
            throw new AppException(ErrorCode.ORDER_EMPTY);
        }

        // validate against the DB (the source of truth)
        Map<Integer, MenuItem> items = loadAvailableItems(cart.keySet());
        Integer restaurantId = singleRestaurantId(items.values());

        // snapshot lines: name and price come from the DB, never from the client
        List<OrderLine> lines = toLines(cart, items);

        // price it
        Money subtotal = lines.stream()
                .map(OrderLine::lineTotal)
                .reduce(Money.zero(Money.EGP), Money::add);
        PriceSummary price = pricingService.price(subtotal, request.discountType(), request.discountValue());

        // create and save (the items are saved by cascade)
        Order order = Order.create(userId, restaurantId, lines, price);
        menuItemService.assertAvailable(order);        // 409 with a message if something ran out
        orderRepository.save(order);

        // the cart is cleared AFTER the commit, by CartCleanupListener
        events.publishEvent(new OrderPlacedEvent(order.getId(), userId));

        // map inside the transaction: order.items is lazy and needs the open session
        return orderMapper.toResponse(order);
    }

    // ---------- private helpers (no @Transactional: they run inside placeOrder's transaction) ----------

    // load all items in one query, then validate availability and existence
    private Map<Integer, MenuItem> loadAvailableItems(Set<Integer> ids) {
        Map<Integer, MenuItem> items = menuItemRepository.findAllById(ids).stream()    // ONE query
                .collect(Collectors.toMap(MenuItem::getId, Function.identity()));
        for (Integer id : ids) {
            MenuItem item = items.get(id);
            if (item == null) {
                throw new ResourceNotFoundException(Constants.MENU_ITEM, id);
            }
            if (!item.isAvailable()) {
                throw new AppException(ErrorCode.MENU_ITEM_UNAVAILABLE, item.getName());
            }
        }
        return items;
    }

    // last safety net: addToCart already blocks mixed restaurants, but Redis data is not trusted
    private Integer singleRestaurantId(Collection<MenuItem> items) {
        Set<Integer> restaurantIds = items.stream()
                .map(i -> i.getRestaurant().getId())        // getId() on a lazy proxy does not hit the DB
                .collect(Collectors.toSet());
        if (restaurantIds.size() != 1) {
            throw new AppException(ErrorCode.CART_MIXED_RESTAURANTS);
        }
        return restaurantIds.iterator().next();
    }

    // snapshot the cart into order lines, using the DB as the source of truth for name and price
    private List<OrderLine> toLines(Map<Integer, Integer> cart, Map<Integer, MenuItem> items) {
        return cart.entrySet().stream()
                .map(e -> {
                    MenuItem item = items.get(e.getKey());
                    return new OrderLine(item.getId(), item.getName(),
                            Money.egpMoney(item.getPrice()), e.getValue());
                })
                .toList();
    }


// ---------- queries ----------

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Integer id) {
        return orderMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getMyOrders(Integer userId, OrderStatus status, Pageable pageable) {
        validateSort(pageable);
        Specification<Order> spec = Specification.allOf(
                OrderSpecs.userId(userId),
                OrderSpecs.status(status));
        return PageResponse.from(orderRepository.findAll(spec, pageable).map(orderMapper::toSummary));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getRestaurantOrders(Integer restaurantId, OrderStatus status, Pageable pageable) {
        validateSort(pageable);
        Specification<Order> spec = Specification.allOf(
                OrderSpecs.restaurantId(restaurantId),
                OrderSpecs.status(status));
        return PageResponse.from(orderRepository.findAll(spec, pageable).map(orderMapper::toSummary));
    }

// ---------- commands (state changes) ----------

    @Transactional
    public OrderResponse cancel(Integer id) {
        Order order = findOrThrow(id);
        order.cancel();                              // the entity checks the transition rules
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse startPreparing(Integer id) {
        Order order = findOrThrow(id);
        order.startPreparing();
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse markOutForDelivery(Integer id) {
        Order order = findOrThrow(id);
        order.markOutForDelivery();
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse markDelivered(Integer id) {
        Order order = findOrThrow(id);
        order.markDelivered();
        return orderMapper.toResponse(order);
    }

// ---------- helpers ----------

    private Order findOrThrow(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ORDER, id));
    }

    private void validateSort(Pageable pageable) {
        pageable.getSort().forEach(o -> {
            if (!SORTABLE.contains(o.getProperty())) {
                throw new AppException(ErrorCode.INVALID_SORT_FIELD, o.getProperty(), SORTABLE);
            }
        });
    }
}