package com.example.food_delivery_app.features.restaurant.dto;



import com.example.food_delivery_app.features.restaurant.entity.Category;
import com.example.food_delivery_app.features.restaurant.entity.Restaurant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {

    @Mapping(target = "categoryId",   source = "category.id")
    // For the categoryName, Java will get the name from the category object in the Restaurant entity.
    // but because category is lazy loaded, we need to make sure that the category is fetched before mapping.
    // That is why hibernate will fetch the category when we access the name property.
    @Mapping(target = "categoryName", source = "category.name")
    RestaurantResponse toResponse(Restaurant restaurant);

    // Request + Category -> Entity.
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category",  source = "category")
    @Mapping(target = "name",        source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "address",     source = "request.address")
    @Mapping(target = "phone",       source = "request.phone")
    @Mapping(target = "open",        source = "request.open")
    Restaurant toEntity(CreateRestaurantRequest request, Category category);

    List<RestaurantResponse> toListResponse(
            List<Restaurant> restaurants
    );
}