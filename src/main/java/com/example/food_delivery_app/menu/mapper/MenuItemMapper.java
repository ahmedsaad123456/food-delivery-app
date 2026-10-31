package com.example.food_delivery_app.menu.mapper;

import com.example.food_delivery_app.menu.dto.CreateMenuItemRequest;
import com.example.food_delivery_app.menu.dto.MenuItemResponse;
import com.example.food_delivery_app.menu.dto.UpdateMenuItemRequest;
import com.example.food_delivery_app.menu.entity.MenuItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {

    @Mapping(target = "restaurantId", source = "restaurant.id")
    MenuItemResponse toResponse(MenuItem item);

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "restaurant", ignore = true)      // set in the service
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    MenuItem toEntity(CreateMenuItemRequest request);

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    void update(UpdateMenuItemRequest request, @MappingTarget MenuItem item);
}