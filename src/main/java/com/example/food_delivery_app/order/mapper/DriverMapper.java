package com.example.food_delivery_app.order.mapper;


import com.example.food_delivery_app.order.dto.driver.CreateDriverRequest;
import com.example.food_delivery_app.order.dto.driver.DriverResponse;
import com.example.food_delivery_app.order.entity.driver.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Driver toDriverEntity(CreateDriverRequest request);

    DriverResponse toDriverResponse(Driver driver);
}