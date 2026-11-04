package com.example.food_delivery_app.restaurant.mapper;

import com.example.food_delivery_app.restaurant.dto.menu_item.CreateMenuItemRequest;
import com.example.food_delivery_app.restaurant.dto.menu_item.MenuItemResponse;
import com.example.food_delivery_app.restaurant.dto.menu_item.UpdateMenuItemRequest;
import com.example.food_delivery_app.restaurant.entity.MenuItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {

    @Mapping(target = "restaurantId", source = "restaurant.id")
    MenuItemResponse toMenuItemResponse(MenuItem item);

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "restaurant", ignore = true)      // set in the service
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    MenuItem toMenuItemEntity(CreateMenuItemRequest request);

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    void updateMenuItem(UpdateMenuItemRequest request, @MappingTarget MenuItem item);
}