package com.example.food_delivery_app.restaurant.dto.menu_item;

import java.util.List;

public record MenuSectionResponse(String section, List<MenuItemResponse> items) {}
