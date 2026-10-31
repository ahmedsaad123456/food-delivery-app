package com.example.food_delivery_app.menu.dto;

import java.util.List;

public record MenuSectionResponse(String section, List<MenuItemResponse> items) {}
