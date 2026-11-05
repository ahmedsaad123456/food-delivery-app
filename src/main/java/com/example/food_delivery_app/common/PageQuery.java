package com.example.food_delivery_app.common;


import com.example.food_delivery_app.exceptions.AppException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public record PageQuery(
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size,
        String sortBy,
        Sort.Direction direction) {

    public Pageable toPageable(Set<String> sortable, String defaultSortBy, Sort.Direction defaultDirection) {
        int p = page == null ? 0 : page;
        int s = size == null ? 20 : size;
        String field = (sortBy == null || sortBy.isBlank()) ? defaultSortBy : sortBy;
        Sort.Direction dir = direction == null ? defaultDirection : direction;

        if (!sortable.contains(field)) {
            throw new AppException(ErrorCode.INVALID_SORT_FIELD, field, sortable);
        }
        return PageRequest.of(p, s, Sort.by(dir, field));
    }


}