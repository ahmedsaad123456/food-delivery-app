package com.example.food_delivery_app.restaurant.repository;

import com.example.food_delivery_app.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


// extends JpaSpecificationExecutor<Restaurant> allows us to use Specifications for dynamic queries
@Repository
public interface RestaurantRepository
        extends JpaRepository<Restaurant, Integer>, JpaSpecificationExecutor<Restaurant> {

    boolean existsByIdAndOwnerEmail(Integer id, String email);

    @Override
    // this is the only method we need to override to add the @EntityGraph annotation
    // To avoid the N+1 problem, we use @EntityGraph to fetch the category along with the restaurant in a single query
    @EntityGraph(attributePaths = "category")
    Page<Restaurant> findAll(Specification<Restaurant> spec, Pageable pageable);

}
