package com.example.food_delivery_app.common;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


public record SimplePageQuery(
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size
        ) {


    public Pageable toPageable() {
        int p = page == null ? 0 : page;
        int s = size == null ? 20 : size;
        return PageRequest.of(p, s);
    }
}